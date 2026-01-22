<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>数据看板</h2>
      <p>展示核心统计指标与图表占位。</p>
    </header>
    <section class="kpi-grid">
      <div v-for="kpi in kpis" :key="kpi.label" class="kpi-card">
        <p>{{ kpi.label }}</p>
        <h3>{{ kpi.value }}</h3>
      </div>
    </section>
    <section class="chart-grid">
      <div class="chart-card">
        <h3>服务时长趋势</h3>
        <div class="chart-placeholder">折线图占位</div>
      </div>
      <div class="chart-card">
        <h3>活动类型分布</h3>
        <div class="chart-placeholder">饼图占位</div>
      </div>
      <div class="chart-card">
        <h3>积分发放与使用</h3>
        <div class="chart-placeholder">柱状图占位</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const kpis = [
  { label: "累计服务时长", value: "12,580 小时" },
  { label: "累计活动场次", value: "320 场" },
  { label: "活跃志愿者", value: "1,250 人" },
  { label: "积分发放", value: "98,000 分" },
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: grid;
  gap: 12px;
}

.chart-placeholder {
  height: 180px;
  background: #f1f5f9;
  border: 1px dashed #cbd5f5;
  border-radius: 8px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
