<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import volunteer1 from "../assets/volunteer-1.jpg";
import volunteer2 from "../assets/volunteer-2.jpg";
import volunteer3 from "../assets/volunteer-3.jpg";
import { getJson } from "../utils/api";
import { useToast } from "../utils/toast";

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

interface Activity {
  id: number;
  title: string;
  description: string;
  type: string;
  location: string;
  startTime: string;
  endTime: string;
  status: string;
  pointsPerHour: number;
  maxParticipants: number;
  curParticipants: number;
  signupStatus: string | null;
}

const router = useRouter();
const { info, error } = useToast();

const userProfile = ref<UserProfile | null>(null);
const signupRecords = ref<SignupRecord[]>([]);
const hotActivities = ref<Activity[]>([]);
const loading = ref(false);

const images = [volunteer1, volunteer2, volunteer3];

const curImageIndex = ref(0);
const imagePlayInterval = ref<number | null>(null);

const startImagePlay = () => {
  imagePlayInterval.value = setInterval(() => {
    curImageIndex.value = (curImageIndex.value + 1) % images.length;
  }, 3000);
};

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

const fetchHotActivities = async () => {
  try {
    const activities = await getJson<Activity[]>("/api/activity/hot-activities");
    hotActivities.value = activities;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "请检查网络后重试";
    error("获取热门活动失败", msg);
  }
};

const formatActivityTime = (startTime: string) => {
  const date = new Date(startTime);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${month}月${day}日 ${hours}:${minutes}`;
};

const formatActivityType = (type: string) => {
  const typeMap: Record<string, string> = {
    COMMUNITY_SERVICE: "社区服务",
    CULTURAL_EVENTS: "文化活动",
    CHILDREN_TUTORING: "儿童辅导",
    DISABILITIES_SUPPORT: "助残服务",
    ELDERLY_CARE: "养老服务",
    ENVIRONMENTAL_PROTECTION: "环保活动",
    EDUCATION_SUPPORT: "教育支持",
    HEALTH_CARE: "医疗健康",
    EMERGENCY_RELIEF: "应急救援",
    OTHER: "其他",
  };
  return typeMap[type] || type;
};

const handleSignupActivity = (activityId: number) => {
  const { isLoggedIn, volunteerStatus } = getUserStatus();

  if (!isLoggedIn) {
    info("请先登录/注册");
    router.push("/login");
    return;
  }

  if (volunteerStatus !== "CERTIFIED") {
    info("您尚未认证为志愿者", "完成认证后即可报名参与活动");
    return;
  }

  router.push(`/activities?signup=${activityId}`);
};

onMounted(() => {
  fetchUserData();
  fetchHotActivities();
  startImagePlay();
});
</script>

<template>
  <div class="home">
    <section class="header">
      <div class="header-content">
        <p class="header-tag">社区志愿服务平台</p>
        <h2>让公益更加高效，让志愿更有温度</h2>
        <p class="header-desc">
          汇聚爱心力量，连接邻里温暖。参与志愿活动，收获成长与认可，让每一份善意都有回响。
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
        <div class="image-container">
          <transition name="slide" mode="out-in">
            <img :key="curImageIndex" :src="images[curImageIndex]" alt="" />
          </transition>
        </div>
      </div>
    </section>

    <section class="stats">
      <div v-if="loading" class="loading-state">加载中...</div>
      <div
        v-else-if="!getUserStatus().isLoggedIn"
        class="stat-card"
      >
        <p class="login-text">请登录后查看数据</p>
      </div>
      <div
        v-else-if="getUserStatus().volunteerStatus !== 'CERTIFIED'"
        class="stat-card"
      >
        <p class="volunteer-title">您暂时还不是志愿者</p>
        <p class="volunteer-desc">
          成为志愿者，参与社区服务，用行动传递温暖，让爱心点亮生活。每一次志愿服务都是对社会的贡献，也是对自我的提升。立即申请，开启您的志愿之旅！
        </p>
      </div>
      <template v-else>
        <div class="stat-content service-hours">
          <p class="stat-label">累计志愿服务时长</p>
          <p class="stat-value">{{ userProfile?.serviceHours ?? 0 }} 小时</p>
        </div>
        <div class="stat-content points">
          <p class="stat-label">当前积分</p>
          <p class="stat-value">{{ userProfile?.points ?? 0 }} 分</p>
        </div>
        <div class="stat-content signed-up">
          <p class="stat-label">已报名活动数</p>
          <p class="stat-value">{{ signupRecords.length }} 个</p>
        </div>
        <div class="stat-content participated">
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
          v-for="activity in hotActivities"
          :key="activity.id"
        >
          <span class="activity-type">{{ formatActivityType(activity.type) }}</span>
          <h4>{{ activity.title }}</h4>
          <p class="activity-info">
            {{ formatActivityTime(activity.startTime) }} · {{ activity.location }}
          </p>
          <p class="activity-desc">{{ activity.description }}</p>
          <button
            type="button"
            class="join-button"
            :disabled="!!activity.signupStatus"
            @click="handleSignupActivity(activity.id)"
          >
            {{ activity.signupStatus ? "已报名" : "报名参与" }}
          </button>
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
  gap: 20px;
  padding: 10px 25px;
  background: linear-gradient(135deg, #e9f2ff 0%, #f8fbff 100%);
  border-radius: 16px;
  border: 1px solid #e0e7ff;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
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
  margin-bottom: 12px;
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

.join-button:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.join-button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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
  margin: 1rem auto;
  overflow: hidden;
  position: relative;
  height: 320px;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.5s ease;
}

.slide-enter-from {
  transform: translateX(100%);
}

.slide-leave-to {
  transform: translateX(-100%);
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
  padding: 18px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.service-hours {
  background: #e86a6a;
}

.points {
  background: #5fb08c;
}

.signed-up {
  background: #f9a95d;
}

.participated {
  background: #b26eb2;
}

.stat-label {
  font-size: 16px;
  color: white;
  margin-top: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: white;
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

.activity-link:hover {
  text-decoration: underline;
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
  min-height: 200px;
}

.activity-content .join-button {
  margin-top: auto;
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
