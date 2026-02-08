<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <router-link to="/home" class="logo">
          <h1>志愿服务平台</h1>
        </router-link>
        <nav class="nav-menu" ref="navMenuRef">
          <router-link to="/home" class="nav-link">首页</router-link>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRouteGroup === 'activities' },
              ]"
              aria-controls="nav-activities"
              :aria-expanded="activeMenu === 'activities'"
              @click="toggleMenu('activities')"
              @keydown="(event) => handleTriggerKeydown(event, 'activities')"
            >
              活动中心
            </button>
            <div
              id="nav-activities"
              v-show="activeMenu === 'activities'"
              class="nav-dropdown"
            >
              <router-link to="/activities" class="nav-dropdown-link"
                >活动列表</router-link
              >
              <router-link to="/signups" class="nav-dropdown-link"
                >报名记录</router-link
              >
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRouteGroup === 'volunteer' },
              ]"
              aria-controls="nav-volunteer"
              :aria-expanded="activeMenu === 'volunteer'"
              @click="toggleMenu('volunteer')"
              @keydown="(event) => handleTriggerKeydown(event, 'volunteer')"
            >
              兑换中心
            </button>
            <div
              id="nav-volunteer"
              v-show="activeMenu === 'volunteer'"
              class="nav-dropdown"
            >
              <router-link to="/exchange" class="nav-dropdown-link"
                >商品列表</router-link
              >
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRouteGroup === 'dashboard' },
              ]"
              aria-controls="nav-dashboard"
              :aria-expanded="activeMenu === 'dashboard'"
              @click="toggleMenu('dashboard')"
              @keydown="(event) => handleTriggerKeydown(event, 'dashboard')"
            >
              数据看板
            </button>
            <div
              id="nav-dashboard"
              v-show="activeMenu === 'dashboard'"
              class="nav-dropdown"
            >
              <router-link to="/dashboard" class="nav-dropdown-link"
                >看板总览</router-link
              >
              <router-link to="/data-export" class="nav-dropdown-link"
                >数据导出</router-link
              >
            </div>
          </div>
          <div class="nav-group">
            <button
              type="button"
              :class="[
                'nav-link nav-trigger',
                { 'is-active': activeRouteGroup === 'admin' },
              ]"
              aria-controls="nav-admin"
              :aria-expanded="activeMenu === 'admin'"
              @click="toggleMenu('admin')"
              @keydown="(event) => handleTriggerKeydown(event, 'admin')"
            >
              管理后台
            </button>
            <div
              id="nav-admin"
              v-show="activeMenu === 'admin'"
              class="nav-dropdown"
            >
              <router-link to="/admin/volunteers" class="nav-dropdown-link"
                >志愿者审核</router-link
              >
              <router-link to="/admin/activities" class="nav-dropdown-link"
                >活动管理</router-link
              >
              <router-link to="/admin/points" class="nav-dropdown-link">
                积分管理
              </router-link>
              <router-link to="/admin/exchange" class="nav-dropdown-link">
                兑换管理
              </router-link>
              <router-link to="/data-import" class="nav-dropdown-link"
                >数据导入</router-link
              >
            </div>
          </div>
        </nav>
        <div class="user-area" ref="userMenuRef">
          <button
            class="avatar-btn"
            @click="toggleUserMenu"
            aria-label="用户菜单"
          >
            <span class="avatar-circle">{{ avatarText }}</span>
          </button>
          <transition name="fade">
            <div v-if="menuOpen" class="user-dropdown">
              <div class="user-card">
                <div class="avatar-large">{{ avatarText }}</div>
                <div>
                  <p class="user-name">{{ displayName }}</p>
                  <p class="user-role">{{ roleLabel }}</p>
                </div>
              </div>
              <div v-if="currentUser" class="user-stats">
                <div class="stat">
                  <span class="stat-label">积分余额</span>
                  <span class="stat-value">{{ displayPoints }}</span>
                </div>
                <div class="stat">
                  <span class="stat-label">志愿者认证</span>
                  <span class="stat-value status-value">
                    <span class="status-badge" :class="statusBadgeClass">
                      {{ volunteerStatusLabel }}
                    </span>
                  </span>
                </div>
              </div>
              <div class="user-actions">
                <template v-if="currentUser">
                  <router-link
                    to="/user-center"
                    class="dropdown-btn primary user-center-btn"
                  >
                    进入个人中心
                  </router-link>
                  <button class="dropdown-btn" @click="handleLogout">
                    退出登录
                  </button>
                </template>
                <template v-else>
                  <button class="dropdown-btn primary" @click="goToLogin">
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

    <div v-if="showLogoutConfirm" class="modal-overlay">
      <div class="modal-card" role="dialog" aria-modal="true">
        <h3>确认退出登录</h3>
        <p>是否真的退出登录？</p>
        <div class="modal-actions">
          <button class="modal-btn primary" @click="confirmLogout">确认</button>
          <button class="modal-btn" @click="cancelLogout">取消</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="global-loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import GlobalToast from "./components/GlobalToast.vue";
import { getJson } from "./utils/api";
import { useToast } from "./utils/toast";

type UserRole = "ADMIN" | "VOLUNTEER" | "USER";
type VolunteerStatus =
  | "CERTIFIED"
  | "REVIEWING"
  | "REJECTED"
  | "SUSPENDED"
  | null;
type MenuName = "activities" | "volunteer" | "dashboard" | "admin";

interface UserProfile {
  id?: number | string;
  username?: string;
  role?: UserRole;
  points?: number;
  volunteerStatus?: VolunteerStatus;
  token?: string;
}

const route = useRoute();
const router = useRouter();
const { info } = useToast();

const loading = ref(true);
const currentUser = ref<UserProfile | null>(null);
const menuOpen = ref(false);
const showLogoutConfirm = ref(false);
const userMenuRef = ref<HTMLElement | null>(null);
const navMenuRef = ref<HTMLElement | null>(null);
const activeMenu = ref<MenuName | null>(null);

const activeRouteGroup = computed<MenuName | null>(() => {
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

const displayName = computed(() => currentUser.value?.username || "游客");

const displayPoints = computed(() => currentUser.value?.points ?? 0);

const avatarText = computed(() =>
  displayName.value ? displayName.value.slice(0, 1) : "游",
);

const isVolunteerVerified = computed(
  () =>
    currentUser.value?.role === "ADMIN" ||
    currentUser.value?.volunteerStatus === "CERTIFIED",
);

const isVolunteerReviewing = computed(
  () =>
    currentUser.value?.role !== "ADMIN" &&
    currentUser.value?.volunteerStatus === "REVIEWING",
);

const volunteerStatusLabel = computed(() => {
  if (!currentUser.value) return "未登录";
  if (currentUser.value.role === "ADMIN") return "管理员权限";
  const status = currentUser.value.volunteerStatus;
  if (status === "CERTIFIED") return "已认证";
  if (status === "REVIEWING") return "待审核";
  if (status === "REJECTED") return "未通过";
  if (status === "SUSPENDED") return "已停用";
  return "未申请";
});

const statusBadgeClass = computed(() => ({
  "is-verified": isVolunteerVerified.value,
  "is-reviewing": isVolunteerReviewing.value,
  "is-muted": !isVolunteerVerified.value && !isVolunteerReviewing.value,
}));

const roleLabel = computed(() => {
  if (!currentUser.value) return "游客";
  const role = currentUser.value.role;
  const roleMap = {
    ADMIN: "管理员",
    VOLUNTEER: "志愿者",
    USER: "普通用户",
  };
  if (!role) return "游客";
  return roleMap[role] || role;
});

const handleUserUpdated = (event: Event) => {
  const customEvent = event as CustomEvent<{ username?: string }>;
  const nextUsername = customEvent.detail?.username;
  if (nextUsername && currentUser.value) {
    currentUser.value = {
      ...currentUser.value,
      username: nextUsername,
    };
  }
  checkAuth();
};

onMounted(() => {
  checkAuth();
  loading.value = false;
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("user-updated", handleUserUpdated);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("user-updated", handleUserUpdated);
});

const checkAuth = () => {
  try {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      const parsedUser = JSON.parse(userStr);
      currentUser.value = parsedUser;
      if (parsedUser.id) {
        refreshProfile(parsedUser.id);
      }
      return;
    }
  } catch (error) {
    console.error("读取用户信息失败:", error);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
  }
  currentUser.value = null;
};

const refreshProfile = async (userId: number | string) => {
  try {
    const profile = await getJson<UserProfile>(`/api/users/${userId}/profile`);
    const baseUser = currentUser.value ?? {};
    const updatedUser = {
      ...baseUser,
      ...profile,
      token: baseUser.token,
    };
    currentUser.value = updatedUser;
    localStorage.setItem("user", JSON.stringify(updatedUser));
  } catch (error) {
    console.error("获取用户资料失败:", error);
  }
};

const toggleUserMenu = () => {
  menuOpen.value = !menuOpen.value;
};

const toggleMenu = (menuName: MenuName) => {
  if (menuName === "admin" && currentUser.value?.role !== "ADMIN") {
    info("您不是管理员，请勿点击后台操作");
    return;
  }
  activeMenu.value = activeMenu.value === menuName ? null : menuName;
};

const handleTriggerKeydown = (event: KeyboardEvent, menuName: MenuName) => {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    toggleMenu(menuName);
  }
};

const handleClickOutside = (event: MouseEvent) => {
  if (!userMenuRef.value) return;
  if (!userMenuRef.value.contains(event.target as Node)) {
    menuOpen.value = false;
  }
  if (navMenuRef.value && !navMenuRef.value.contains(event.target as Node)) {
    activeMenu.value = null;
  }
};

const handleLogout = () => {
  showLogoutConfirm.value = true;
  menuOpen.value = false;
};

const confirmLogout = () => {
  localStorage.removeItem("user");
  localStorage.removeItem("token");
  currentUser.value = null;
  showLogoutConfirm.value = false;
  router.push("/login");
};

const cancelLogout = () => {
  showLogoutConfirm.value = false;
};

const goToLogin = () => {
  menuOpen.value = false;
  router.push("/login");
};

router.afterEach(() => {
  checkAuth();
  menuOpen.value = false;
  activeMenu.value = null;
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f5f7fa;
}

#app {
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

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  gap: 20px;
}

.logo {
  text-decoration: none;
  color: inherit;
}

.logo h1 {
  font-size: 20px;
  font-weight: 600;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-group {
  position: relative;
}

.nav-trigger {
  cursor: pointer;
  background: transparent;
  border: none;
  font: inherit;
}

.nav-dropdown {
  position: absolute;
  top: 42px;
  left: 0;
  background: #fff;
  border-radius: 10px;
  min-width: 160px;
  padding: 8px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  display: flex;
  flex-direction: column;
  gap: 4px;
  z-index: 120;
}

.nav-dropdown-link {
  color: #111827;
  text-decoration: none;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 14px;
}

.nav-dropdown-link:hover {
  background: #f3f4f6;
}

.nav-dropdown-link + .nav-dropdown-link {
  border-top: 1px solid #aaa;
  margin-top: 4px;
  padding-top: 8px;
}

.nav-link {
  color: #e5e7eb;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.3s;
  font-size: 14px;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
  background: #2563eb;
}

.nav-link.is-active {
  background: #2563eb;
  color: #fff;
}

.user-area {
  position: relative;
}

.avatar-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
}

.avatar-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
}

.user-dropdown {
  position: absolute;
  right: 0;
  top: 48px;
  background: #fff;
  color: #111827;
  min-width: 220px;
  border-radius: 12px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);
  padding: 16px;
  display: grid;
  gap: 14px;
  z-index: 20;
}

.user-card {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar-large {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.user-name {
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
}

.user-stats {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 12px;
}

.stat {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #374151;
}

.status-value {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-badge {
  border: 1px solid #ef4444;
  color: #ef4444;
  border-radius: 999px;
  padding: 1px 8px;
  font-size: 12px;
  line-height: 1.4;
  font-weight: 500;
}

.status-badge.is-verified {
  border-color: #10b981;
  color: #10b981;
}

.status-badge.is-reviewing {
  border-color: #f59e0b;
  color: #d97706;
}

.status-badge.is-muted {
  border-color: #9ca3af;
  color: #6b7280;
}

.user-actions {
  display: grid;
  gap: 8px;
}

.dropdown-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  line-height: 1.4;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.dropdown-btn.primary {
  background: #2563eb;
  color: #fff;
  border: none;
}

.user-center-btn {
  text-align: center;
  text-decoration: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
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

.global-loading p {
  margin-top: 15px;
  color: #3498db;
  font-weight: 500;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.modal-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  width: min(360px, 90%);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
  display: grid;
  gap: 10px;
  text-align: center;
}

.modal-card p {
  color: #4b5563;
}

.modal-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.modal-btn {
  border-radius: 8px;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  cursor: pointer;
  font-weight: 600;
}

.modal-btn.primary {
  background: #2563eb;
  color: #fff;
  border: none;
}
</style>
