<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "../utils/toast";
import { getJson } from "../utils/api";

interface User {
  role?: string | null;
  volunteerStatus?: string | null;
  id?: number | null;
}

interface UserProfile {
  id: number;
  volunteerId: number | null;
  username: string;
  role: string;
  phone: string | null;
  realName: string | null;
  volunteerStatus: string | null;
  applyReason: string | null;
  points: number | null;
  serviceHours: number | null;
}

interface SignupRecord {
  id: number;
  volunteerId: number;
  activityId: number;
  status: string;
  actualHours: number | null;
  points: number | null;
  signupTime: string;
  volunteerStartTime: string | null;
  volunteerEndTime: string | null;
  updateTime: string | null;
  note: string | null;
}

const router = useRouter();
const { info, error } = useToast();

const userProfile = ref<UserProfile | null>(null);
const signupRecords = ref<SignupRecord[]>([]);
const loading = ref(false);

const getUserStatus = () => {
  const localUser = localStorage.getItem("user");
  if (!localUser) {
    return {
      isLoggedIn: false,
      role: null,
      volunteerStatus: null,
      userId: null,
    };
  }

  try {
    const parsedUser = JSON.parse(localUser) as User | null;

    return {
      isLoggedIn: true,
      role: parsedUser?.role ?? null,
      volunteerStatus: parsedUser?.volunteerStatus ?? null,
      userId: parsedUser?.id ?? null,
    };
  } catch (err) {
    return {
      isLoggedIn: false,
      role: null,
      volunteerStatus: null,
      userId: null,
    };
  }
};

const fetchUserData = async () => {
  const { isLoggedIn, userId, volunteerStatus } = getUserStatus();

  if (!isLoggedIn || !userId) {
    return;
  }

  if (volunteerStatus !== "CERTIFIED") {
    return;
  }

  try {
    loading.value = true;
    const profile = await getJson<UserProfile>(`/api/user/${userId}/profile`);
    userProfile.value = profile;

    if (profile.volunteerId) {
      const records = await getJson<SignupRecord[]>(
        `/api/volunteer/${profile.volunteerId}/signup-records/all`
      );
      signupRecords.value = records;
    }
  } catch (err) {
    const msg = err instanceof Error ? err.message : "请检查网络后重试";
    error("获取用户数据失败", msg);
  } finally {
    loading.value = false;
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

  info("您尚未认证为志愿者", "完成认证后即可报名参与活动");
};

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

onMounted(() => {
  fetchUserData();
});
</script>

<template>
  <div class="home">
    <section class="header">
      <div class="header-content">
        <p class="header-tag">社区志愿服务平台</p>
        <h2>让公益更加高效，让志愿更有温度</h2>
        <p class="header-desc">
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
      <div v-if="loading" class="loading-state">加载中...</div>
      <div
        v-else-if="!getUserStatus().isLoggedIn"
        class="stat-card login-prompt"
      >
        <p class="login-text">请登录后查看数据</p>
      </div>
      <div
        v-else-if="getUserStatus().volunteerStatus !== 'CERTIFIED'"
        class="stat-card volunteer-prompt"
      >
        <p class="volunteer-title">您暂时还不是志愿者</p>
        <p class="volunteer-desc">
          成为志愿者，参与社区服务，用行动传递温暖，让爱心点亮生活。每一次志愿服务都是对社会的贡献，也是对自我的提升。立即申请，开启您的志愿之旅！
        </p>
      </div>
      <template v-else>
        <div class="stat-content">
          <p class="stat-label">累计志愿服务时长</p>
          <p class="stat-value">{{ userProfile?.serviceHours ?? 0 }} 小时</p>
        </div>
        <div class="stat-content">
          <p class="stat-label">当前积分</p>
          <p class="stat-value">{{ userProfile?.points ?? 0 }} 分</p>
        </div>
        <div class="stat-content">
          <p class="stat-label">已报名活动数</p>
          <p class="stat-value">{{ signupRecords.length }} 个</p>
        </div>
        <div class="stat-content">
          <p class="stat-label">已参与活动数</p>
          <p class="stat-value">
            {{
              signupRecords.filter((r) => r.status === "PARTICIPATED").length
            }}
            个
          </p>
        </div>
      </template>
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
          <p class="activity-desc">{{ activity.desc }}</p>
          <button type="button" class="join-button">报名参与</button>
        </article>
      </div>
    </section>
  </div>
</template>

<style lang="css" scoped>
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

.header-desc {
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
  cursor: pointer;
  transition: all 0.2s ease;
}

.join-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.lookup-button {
  background: white;
  font-weight: 500;
  color: #2563eb;
  padding: 10px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.lookup-button:hover {
  color: #1d4ed8;
  background: #f8fafc;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
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

.loading-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #6b7280;
  font-size: 16px;
}

.stat-card {
  grid-column: 1 / -1;
  background: white;
  padding: 40px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.login-text {
  font-size: 18px;
  color: #6b7280;
  margin: 0;
}

.volunteer-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.volunteer-desc {
  font-size: 15px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
  max-width: 600px;
}

.stat-content {
  background: white;
  padding: 18px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.stat-label {
  font-size: 20px;
  font-weight: 700;
  color: black;
  margin-top: 6px;
}

.stat-value {
  font-size: 18px;
  color: #1f2937;
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
  border: 2px solid #eef2f7;
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

.activity-desc {
  flex: 1;
  font-size: 14px;
  color: #4b5563;
}
</style>
