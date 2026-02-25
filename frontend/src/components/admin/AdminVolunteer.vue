<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getJson, postJson } from "../../utils/api";
import type { PageResponse } from "../../utils/page";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";

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

type FilterTab = {
  value: "ALL" | "REVIEWING" | "CERTIFIED" | "SUSPENDED";
  label: string;
};

type Action = "APPROVE" | "REJECT" | "SUSPEND" | "RESUME";

const { error } = useToast();
const loading = ref(false);
const {
  pageObject,
  pageRanges,
  updatePageState,
  goToPage,
  prevPage,
  nextPage,
} = usePagination(8);

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
const filters: FilterTab[] = [
  { value: "ALL", label: "全部" },
  { value: "REVIEWING", label: "待审核" },
  { value: "CERTIFIED", label: "已认证" },
  { value: "SUSPENDED", label: "已停用" },
];
const curFilter = ref<FilterTab["value"]>("ALL");

const changeFilter = (value: FilterTab["value"]) => {
  if (curFilter.value === value) {
    return;
  }
  curFilter.value = value;
  fetchVolunteers(0);
};

// 志愿者
const curVolunteer = ref<Volunteer | null>(null);
const volunteers = ref<Volunteer[]>([]);

const fetchVolunteers = async (page: number) => {
  loading.value = true;
  try {
    const data = await getJson<PageResponse<Volunteer>>(
      `/api/admin/volunteer?status=${curFilter.value}&page=${page}&size=${pageObject.value.pageSize}`,
    );
    volunteers.value = data.content;
    updatePageState(data);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "加载失败";
    error("加载志愿者失败", msg);
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchVolunteers(0));

const statusLabel = (status?: Volunteer["status"]) => {
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
      return status || "-";
  }
};

// 操作
const curAction = ref<Action | null>(null);
const showActionDialog = ref(false);
const showInfoDialog = ref(false);
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

const openInfoDialog = (v: Volunteer) => {
  curVolunteer.value = v;
  showInfoDialog.value = true;
};

const closeInfoDialog = () => {
  curVolunteer.value = null;
  showInfoDialog.value = false;
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
    await postJson(`/api/admin/volunteer/${curVolunteer.value.id}/review`, {
      action: curAction.value,
      note,
    });
    closeActionDialog();
    await fetchVolunteers(pageObject.value.curPage);
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
            <th>状态</th>
            <th>审核时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="loading-row">
            <td colspan="5">正在加载...</td>
          </tr>
          <tr v-else-if="volunteers.length === 0" class="empty-row">
            <td colspan="5">暂无符合条件的申请。</td>
          </tr>
          <tr v-else v-for="v in volunteers" :key="v.id">
            <td>{{ v.name }}</td>
            <td>{{ v.phone }}</td>
            <td>{{ statusLabel(v.status) }}</td>
            <td>{{ formatTime(v.reviewTime) }}</td>
            <td>
              <div class="actions">
                <button type="button" class="info" @click="openInfoDialog(v)">
                  详情
                </button>
                <button
                  v-if="v.status === 'REVIEWING'"
                  type="button"
                  class="approve"
                  @click="openReviewDialog(v, 'APPROVE')"
                >
                  通过
                </button>
                <button
                  v-if="v.status === 'REVIEWING'"
                  class="reject"
                  type="button"
                  @click="openReviewDialog(v, 'REJECT')"
                >
                  拒绝
                </button>
                <button
                  v-if="v.status === 'CERTIFIED'"
                  class="reject"
                  type="button"
                  @click="openReviewDialog(v, 'SUSPEND')"
                >
                  停用
                </button>
                <button
                  v-if="v.status === 'SUSPENDED'"
                  class="approve"
                  type="button"
                  @click="openReviewDialog(v, 'RESUME')"
                >
                  恢复
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

    <div v-if="showInfoDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>志愿者详情</h3>
        <p><strong>姓名：</strong>{{ curVolunteer?.name }}</p>
        <p><strong>手机号：</strong>{{ curVolunteer?.phone }}</p>
        <p><strong>状态：</strong>{{ statusLabel(curVolunteer?.status) }}</p>
        <p v-if="curVolunteer?.status === 'REVIEWING'">
          <strong>申请原因：</strong>{{ curVolunteer?.applyReason || "无" }}
        </p>
        <p v-if="curVolunteer?.status !== 'REVIEWING'">
          <strong>审核时间：</strong>{{ formatTime(curVolunteer?.reviewTime) }}
        </p>
        <p v-if="curVolunteer?.status !== 'REVIEWING'">
          <strong>
            {{
              curVolunteer?.status === "SUSPENDED" ? "停用原因" : "审核备注"
            }}：
          </strong>
          {{ curVolunteer?.reviewNote || "无" }}
        </p>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeInfoDialog">
            关闭
          </button>
        </div>
      </div>
    </div>

    <Pagination
      :page-object="pageObject"
      :items="volunteers"
      :loading="loading"
      :page-ranges="pageRanges"
      :go-to-page="(page: number) => goToPage(page, fetchVolunteers)"
      :prev-page="() => prevPage(fetchVolunteers)"
      :next-page="() => nextPage(fetchVolunteers)"
    ></Pagination>
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
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.filter-button.active {
  background: #2563eb;
  color: white;
  border: none;
}

.filter-button.active:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.table-card {
  background: white;
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
  gap: 10px;
}

.actions button {
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
  transition: all 0.2s ease;
}

.actions button:hover:not(:disabled) {
  cursor: pointer;
  transform: translateY(-1px);
}

.actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.info {
  background: #2563eb;
}

.info:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.approve {
  background: #22c55e;
}

.approve:hover:not(:disabled) {
  background: #16a34a;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.reject {
  background: #ef4444;
}

.reject:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
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
  background: white;
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

.dialog-actions button {
  min-width: 80px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.dialog-actions button:hover {
  cursor: pointer;
  transform: translateY(-1px);
}

.confirm-button {
  background: #22c55e;
  color: white;
  border: none;
}

.confirm-button:hover {
  background: #16a34a;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.cancel-button {
  background: white;
  color: #374151;
  border: 2px solid #e5e7eb;
}

.cancel-button:hover {
  background: #efefef;
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.close-button {
  background: #2563eb;
  color: white;
  border: none;
}

.close-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}
</style>
