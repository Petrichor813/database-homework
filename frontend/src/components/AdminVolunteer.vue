<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>志愿者审核</h2>
      <p>审核志愿者注册申请。</p>
    </header>
    <div class="filter-bar">
      <button
        v-for="option in filterOptions"
        :key="option.value"
        :class="['filter-button', { active: statusFilter === option.value }]"
        type="button"
        @click="changeFilter(option.value)"
      >
        {{ option.label }}
      </button>
    </div>
    <section class="table-card">
      <div class="table-header">
        <span>姓名</span>
        <span>手机号</span>
        <span>状态</span>
        <span>备注</span>
        <span>审核时间</span>
        <span>操作</span>
      </div>
      <div v-if="loading" class="table-row empty">正在加载...</div>
      <div v-else-if="volunteers.length === 0" class="table-row empty">
        <center>暂无符合条件的申请。</center>
      </div>
      <div v-else v-for="item in volunteers" :key="item.id" class="table-row">
        <span>{{ item.name }}</span>
        <span>{{ item.phone }}</span>
        <span>{{ statusLabel(item.status) }}</span>
        <span>{{ item.reviewNote || "-" }}</span>
        <span>{{ formatTime(item.reviewTime) }}</span>
        <div class="actions">
          <button
            class="approve"
            type="button"
            :disabled="item.status !== 'PENDING'"
            @click="openReview(item, 'APPROVE')"
          >
            通过
          </button>
          <button
            class="reject"
            type="button"
            :disabled="item.status !== 'PENDING'"
            @click="openReview(item, 'REJECT')"
          >
            拒绝
          </button>
        </div>
      </div>
    </section>
    <div v-if="error" class="error-banner">{{ error }}</div>

    <div v-if="showDialog" class="dialog-backdrop">
      <div class="dialog-card">
        <h3>{{ dialogTitle }}</h3>
        <p>请填写审核备注：</p>
        <textarea
          v-model="reviewNote"
          rows="4"
          placeholder="请输入备注"
        ></textarea>
        <div class="dialog-actions">
          <button type="button" class="ghost" @click="closeDialog">取消</button>
          <button type="button" class="primary" @click="submitReview">
            提交
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getJson, postJson } from "../utils/api";

type VolunteerRecord = {
  id: number;
  userId: number;
  name: string;
  phone: string;
  status: "PENDING" | "CERTIFIED" | "REJECTED" | "SUSPENDED";
  reviewNote?: string | null;
  createTime: string;
  reviewTime?: string | null;
};

type ReviewAction = "APPROVE" | "REJECT";

type FilterOption = {
  value: "ALL" | "PENDING" | "PROCESSED";
  label: string;
};

const filterOptions: FilterOption[] = [
  { value: "ALL", label: "全部" },
  { value: "PENDING", label: "申请中" },
  { value: "PROCESSED", label: "已操作" },
];

const statusFilter = ref<FilterOption["value"]>("ALL");
const volunteers = ref<VolunteerRecord[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

const showDialog = ref(false);
const reviewNote = ref("");
const currentAction = ref<ReviewAction | null>(null);
const currentVolunteer = ref<VolunteerRecord | null>(null);

const dialogTitle = computed(() =>
  currentAction.value === "APPROVE" ? "通过审核" : "拒绝审核",
);

const fetchVolunteers = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await getJson<VolunteerRecord[]>(
      `/api/admin/volunteers?status=${statusFilter.value}`,
    );
    volunteers.value = data;
  } catch (err) {
    const message = err instanceof Error ? err.message : "加载失败";
    error.value = message;
  } finally {
    loading.value = false;
  }
};

const changeFilter = (value: FilterOption["value"]) => {
  if (statusFilter.value === value) return;
  statusFilter.value = value;
  fetchVolunteers();
};

const openReview = (item: VolunteerRecord, action: ReviewAction) => {
  currentVolunteer.value = item;
  currentAction.value = action;
  reviewNote.value = "";
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
  currentVolunteer.value = null;
  currentAction.value = null;
  reviewNote.value = "";
};

const submitReview = async () => {
  if (!currentVolunteer.value || !currentAction.value) return;
  const note = reviewNote.value.trim();
  if (!note) {
    error.value = "审核备注不能为空";
    return;
  }
  error.value = null;
  try {
    await postJson(
      `/api/admin/volunteers/${currentVolunteer.value.id}/review`,
      {
        action: currentAction.value,
        note,
      },
    );
    closeDialog();
    await fetchVolunteers();
  } catch (err) {
    const message = err instanceof Error ? err.message : "提交失败";
    error.value = message;
  }
};

const statusLabel = (status: VolunteerRecord["status"]) => {
  switch (status) {
    case "PENDING":
      return "审核中";
    case "CERTIFIED":
      return "已通过";
    case "REJECTED":
      return "已拒绝";
    case "SUSPENDED":
      return "已停用";
    default:
      return status;
  }
};

const formatTime = (time?: string | null) => {
  if (!time) return "-";
  const parsed = new Date(time);
  return Number.isNaN(parsed.getTime()) ? time : parsed.toLocaleString();
};

onMounted(fetchVolunteers);
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

.filter-bar {
  display: flex;
  gap: 12px;
}

.filter-button {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 14px;
  color: #374151;
}

.filter-button.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
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
  grid-template-columns: 1fr 1fr 1fr 1.2fr 1.2fr 1fr;
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

.table-row.empty {
  grid-template-columns: 1fr;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 8px;
}

.actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.approve {
  background: #16a34a;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}

.reject {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}

.error-banner {
  background: #fee2e2;
  color: #991b1b;
  padding: 10px 14px;
  border-radius: 8px;
}

.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.dialog-card {
  background: #fff;
  width: min(420px, 90vw);
  padding: 20px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dialog-card textarea {
  resize: vertical;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  font-size: 14px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-actions .ghost {
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
}

.dialog-actions .primary {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
}
</style>
