<script setup lang="ts">
import { onMounted, ref } from "vue";
import Pagination from "../utils/Pagination.vue";
import { getJson } from "../../utils/api";
import { usePagination, type PageResponse } from "../../utils/page";
import { useToast } from "../../utils/toast";

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

interface Activity {
  id: number;
  title: string;
  description: string;
  type: ActivityType;
  location: string;
  startTime: string;
  endTime: string;
  status: ActivityStatus;
}

const { info, error } = useToast();
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
      `/api/activities?${query.toString()}`,
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

// TODO: 活动详情弹窗
const showDetail = (item: Activity) => {
  info(item.title, item.description || "暂无活动详情");
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

const canSignup = (status: ActivityStatus) =>
  status === "RECRUITING" || status === "CONFIRMED";

const canCancel = (_status: ActivityStatus) => false;

const formatDateTime = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const yyyy = date.getFullYear();
  const mm = `${date.getMonth() + 1}`.padStart(2, "0");
  const dd = `${date.getDate()}`.padStart(2, "0");
  const hh = `${date.getHours()}`.padStart(2, "0");
  const min = `${date.getMinutes()}`.padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
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
            {{ formatDateTime(item.startTime) }} 至
            {{ formatDateTime(item.endTime) }} ｜ {{ item.location }} ｜
            {{ statusLabel(item.status) }}
          </p>
        </div>

        <div class="row-actions">
          <button type="button" @click="showDetail(item)">详细信息</button>
          <button
            type="button"
            class="primary"
            :disabled="!canSignup(item.status)"
          >
            报名
          </button>
          <button
            type="button"
            class="danger"
            :disabled="!canCancel(item.status)"
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
  </div>
</template>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
}

.row-actions .primary {
  background: #2563eb;
  border: none;
  color: white;
}

.row-actions .danger {
  background: #ef4444;
  border: none;
  color: white;
}

.row-actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.empty {
  text-align: center;
  color: #6b7280;
  padding: 18px 0;
}
</style>
