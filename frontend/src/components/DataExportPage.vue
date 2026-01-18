<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>数据导出</h2>
      <p>导出 CSV / PNG 报表（占位）。</p>
    </header>
    <section class="export-grid">
      <div class="export-card">
        <h3>导出活动数据</h3>
        <p>导出活动列表、报名记录</p>
        <button>导出 CSV</button>
      </div>
      <div class="export-card">
        <h3>导出志愿者数据</h3>
        <p>导出志愿时长、积分统计</p>
        <button>导出 CSV</button>
      </div>
      <div class="export-card">
        <h3>导出看板图片</h3>
        <p>下载 PNG 报表</p>
        <button>导出 PNG</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.export-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.export-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: grid;
  gap: 8px;
}

.export-card button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}
</style>
