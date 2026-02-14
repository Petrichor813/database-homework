<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getJson, postJson } from "../../utils/api";
import { useToast } from "../../utils/toast";

type VolunteerStatus = "CERTIFIED" | "REVIEWING" | "REJECTED" | "SUSPENDED";

type Volunteer = {
  id: number;
  userId: number;
  name: string;
  phone: string;
  applyReason?: string | null;
  status: VolunteerStatus;
  reviewNote?: string | null;
  createTime: string;
  reviewTime?: string | null;
};

type FilterOption = {
  value: "ALL" | "REVIEWING" | "PROCESSED" | "SUSPENDED";
  label: string;
};

type Action = "APPROVE" | "REJECT" | "SUSPEND" | "RESUME";

const { error } = useToast();
const loading = ref(false);

// 工具函数
const formatTime = (time?: string | null) => {
  if (!time) {
    return "-";
  }

  const parsedTime = new Date(time);
  return Number.isNaN(parsedTime.getTime())
    ? time
    : parsedTime.toLocaleString();
};

// 过滤选项卡
const filters: FilterOption[] = [
  { value: "ALL", label: "全部" },
  { value: "REVIEWING", label: "待审核" },
  { value: "PROCESSED", label: "已处理" },
  { value: "SUSPENDED", label: "已停用" },
];
const curFilter = ref<FilterOption["value"]>("ALL");

const changeFilter = (value: FilterOption["value"]) => {
  if (curFilter.value === value) {
    return;
  }
  curFilter.value = value;
  fetchVolunteers();
};

// 志愿者
const curVolunteer = ref<Volunteer | null>(null);
const volunteers = ref<Volunteer[]>([]);

const fetchVolunteers = async () => {
  loading.value = true;
  try {
    const data = await getJson<Volunteer[]>(
      `/api/admin/volunteers?status=${curFilter.value}`,
    );
    volunteers.value = data;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "加载失败";
    error("加载志愿者失败", msg);
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchVolunteers());

const statusLabel = (status: Volunteer["status"]) => {
  switch (status) {
    case "CERTIFIED":
      return "已通过";
    case "REVIEWING":
      return "待审核";
    case "REJECTED":
      return "已拒绝";
    case "SUSPENDED":
      return "已停用";
    default:
      return status;
  }
};

// 操作
const curAction = ref<Action | null>(null);
const showActionDialog = ref(false);
const reviewNote = ref<string>("");

const actionDialogTitle = computed(() => {
  switch (curAction.value) {
    case "APPROVE":
      return "通过志愿者申请";
    case "REJECT":
      return "拒绝志愿者申请";
    case "SUSPEND":
      return "停用志愿者";
    case "RESUME":
      return "恢复志愿者";
    default:
      return "";
  }
});

const openReviewDialog = (v: Volunteer, action: Action) => {
  curVolunteer.value = v;
  curAction.value = action;
  reviewNote.value = "";
  showActionDialog.value = true;
};

const closeActionDialog = () => {
  curVolunteer.value = null;
  curAction.value = null;
  reviewNote.value = "";
  showActionDialog.value = false;
};

const submitReview = async () => {
  if (!curVolunteer.value || !curAction.value) {
    return;
  }

  const note = reviewNote.value.trim();
  if (!note) {
    error("提交审核失败", "审核备注不能为空");
    return;
  }

  try {
    await postJson(`/api/admin/volunteers/${curVolunteer.value.id}/review`, {
      action: curAction.value,
      note,
    });
    closeActionDialog();
    await fetchVolunteers();
  } catch (err) {
    const msg = err instanceof Error ? err.message : "提交失败";
    error("提交审核失败", msg);
  }
};
</script>

<template>
  <div class="admin-volunteer">
    <header class="page-header">
      <h2>志愿者管理</h2>
      <p>管理系统中的志愿者信息，包括审核、停用和恢复等</p>
    </header>

    <div class="filter-bar">
      <button
        type="button"
        v-for="filter in filters"
        :key="filter.value"
        :class="['filter-button', { active: filter.value === curFilter }]"
        @click="changeFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </div>

    <section class="table-card">
      <table class="volunteer-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>手机号</th>
            <th>申请原因</th>
            <th>状态</th>
            <th>备注</th>
            <th>审核时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="loading-row">
            <td colspan="7">正在加载...</td>
          </tr>
          <tr v-else-if="volunteers.length === 0" class="empty-row">
            <td colspan="7">暂无符合条件的申请。</td>
          </tr>
          <tr v-else v-for="v in volunteers" :key="v.id">
            <td>{{ v.name }}</td>
            <td>{{ v.phone }}</td>
            <td>{{ v.applyReason || "-" }}</td>
            <td>{{ statusLabel(v.status) }}</td>
            <td>{{ v.reviewNote || "-" }}</td>
            <td>{{ formatTime(v.reviewTime) }}</td>
            <td>
              <div class="actions">
                <button
                  class="approve"
                  type="button"
                  :disabled="v.status !== 'REVIEWING'"
                  @click="openReviewDialog(v, 'APPROVE')"
                >
                  通过
                </button>
                <button
                  class="reject"
                  type="button"
                  :disabled="v.status !== 'REVIEWING'"
                  @click="openReviewDialog(v, 'REJECT')"
                >
                  拒绝
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div v-if="showActionDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>{{ actionDialogTitle }}</h3>
        <textarea v-model="reviewNote" placeholder="请输入审核备注" rows="5">
        </textarea>
        <div class="dialog-actions">
          <button type="button" class="confirm-button" @click="submitReview">
            确认
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeActionDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.admin-volunteer {
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
  min-width: 80px;
  background: white;
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  padding: 6px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 20px;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    border 0.2s ease;
}

.filter-button.active {
  background: #2563eb;
  color: white;
  border: none;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.volunteer-table {
  width: 100%;
  border-collapse: collapse;
}

.volunteer-table th,
.volunteer-table td {
  padding: 14px 16px;
  text-align: center;
}

.volunteer-table th {
  background: #f8fafc;
  color: #6b7280;
  font-size: 16px;
  font-weight: 600;
}

.volunteer-table tbody tr {
  border-top: 1px solid #e5e7eb;
}

.loading-row td,
.empty-row td {
  color: #6b7280;
  text-align: center;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.approve {
  background: #16a34a;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}

.reject {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}

.dialog-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 2;
}

.dialog-area {
  display: flex;
  flex-direction: column;
  width: min(420px, 90vw);
  background: #fff;
  padding: 20px;
  gap: 12px;
  border-radius: 12px;
}

.dialog-area h3 {
  text-align: center;
}

.dialog-area textarea {
  resize: vertical;
  font-size: 14px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.dialog-area textarea:focus {
  border-color: #2563eb;
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.confirm-button {
  min-width: 80px;
  background: #22c55e;
  color: white;
  cursor: pointer;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.confirm-button:hover {
  background: #16a34a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.cancel-button {
  min-width: 80px;
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
</style>
