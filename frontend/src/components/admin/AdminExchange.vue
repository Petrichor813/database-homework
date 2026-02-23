<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>兑换管理</h2>
      <p>处理兑换订单与库存管理（占位）。</p>
    </header>
    <section class="table-card">
      <div class="table-header">
        <span>商品</span>
        <span>数量</span>
        <span>积分</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div v-for="order in orders" :key="order.id" class="table-row">
        <span>{{ order.itemName }}</span>
        <span>{{ order.number }}</span>
        <span>{{ order.totalPoints }}</span>
        <span>{{ getStatusText(order.status) }}</span>
        <div class="actions">
          <button type="button" @click="openDetailDialog(order)">查看详情</button>
        </div>
      </div>
    </section>
    
    <!-- 兑换订单详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>兑换订单详情</h3>
        <p><strong>商品名称：</strong>{{ curOrder?.itemName }}</p>
        <p><strong>商品类别：</strong>{{ getCategoryText(curOrder?.itemCategory || '') }}</p>
        <p><strong>兑换数量：</strong>{{ curOrder?.number }}</p>
        <p><strong>消耗积分：</strong>{{ curOrder?.totalPoints }}</p>
        <p><strong>订单状态：</strong>{{ getStatusText(curOrder?.status || '') }}</p>
        <p><strong>下单时间：</strong>{{ formatDateTime(curOrder?.orderTime || '') }}</p>
        <p><strong>处理时间：</strong>{{ formatDateTime(curOrder?.processTime || '') }}</p>
        <p><strong>兑换志愿者：</strong>{{ curOrder?.volunteerName }}</p>
        <p><strong>收货信息：</strong>{{ curOrder?.recvInfo || "无" }}</p>
        <p><strong>备注：</strong>{{ curOrder?.note || "无" }}</p>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeDetailDialog">
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

interface ExchangeOrder {
  id: number;
  itemName: string;
  itemCategory: "DAILY_NECESSITIES" | "STATIONERY" | "BOOKS" | "CERTIFICATES" | "OTHER";
  number: number;
  totalPoints: number;
  status: "REVIEWING" | "PROCESSING" | "COMPLETED" | "CANCELLED" | "REJECTED";
  orderTime: string;
  processTime: string;
  volunteerName: string;
  volunteerId: number;
  recvInfo: string;
  note: string;
}

const orders = ref<ExchangeOrder[]>([
  { 
    id: 1,
    itemName: "环保礼包", 
    itemCategory: "DAILY_NECESSITIES",
    number: 1, 
    totalPoints: 120, 
    status: "REVIEWING",
    orderTime: "2025-04-12T09:30:00",
    processTime: "",
    volunteerName: "张三",
    volunteerId: 1001,
    recvInfo: "张三，13800138000，北京市朝阳区某某街道",
    note: "希望尽快发货"
  },
  { 
    id: 2,
    itemName: "志愿徽章", 
    itemCategory: "CERTIFICATES",
    number: 2, 
    totalPoints: 160, 
    status: "COMPLETED",
    orderTime: "2025-04-10T14:15:00",
    processTime: "2025-04-11T10:30:00",
    volunteerName: "李同学",
    volunteerId: 1002,
    recvInfo: "李同学，13900139000，上海市浦东新区某某路",
    note: ""
  },
]);

// 兑换订单详情弹窗
const showDetailDialog = ref(false);
const curOrder = ref<ExchangeOrder | null>(null);

// 打开详情弹窗
const openDetailDialog = (order: ExchangeOrder) => {
  curOrder.value = order;
  showDetailDialog.value = true;
};

// 关闭详情弹窗
const closeDetailDialog = () => {
  curOrder.value = null;
  showDetailDialog.value = false;
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

// 获取商品类别文本
const getCategoryText = (category: string) => {
  switch (category) {
    case "DAILY_NECESSITIES":
      return "日用品";
    case "STATIONERY":
      return "文具";
    case "BOOKS":
      return "书籍";
    case "CERTIFICATES":
      return "证书徽章";
    case "OTHER":
      return "其他";
    default:
      return category;
  }
};

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return "未处理";
  
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};
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

.table-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-header,
.table-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
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

.actions {
  display: flex;
  gap: 8px;
}

.actions button {
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
}

/* 弹窗样式 */
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