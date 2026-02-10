<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>个人中心</h2>
      <p>汇总志愿者资料、积分与兑换信息。</p>
    </header>

    <section class="center-layout">
      <aside class="tab-menu" aria-label="个人中心导航">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeTab === tab.value }"
          type="button"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </aside>

      <div class="tab-content">
        <section v-if="activeTab === 'PROFILE'" class="profile-grid">
          <div class="profile-card">
            <div class="avatar-placeholder">{{ avatarText }}</div>
            <div class="info">
              <p class="name">{{ displayName }}</p>
              <p class="role">{{ roleLabel }}</p>
              <button class="ghost-btn" @click="openEditProfile">
                编辑资料
              </button>
            </div>
          </div>
          <div class="profile-card">
            <h3>基础信息</h3>
            <p></p>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">手机号</span>
                <span class="value">{{ displayPhone }}</span>
              </div>
              <div class="info-item">
                <span class="label">服务时长</span>
                <span class="value">{{ displayHours }} 小时</span>
              </div>
              <div class="info-item">
                <span class="label">积分余额</span>
                <span class="value">{{ displayPoints }}</span>
              </div>
            </div>
          </div>
          <div class="profile-card">
            <h3>志愿者申请</h3>
            <p></p>
            <p class="panel-tip">在此提交认证申请或补充审核材料。</p>
            <button v-if="showSupplement" class="ghost-btn">
              提交补充材料
            </button>
            <button
              v-if="canApplyVolunteer"
              class="primary-btn"
              :disabled="isApplying"
              @click="openApplyVolunteer"
            >
              {{ applyVolunteerLabel }}
            </button>
          </div>
          <div class="profile-card">
            <h3>志愿者认证状态</h3>
            <p></p>
            <div class="status-block" :class="[statusClass, statusSizeClass]">
              {{ volunteerStatusLabel }}
            </div>
          </div>
        </section>
        <section v-else-if="activeTab === 'SIGNUP'" class="panel-card">
          <h3>报名记录</h3>
          <p class="panel-tip">查看您参与活动的报名信息。</p>
          <section class="empty-card">
            <p>暂无报名记录</p>
          </section>
        </section>

        <section v-else-if="activeTab === 'EXCHANGE'" class="record-panel">
          <div class="summary">
            <div class="summary-card">
              <p>当前积分</p>
              <h3>{{ displayPoints }}</h3>
            </div>
            <div class="summary-card">
              <p>本月新增</p>
              <h3>+{{ monthlyEarned }}</h3>
            </div>
            <div class="summary-card">
              <p>兑换记录</p>
              <router-link to="/exchange-records" class="primary-link">
                查看兑换记录
              </router-link>
            </div>
          </div>

          <section v-if="records.length" class="table-card">
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
          <section v-else class="empty-card">
            <p>暂无积分变动记录</p>
          </section>
        </section>

        <section v-else class="panel-card">
          <h3>账号与安全</h3>
          <p class="panel-tip">进行账号退出与注销管理，请谨慎操作。</p>
          <div class="panel-actions">
            <button class="ghost-btn" type="button" @click="handleLogout">
              退出登录
            </button>
            <button
              class="danger-btn"
              type="button"
              @click="handleDeleteAccount"
            >
              注销账号
            </button>
          </div>
        </section>
      </div>
    </section>

    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-card" role="dialog" aria-modal="true">
        <h3>编辑资料</h3>
        <div class="form-grid">
          <label class="form-field">
            <span>用户名</span>
            <input v-model="editForm.username" type="text" maxlength="20" />
          </label>
          <label class="form-field">
            <span>手机号</span>
            <input v-model="editForm.phone" type="text" maxlength="20" />
          </label>
        </div>
        <div class="modal-actions">
          <button
            class="modal-btn primary"
            :disabled="isSaving"
            @click="saveProfile"
          >
            保存修改
          </button>
          <button class="modal-btn" @click="closeEditProfile">取消</button>
        </div>
        <p class="modal-tip">修改手机号后将自动同步到已有志愿者档案。</p>
      </div>
    </div>

    <div v-if="showApplyModal" class="modal-overlay">
      <div class="modal-card" role="dialog" aria-modal="true">
        <h3>志愿者认证申请</h3>
        <div class="form-grid">
          <label class="form-field">
            <span>真实姓名</span>
            <input v-model="applyForm.realName" type="text" maxlength="20" />
          </label>
          <label class="form-field">
            <span>手机号确认（可选）</span>
            <input v-model="applyForm.phone" type="text" maxlength="20" />
          </label>
        </div>
        <div class="modal-actions">
          <button
            class="modal-btn primary"
            :disabled="isApplying"
            @click="applyVolunteer"
          >
            提交申请
          </button>
          <button class="modal-btn" @click="closeApplyVolunteer">取消</button>
        </div>
        <p class="modal-tip">请填写真实姓名以便进行志愿者身份审核。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { getJson, postJson, putJson } from "../utils/api";
import { useToast } from "../utils/toast";

type UserRole = "ADMIN" | "VOLUNTEER" | "USER";
type UserCenterTab = "PROFILE" | "SIGNUP" | "EXCHANGE" | "SECURITY";

type VolunteerStatus =
  | "CERTIFIED"
  | "REVIEWING"
  | "REJECTED"
  | "SUSPENDED"
  | null;

interface PointsRecord {
  time: string;
  type: string;
  amount?: number;
  note?: string;
}

interface UserProfile {
  id?: number | string;
  username?: string;
  role?: UserRole;
  phone?: string;
  points?: number;
  serviceHours?: number;
  volunteerStatus?: VolunteerStatus;
  pointsRecords?: PointsRecord[];
}

interface FormattedRecord {
  key: string;
  time: string;
  typeLabel: string;
  amount: string;
  note: string;
}

const router = useRouter();
const { success, error } = useToast();

const activeTab = ref<UserCenterTab>("PROFILE");
const tabs: { label: string; value: UserCenterTab }[] = [
  { label: "用户信息", value: "PROFILE" },
  { label: "报名记录", value: "SIGNUP" },
  { label: "兑换记录", value: "EXCHANGE" },
  { label: "账号与安全", value: "SECURITY" },
];
const profile = ref<UserProfile | null>(null);
const records = ref<PointsRecord[]>([]);
const displayName = computed(() => profile.value?.username || "游客");
const showEditModal = ref(false);
const editForm = reactive({
  username: "",
  phone: "",
});
const showApplyModal = ref(false);
const applyForm = reactive({
  realName: "",
  phone: "",
});
const isSaving = ref(false);
const isApplying = ref(false);

const avatarText = computed(() =>
  displayName.value ? displayName.value.slice(0, 1) : "游",
);

const roleLabel = computed(() => {
  if (!profile.value?.role) return "游客";
  const roleMap: Record<UserRole, string> = {
    ADMIN: "管理员",
    VOLUNTEER: "志愿者",
    USER: "普通用户",
  };
  const role = profile.value.role;
  if (!role) return "游客";
  return roleMap[role] || role;
});

const maskPhone = (phone?: string) => {
  if (!phone) return "";
  const trimmed = phone.trim();
  if (trimmed.length < 7) return trimmed;
  const prefix = trimmed.slice(0, 3);
  const suffix = trimmed.slice(-4);
  return `${prefix}****${suffix}`;
};

const displayPhone = computed(() => {
  const phone = maskPhone(profile.value?.phone);
  return phone || "暂无";
});

const displayPoints = computed(() => profile.value?.points ?? 0);
const displayHours = computed(() => profile.value?.serviceHours ?? 0);

const volunteerStatusLabel = computed(() => {
  if (!profile.value) return "未登录";
  if (profile.value.role === "ADMIN") return "管理员权限";
  const status = profile.value.volunteerStatus;
  if (status === "CERTIFIED") return "已认证";
  if (status === "REVIEWING") return "等待管理员审核";
  if (status === "REJECTED") return "未通过审核";
  if (status === "SUSPENDED") return "已停用";
  return "未申请";
});

const statusClass = computed(() => {
  if (profile.value?.role === "ADMIN") return "status-admin";
  if (profile.value?.volunteerStatus === "CERTIFIED") return "status-certified";
  if (profile.value?.volunteerStatus === "REVIEWING") return "status-reviewing";
  if (profile.value?.volunteerStatus === "REJECTED") return "status-rejected";
  if (profile.value?.volunteerStatus === "SUSPENDED") return "status-suspended";
  return "status-muted";
});

const statusSizeClass = computed(() =>
  profile.value?.volunteerStatus ? "" : "status-empty",
);

const canApplyVolunteer = computed(() => {
  if (profile.value?.role !== "USER") return false;
  return (
    !profile.value?.volunteerStatus ||
    profile.value?.volunteerStatus === "REJECTED"
  );
});

const applyVolunteerLabel = computed(() =>
  profile.value?.volunteerStatus === "REJECTED"
    ? "重新提交申请"
    : "申请认证为志愿者",
);

const showSupplement = computed(
  () => profile.value?.volunteerStatus === "REVIEWING",
);

const formattedRecords = computed<FormattedRecord[]>(() =>
  records.value.map((record: PointsRecord) => {
    const amountValue = Number(record.amount ?? 0);
    const amountText = amountValue >= 0 ? `+${amountValue}` : `${amountValue}`;
    return {
      key: `${record.time}-${record.type}-${record.amount}`,
      time: record.time,
      typeLabel: resolveRecordType(record.type),
      amount: amountText,
      note: record.note || "—",
    };
  }),
);

const monthlyEarned = computed(() => {
  const now = new Date();
  return formattedRecords.value
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
      const amount = Number(record.amount.replace("+", ""));
      return amount > 0 ? sum + amount : sum;
    }, 0);
});

const resolveRecordType = (type: string) => {
  const map: Record<string, string> = {
    ACTIVITY_EARN: "活动结算",
    EXCHANGE_USE: "积分兑换",
    ADMIN_ADJUST: "管理员调整",
    SYSTEM_BONUS: "系统奖励",
    EXPIRED_DEDUCT: "过期扣除",
  };
  return map[type] || "积分变动";
};

const loadProfile = async () => {
  const userStr = localStorage.getItem("user");
  if (!userStr) {
    return;
  }

  try {
    const user = JSON.parse(userStr) as UserProfile;
    profile.value = user;
    if (!user.id) return;
    const data = await getJson<UserProfile>(`/api/users/${user.id}/profile`);
    profile.value = data;
    records.value = data.pointsRecords ?? [];
  } catch (err) {
    const message = err instanceof Error ? err.message : "获取个人信息失败";
    error("加载失败", message);
  }
};

onMounted(() => {
  loadProfile();
});

const openEditProfile = () => {
  if (!profile.value) return;
  editForm.username = profile.value.username ?? "";
  editForm.phone = profile.value.phone ?? "";
  showEditModal.value = true;
};

const closeEditProfile = () => {
  showEditModal.value = false;
};

const openApplyVolunteer = () => {
  if (!profile.value) return;
  applyForm.realName = "";
  applyForm.phone = profile.value.phone ?? "";
  showApplyModal.value = true;
};

const closeApplyVolunteer = () => {
  showApplyModal.value = false;
};

const updateLocalUser = (data: UserProfile) => {
  const userStr = localStorage.getItem("user");
  if (!userStr) return;
  try {
    const parsedUser = JSON.parse(userStr) as UserProfile;
    const updatedUser = {
      ...parsedUser,
      username: data.username ?? parsedUser.username,
      phone: data.phone ?? parsedUser.phone,
      volunteerStatus: data.volunteerStatus ?? parsedUser.volunteerStatus,
    };
    localStorage.setItem("user", JSON.stringify(updatedUser));
  } catch (storageError) {
    console.error("更新本地用户信息失败:", storageError);
  }
};

const saveProfile = async () => {
  if (!profile.value?.id || isSaving.value) return;
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
    window.dispatchEvent(
      new CustomEvent("user-updated", {
        detail: { username: updated.username ?? username },
      }),
    );
    success("保存成功", "资料已更新");
    showEditModal.value = false;
  } catch (err) {
    const message = err instanceof Error ? err.message : "保存失败";
    error("保存失败", message);
  } finally {
    isSaving.value = false;
  }
};

const applyVolunteer = async () => {
  if (!profile.value?.id || isApplying.value) return;
  const realName = applyForm.realName.trim();
  if (!realName) {
    error("申请失败", "真实姓名不能为空");
    return;
  }
  const phone = applyForm.phone.trim();
  if (phone && profile.value.phone && phone !== profile.value.phone) {
    error("申请失败", "手机号与当前绑定手机号不一致");
    return;
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
    success("已提交申请", "等待管理员审核");
    showApplyModal.value = false;
  } catch (err) {
    const message = err instanceof Error ? err.message : "申请失败";
    error("申请失败", message);
  } finally {
    isApplying.value = false;
  }
};

const handleLogout = () => {
  const shouldLogout = window.confirm("确认退出登录吗？");
  if (!shouldLogout) return;
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  success("已退出登录", "欢迎下次再来");
  router.push("/login");
};

const handleDeleteAccount = () => {
  const confirmed = window.confirm("注销账号后无法恢复，是否确认继续？");
  if (!confirmed) return;
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  success("账号已注销", "如需继续使用，请重新注册");
  router.push("/login");
};
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.center-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.tab-menu {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tab-btn {
  border: none;
  background: transparent;
  border-radius: 8px;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  color: #475569;
}

.tab-btn.active {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.tab-content {
  min-width: 0;
  display: grid;
  gap: 16px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: grid;
  gap: 14px;
}

.panel-tip {
  color: #6b7280;
  font-size: 14px;
}

.panel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.record-panel {
  display: grid;
  gap: 16px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-placeholder {
  height: 120px;
  background: linear-gradient(135deg, #e0e7ff, #f8fafc);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d4ed8;
  font-size: 32px;
  font-weight: 600;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name {
  font-weight: 600;
}

.role {
  color: #6b7280;
  font-size: 14px;
}

.info-grid {
  display: grid;
  gap: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  background: #f8fafc;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
}

.info-item .label {
  color: #6b7280;
}

.info-item .value {
  font-weight: 600;
  color: #111827;
}

.status-block {
  background: #fef3c7;
  color: #92400e;
  padding: 8px 12px;
  border-radius: 8px;
  width: fit-content;
  font-weight: 600;
}

.status-certified {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #ecfdf3;
  color: #065f46;
}

.status-reviewing {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #fef3c7;
  color: #92400e;
}

.status-rejected {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #fee2e2;
  color: #b91c1c;
}

.status-suspended {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #e0f2fe;
  color: #0369a1;
}

.status-muted {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #f3f4f6;
  color: #6b7280;
}

.status-admin {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
  background: #eef2ff;
  color: #3730a3;
}

.status-empty {
  width: 100%;
  text-align: center;
  padding: 14px 16px;
  font-size: 16px;
}

.ghost-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.primary-btn {
  border: none;
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

.danger-btn {
  border: none;
  background: #dc2626;
  color: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

.danger-btn:hover {
  background: #b91c1c;
}

.primary-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: grid;
  gap: 10px;
}

.primary-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  text-decoration: none;
  font-size: 14px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
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

.empty-card {
  background: #fff;
  border-radius: 12px;
  border: 1px dashed #e5e7eb;
  padding: 24px;
  color: #6b7280;
  text-align: center;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 120;
}

.modal-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  min-width: 320px;
  max-width: 420px;
  display: grid;
  gap: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.form-grid {
  display: grid;
  gap: 12px;
}

.form-field {
  display: grid;
  gap: 6px;
  font-size: 14px;
  color: #374151;
}

.form-field input {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 14px;
  cursor: pointer;
}

.modal-btn.primary {
  border: none;
  background: #2563eb;
  color: #fff;
}

.modal-btn:disabled {
  background: #94a3b8;
  color: #fff;
  border: none;
  cursor: not-allowed;
}

.modal-tip {
  font-size: 12px;
  color: #6b7280;
}
</style>
