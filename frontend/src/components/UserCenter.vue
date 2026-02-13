<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { deleteJson, getJson, postJson, putJson } from "../utils/api";
import { useToast } from "../utils/toast";

const router = useRouter();
const { success, error } = useToast();

type UserCenterTab = "PROFILE" | "POINT" | "SECURITY";
type UserRole = "ADMIN" | "VOLUNTEER" | "USER";
type VolunteerStatus =
  | "CERTIFIED"
  | "REVIEWING"
  | "REJECTED"
  | "SUSPENDED"
  | null;

interface PointsChangeRecord {
  time: string;
  type: string;
  amount?: number;
  note?: string;
}

interface FormattedRecord {
  key: string;
  time: string;
  typeLabel: string;
  amount: string;
  note: string;
}

interface UserProfile {
  id?: number | string;
  username?: string;
  realName?: string;
  role?: UserRole;
  phone?: string;
  points?: number;
  serviceHours?: number;
  volunteerStatus?: VolunteerStatus;
  pointsChangeRecords?: PointsChangeRecord[];
}

// 侧边栏选项卡
const activeTab = ref<UserCenterTab>("PROFILE");
const tabs: { label: string; value: UserCenterTab }[] = [
  { label: "用户信息", value: "PROFILE" },
  { label: "积分变动记录", value: "POINT" },
  { label: "账号与安全", value: "SECURITY" },
];

// 用户信息
const profile = ref<UserProfile | null>(null);

const displayUserName = computed(() => profile.value?.username || "游客");
const userCircleText = computed(() =>
  displayUserName.value ? displayUserName.value.slice(0, 1) : "游",
);
const displayRealName = computed(
  () => profile.value?.realName?.trim() || "未填写",
);

const roleMap: Record<UserRole, string> = {
  ADMIN: "管理员",
  VOLUNTEER: "志愿者",
  USER: "普通用户",
};
const roleLabel = computed(() =>
  profile.value?.role
    ? (roleMap[profile.value.role] ?? profile.value.role)
    : "游客",
);

const maskPhone = (phone?: string) => {
  if (!phone) {
    return;
  }

  const trimmedPhone = phone.trim();

  if (trimmedPhone.length < 7) {
    return trimmedPhone;
  }

  const prefix = trimmedPhone.slice(0, 3);
  const suffix = trimmedPhone.slice(-4);
  return `${prefix}****${suffix}`;
};

const displayPhone = computed(() => {
  const phone = maskPhone(profile.value?.phone);
  return phone || "暂无";
});

const displayPoints = computed(() => profile.value?.points ?? 0);
const displayHours = computed(() => profile.value?.serviceHours ?? 0);

// 编辑用户资料
const showEditDialog = ref(false);
const isSaving = ref(false);

const editForm = reactive({
  username: "",
  phone: "",
});

const openEditDialog = () => {
  if (!profile.value) {
    return;
  }

  editForm.username = profile.value.username ?? "";
  editForm.phone = profile.value.phone ?? "";
  showEditDialog.value = true;
};

const closeEditDialog = () => {
  showEditDialog.value = false;
};

const updateLocalUser = (data: UserProfile) => {
  const localUser = localStorage.getItem("user");
  if (!localUser) {
    return;
  }

  try {
    const parsedUser = JSON.parse(localUser) as UserProfile;
    const updatedUser = {
      ...parsedUser,
      username: data.username ?? parsedUser.username,
      phone: data.phone ?? parsedUser.phone,
      volunteerStatus: data.volunteerStatus ?? parsedUser.volunteerStatus,
    };
    localStorage.setItem("user", JSON.stringify(updatedUser));
  } catch (err) {
    const msg = err instanceof Error ? err.message : "更新本地用户信息失败";
    error("更新失败", msg);
  }
};

const saveProfile = async () => {
  if (!profile.value?.id || isSaving.value) {
    return;
  }

  const username = editForm.username.trim();
  if (!username) {
    error("保存失败", "用户名不能为空");
    return;
  }

  isSaving.value = true;

  try {
    const updated = await putJson<UserProfile>(
      `/api/users/${profile.value.id}/profile`,
      {
        username,
        phone: editForm.phone.trim(),
      },
    );
    profile.value = updated;
    updateLocalUser(updated);
    dispatchEvent(
      new CustomEvent("user-updated", {
        detail: { username: updated.username ?? username },
      }),
    );
    success("保存成功", "用户资料已更新");
    showEditDialog.value = false;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "保存用户资料失败";
    error("保存失败", msg);
  } finally {
    isSaving.value = false;
  }
};

// 申请志愿者、补充材料
const showApplyDialog = ref(false);
const isApplying = ref(false);

const showSupplement = computed(
  () => profile.value?.volunteerStatus === "REVIEWING",
);

const applyForm = reactive({
  realName: "",
  phone: "",
});

const canApply = computed(() => {
  if (profile.value?.role !== "USER") return false;
  return (
    !profile.value?.volunteerStatus ||
    profile.value?.volunteerStatus === "REJECTED"
  );
});

const applyLabel = computed(() =>
  profile.value?.volunteerStatus === "REJECTED"
    ? "重新提交申请"
    : "申请认证为志愿者",
);

const openApplyDialog = () => {
  if (!profile.value) return;
  applyForm.realName = "";
  applyForm.phone = profile.value.phone ?? "";
  showApplyDialog.value = true;
};

const closeApplyDialog = () => {
  showApplyDialog.value = false;
};

const applyVolunteer = async () => {
  if (!profile.value?.id || isApplying.value) {
    return;
  }

  const realName = applyForm.realName.trim();
  if (!realName) {
    error("提交申请失败", "真实姓名不能为空");
    return;
  }

  const phone = applyForm.phone.trim();
  if (!phone) {
    error("提交申请失败", "手机号不能为空");
  }
  if (phone && profile.value.phone && phone !== profile.value.phone) {
    error("提交申请失败", "输入手机号和当前绑定的手机号不一致");
  }

  isApplying.value = true;

  try {
    const updated = await postJson<UserProfile>(
      `/api/users/${profile.value.id}/volunteer-apply`,
      {
        realName,
        phone,
      },
    );
    profile.value = updated;
    updateLocalUser(updated);
    success("提交申请成功", "请等待管理员审核");
    showApplyDialog.value = false;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "申请故障，请耐心等待修复";
    error("提交申请失败", msg);
  }
};

// 志愿者状态
const volunteerStatusLabel = computed(() => {
  if (!profile.value) {
    return "未登录";
  }
  if (profile.value.role === "ADMIN") {
    return "管理员权限";
  }

  const status = profile.value.volunteerStatus;
  switch (status) {
    case "CERTIFIED":
      return "已认证";
    case "REVIEWING":
      return "等待管理员审核";
    case "REJECTED":
      return "未通过审核";
    case "SUSPENDED":
      return "已停用";
    default:
      return "未申请";
  }
});

const volunteerStatusMap = new Map<VolunteerStatus, string>([
  ["CERTIFIED", "certified"],
  ["REVIEWING", "reviewing"],
  ["REJECTED", "rejected"],
  ["SUSPENDED", "suspended"],
  [null, "muted"],
]);

const volunteerStatus = computed(() => {
  if (profile.value?.role === "ADMIN") {
    return "admin";
  }

  return (
    volunteerStatusMap.get(profile.value?.volunteerStatus ?? null) ?? "muted"
  );
});

// 积分变动记录
const records = ref<PointsChangeRecord[]>([]);

const pointsChangeMap: Record<string, string> = {
  ACTIVITY_EARN: "活动结算",
  EXCHANGE_USE: "积分兑换",
  ADMIN_ADJUST: "管理员调整",
  SYSTEM_BONUS: "系统奖励",
  EXPIRED_DEDUCT: "过期扣除",
};

const formattedRecords = computed<FormattedRecord[]>(() =>
  records.value.map((record: PointsChangeRecord) => {
    const amount = Number(record.amount ?? 0);
    const amountStr = amount >= 0 ? `+${amount}` : `${amount}`;
    return {
      key: `${record.time}-${record.type}-${record.amount}`,
      time: record.time,
      typeLabel: pointsChangeMap[record.type] || "积分变动",
      amount: amountStr,
      note: record.note || "—",
    };
  }),
);

const monthlyChange = computed(() => {
  const now = new Date();
  const monthlyTotal = formattedRecords.value
    .filter((record: FormattedRecord) => {
      const recordDate = new Date(record.time);
      if (Number.isNaN(recordDate.getTime())) {
        return false;
      }
      return (
        recordDate.getFullYear() === now.getFullYear() &&
        recordDate.getMonth() === now.getMonth()
      );
    })
    .reduce((sum: number, record: FormattedRecord) => {
      const amount = Number(record.amount);
      return sum + amount;
    }, 0);

  return monthlyTotal >= 0 ? `+${monthlyTotal}` : `${monthlyTotal}`;
});

// 加载用户信息
const loadProfile = async () => {
  const localUser = localStorage.getItem("user");
  if (!localUser) {
    return;
  }

  try {
    const parsedUser = JSON.parse(localUser);
    profile.value = parsedUser;
    if (!parsedUser.id) {
      return;
    }

    const data = await getJson<UserProfile>(
      `/api/users/${parsedUser.id}/profile`,
    );
    profile.value = data;
    records.value = data.pointsChangeRecords ?? [];
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取用户信息失败";
    error("加载失败", msg);
  }
};

// 加载页面时即获取用户信息
onMounted(() => loadProfile());

// 退出登录与账号注销
const showLogoutDialog = ref(false);
const showDeleteDialog = ref(false);

const openLogoutDialog = () => {
  showLogoutDialog.value = true;
};

const closeLogoutDialog = () => {
  showLogoutDialog.value = false;
};

const handleLogout = async () => {
  try {
    await postJson<{ message: string }>("/api/users/logout", {});
  } catch (err) {
    const msg = err instanceof Error ? err.message : "退出登录失败";
    error("退出失败", msg);
    return;
  }

  localStorage.removeItem("user");
  localStorage.removeItem("token");
  success("已退出登录", "期待您的再次访问");
  closeLogoutDialog();
  router.push("/login");
};

const openDeleteDialog = () => {
  showDeleteDialog.value = true;
};

const closeDeleteDialog = () => {
  showDeleteDialog.value = false;
};

// TODO: 注销，暂时用的是退出登录的逻辑
const handleDeleteAccount = async () => {
  try {
    await deleteJson<{ message: string }>(
      `/api/users/${profile.value?.id}`,
      {},
    );
  } catch (err) {
    const msg = err instanceof Error ? err.message : "注销账号失败";
    error("注销失败", msg);
    return;
  }

  localStorage.removeItem("user");
  localStorage.removeItem("token");
  success("账号已注销", "如需继续使用，请重新注册账号");
  closeDeleteDialog();
  router.push("/login");
};
</script>

<template>
  <div class="user-center">
    <h2>个人中心</h2>

    <section class="layout">
      <aside class="tab-menu" aria-label="个人中心导航">
        <button
          type="button"
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-button"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </aside>

      <div class="tab-page">
        <section v-if="activeTab === 'PROFILE'" class="profile">
          <div class="profile-card">
            <div class="profile-content">
              <div class="user-circle">
                {{ userCircleText }}
              </div>
              <div class="simple-info">
                <div class="username">{{ displayUserName }}</div>
                <div class="role">{{ roleLabel }}</div>
              </div>
            </div>
            <button
              type="button"
              class="edit-profile-button"
              @click="openEditDialog"
            >
              修改用户信息
            </button>
          </div>

          <div class="profile-card">
            <h3>基础信息</h3>
            <div class="basic-info">
              <div class="info-item">
                <span class="label">真名</span>
                <span class="value">{{ displayRealName }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号</span>
                <span class="value">{{ displayPhone }}</span>
              </div>
              <div class="info-item">
                <span class="label">服务时长</span>
                <span class="value">{{ displayHours }}</span>
              </div>
              <div class="info-item">
                <span class="label">积分余额</span>
                <span class="value">{{ displayPoints }}</span>
              </div>
            </div>
          </div>

          <div class="profile-card">
            <h3>志愿者申请</h3>
            <p class="apply-tip">在此提交或补充申请志愿者材料。</p>
            <button
              type="button"
              v-if="showSupplement"
              class="show-supplement-button"
            >
              提交补充材料
            </button>
            <button
              type="button"
              v-if="canApply"
              :disabled="isApplying"
              class="apply-volunteer-button"
              @click="openApplyDialog"
            >
              {{ applyLabel }}
            </button>
          </div>

          <div class="profile-card">
            <h3>志愿者认证状态</h3>
            <div class="status-block" :class="volunteerStatus">
              {{ volunteerStatusLabel }}
            </div>
          </div>
        </section>

        <section v-else-if="activeTab === 'POINT'" class="points-change-record">
          <div class="summary">
            <div class="summary-card">
              <h4>当前积分</h4>
              <p>{{ displayPoints }}</p>
            </div>
            <div class="summary-card">
              <h4>本月积分变动</h4>
              <p>{{ monthlyChange }}</p>
            </div>
            <div class="summary-card">
              <h4>兑换记录</h4>
              <router-link to="/exchange-records" class="exchange-record-link">
                查看兑换记录
              </router-link>
            </div>
          </div>

          <section v-if="records.length" class="record-table">
            <div class="table-header">
              <span>时间</span>
              <span>类型</span>
              <span>变动</span>
              <span>备注</span>
            </div>
            <div
              v-for="record in formattedRecords"
              :key="record.key"
              class="table-row"
            >
              <span>{{ record.time }}</span>
              <span>{{ record.typeLabel }}</span>
              <span
                :class="record.amount.startsWith('+') ? 'positive' : 'negative'"
              >
                {{ record.amount }}
              </span>
              <span>{{ record.note }}</span>
            </div>
          </section>
          <section v-else class="empty-record">
            <p>暂无积分变动记录</p>
          </section>
        </section>

        <section v-else class="security">
          <h3>账号与安全</h3>
          <p class="security-tip">管理账号的退出登录与注销，请谨慎操作。</p>
          <div class="account-actions">
            <button
              type="button"
              class="logout-button"
              @click="openLogoutDialog"
            >
              退出登录
            </button>
            <button
              type="button"
              class="delete-button"
              @click="openDeleteDialog"
            >
              注销账号
            </button>
          </div>
        </section>
      </div>
    </section>

    <div v-if="showEditDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>编辑资料</h3>
        <div class="edit-form">
          <label for="username" class="form-row">
            <span>用户名</span>
            <input
              v-model="editForm.username"
              type="text"
              placeholder="请输入用户名"
              maxlength="20"
            />
            <button
              v-if="editForm.username"
              type="button"
              class="clear-button"
              @click="editForm.username = ''"
            >
              ×
            </button>
          </label>
          <label for="phone" class="form-row">
            <span>手机号</span>
            <input
              v-model="editForm.phone"
              type="text"
              placeholder="请输入手机号"
              maxlength="20"
            />
            <button
              v-if="editForm.phone"
              type="button"
              class="clear-button"
              @click="editForm.phone = ''"
            >
              ×
            </button>
          </label>
        </div>
        <div class="edit-buttons">
          <button type="button" class="save-button" @click="saveProfile">
            保存
          </button>
          <button type="button" class="cancel-button" @click="closeEditDialog">
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showApplyDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>申请志愿者</h3>
        <div class="edit-form">
          <label for="username" class="form-row">
            <span>真实姓名</span>
            <input
              v-model="applyForm.realName"
              type="text"
              placeholder="请输入真实姓名"
              maxlength="20"
            />
            <button
              v-if="applyForm.realName"
              type="button"
              class="clear-button"
              @click="applyForm.realName = ''"
            >
              ×
            </button>
          </label>
          <label for="phone" class="form-row">
            <span>手机号</span>
            <input
              v-model="applyForm.phone"
              type="text"
              placeholder="请输入手机号"
              maxlength="20"
            />
            <button
              v-if="applyForm.phone"
              type="button"
              class="clear-button"
              @click="applyForm.phone = ''"
            >
              ×
            </button>
          </label>
        </div>
        <div class="edit-buttons">
          <button type="button" class="save-button" @click="applyVolunteer">
            提交
          </button>
          <button type="button" class="cancel-button" @click="closeApplyDialog">
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showLogoutDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>确认退出登录</h3>
        <p class="dialog-tip">退出登录后将需要重新登录，是否确认继续？</p>
        <div class="edit-buttons">
          <button
            type="button"
            class="logout-confirm-button"
            @click="handleLogout"
          >
            确认退出
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeLogoutDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDeleteDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>确认注销账号</h3>
        <p class="dialog-tip">注销账号后所有数据将无法恢复，请谨慎操作！</p>
        <div class="edit-buttons">
          <button
            type="button"
            class="delete-confirm-button"
            @click="handleDeleteAccount"
          >
            确认注销
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeDeleteDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.user-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.layout {
  display: grid;
  align-items: start;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.tab-menu {
  display: flex;
  flex-direction: column;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 8px;
  gap: 8px;
}

.tab-button {
  background: transparent;
  text-align: center;
  cursor: pointer;
  font-size: 16px;
  color: #475569;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
}

.tab-button.active {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.tab-page {
  display: grid;
  min-width: 0;
  gap: 16px;
}

.profile {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.profile-card {
  display: flex;
  flex-direction: column;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  gap: 20px;
  min-height: 160px;
}

.profile-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.user-circle {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  background: #e6ebfe;
  font-size: 40px;
  font-weight: 600;
  color: #1d4ed8;
  border-radius: 100%;
  width: 120px;
  height: 120px;
}

.simple-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  gap: 10px;
}

.username {
  font-size: 30px;
  font-weight: bold;
}

.role {
  font-size: 20px;
  color: #6b7280;
}

.edit-profile-button {
  background: white;
  color: #2563eb;
  cursor: pointer;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
}

.edit-profile-button:hover {
  background: #f8fafc;
  color: #1d4ed8;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
  transition: all 0.2s ease;
}

.basic-info {
  display: grid;
  gap: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  background: #f8fafc;
  font-size: 14px;
  padding: 8px 20px;
  border-radius: 8px;
}

.info-item .label {
  font-weight: 600;
  color: #6b7280;
}

.info-item .value {
  font-weight: 500;
  color: #111827;
}

.apply-tip {
  font-size: 16px;
  color: #6b7280;
  margin-top: -15px;
}

.show-supplement-button {
  background: #22c55e;
  color: white;
  font-weight: 500;
  cursor: pointer;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.show-supplement-button:hover {
  background: #16a34a;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.apply-volunteer-button {
  cursor: pointer;
  background: #2563eb;
  color: white;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
}

.apply-volunteer-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.2s ease;
}

.apply-volunteer-button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.status-block {
  width: 100%;
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  padding: 14px 16px;
  border-radius: 8px;
}

.admin {
  background: #eef2ff;
  color: #3730a3;
}

.certified {
  background: #ecfdf3;
  color: #065f46;
}

.reviewing {
  background: #fef3c7;
  color: #92400e;
}

.rejected {
  background: #fee2e2;
  color: #b91c1c;
}

.suspended {
  background: #7f1d1d;
  color: #fee2e2;
}

.muted {
  background: #f3f4f6;
  color: #6b7280;
}

.points-change-record {
  display: grid;
  gap: 16px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.summary-card {
  display: grid;
  background: white;
  padding: 16px;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.summary-card h4 {
  font-size: 22px;
}

.summary-card p {
  font-size: 20px;
  font-weight: 500;
}

.exchange-record-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  padding: 8px 12px;
}

.exchange-record-link:hover {
  text-decoration: underline;
}

.record-table {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.table-header,
.table-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 14px 16px;
  align-items: center;
}

.table-header {
  background: #f8fafc;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

.table-row {
  border-top: 1px solid #e5e7eb;
}

.positive {
  color: #16a34a;
  font-weight: 600;
}

.negative {
  color: #dc2626;
  font-weight: 600;
}

.empty-record {
  background: white;
  color: #6b7280;
  text-align: center;
  padding: 24px;
  border: 1px dashed #e5e7eb;
  border-radius: 12px;
}

.security {
  display: grid;
  background: white;
  padding: 16px;
  gap: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.security-tip {
  color: #6b7280;
  font-size: 16px;
}

.account-actions {
  display: flex;
  gap: 16px;
}

.logout-button {
  background: white;
  color: #111827;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
}

.logout-button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
  transition: all 0.2s ease;
}

.delete-button {
  background: #dc2626;
  cursor: pointer;
  color: white;
  font-weight: 600;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
}

.delete-button:hover {
  background: red;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
  transition: all 0.2s ease;
}

.dialog-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  background: rgba(15, 23, 42, 0.4);
  inset: 0;
  z-index: 100;
}

.dialog-body {
  display: grid;
  background: white;
  min-width: 320px;
  max-width: 400px;
  padding: 20px;
  gap: 16px;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.edit-form {
  display: grid;
  gap: 12px;
}

.form-row {
  display: grid;
  position: relative;
  font-size: 14px;
  color: #374151;
  gap: 6px;
}

.form-row span {
  font-size: 16px;
  font-weight: 500;
}

.form-row input {
  font-size: 14px;
  padding: 8px 36px 8px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.2s ease;
}

.form-row input:focus {
  border-color: #2563eb;
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.clear-button {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  width: 20px;
  height: 20px;
  line-height: 1;
  background: #e5e7eb;
  font-size: 14px;
  color: #6b7280;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  padding: 0;
  margin-top: 13px;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.clear-button:hover {
  background: #d1d5db;
  color: #374151;
}

.edit-buttons {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.save-button {
  min-width: 100px;
  background: #22c55e;
  color: white;
  cursor: pointer;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.save-button:hover {
  background: #16a34a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.save-button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.cancel-button {
  min-width: 100px;
  background: white;
  cursor: pointer;
  padding: 10px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
}

.cancel-button:hover {
  background: #efefef;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
  transition: all 0.2s ease;
}

.dialog-tip {
  font-size: 14px;
  color: #6b7280;
  text-align: center;
  margin: 0;
  line-height: 1.5;
}

.logout-confirm-button {
  min-width: 100px;
  cursor: pointer;
  background: #2563eb;
  color: white;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
}

.logout-confirm-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.2s ease;
}

.delete-confirm-button {
  min-width: 100px;
  background: #dc2626;
  cursor: pointer;
  color: white;
  font-weight: 600;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
}

.delete-confirm-button:hover {
  background: red;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
  transition: all 0.2s ease;
}
</style>
