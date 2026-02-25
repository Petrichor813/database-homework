<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getJson, postJson } from "../../utils/api";
import type { PageResponse } from "../../utils/page";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";

const { error, success } = useToast();
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
  activityId: number; // 添加活动ID，用于取消报名
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

// 报名记录详情弹窗
const showDetailDialog = ref(false);
const curRecord = ref<SignupRecord | null>(null);

// 打开详情弹窗
const openDetailDialog = (record: SignupRecord) => {
  curRecord.value = record;
  showDetailDialog.value = true;
};

// 关闭详情弹窗
const closeDetailDialog = () => {
  curRecord.value = null;
  showDetailDialog.value = false;
};

// 判断是否可以取消报名
const canCancelSignup = (record: SignupRecord) => {
  // 只有待审核或已确认状态才能取消报名
  return record.status === "REVIEWING" || record.status === "CONFIRMED";
};

// 取消报名
const handleCancelSignup = async (record: SignupRecord) => {
  try {
    const response = await postJson<{ id: number; message: string }>(
      "/api/activity/cancel-signup",
      {
        activityId: record.activityId,
      },
    );

    // 更新报名记录状态
    record.status = "CANCELLED";

    success("取消报名成功", response.message);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "取消报名失败";
    error("取消报名失败", msg);
  }
};

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
  const userStr = localStorage.getItem("user");
  if (!userStr) {
    error("用户未登录");
    return;
  }

  let user: { volunteerId?: number | string };
  try {
    user = JSON.parse(userStr);
  } catch (err) {
    error("用户信息解析失败");
    return;
  }

  if (!user.volunteerId) {
    error("您还未申请成为志愿者");
    return;
  }

  try {
    loading.value = true;

    const data = await getJson<PageResponse<SignupRecord>>(
      `/api/volunteer/${user.volunteerId}/signup-records?page=${page}&size=${pageObject.value.pageSize}`,
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
              <button
                type="button"
                class="info-button"
                @click="openDetailDialog(record)"
              >
                查看详情
              </button>
              <button
                v-if="canCancelSignup(record)"
                type="button"
                class="cancel-button"
                @click="handleCancelSignup(record)"
              >
                取消报名
              </button>
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

    <!-- 报名记录详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>报名详情</h3>
        <p><strong>活动名称：</strong>{{ curRecord?.activityTitle }}</p>
        <p><strong>活动开始时间：</strong>{{ curRecord?.activityStartTime }}</p>
        <p><strong>活动结束时间：</strong>{{ curRecord?.activityEndTime }}</p>
        <p>
          <strong>实际参与时间：</strong>{{ curRecord?.volunteerStartTime }}
        </p>
        <p><strong>实际结束时间：</strong>{{ curRecord?.volunteerEndTime }}</p>
        <p><strong>报名时间：</strong>{{ curRecord?.signupTime }}</p>
        <p>
          <strong>状态：</strong>
          <span class="status" :class="getStatusClass(curRecord?.status || '')">
            {{ getStatusText(curRecord?.status || "") }}
          </span>
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

/* 按钮样式 */
.info-button,
.cancel-button {
  cursor: pointer;
  border-radius: 8px;
  padding: 6px 12px;
  transition: all 0.2s ease;
}

.info-button {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  margin-right: 8px;
}

.info-button:hover {
  background: #f8fafc;
  border-color: #d1d5db;
}

.cancel-button {
  background: #ef4444;
  color: white;
  border: none;
  margin-right: 0;
}

.cancel-button:hover {
  background: #dc2626;
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
  transition: all 0.2s ease;
}

.dialog-actions button:hover {
  cursor: pointer;
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
