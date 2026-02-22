<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>积分管理</h2>
      <p>审核积分发放与调整记录（占位）。</p>
    </header>
    <section class="toolbar">
      <button class="primary">新增积分调整</button>
      <button>导出记录</button>
    </section>
    <section class="table-card">
      <div class="table-header">
        <span>志愿者</span>
        <span>变动</span>
        <span>原因</span>
        <span>时间</span>
      </div>
      <div v-for="record in records" :key="record.name" class="table-row">
        <span>{{ record.name }}</span>
        <span>{{ record.change }}</span>
        <span>{{ record.reason }}</span>
        <span>{{ record.time }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const records = [
  { name: "张三", change: "+40", reason: "活动结算", time: "2025-04-10" },
  { name: "李同学", change: "-20", reason: "兑换扣减", time: "2025-04-08" },
];
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

.toolbar {
  display: flex;
  gap: 12px;
}

.toolbar button {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  background: white;
  cursor: pointer;
}

.toolbar .primary {
  background: #2563eb;
  color: white;
  border: none;
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
