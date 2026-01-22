<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-content">
        <p class="hero-tag">社区志愿服务平台</p>
        <h2>让公益更高效，让志愿更有温度</h2>
        <p class="hero-desc">
          统一入口、活动管理、积分激励与数据看板，帮助社区更好组织志愿服务。
        </p>
        <div class="hero-actions">
          <button type="button" class="attend-btn" @click="handleJoin">
            立即参与
          </button>
          <button type="button" class="browse-btn" @click="handleBrowse">
            查看活动
          </button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="hero-card">
          <h3>平台能力概览</h3>
          <ul>
            <li>活动发布 / 报名 / 审核</li>
            <li>志愿时长与积分结算</li>
            <li>积分兑换与公益激励</li>
            <li>数据导入、看板分析与导出</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="stats">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <p class="stat-value">{{ stat.value }}</p>
        <p class="stat-label">{{ stat.label }}</p>
      </div>
    </section>

    <section class="section">
      <div class="section-header">
        <h3>热门活动（示例）</h3>
        <router-link to="/login" class="text-link">查看全部</router-link>
      </div>
      <div class="activity-grid">
        <article
          v-for="activity in activities"
          :key="activity.title"
          class="activity-card"
        >
          <span class="activity-tag">{{ activity.type }}</span>
          <h4>{{ activity.title }}</h4>
          <p class="activity-meta">
            {{ activity.time }} · {{ activity.location }}
          </p>
          <p class="activity-desc">{{ activity.desc }}</p>
          <button class="browse-btn">报名参与</button>
        </article>
      </div>
    </section>

    <section class="section">
      <div class="section-header">
        <h3>数据看板预览</h3>
        <span class="text-muted">支持导出 PNG / CSV</span>
      </div>
      <div class="dashboard">
        <div class="dashboard-card">
          <h4>服务时长趋势</h4>
          <div class="chart-placeholder">折线图区域</div>
        </div>
        <div class="dashboard-card">
          <h4>活动类型分布</h4>
          <div class="chart-placeholder">饼图区域</div>
        </div>
        <div class="dashboard-card">
          <h4>积分发放与使用</h4>
          <div class="chart-placeholder">柱状图区域</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

import { useToast } from "../utils/toast";

interface StoredUser {
  role?: string | null;
  volunteerStatus?: string | null;
}

const router = useRouter();
const { info } = useToast();

const getUserStatus = () => {
  const storedUser = localStorage.getItem("user");

  if (!storedUser) {
    return { isLoggedIn: false, role: null, volunteerStatus: null };
  }

  try {
    const parsedUser = JSON.parse(storedUser) as StoredUser | null;

    return {
      isLoggedIn: true,
      role: parsedUser?.role ?? null,
      volunteerStatus: parsedUser?.volunteerStatus ?? null,
    };
  } catch {
    return { isLoggedIn: false, role: null, volunteerStatus: null };
  }
};

const handleJoin = () => {
  const { isLoggedIn, role, volunteerStatus } = getUserStatus();

  if (!isLoggedIn) {
    router.push("/login");
    return;
  }

  if (role === "ADMIN" || volunteerStatus === "CERTIFIED") {
    router.push("/activities");
    return;
  }

  info("您尚未认证为志愿者，完成认证后即可报名参与活动。");
};

const handleBrowse = () => {
  const { isLoggedIn, role, volunteerStatus } = getUserStatus();

  if (!isLoggedIn) {
    router.push("/login");
    return;
  }

  if (role === "ADMIN" || volunteerStatus === "CERTIFIED") {
    router.push("/activities");
    return;
  }

  router.push("/activities");
};

const stats = [
  { label: "累计志愿时长", value: "12,580 小时" },
  { label: "服务居民人次", value: "8,960 人次" },
  { label: "累计活动场次", value: "320 场" },
  { label: "在线志愿者", value: "1,250 名" },
];

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
.home-page {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  padding: 30px;
  background: linear-gradient(135deg, #e9f2ff 0%, #f8fbff 100%);
  border-radius: 16px;
  border: 1px solid #e0e7ff;
}

.hero-tag {
  font-size: 14px;
  color: #1d4ed8;
  font-weight: 600;
  margin-bottom: 10px;
}

.hero h2 {
  font-size: 32px;
  color: #1f2937;
  margin-bottom: 12px;
}

.hero-desc {
  color: #4b5563;
  line-height: 1.7;
  margin-bottom: 20px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.attend-btn {
  background: #2563eb;
  color: #fff;
  padding: 10px 18px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
}

.browse-btn {
  background: #fff;
  color: #2563eb;
  padding: 10px 18px;
  border-radius: 8px;
  border: 1px solid #cbd5f5;
  cursor: pointer;
}

.hero-panel {
  display: flex;
  align-items: center;
}

.hero-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(30, 64, 175, 0.08);
}

.hero-card h3 {
  margin-bottom: 12px;
  color: #1e3a8a;
}

.hero-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
  color: #374151;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  padding: 18px;
  border-radius: 12px;
  border: 1px solid #eef2f7;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  color: #6b7280;
  margin-top: 6px;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.section-header h3 {
  margin: 0;
  color: #1f2937;
}

.text-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 14px;
}

.text-muted {
  color: #9ca3af;
  font-size: 13px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.activity-card {
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-tag {
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 999px;
  width: fit-content;
}

.activity-meta {
  font-size: 13px;
  color: #6b7280;
}

.activity-desc {
  color: #4b5563;
  font-size: 14px;
  flex: 1;
}

.dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.dashboard-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.chart-placeholder {
  background: #f8fafc;
  border: 1px dashed #cbd5f5;
  border-radius: 8px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  margin-top: 12px;
}
</style>
