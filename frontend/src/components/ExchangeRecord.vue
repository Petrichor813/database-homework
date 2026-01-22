<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>兑换记录</h2>
      <p>查看最近兑换的商品与状态（占位）。</p>
    </header>

    <section class="record-list">
      <article v-for="record in records" :key="record.id" class="record-card">
        <div>
          <h3>{{ record.item }}</h3>
          <p class="meta">兑换时间：{{ record.time }}</p>
          <p class="meta">消耗积分：{{ record.points }}</p>
        </div>
        <span class="status" :class="record.statusClass">{{ record.status }}</span>
      </article>
    </section>
  </div>
</template>

<script setup>
const records = [
  {
    id: 1,
    item: '环保礼包',
    time: '2025-04-02 14:20',
    points: 120,
    status: '配送中',
    statusClass: 'processing'
  },
  {
    id: 2,
    item: '志愿徽章',
    time: '2025-03-19 09:10',
    points: 80,
    status: '已完成',
    statusClass: 'done'
  },
  {
    id: 3,
    item: '社区福利券',
    time: '2025-02-26 16:40',
    points: 200,
    status: '待发货',
    statusClass: 'pending'
  }
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

.record-list {
  display: grid;
  gap: 16px;
}

.record-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.meta {
  color: #6b7280;
  font-size: 14px;
}

.status {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status.processing {
  background: #dbeafe;
  color: #1d4ed8;
}

.status.done {
  background: #dcfce7;
  color: #166534;
}

.status.pending {
  background: #fef3c7;
  color: #92400e;
}
</style>