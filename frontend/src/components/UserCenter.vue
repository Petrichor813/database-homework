<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>个人中心</h2>
      <p>汇总志愿者资料、积分与兑换信息。</p>
    </header>

    <section class="profile-grid">
      <div class="profile-card">
        <div class="avatar-placeholder">头像占位</div>
        <div class="info">
          <p class="name">张三</p>
          <p class="role">志愿者 · 待审核</p>
          <button class="ghost-btn">编辑资料</button>
        </div>
      </div>
      <div class="profile-card">
        <h3>基础信息</h3>
        <ul>
          <li>手机号：138****8888</li>
          <li>服务时长：120 小时</li>
          <li>积分余额：260</li>
        </ul>
      </div>
      <div class="profile-card">
        <h3>认证状态</h3>
        <div class="status-block">等待管理员审核</div>
        <button class="ghost-btn">提交补充材料</button>
      </div>
    </section>

    <section class="summary">
      <div class="summary-card">
        <p>当前积分</p>
        <h3>260</h3>
      </div>
      <div class="summary-card">
        <p>本月新增</p>
        <h3>+80</h3>
      </div>
      <div class="summary-card">
        <p>兑换记录</p>
        <router-link to="/exchange-records" class="primary-link">
          查看兑换记录
        </router-link>
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

<script setup>
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

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-placeholder {
  height: 120px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name {
  font-weight: 600;
}

.role {
  color: #6b7280;
  font-size: 14px;
}

.status-block {
  background: #fef3c7;
  color: #92400e;
  padding: 8px 12px;
  border-radius: 8px;
  width: fit-content;
}

.ghost-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
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
  display: grid;
  gap: 10px;
}

.primary-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  text-decoration: none;
  font-size: 14px;
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