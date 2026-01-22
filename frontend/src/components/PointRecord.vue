<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>积分记录</h2>
      <p>查看积分获取与消耗记录（占位）。</p>
    </header>
    <section class="summary">
      <div class="summary-card">
        <p>当前积分</p>
        <h3>260</h3>
      </div>
      <div class="summary-card">
        <p>本月新增</p>
        <h3>+80</h3>
      </div>
    </section>
    <section class="table-card">
      <div class="table-header">
        <span>时间</span>
        <span>类型</span>
        <span>变动</span>
        <span>备注</span>
      </div>
      <div v-for="record in records" :key="record.time" class="table-row">
        <span>{{ record.time }}</span>
        <span>{{ record.type }}</span>
        <span :class="record.amount.startsWith('+') ? 'positive' : 'negative'">
          {{ record.amount }}
        </span>
        <span>{{ record.note }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const records = [
  { time: '2025-04-08', type: '活动结算', amount: '+40', note: '社区清洁行动' },
  { time: '2025-04-02', type: '积分兑换', amount: '-60', note: '兑换环保礼包' },
  { time: '2025-03-28', type: '活动结算', amount: '+30', note: '防诈宣传' }
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

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
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

.positive {
  color: #16a34a;
  font-weight: 600;
}

.negative {
  color: #dc2626;
  font-weight: 600;
}
</style>
