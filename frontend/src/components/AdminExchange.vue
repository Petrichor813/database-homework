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
      </div>
      <div v-for="order in orders" :key="order.item" class="table-row">
        <span>{{ order.item }}</span>
        <span>{{ order.count }}</span>
        <span>{{ order.points }}</span>
        <span>{{ order.status }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const orders = [
  { item: '环保礼包', count: 1, points: '120', status: '待处理' },
  { item: '志愿徽章', count: 2, points: '160', status: '已完成' }
]
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
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-header,
.table-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
</style>
