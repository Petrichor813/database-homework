<script setup lang="ts">
import { useRouter } from "vue-router";
import { useToast } from "../utils/toast";

interface User {
  role?: string | null;
  volunteerStatus?: string | null;
}

const router = useRouter();
const { info } = useToast();

const getUserStatus = () => {
  const localUser = localStorage.getItem("user");
  if (!localUser) {
    return {
      isLoggedIn: false,
      role: null,
      volunteerStatus: null,
    };
  }

  try {
    const parsedUser = JSON.parse(localUser) as User | null;

    return {
      isLoggedIn: true,
      role: parsedUser?.role ?? null,
      volunteerStatus: parsedUser?.volunteerStatus ?? null,
    };
  } catch (err) {
    return {
      isLoggedIn: false,
      role: null,
      volunteerStatus: null,
    };
  }
};

const handleJoin = () => {
  const { isLoggedIn, role, volunteerStatus } = getUserStatus();

  if (!isLoggedIn) {
    info("请先登录/注册");
    router.push("/login");
    return;
  }

  if (role === "ADMIN" || volunteerStatus === "CERTIFIED") {
    router.push("/activities");
    return;
  }

  info("您尚未认证为志愿者，完成认证后即可报名参与活动");
};

const mockStats = [
  { label: "累计志愿时长", value: "12,580 小时" },
  { label: "服务居民人次", value: "8,960 人次" },
  { label: "累计活动场次", value: "320 场" },
  { label: "在线志愿者", value: "1,250 名" },
];

const mockActivities = [
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

<template>
  <div class="home">
    <section class="header">
      <div class="header-content">
        <p class="header-tag">社区志愿服务平台</p>
        <h2>让公益更加高效，让志愿更有温度</h2>
        <p class="header-description">
          统一入口、活动管理、积分激励与数据看板，帮助社区更好组织志愿服务。
        </p>
        <div class="activity-actions">
          <button type="button" class="join-button" @click="handleJoin">
            立即参与
          </button>
          <button
            type="button"
            class="lookup-button"
            @click="() => router.push('/activities')"
          >
            查看活动
          </button>
        </div>
      </div>
      <div class="header-panel">
        <h3>平台能力概览</h3>
        <ul>
          <li>活动发布 / 报名 / 审核</li>
          <li>志愿时长与积分结算</li>
          <li>积分兑换与公益激励</li>
          <li>数据导入、看板分析与导出</li>
        </ul>
      </div>
    </section>

    <section class="stats">
      <div class="stat-content" v-for="data in mockStats" :key="data.label">
        <p class="stat-value">{{ data.value }}</p>
        <p class="stat-label">{{ data.label }}</p>
      </div>
    </section>

    <section class="activity">
      <div class="activity-header">
        <h3>热门活动</h3>
        <router-link to="/activities" class="activity-link">
          查看全部活动
        </router-link>
      </div>
      <div class="activity-grid">
        <article
          class="activity-content"
          v-for="activity in mockActivities"
          :key="activity.title"
        >
          <span class="activity-type">{{ activity.type }}</span>
          <h4>{{ activity.title }}</h4>
          <p class="activity-info">
            {{ activity.time }} · {{ activity.location }}
          </p>
          <p class="activity-description">{{ activity.desc }}</p>
          <button type="button" class="lookup-button">报名参与</button>
        </article>
      </div>
    </section>
  </div>
</template>

<style lang="css">
.home {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.header {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  padding: 30px;
  background: linear-gradient(135deg, #e9f2ff 0%, #f8fbff 100%);
  border-radius: 16px;
  border: 1px solid #e0e7ff;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  text-align: left;
}

.header-tag {
  font-size: 18px;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 10px;
}

.header h2 {
  font-size: 32px;
  color: #1f2937;
  margin-bottom: 12px;
}

.header-description {
  color: #4b5563;
  line-height: 1.7;
  margin-bottom: 20px;
}

.activity-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.join-button {
  font-weight: 600;
  background: #2563eb;
  color: white;
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
}

.join-button:hover {
  cursor: pointer;
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.2s ease;
}

.lookup-button {
  background: white;
  font-weight: 500;
  color: #2563eb;
  padding: 10px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.lookup-button:hover {
  cursor: pointer;
  color: #1d4ed8;
  background: #f8fafc;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
  transition: all 0.2s ease;
}

.header-panel {
  display: flex;
  align-items: center;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(30, 64, 175, 0.08);
}

.header-panel h3 {
  color: #1e3a8a;
  margin-bottom: 12px;
  margin-right: 20px;
}

.header-panel ul {
  display: grid;
  color: #374151;
  list-style: none;
  gap: 10px;
  padding: 0;
  margin: 0;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-content {
  background: white;
  padding: 18px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
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

.activity {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.04);
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.activity-header h3 {
  margin: 0;
  color: #1f2937;
}

.activity-link {
  font-size: 14px;
  color: #2563eb;
  text-decoration: none;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.activity-type {
  width: fit-content;
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 999px;
}

.activity-info {
  font-size: 14px;
  color: #6b7280;
}

.activity-description {
  flex: 1;
  font-size: 14px;
  color: #4b5563;
}
</style>
