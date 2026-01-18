<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>数据导入</h2>
      <p>支持 CSV / Excel 批量导入（占位）。</p>
    </header>
    <section class="upload-card">
      <div class="upload-placeholder">
        <p>拖拽文件到此处或点击上传</p>
        <button>选择文件</button>
      </div>
      <div class="upload-notes">
        <h3>导入提示</h3>
        <ul>
          <li>请使用平台模板文件</li>
          <li>导入后将生成活动/志愿者数据</li>
          <li>支持 CSV/Excel 格式</li>
        </ul>
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

.upload-card {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 1fr;
  gap: 16px;
}

.upload-placeholder {
  background: #fff;
  border-radius: 12px;
  border: 2px dashed #cbd5f5;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

.upload-placeholder button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.upload-notes {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
}

.upload-notes ul {
  padding-left: 18px;
  color: #6b7280;
}
</style>
