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

interface ExchangeRecord {
  id: number;
  itemName: string;
  number: number;
  totalPoints: number;
  status: string;
  orderTime: string;
  processTime: string;
  note: string;
  recvInfo: string;
}

const records = ref<ExchangeRecord[]>([]);
const loading = ref(false);

// 获取兑换记录
const fetchExchangeRecords = async (page: number = pageObject.value.curPage) => {
  const volunteerId = localStorage.getItem("volunteerId");
  if (!volunteerId) {
    error("用户未登录或不是志愿者");
    return;
  }

  try {
    loading.value = true;

    const data = await getJson<PageResponse<ExchangeRecord>>(
      `/api/volunteers/${volunteerId}/exchange-records?page=${page}&size=${pageObject.value.pageSize}`,
    );

    records.value = data.content;
    updatePageState({
      curPage: data.curPage,
      pageSize: data.pageSize,
      totalElements: data.totalElements,
      totalPages: data.totalPages,
    });
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取兑换记录失败";
    error("获取兑换记录失败", msg);
  } finally {
    loading.value = false;
  }
};

// 获取状态对应的CSS类
const getStatusClass = (status: string) => {
  switch (status) {
    case "REVIEWING":
      return "reviewing";
    case "PROCESSING":
      return "processing";
    case "COMPLETED":
      return "done";
    case "CANCELLED":
    case "REJECTED":
      return "done";
    default:
      return "";
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case "REVIEWING":
      return "待处理";
    case "PROCESSING":
      return "处理中";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    case "REJECTED":
      return "已拒绝";
    default:
      return status;
  }
};

// 显示处理时间
const displayProcessTime = (time: string | null) => {
  return time || "未处理";
};

onMounted(() => {
  fetchExchangeRecords();
});
</script>

<template>
  <div class="exchange-record">
    <header class="page-header">
      <h2>兑换记录</h2>
      <p>查看最近兑换的商品与状态。</p>
    </header>
    
    <section class="table-card">
      <table>
        <thead>
          <tr>
            <th>商品名称</th>
            <th>兑换数量</th>
            <th>下单时间</th>
            <th>状态</th>
            <th>处理时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="loading">加载中...</td>
          </tr>
          <tr v-else-if="records.length === 0">
            <td colspan="6" class="no-data">暂无兑换记录</td>
          </tr>
          <tr v-else v-for="record in records" :key="record.id">
            <td>{{ record.itemName }}</td>
            <td>{{ record.number }}</td>
            <td>{{ record.orderTime }}</td>
            <td>
              <span class="status" :class="getStatusClass(record.status)">{{
                getStatusText(record.status)
              }}</span>
            </td>
            <td>{{ displayProcessTime(record.processTime) }}</td>
            <td>
              <!-- TODO: 需要增加查看详情功能和撤销功能 -->
              <button>查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
    
    <!-- 分页组件 -->
    <Pagination
      :page-object="pageObject"
      :items="records"
      :loading="loading"
      :page-ranges="pageRanges"
      :go-to-page="(page: number) => goToPage(page, fetchExchangeRecords)"
      :prev-page="() => prevPage(fetchExchangeRecords)"
      :next-page="() => nextPage(fetchExchangeRecords)"
    />
  </div>
</template>

<style lang="css" scoped>
.exchange-record {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.table-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
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

.status.processing {
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

.loading,
.no-data {
  text-align: center;
  padding: 20px;
  color: #6b7280;
}
</style>