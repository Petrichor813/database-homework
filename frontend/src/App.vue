<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import GlobalToast from "./components/utils/GlobalToast.vue";
import { useToast } from "./utils/toast";
import { getJson } from "./utils/api";

type MenuName = "activities" | "volunteer" | "dashboard" | "admin";
type UserRole = "ADMIN" | "VOLUNTEER" | "USER";
type VolunteerStatus =
  | "CERTIFIED"
  | "REVIEWING"
  | "REJECTED"
  | "SUSPENDED"
  | null;

interface UserProfile {
  id?: number | string;
  username?: string;
  role?: UserRole;
  points?: number;
  volunteerStatus?: VolunteerStatus;
  token?: string;
}

const route = useRoute(); // 当前路由信息
const router = useRouter(); // 路由实例对象，用于跳转
const { info, error } = useToast();

// 导航栏相关
const navMenu = ref<HTMLElement | null>(null);
const activeMenu = ref<MenuName | null>(null);

// 用户相关
const curUser = ref<UserProfile | null>(null);
const userMenu = ref<HTMLElement | null>(null);
const userMenuOpen = ref(false);

// 退出登录的窗口
const showLogoutDialog = ref(false);

// 加载网页
const loading = ref(true);

const activeRoutes = computed<MenuName | null>(() => {
  const path = route.path;

  if (path.startsWith("/activities") || path.startsWith("/signups")) {
    return "activities";
  }
  if (path.startsWith("/exchange") || path.startsWith("/exchange-records")) {
    return "volunteer";
  }
  if (path.startsWith("/dashboard") || path.startsWith("/data-export")) {
    return "dashboard";
  }
  if (path.startsWith("/admin") || path.startsWith("/data-import")) {
    return "admin";
  }
  return null;
});

const toggleNavMenu = (menuName: MenuName) => {
  if (menuName === "admin") {
    if (!curUser.value) {
      info("请先登录");
      return;
    }
    if (curUser.value?.role !== "ADMIN") {
      info("您不是管理员，请勿点击后台操作");
      return;
    }
  }
  activeMenu.value = activeMenu.value === menuName ? null : menuName;
};

const toggleUserMenu = () => {
  userMenuOpen.value = !userMenuOpen.value;
};

// 点击空白处关掉下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  if (!userMenu.value) {
    return;
  }
  if (!userMenu.value.contains(event.target as Node)) {
    userMenuOpen.value = false;
  }
  if (navMenu.value && !navMenu.value.contains(event.target as Node)) {
    activeMenu.value = null;
  }
};

const displayName = computed(() => curUser.value?.username || "游客");
const userCircleText = computed(() =>
  displayName.value ? displayName.value.slice(0, 1) : "游",
);

const displayPoints = computed(() => curUser.value?.points ?? 0);

const roleMap: Record<UserRole, string> = {
  ADMIN: "管理员",
  VOLUNTEER: "志愿者",
  USER: "普通用户",
};

const roleLabel = computed(() => {
  if (!curUser.value) {
    return "游客";
  }

  const role = curUser.value.role;
  if (!role) {
    return "游客";
  }

  return roleMap[role] || role;
});

const volunteerStatusLabel = computed(() => {
  if (!curUser.value) {
    return "未登录";
  }
  if (curUser.value.role === "ADMIN") {
    return "管理员";
  }

  const status = curUser.value.volunteerStatus;
  switch (status) {
    case "CERTIFIED":
      return "已认证";
    case "REVIEWING":
      return "等待管理员审核";
    case "REJECTED":
      return "未通过审核";
    case "SUSPENDED":
      return "已停用";
    default:
      return "未申请";
  }
});

const volunteerStatusMap = new Map<VolunteerStatus, string>([
  ["CERTIFIED", "certified"],
  ["REVIEWING", "reviewing"],
  ["REJECTED", "rejected"],
  ["SUSPENDED", "suspended"],
  [null, "muted"],
]);

const volunteerStatus = computed(() => {
  if (curUser.value?.role === "ADMIN") {
    return "admin";
  }

  return (
    volunteerStatusMap.get(curUser.value?.volunteerStatus ?? null) ?? "muted"
  );
});

const handleLogout = () => {
  showLogoutDialog.value = true;
  userMenuOpen.value = false;
};

const confirmLogout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  curUser.value = null;
  showLogoutDialog.value = false;
  router.push("/login");
};

const cancelLogout = () => {
  showLogoutDialog.value = false;
};

const goToLogin = () => {
  userMenuOpen.value = false;
  router.push("/login");
};

// 路由跳转后，检查认证状态，并关掉已经打开的下拉菜单
router.afterEach(() => {
  checkAuth();
  userMenuOpen.value = false;
  activeMenu.value = null;
});

const refreshProfile = async (userId: number | string) => {
  try {
    const profile = await getJson<UserProfile>(`/api/user/${userId}/profile`);
    const oldUser = curUser.value ?? {};
    const newUser = {
      ...oldUser,
      ...profile,
      token: oldUser.token,
    };
    curUser.value = newUser;
    localStorage.setItem("user", JSON.stringify(newUser));
  } catch (err) {
    error("用户资料获取失败！");
  }
};

const checkAuth = () => {
  try {
    const localUser = localStorage.getItem("user");
    if (localUser) {
      const parsedUser = JSON.parse(localUser);
      curUser.value = parsedUser;
      if (parsedUser.id) {
        refreshProfile(parsedUser.id);
      }
      return;
    }
  } catch (err) {
    error("用户信息读取失败！");
    localStorage.removeItem("user");
    localStorage.removeItem("token");
  }
  curUser.value = null;
};

// 个人中心用户信息修改之后，页面要及时修改
const handleUserProfileUpdate = (event: Event) => {
  const updateEvent = event as CustomEvent<{ username?: string }>;
  const updatedUserName = updateEvent.detail?.username;
  if (updatedUserName && curUser.value) {
    curUser.value = {
      ...curUser.value,
      username: updatedUserName,
    };
  }
  checkAuth();
};

onMounted(() => {
  checkAuth();
  loading.value = false;
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("user-updated", handleUserProfileUpdate);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("user-updated", handleUserProfileUpdate);
});
</script>

<template>
  <div class="app">
    <header class="app-header">
      <div class="content">
        <router-link to="/home" class="logo">
          <h1>志愿服务平台</h1>
        </router-link>

        <nav class="nav-menu" ref="navMenu">
          <router-link to="/home" class="nav-link"> 首页 </router-link>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRoutes === 'activities' },
              ]"
              aria-controls="nav-activities"
              :aria-expanded="activeMenu === 'activities'"
              @click="toggleNavMenu('activities')"
            >
              活动中心
            </button>
            <div
              id="nav-activities"
              v-show="activeMenu === 'activities'"
              class="nav-dropdown"
            >
              <router-link to="/activities" class="nav-dropdown-link">
                活动列表
              </router-link>
              <router-link to="/signups" class="nav-dropdown-link">
                报名记录
              </router-link>
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRoutes === 'volunteer' },
              ]"
              aria-controls="nav-volunteer"
              :aria-expanded="activeMenu === 'volunteer'"
              @click="toggleNavMenu('volunteer')"
            >
              兑换中心
            </button>
            <div
              id="nav-volunteer"
              v-show="activeMenu === 'volunteer'"
              class="nav-dropdown"
            >
              <router-link to="/exchange" class="nav-dropdown-link">
                商品列表
              </router-link>
              <router-link to="/exchange-records" class="nav-dropdown-link">
                兑换记录
              </router-link>
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRoutes === 'dashboard' },
              ]"
              aria-controls="nav-dashboard"
              :aria-expanded="activeMenu === 'dashboard'"
              @click="toggleNavMenu('dashboard')"
            >
              数据看板
            </button>
            <div
              id="nav-dashboard"
              v-show="activeMenu === 'dashboard'"
              class="nav-dropdown"
            >
              <router-link to="/dashboard" class="nav-dropdown-link">
                看板总览
              </router-link>
              <router-link to="/data-export" class="nav-dropdown-link">
                数据导出
              </router-link>
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRoutes === 'admin' },
              ]"
              aria-controls="nav-admin"
              :aria-expanded="activeMenu === 'admin'"
              @click="toggleNavMenu('admin')"
            >
              管理后台
            </button>
            <div
              id="nav-admin"
              v-show="activeMenu === 'admin'"
              class="nav-dropdown"
            >
              <router-link to="/admin/volunteers" class="nav-dropdown-link">
                志愿者管理
              </router-link>
              <router-link to="/admin/activities" class="nav-dropdown-link">
                活动管理
              </router-link>
              <router-link to="/admin/points" class="nav-dropdown-link">
                积分管理
              </router-link>
              <router-link to="/admin/exchange" class="nav-dropdown-link">
                兑换管理
              </router-link>
              <router-link to="/data-import" class="nav-dropdown-link">
                数据导入
              </router-link>
            </div>
          </div>
        </nav>

        <div class="user-menu" ref="userMenu">
          <button
            type="button"
            class="user-menu-button"
            @click="toggleUserMenu"
          >
            <span class="user-circle">
              {{ userCircleText }}
            </span>
          </button>
          <transition name="fade">
            <div v-if="userMenuOpen" class="user-dropdown">
              <div class="user-card">
                <div class="user-large-circle">
                  {{ userCircleText }}
                </div>
                <div>
                  <p class="user-name">{{ displayName }}</p>
                  <p class="user-role">{{ roleLabel }}</p>
                </div>
              </div>
              <div class="user-info">
                <div class="info-row">
                  <span class="info-label">积分余额</span>
                  <span>{{ displayPoints }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">志愿者认证</span>
                  <span class="volunteer-status" :class="volunteerStatus">
                    {{ volunteerStatusLabel }}
                  </span>
                </div>
              </div>
              <div class="user-actions">
                <template v-if="curUser">
                  <router-link
                    to="/user-center"
                    class="user-action-button user-action-button--center"
                  >
                    进入个人中心
                  </router-link>
                  <button
                    class="user-action-button user-action-button--logout"
                    @click="handleLogout"
                  >
                    退出登录
                  </button>
                </template>
                <template v-else>
                  <button
                    class="user-action-button user-action-button--login"
                    @click="goToLogin"
                  >
                    登录/注册
                  </button>
                </template>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <GlobalToast />

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-if="showLogoutDialog" class="dialog-bg">
      <div class="dialog-container" role="dialog" aria-modal="true">
        <h3>确认退出登录</h3>
        <p>是否真的退出登录？</p>
        <div class="logout-actions">
          <button
            class="logout-buttons logout-button--confirm"
            @click="confirmLogout"
          >
            确认
          </button>
          <button
            class="logout-buttons logout-button--cancel"
            @click="cancelLogout"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Segoe UI";
  line-height: 1.6;
  color: #333333;
  background-color: #f5f7fa;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #111827;
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.content {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  gap: 20px;
}

.logo {
  text-decoration: none;
  color: white;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  color: #e5e7eb;
  font-size: 18px;
  font-weight: 500;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.3s;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
  background: #2563eb;
}

.nav-link.is-active {
  background: #2563eb;
  color: white;
}

.nav-group {
  position: relative;
}

.nav-trigger {
  font-size: 18px;
  font-weight: 500;
  cursor: pointer;
  background: transparent;
  border: none;
}

.nav-dropdown {
  display: flex;
  flex-direction: column;
  position: absolute;
  background: white;
  min-width: 160px;
  top: 42px;
  left: 0;
  border-radius: 10px;
  padding: 8px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  gap: 4px;
  z-index: 120;
}

.nav-dropdown-link {
  color: #111827;
  font-size: 16px;
  text-decoration: none;
  padding: 8px 10px;
}

.nav-dropdown-link:hover {
  background: #f3f4f6;
}

.nav-dropdown-link + .nav-dropdown-link {
  border-top: 1px solid grey;
  margin-top: 4px;
  padding-top: 8px;
}

.user-menu {
  position: relative;
}

.user-menu-button {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
}

.user-circle {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  font-weight: 600;
  border-radius: 50%;
  background: #2563eb;
  color: white;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.user-dropdown {
  display: grid;
  position: absolute;
  min-width: 220px;
  top: 48px;
  right: 0;
  border-radius: 12px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);
  padding: 16px;
  gap: 14px;
  background: white;
  color: #111827;
  z-index: 20;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-large-circle {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  font-weight: 700;
  background: #e0e7ff;
  color: #1d4ed8;
}

.user-name {
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
}

.user-info {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #374151;
  padding: 4px 0;
}

.info-label {
  font-weight: bold;
}

.volunteer-status {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  border-radius: 50px;
  padding: 1px 8px;
  gap: 8px;
  line-height: 1.4;
}

.volunteer-status.admin {
  color: #a116d4;
  border: 1px solid #a116d4;
}

.volunteer-status.certified {
  color: #10b981;
  border: 1px solid #10b981;
}

.volunteer-status.reviewing {
  color: #d97706;
  border: 1px solid #f59e0b;
}

.volunteer-status.rejected,
.volunteer-status.suspended {
  color: #ef4444;
  border: 1px solid #ef4444;
}

.volunteer-status.muted {
  color: #6b7280;
  border: 1px solid #9ca3af;
}

.user-actions {
  display: grid;
  gap: 8px;
}

.user-action-button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  font-size: 14px;
  font-weight: 500;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-action-button--center {
  background: #2563eb;
  color: white;
}

.user-action-button--center:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.user-action-button--logout {
  background: white;
  color: black;
  border: 2px solid #cbd5f5;
}

.user-action-button--logout:hover {
  background: #eeeeee;
  color: #475569;
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.user-action-button--login {
  background: #2563eb;
  color: white;
}

.user-action-button--login:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  z-index: 1000;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading p {
  margin-top: 15px;
  color: #3498db;
  font-weight: 500;
}

.dialog-bg {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  z-index: 1001;
}

.dialog-container {
  display: grid;
  text-align: center;
  width: min(360px, 90%);
  border-radius: 14px;
  padding: 20px 24px;
  gap: 10px;
  background: white;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.dialog-container p {
  color: #4b5563;
}

.logout-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.logout-buttons {
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-button--confirm {
  background: #2563eb;
  color: white;
  border: none;
}

.logout-button--confirm:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.logout-button--cancel {
  background: white;
  border: 2px solid #cbd5f5;
}

.logout-button--cancel:hover {
  background: #eee;
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}
</style>
