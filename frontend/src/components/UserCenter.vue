<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>个人中心</h2>
      <p>汇总志愿者资料、积分与兑换信息。</p>
    </header>

    <section class="profile-grid">
      <div class="profile-card">
        <div class="avatar-placeholder">{{ avatarText }}</div>
        <div class="info">
          <p class="name">{{ displayName }}</p>
          <p class="role">{{ roleLabel }}</p>
          <button class="ghost-btn">编辑资料</button>
        </div>
      </div>
      <div class="profile-card">
        <h3>基础信息</h3>
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
        <h3>认证状态</h3>
        <div class="status-block" :class="statusClass">
          {{ volunteerStatusLabel }}
        </div>
        <button v-if="showSupplement" class="ghost-btn">提交补充材料</button>
      </div>
    </section>

    <section class="summary">
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
    </section>

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
        <span :class="record.amount.startsWith('+') ? 'positive' : 'negative'">
          {{ record.amount }}
        </span>
        <span>{{ record.note }}</span>
      </div>
    </section>
    <section v-else class="empty-card">
      <p>暂无积分变动记录</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getJson } from "../utils/api";
import { useToast } from "../utils/toast";

type UserRole = "ADMIN" | "VOLUNTEER" | "USER";
type VolunteerStatus =
  | "CERTIFIED"
  | "PENDING"
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

const { error } = useToast();

const profile = ref<UserProfile | null>(null);
const records = ref<PointsRecord[]>([]);

const displayName = computed(() => profile.value?.username || "游客");
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
const displayPhone = computed(() => profile.value?.phone || "暂无");
const displayPoints = computed(() => profile.value?.points ?? 0);
const displayHours = computed(() => profile.value?.serviceHours ?? 0);

const volunteerStatusLabel = computed(() => {
  if (!profile.value) return "未登录";
  if (profile.value.role === "ADMIN") return "管理员权限";
  const status = profile.value.volunteerStatus;
  if (status === "CERTIFIED") return "已认证";
  if (status === "PENDING") return "等待管理员审核";
  if (status === "REJECTED") return "未通过审核";
  if (status === "SUSPENDED") return "已停用";
  return "未申请";
});

const statusClass = computed(() => {
  if (profile.value?.role === "ADMIN") return "status-admin";
  if (profile.value?.volunteerStatus === "CERTIFIED") return "status-certified";
  if (profile.value?.volunteerStatus === "PENDING") return "status-pending";
  if (profile.value?.volunteerStatus === "REJECTED") return "status-rejected";
  if (profile.value?.volunteerStatus === "SUSPENDED") return "status-suspended";
  return "status-muted";
});

const showSupplement = computed(
  () =>
    profile.value?.volunteerStatus === "PENDING" ||
    profile.value?.volunteerStatus === "REJECTED",
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

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
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
  background: #ecfdf3;
  color: #065f46;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-rejected {
  background: #fee2e2;
  color: #b91c1c;
}

.status-suspended {
  background: #e0f2fe;
  color: #0369a1;
}

.status-muted {
  background: #f3f4f6;
  color: #6b7280;
}

.status-admin {
  background: #eef2ff;
  color: #3730a3;
}

.ghost-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
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
</style>
