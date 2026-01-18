<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>志愿者审核</h2>
      <p>审核志愿者注册申请（占位）。</p>
    </header>
    <section class="table-card">
      <div class="table-header">
        <span>姓名</span>
        <span>手机号</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div v-for="item in volunteers" :key="item.name" class="table-row">
        <span>{{ item.name }}</span>
        <span>{{ item.phone }}</span>
        <span>{{ item.status }}</span>
        <div class="actions">
          <button class="approve">通过</button>
          <button class="reject">拒绝</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
const volunteers = [
  { name: '李同学', phone: '138****1111', status: '待审核' },
  { name: '王老师', phone: '139****2222', status: '待审核' }
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
  grid-template-columns: 1fr 1fr 1fr 1fr;
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

.approve {
  background: #16a34a;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}

.reject {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
}
</style>
