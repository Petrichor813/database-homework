<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getJson } from "../../utils/api";
import type { PageResponse } from "../../utils/page";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";

const { error } = useToast();
const {
  pageObject,
  updatePageState,
  pageRanges,
  goToPage,
  prevPage,
  nextPage,
} = usePagination();

interface SignupRecord {
  id: number | string;
  activityTitle: string;
  activityStartTime: string;
  activityEndTime: string;
  volunteerStartTime: string;
  volunteerEndTime: string;
  status: string;
  signupTime: string;
}

const records = ref<SignupRecord[]>([]);
const loading = ref(false);

const getStatusClass = (status: string) => {
  switch (status) {
    case "REVIEWING":
      return "reviewing";
    case "CONFIRMED":
    case "PARTICIPATED":
      return "active";
    case "CANCELLED":
    case "REJECTED":
    case "UNARRIVED":
      return "done";
    default:
      return "";
  }
};

const getStatusText = (status: string) => {
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
      return status;
  }
};

const fetchSignupRecords = async (page: number = pageObject.value.curPage) => {
  const volunteerId = localStorage.getItem("volunteerId");
  if (!volunteerId) {
    error("用户未登录或不是志愿者");
    return;
  }

  try {
    loading.value = true;

    const data = await getJson<PageResponse<SignupRecord>>(
      `/api/volunteers/${volunteerId}/signup-records?page=${page}&size=${pageObject.value.pageSize}`,
    );

    records.value = data.content;
    updatePageState({
      curPage: data.curPage,
      pageSize: data.pageSize,
      totalElements: data.totalElements,
      totalPages: data.totalPages,
    });
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取报名记录失败";
    error("获取报名记录失败", msg);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchSignupRecords();
});
</script>

<template>
  <div class="signup-record">
    <header class="page-header">
      <h2>报名记录</h2>
      <p>查看已报名/进行中/已完成的活动记录。</p>
    </header>
    <section class="table-area">
      <table>
        <thead>
          <tr>
            <th>活动名称</th>
            <th>实际参与时间</th>
            <th>实际结束时间</th>
            <th>报名时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="loading">加载中...</td>
          </tr>
          <tr v-else-if="records.length === 0">
            <td colspan="6" class="no-data">暂无报名记录</td>
          </tr>
          <tr v-else v-for="record in records" :key="record.id">
            <td>{{ record.activityTitle }}</td>
            <td>{{ record.volunteerStartTime }}</td>
            <td>{{ record.volunteerEndTime }}</td>
            <td>{{ record.signupTime }}</td>
            <td>
              <span class="status" :class="getStatusClass(record.status)">{{
                getStatusText(record.status)
              }}</span>
            </td>
            <td>
              <!-- TODO: 需要增加查看详情功能和取消报名功能 -->
              <button>查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <Pagination
      :page-object="pageObject"
      :items="records"
      :loading="loading"
      :page-ranges="pageRanges"
      :go-to-page="(page: number) => goToPage(page, fetchSignupRecords)"
      :prev-page="() => prevPage(fetchSignupRecords)"
      :next-page="() => nextPage(fetchSignupRecords)"
    />
  </div>
</template>

<style lang="css" scoped>
.signup-record {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.table-area {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

.loading,
.no-data {
  text-align: center;
  padding: 20px;
  color: #6b7280;
}

th {
  background: #f8fafc;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  text-align: left;
  padding: 14px 16px;
}

td {
  padding: 14px 16px;
  border-top: 1px solid #e5e7eb;
}

tr:nth-child(even) {
  background-color: #f9fafb;
}

tr:hover {
  background-color: #f3f4f6;
}

.status {
  display: inline-block;
  width: fit-content;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 999px;
}

.status.reviewing {
  background: #fef3c7;
  color: #92400e;
}

.status.active {
  background: #dbeafe;
  color: #1d4ed8;
}

.status.done {
  background: #dcfce7;
  color: #166534;
}

td button {
  background: white;
  cursor: pointer;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 12px;
}

td button:hover {
  background: #f8fafc;
}
</style>