<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getJson, putJson, deleteJson } from "../../utils/api";
import { usePagination, type PageResponse } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";

type ActivityStatus =
  | "RECRUITING"
  | "CONFIRMED"
  | "ONGOING"
  | "COMPLETED"
  | "CANCELLED";

type ActivityType =
  | "COMMUNITY_SERVICE"
  | "ENVIRONMENTAL_PROTECTION"
  | "ELDERLY_CARE"
  | "CHILDREN_TUTORING"
  | "DISABILITIES_SUPPORT"
  | "CULTURAL_EVENTS"
  | "HEALTH_PROMOTION"
  | "OTHER";

type SignupStatus =
  | "REVIEWING"
  | "CONFIRMED"
  | "PARTICIPATED"
  | "CANCELLED"
  | "REJECTED"
  | "UNARRIVED";

interface Activity {
  id: number;
  title: string;
  description: string;
  type: ActivityType;
  location: string;
  startTime: string;
  endTime: string;
  status: ActivityStatus;
  pointsPerHour: number;
  maxParticipants: number;
  curParticipants: number;
}

interface SignupRecord {
  id: number;
  signupId: number;
  volunteerId: number;
  volunteerName: string;
  volunteerPhone: string;
  status: SignupStatus;
  volunteerStartTime: string | null;
  volunteerEndTime: string | null;
  actualHours: number | null;
  points: number | null;
  signupTime: string;
  note: string | null;
}

const { success, error } = useToast();
const {
  pageObject,
  pageRanges,
  updatePageState,
  goToPage,
  prevPage,
  nextPage,
} = usePagination(8);

const loading = ref(false);
const activities = ref<Activity[]>([]);
const keyword = ref("");
const filterDate = ref("");
const filterType = ref("ALL");
const filterStatus = ref("ALL");
const sortType = ref("time");

const activityTypeOptions = [
  { label: "社区服务", value: "COMMUNITY_SERVICE" },
  { label: "环境保护", value: "ENVIRONMENTAL_PROTECTION" },
  { label: "敬老助老", value: "ELDERLY_CARE" },
  { label: "儿童助学", value: "CHILDREN_TUTORING" },
  { label: "助残服务", value: "DISABILITIES_SUPPORT" },
  { label: "文化活动", value: "CULTURAL_EVENTS" },
  { label: "健康宣传", value: "HEALTH_PROMOTION" },
  { label: "其他", value: "OTHER" },
];

const fetchActivities = async (page = 0) => {
  loading.value = true;
  try {
    const query = new URLSearchParams({
      page: String(page),
      size: String(pageObject.value.pageSize),
      type: filterType.value,
      status: filterStatus.value,
      sort: sortType.value,
    });

    if (keyword.value) {
      query.append("keyword", keyword.value);
    }
    if (filterDate.value) {
      query.append("date", filterDate.value);
    }

    const response = await getJson<PageResponse<Activity>>(
      `/api/activity/get-activities?${query.toString()}`
    );

    activities.value = response.content;

    updatePageState({
      curPage: response.curPage,
      pageSize: response.pageSize,
      totalElements: response.totalElements,
      totalPages: response.totalPages,
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : "未知错误";
    error("活动加载失败", msg);
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  await fetchActivities(0);
};

const statusLabel = (status: ActivityStatus) => {
  switch (status) {
    case "RECRUITING":
    case "CONFIRMED":
      return "报名中";
    case "ONGOING":
      return "进行中";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    default:
      return "未知";
  }
};

const activityTypeLabel = (type: ActivityType) => {
  const option = activityTypeOptions.find((opt) => opt.value === type);
  return option ? option.label : type;
};

const isActivityEnded = (status: ActivityStatus) => {
  return status === "COMPLETED" || status === "CANCELLED";
};

const canEdit = (status: ActivityStatus) => {
  return (
    status === "RECRUITING" || status === "CONFIRMED" || status === "ONGOING"
  );
};

const canDelete = (status: ActivityStatus) => {
  return status === "RECRUITING" || status === "CONFIRMED";
};

const handleDelete = async (activity: Activity) => {
  if (!confirm(`确定要删除活动"${activity.title}"吗？此操作不可恢复。`)) {
    return;
  }

  try {
    await deleteJson(`/api/admin/activity/${activity.id}`);
    success("删除成功", "活动已删除");
    await fetchActivities(pageObject.value.curPage);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "删除失败";
    error("删除失败", msg);
  }
};

onMounted(() => {
  fetchActivities(0);
});

const showEditDialog = ref(false);
const showSignupDialog = ref(false);
const showEditSignupDialog = ref(false);
const curActivity = ref<Activity | null>(null);
const signupRecords = ref<SignupRecord[]>([]);
const curSignupRecord = ref<SignupRecord | null>(null);

const editForm = ref({
  title: "",
  description: "",
  type: "OTHER" as ActivityType,
  location: "",
  startDate: "",
  startTime: "",
  endDate: "",
  endTime: "",
  pointsPerHour: 0,
  maxParticipants: 0,
  status: "RECRUITING" as ActivityStatus,
});

const editSignupForm = ref({
  status: "REVIEWING" as SignupStatus,
  volunteerStartDate: "",
  volunteerStartTime: "",
  volunteerEndDate: "",
  volunteerEndTime: "",
  actualHours: 0,
  points: 0,
  note: "",
});

const parseDateTime = (dateTimeStr: string | null | undefined): { date: string; time: string } => {
  if (!dateTimeStr) {
    return { date: "", time: "" };
  }
  const parts = dateTimeStr.split(" ");
  if (parts.length < 2) {
    return { date: "", time: "" };
  }
  const [date, time] = parts;
  if (!date) {
    return { date: "", time: "" };
  }
  if (!time) {
    return { date, time: "" };
  }
  
  const timeParts = time.split(":");
  if (timeParts.length < 2) {
    return { date, time: "" };
  }
  const [hour, minute] = timeParts;
  return { date, time: `${hour}:${minute}` };
};

const openEditDialog = (activity: Activity) => {
  curActivity.value = activity;

  const startDateTime = parseDateTime(activity.startTime);
  const endDateTime = parseDateTime(activity.endTime);

  editForm.value = {
    title: activity.title,
    description: activity.description || "",
    type: activity.type,
    location: activity.location,
    startDate: startDateTime.date,
    startTime: startDateTime.time,
    endDate: endDateTime.date,
    endTime: endDateTime.time,
    pointsPerHour: activity.pointsPerHour,
    maxParticipants: activity.maxParticipants,
    status: activity.status,
  };
  showEditDialog.value = true;
};

const closeEditDialog = () => {
  curActivity.value = null;
  showEditDialog.value = false;
};

const handleSaveActivity = async () => {
  if (!curActivity.value) return;

  const payload = {
    ...editForm.value,
    startTime: `${editForm.value.startDate} ${editForm.value.startTime}:00`,
    endTime: `${editForm.value.endDate} ${editForm.value.endTime}:00`,
  };

  try {
    await putJson(`/api/admin/activity/${curActivity.value.id}`, payload);
    success("保存成功", "活动信息已更新");
    closeEditDialog();
    await fetchActivities(pageObject.value.curPage);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "保存失败";
    error("保存失败", msg);
  }
};

const openSignupDialog = async (activity: Activity) => {
  curActivity.value = activity;
  try {
    const records = await getJson<SignupRecord[]>(
      `/api/admin/activity/${activity.id}/signups`
    );
    signupRecords.value = records;
    showSignupDialog.value = true;
  } catch (e) {
    const msg = e instanceof Error ? e.message : "加载失败";
    error("加载失败", msg);
  }
};

const closeSignupDialog = () => {
  curActivity.value = null;
  signupRecords.value = [];
  showSignupDialog.value = false;
};

const signupStatusLabel = (status: SignupStatus) => {
  switch (status) {
    case "REVIEWING":
      return "待审核";
    case "CONFIRMED":
      return "已确认";
    case "PARTICIPATED":
      return "已参与";
    case "CANCELLED":
      return "已取消";
    case "REJECTED":
      return "已拒绝";
    case "UNARRIVED":
      return "未到场";
    default:
      return "未知";
  }
};

const openEditSignupDialog = (record: SignupRecord) => {
  curSignupRecord.value = record;

  const startDateTime = parseDateTime(record.volunteerStartTime);
  const endDateTime = parseDateTime(record.volunteerEndTime);

  editSignupForm.value = {
    status: record.status,
    volunteerStartDate: startDateTime.date,
    volunteerStartTime: startDateTime.time,
    volunteerEndDate: endDateTime.date,
    volunteerEndTime: endDateTime.time,
    actualHours: record.actualHours || 0,
    points: record.points || 0,
    note: record.note || "",
  };
  showEditSignupDialog.value = true;
};

const closeEditSignupDialog = () => {
  curSignupRecord.value = null;
  showEditSignupDialog.value = false;
};

const handleSaveSignup = async () => {
  if (!curActivity.value || !curSignupRecord.value) return;

  const payload = {
    ...editSignupForm.value,
    volunteerStartTime:
      editSignupForm.value.volunteerStartDate &&
      editSignupForm.value.volunteerStartTime
        ? `${editSignupForm.value.volunteerStartDate} ${editSignupForm.value.volunteerStartTime}:00`
        : null,
    volunteerEndTime:
      editSignupForm.value.volunteerEndDate &&
      editSignupForm.value.volunteerEndTime
        ? `${editSignupForm.value.volunteerEndDate} ${editSignupForm.value.volunteerEndTime}:00`
        : null,
  };

  try {
    await putJson(
      `/api/admin/activity/${curActivity.value.id}/signups/${curSignupRecord.value.id}`,
      payload
    );
    success("保存成功", "报名记录已更新");
    closeEditSignupDialog();
    await openSignupDialog(curActivity.value);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "保存失败";
    error("保存失败", msg);
  }
};

const handleReviewAction = async (
  record: SignupRecord,
  action: SignupStatus
) => {
  if (!curActivity.value) return;

  try {
    await putJson(
      `/api/admin/activity/${curActivity.value.id}/signups/${record.id}`,
      { status: action }
    );
    success("操作成功", "报名状态已更新");
    await openSignupDialog(curActivity.value);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "操作失败";
    error("操作失败", msg);
  }
};
</script>

<template>
  <div class="admin-activity">
    <header class="page-header">
      <h2>活动管理</h2>
      <p>发布、编辑、删除活动，管理志愿者报名</p>
    </header>

    <section class="search-wrap">
      <input
        v-model.trim="keyword"
        type="text"
        placeholder="搜索活动标题/描述关键词"
        @keyup.enter="handleSearch"
      />
      <button type="button" @click="handleSearch">搜索</button>
    </section>

    <section class="filters-wrap">
      <label class="filter-item">
        <span>日期</span>
        <input v-model="filterDate" type="date" @change="handleSearch" />
      </label>

      <label class="filter-item">
        <span>活动类型</span>
        <select v-model="filterType" @change="handleSearch">
          <option value="ALL">全部类型</option>
          <option
            v-for="option in activityTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </label>

      <label class="filter-item">
        <span>活动状态</span>
        <select v-model="filterStatus" @change="handleSearch">
          <option value="ALL">全部状态</option>
          <option value="RECRUITING">报名中</option>
          <option value="ONGOING">进行中</option>
          <option value="COMPLETED">已完成</option>
        </select>
      </label>

      <label class="filter-item">
        <span>排序方式</span>
        <select v-model="sortType" @change="handleSearch">
          <option value="time">按时间（最新优先）</option>
          <option value="status">按状态（报名中优先）</option>
        </select>
      </label>
    </section>

    <section class="activity-list">
      <p v-if="loading" class="empty">活动加载中...</p>
      <p v-else-if="activities.length === 0" class="empty">暂无匹配活动</p>

      <article
        v-for="item in activities"
        v-else
        :key="item.id"
        class="activity-row"
        :class="{ faded: isActivityEnded(item.status) }"
      >
        <div class="row-main">
          <h3>{{ item.title }}</h3>
          <p class="meta">
            {{ item.startTime }} 至 {{ item.endTime }} ｜ {{ item.location }} ｜
            {{ activityTypeLabel(item.type) }} ｜ {{ statusLabel(item.status) }}
            <span v-if="item.maxParticipants"
              >｜ {{ item.curParticipants || 0 }}/{{
                item.maxParticipants
              }}人</span
            >
          </p>
        </div>

        <div class="row-actions">
          <button
            type="button"
            :disabled="!canEdit(item.status)"
            @click="openEditDialog(item)"
          >
            编辑
          </button>
          <button type="button" @click="openSignupDialog(item)">
            查看报名
          </button>
          <button
            type="button"
            class="danger"
            :disabled="!canDelete(item.status)"
            @click="handleDelete(item)"
          >
            删除
          </button>
        </div>
      </article>
    </section>

    <Pagination
      :page-object="pageObject"
      :items="activities"
      :loading="loading"
      :page-ranges="pageRanges"
      :go-to-page="(page: number) => goToPage(page, fetchActivities)"
      :prev-page="() => prevPage(fetchActivities)"
      :next-page="() => nextPage(fetchActivities)"
    />

    <div v-if="showEditDialog" class="dialog-bg">
      <div class="dialog-area large">
        <h3>编辑活动</h3>
        <div class="form-group">
          <label>活动名称</label>
          <input v-model="editForm.title" type="text" />
        </div>
        <div class="form-group">
          <label>活动类型</label>
          <select v-model="editForm.type">
            <option
              v-for="option in activityTypeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>活动地点</label>
          <input v-model="editForm.location" type="text" />
        </div>
        <div class="form-group">
          <label>开始时间</label>
          <div class="datetime-input-group">
            <input
              v-model="editForm.startDate"
              type="date"
              class="date-input"
            />
            <input
              v-model="editForm.startTime"
              type="time"
              class="time-input"
            />
          </div>
        </div>
        <div class="form-group">
          <label>结束时间</label>
          <div class="datetime-input-group">
            <input v-model="editForm.endDate" type="date" class="date-input" />
            <input v-model="editForm.endTime" type="time" class="time-input" />
          </div>
        </div>
        <div class="form-group">
          <label>每小时积分</label>
          <input
            v-model.number="editForm.pointsPerHour"
            type="number"
            min="0"
            step="0.1"
          />
        </div>
        <div class="form-group">
          <label>人数上限</label>
          <input
            v-model.number="editForm.maxParticipants"
            type="number"
            min="1"
          />
        </div>
        <div class="form-group">
          <label>活动状态</label>
          <select v-model="editForm.status">
            <option value="RECRUITING">报名中</option>
            <option value="CONFIRMED">已确认</option>
            <option value="ONGOING">进行中</option>
            <option value="COMPLETED">已完成</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>
        <div class="form-group">
          <label>活动描述</label>
          <textarea v-model="editForm.description" rows="4"></textarea>
        </div>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeEditDialog">
            取消
          </button>
          <button type="button" class="save-button" @click="handleSaveActivity">
            保存
          </button>
        </div>
      </div>
    </div>

    <div v-if="showSignupDialog" class="dialog-bg">
      <div class="dialog-area large">
        <h3>报名管理 - {{ curActivity?.title }}</h3>
        <p v-if="signupRecords.length === 0" class="empty">暂无报名记录</p>
        <div v-else class="signup-list">
          <div
            v-for="record in signupRecords"
            :key="record.id"
            class="signup-row"
          >
            <div class="signup-info">
              <h4>{{ record.volunteerName }}</h4>
              <p class="meta">
                {{ record.volunteerPhone }} ｜
                {{ signupStatusLabel(record.status) }}
              </p>
              <p v-if="record.volunteerStartTime" class="meta">
                服务时间：{{ record.volunteerStartTime }} 至
                {{ record.volunteerEndTime }}
              </p>
              <p v-if="record.actualHours !== null" class="meta">
                实际时长：{{ record.actualHours }}小时
              </p>
              <p v-if="record.points !== null" class="meta">
                积分：{{ record.points }}
              </p>
              <p v-if="record.note" class="meta">备注：{{ record.note }}</p>
            </div>
            <div class="signup-actions">
              <button
                v-if="record.status === 'REVIEWING'"
                type="button"
                class="success"
                @click="handleReviewAction(record, 'CONFIRMED')"
              >
                批准
              </button>
              <button
                v-if="record.status === 'REVIEWING'"
                type="button"
                class="danger"
                @click="handleReviewAction(record, 'REJECTED')"
              >
                拒绝
              </button>
              <button type="button" @click="openEditSignupDialog(record)">
                编辑
              </button>
            </div>
          </div>
        </div>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeSignupDialog">
            关闭
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditSignupDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>编辑报名记录</h3>
        <div class="form-group">
          <label>志愿者姓名</label>
          <input :value="curSignupRecord?.volunteerName" type="text" disabled />
        </div>
        <div class="form-group">
          <label>状态</label>
          <select v-model="editSignupForm.status">
            <option value="REVIEWING">待审核</option>
            <option value="CONFIRMED">已确认</option>
            <option value="PARTICIPATED">已参与</option>
            <option value="CANCELLED">已取消</option>
            <option value="REJECTED">已拒绝</option>
            <option value="UNARRIVED">未到场</option>
          </select>
        </div>
        <div class="form-group">
          <label>志愿者开始时间</label>
          <div class="datetime-input-group">
            <input
              v-model="editSignupForm.volunteerStartDate"
              type="date"
              class="date-input"
            />
            <input
              v-model="editSignupForm.volunteerStartTime"
              type="time"
              class="time-input"
            />
          </div>
        </div>
        <div class="form-group">
          <label>志愿者结束时间</label>
          <div class="datetime-input-group">
            <input
              v-model="editSignupForm.volunteerEndDate"
              type="date"
              class="date-input"
            />
            <input
              v-model="editSignupForm.volunteerEndTime"
              type="time"
              class="time-input"
            />
          </div>
        </div>
        <div class="form-group">
          <label>实际时长（小时）</label>
          <input
            v-model.number="editSignupForm.actualHours"
            type="number"
            min="0"
            step="0.5"
          />
        </div>
        <div class="form-group">
          <label>积分</label>
          <input
            v-model.number="editSignupForm.points"
            type="number"
            min="0"
            step="0.1"
          />
        </div>
        <div class="form-group">
          <label>备注</label>
          <textarea v-model="editSignupForm.note" rows="3"></textarea>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="close-button"
            @click="closeEditSignupDialog"
          >
            取消
          </button>
          <button type="button" class="save-button" @click="handleSaveSignup">
            保存
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-activity {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-header p {
  color: #6b7280;
}

.search-wrap,
.filters-wrap,
.activity-list {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.search-wrap input,
.filters-wrap select,
.filters-wrap input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
}

.search-wrap button {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-wrap button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.filters-wrap {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.filter-item {
  display: grid;
  font-size: 13px;
  color: #374151;
  gap: 6px;
}

.activity-list {
  display: grid;
  gap: 10px;
}

.activity-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
}

.activity-row.faded {
  opacity: 0.5;
}

.meta {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.row-actions button {
  background: white;
  color: #111827;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.row-actions button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.row-actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.row-actions .danger {
  background: #ef4444;
  border: none;
  color: white;
}

.row-actions .danger:hover {
  background: red;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
}

.row-actions .danger:disabled {
  background: #ef4444;
  opacity: 0.45;
}

.empty {
  text-align: center;
  color: #6b7280;
  padding: 18px 0;
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
  max-height: 90vh;
  background: white;
  padding: 20px;
  gap: 12px;
  border-radius: 12px;
  overflow-y: auto;
}

.dialog-area.large {
  width: min(800px, 90vw);
}

.dialog-area h3 {
  text-align: center;
}

.form-group {
  display: grid;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
}

.form-group input:disabled {
  background: #f3f4f6;
  color: #6b7280;
}

.datetime-input-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.datetime-input-group .date-input,
.datetime-input-group .time-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
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

.close-button {
  background: #6b7280;
  color: white;
  border: none;
}

.close-button:hover {
  background: #4b5563;
}

.save-button {
  background: #2563eb;
  color: white;
  border: none;
}

.save-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.signup-list {
  display: grid;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.signup-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
}

.signup-info h4 {
  margin: 0 0 4px 0;
}

.signup-info .meta {
  margin: 2px 0;
}

.signup-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.signup-actions button {
  background: white;
  color: #111827;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.signup-actions button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.signup-actions .success {
  background: #10b981;
  border: none;
  color: white;
}

.signup-actions .success:hover {
  background: #059669;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.signup-actions .danger {
  background: #ef4444;
  border: none;
  color: white;
}

.signup-actions .danger:hover {
  background: red;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
}
</style>
