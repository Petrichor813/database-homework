<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getJson, postJson } from "../../utils/api";
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
  | "NOT_SIGNED_UP"
  | "REVIEWING"
  | "CONFIRMED"
  | "CANCELLED"
  | "REJECTED"
  | "PARTICIPATED"
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
  pointsPerHour?: number;
  maxParticipants?: number;
  curParticipants?: number;
  signupStatus?: SignupStatus; // 添加报名状态字段
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

    // 设置默认报名状态为未报名
    activities.value = response.content.map((activity) => ({
      ...activity,
      signupStatus: activity.signupStatus || "NOT_SIGNED_UP",
    }));

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

// 活动详情弹窗
const showDetailDialog = ref(false);
const curActivity = ref<Activity | null>(null);

const showDetail = (item: Activity) => {
  curActivity.value = item;
  showDetailDialog.value = true;
};

const closeDetailDialog = () => {
  curActivity.value = null;
  showDetailDialog.value = false;
};

const statusLabel = (status: ActivityStatus) => {
  switch (status) {
    case "RECRUITING":
    case "CONFIRMED":
      return "报名中";
    case "ONGOING":
      return "进行中";
    case "COMPLETED":
    case "CANCELLED":
      return "已结束";
    default:
      return "未知";
  }
};

const activityTypeLabel = (type: ActivityType) => {
  const option = activityTypeOptions.find((opt) => opt.value === type);
  return option ? option.label : type;
};

const canSignup = (status: ActivityStatus) =>
  status === "RECRUITING" || status === "CONFIRMED";

const canCancel = (activity: Activity) => {
  // 只有已报名且活动未开始或未结束的状态才能取消报名
  return (
    (activity.signupStatus === "REVIEWING" ||
      activity.signupStatus === "CONFIRMED") &&
    (activity.status === "RECRUITING" || activity.status === "CONFIRMED")
  );
};

// 报名活动
const handleSignup = async (activity: Activity) => {
  try {
    const response = await postJson<{ id: number; message: string }>(
      "/api/activity/signup",
      {
        activityId: activity.id,
        volunteerStartTime: activity.startTime,
        volunteerEndTime: activity.endTime,
      }
    );

    // 更新活动报名状态
    activity.signupStatus = "REVIEWING";
    if (activity.curParticipants !== undefined) {
      activity.curParticipants += 1;
    }

    success("报名成功", response.message);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "报名失败";
    error("报名失败", msg);
  }
};

// 取消报名
const handleCancelSignup = async (activity: Activity) => {
  try {
    const response = await postJson<{ id: number; message: string }>(
      "/api/activity/cancel-signup",
      {
        activityId: activity.id,
      }
    );

    // 更新活动报名状态
    activity.signupStatus = "CANCELLED";
    if (activity.curParticipants !== undefined) {
      activity.curParticipants = Math.max(0, activity.curParticipants - 1);
    }

    success("取消报名成功", response.message);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "取消报名失败";
    error("取消报名失败", msg);
  }
};

onMounted(() => {
  fetchActivities(0);
});
</script>

<template>
  <div class="activity-list">
    <header class="page-header">
      <h2>活动列表</h2>
    </header>

    <section class="search-box">
      <input
        v-model.trim="keyword"
        type="text"
        placeholder="搜索活动标题/描述关键词"
        @keyup.enter="handleSearch"
      />
      <button type="button" @click="handleSearch">搜索</button>
    </section>

    <section class="filters">
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
          <option value="COMPLETED">已结束</option>
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
      >
        <div class="row-main">
          <h3>{{ item.title }}</h3>
          <p class="meta">
            {{ item.startTime }} 至 {{ item.endTime }} ｜ {{ item.location }} ｜
            {{ statusLabel(item.status) }}
            <span v-if="item.maxParticipants"
              >｜ {{ item.curParticipants || 0 }}/{{
                item.maxParticipants
              }}人</span
            >
          </p>
        </div>

        <div class="row-actions">
          <button type="button" class="detail-button" @click="showDetail(item)">
            详细信息
          </button>
          <button
            type="button"
            class="signup-button"
            :disabled="
              !canSignup(item.status) ||
              (item.signupStatus !== undefined &&
                item.signupStatus !== 'NOT_SIGNED_UP' &&
                item.signupStatus !== 'CANCELLED' &&
                item.signupStatus !== 'REJECTED')
            "
            @click="handleSignup(item)"
          >
            {{
              item.signupStatus === "REVIEWING" ||
              item.signupStatus === "CONFIRMED"
                ? "已报名"
                : "报名"
            }}
          </button>
          <button
            type="button"
            class="cancel-button"
            :disabled="!canCancel(item)"
            @click="handleCancelSignup(item)"
          >
            取消报名
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

    <!-- 活动详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>活动详情</h3>
        <p><strong>活动名称：</strong>{{ curActivity?.title }}</p>
        <p>
          <strong>活动类型：</strong>
          {{ activityTypeLabel(curActivity?.type || "OTHER") }}
        </p>
        <p><strong>活动地点：</strong>{{ curActivity?.location || "" }}</p>
        <p>
          <strong>开始时间：</strong>
          {{ curActivity?.startTime || "" }}
        </p>
        <p>
          <strong>结束时间：</strong>
          {{ curActivity?.endTime || "" }}
        </p>
        <p>
          <strong>活动状态：</strong>
          {{ statusLabel(curActivity?.status || "RECRUITING") }}
        </p>
        <p>
          <strong>活动描述：</strong>
          {{ curActivity?.description || "暂无描述" }}
        </p>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeDetailDialog">
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.search-box,
.filters,
.activity-list {
  background: white;
  border-radius: 12px;
  padding: 14px;
}

.search-box {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.search-box input,
.filters select,
.filters input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.2s ease;
}

.filters select {
  cursor: pointer;
}

.search-box input:hover,
.search-box input:focus,
.filters select:hover,
.filters select:focus,
.filters input:hover,
.filters input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-box input:focus,
.filters select:focus,
.filters input:focus {
  border-width: 2px;
}

.search-box button {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-box button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.filters {
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
  color: #111827;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.row-actions button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.row-actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.row-actions .detail-button {
  background: white;
  border: 1px solid #d1d5db;
  color: black;
}

.row-actions .detail-button:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #111827;
  box-shadow: 0 4px 12px rgba(209, 213, 219, 0.3);
}

.row-actions .signup-button {
  background: #2563eb;
  border: none;
  color: white;
}

.row-actions .signup-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.row-actions .cancel-button {
  background: #ef4444;
  border: none;
  color: white;
}

.row-actions .cancel-button:hover {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
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
  background: white;
  padding: 20px;
  gap: 12px;
  border-radius: 12px;
}

.dialog-area h3 {
  text-align: center;
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
  cursor: pointer;
  transition: all 0.2s ease;
}

.dialog-actions button:hover {
  transform: translateY(-1px);
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
