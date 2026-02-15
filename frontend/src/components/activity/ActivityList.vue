<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>活动列表</h2>
      <p>浏览社区志愿活动并进行报名（前端框架占位）。</p>
    </header>
    <section class="filter-bar">
      <input type="text" placeholder="搜索活动名称" />
      <select>
        <option>活动类型</option>
        <option>社区治理</option>
        <option>关爱服务</option>
        <option>安全宣传</option>
      </select>
      <select>
        <option>活动状态</option>
        <option>报名中</option>
        <option>进行中</option>
        <option>已结束</option>
      </select>
      <button>查询</button>
    </section>
    <section class="card-grid">
      <article v-for="item in activities" :key="item.title" class="card">
        <div class="image-placeholder">活动封面占位</div>
        <div class="card-body">
          <span class="tag">{{ item.type }}</span>
          <h3>{{ item.title }}</h3>
          <p class="meta">{{ item.time }} · {{ item.location }}</p>
          <p class="desc">{{ item.desc }}</p>
          <button class="primary">报名</button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
const activities = [
  {
    title: "社区环境清洁行动",
    type: "社区治理",
    time: "本周六 08:30",
    location: "朝阳社区",
    desc: "一起清理公共区域，提升社区环境品质。",
  },
  {
    title: "银龄陪伴计划",
    type: "关爱服务",
    time: "本周日 14:00",
    location: "幸福敬老院",
    desc: "陪伴老人聊天、读书与文娱互动。",
  },
  {
    title: "防诈宣传志愿行动",
    type: "安全宣传",
    time: "下周一 19:00",
    location: "社区广场",
    desc: "协助派发宣传资料与现场讲解。",
  },
];
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header h2 {
  margin-bottom: 6px;
}

.page-header p {
  color: #6b7280;
}

.filter-bar {
  display: grid;
  grid-template-columns: 1.5fr repeat(2, 1fr) auto;
  gap: 12px;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.05);
}

.filter-bar input,
.filter-bar select {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.filter-bar button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.image-placeholder {
  height: 140px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.card-body {
  padding: 16px;
  display: grid;
  gap: 8px;
}

.tag {
  font-size: 12px;
  background: #eff6ff;
  color: #2563eb;
  padding: 4px 8px;
  border-radius: 999px;
  width: fit-content;
}

.meta {
  font-size: 13px;
  color: #6b7280;
}

.desc {
  color: #4b5563;
  font-size: 14px;
}

.primary {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}
</style>
