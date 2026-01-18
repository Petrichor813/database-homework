<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>报名记录</h2>
      <p>查看已报名/进行中/已完成的活动记录。</p>
    </header>
    <section class="table-card">
      <div class="table-header">
        <span>活动名称</span>
        <span>时间</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div v-for="record in records" :key="record.title" class="table-row">
        <span>{{ record.title }}</span>
        <span>{{ record.time }}</span>
        <span class="status" :class="record.statusClass">{{ record.status }}</span>
        <button>查看详情</button>
      </div>
    </section>
  </div>
</template>

<script setup>
const records = [
  { title: '社区环境清洁行动', time: '2025-04-12', status: '已报名', statusClass: 'pending' },
  { title: '银龄陪伴计划', time: '2025-04-20', status: '进行中', statusClass: 'active' },
  { title: '防诈宣传志愿行动', time: '2025-03-18', status: '已完成', statusClass: 'done' }
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
  grid-template-columns: 2fr 1fr 1fr auto;
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

.status {
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 999px;
  width: fit-content;
}

.status.pending {
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

.table-row button {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
}
</style>
