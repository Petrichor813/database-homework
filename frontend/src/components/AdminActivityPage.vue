<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>活动管理</h2>
      <p>发布、编辑、上下架活动（占位）。</p>
    </header>
    <section class="toolbar">
      <button class="primary">新建活动</button>
      <button>批量导入</button>
    </section>
    <section class="table-card">
      <div class="table-header">
        <span>活动名称</span>
        <span>时间</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div v-for="item in activities" :key="item.title" class="table-row">
        <span>{{ item.title }}</span>
        <span>{{ item.time }}</span>
        <span>{{ item.status }}</span>
        <div class="actions">
          <button>编辑</button>
          <button>查看报名</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
const activities = [
  { title: '社区环境清洁行动', time: '2025-04-12', status: '报名中' },
  { title: '银龄陪伴计划', time: '2025-04-20', status: '进行中' }
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

.toolbar {
  display: flex;
  gap: 12px;
}

.toolbar button {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  background: #fff;
  cursor: pointer;
}

.toolbar .primary {
  background: #2563eb;
  color: #fff;
  border: none;
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
  grid-template-columns: 2fr 1fr 1fr 1fr;
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
  background: #fff;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
}
</style>
