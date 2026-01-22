<template>
  <div class="page-shell">
    <header class="page-header">
      <div>
        <h2>兑换商城</h2>
        <p>用积分兑换公益礼包与周边（占位）。</p>
      </div>
      <router-link to="/exchange-records" class="record-entry">兑换记录</router-link>
    </header>
    <section class="product-grid">
      <article v-for="item in items" :key="item.name" class="product-card">
        <div class="image-placeholder">商品图片占位</div>
        <div class="product-body">
          <h3>{{ item.name }}</h3>
          <p class="points">{{ item.points }} 积分</p>
          <p class="desc">{{ item.desc }}</p>
          <button>兑换</button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
const items = [
  { name: '环保礼包', points: 120, desc: '可循环水杯 + 帆布袋' },
  { name: '志愿徽章', points: 80, desc: '纪念徽章套装' },
  { name: '社区福利券', points: 200, desc: '指定商户优惠券' }
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.record-entry {
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 8px 14px;
  text-decoration: none;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.image-placeholder {
  height: 140px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-body {
  padding: 16px;
  display: grid;
  gap: 8px;
}

.points {
  color: #2563eb;
  font-weight: 600;
}

.desc {
  color: #4b5563;
  font-size: 14px;
}

button {
  border: none;
  background: #2563eb;
  color: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}
</style>
