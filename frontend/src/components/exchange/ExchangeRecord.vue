<script setup lang="ts">
import { onMounted, ref } from "vue";
import { deleteJson, getJson } from "../../utils/api";
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

interface ExchangeRecord {
  id: number;
  productName: string;
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
const cancelLoading = ref(false);

const showDetailDialog = ref(false);
const curRecord = ref<ExchangeRecord | null>(null);

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

const openDetailDialog = (record: ExchangeRecord) => {
  curRecord.value = record;
  showDetailDialog.value = true;
};

const closeDetailDialog = () => {
  curRecord.value = null;
  showDetailDialog.value = false;
};

const fetchExchangeRecords = async (
  page: number = pageObject.value.curPage
) => {
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

    const data = await getJson<PageResponse<ExchangeRecord>>(
      `/api/volunteer/${user.volunteerId}/exchange-records?page=${page}&size=${pageObject.value.pageSize}`
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

const canCancel = (status: string) => {
  return status === "REVIEWING";
};

const cancelExchange = async (record: ExchangeRecord) => {
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
    cancelLoading.value = true;

    await deleteJson(
      `/api/volunteer/${user.volunteerId}/exchange-records/${record.id}`
    );

    success("取消兑换成功");
    // 重新获取兑换记录
    fetchExchangeRecords();
  } catch (err) {
    const msg = err instanceof Error ? err.message : "取消兑换失败";
    error("取消兑换失败", msg);
  } finally {
    cancelLoading.value = false;
  }
};

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
            <td>{{ record.productName }}</td>
            <td>{{ record.number }}</td>
            <td>{{ record.orderTime }}</td>
            <td>
              <span class="status" :class="getStatusClass(record.status)">{{
                getStatusText(record.status)
              }}</span>
            </td>
            <td>{{ displayProcessTime(record.processTime) }}</td>
            <td>
              <button type="button" @click="openDetailDialog(record)">
                查看详情
              </button>
              <button
                type="button"
                class="cancel-button"
                @click="cancelExchange(record)"
                :disabled="cancelLoading"
                v-if="canCancel(record.status)"
              >
                取消兑换
              </button>
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

    <!-- 兑换记录详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>兑换详情</h3>
        <p><strong>商品名称：</strong>{{ curRecord?.productName }}</p>
        <p><strong>兑换数量：</strong>{{ curRecord?.number }}</p>
        <p><strong>消耗积分：</strong>{{ curRecord?.totalPoints }}</p>
        <p><strong>下单时间：</strong>{{ curRecord?.orderTime }}</p>
        <p>
          <strong>处理时间：</strong>
          {{ displayProcessTime(curRecord?.processTime || "") }}
        </p>
        <p>
          <strong>订单状态：</strong>
          <span class="status" :class="getStatusClass(curRecord?.status || '')">
            {{ getStatusText(curRecord?.status || "") }}
          </span>
        </p>
        <p><strong>收货信息：</strong>{{ curRecord?.recvInfo || "无" }}</p>
        <p><strong>备注信息：</strong>{{ curRecord?.note || "无" }}</p>
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
  transition: all 0.2s ease;
}

td button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.cancel-button {
  background: #dc2626;
  cursor: pointer;
  color: white;
  font-weight: 600;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  margin-left: 8px;
}

.cancel-button:hover {
  background: red;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
}

.cancel-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading,
.no-data {
  text-align: center;
  padding: 20px;
  color: #6b7280;
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
