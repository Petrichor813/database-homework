<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <router-link to="/home" class="logo">
          <h1>志愿服务平台</h1>
        </router-link>
        <nav class="nav-menu">
          <router-link to="/home" class="nav-link">首页</router-link>
          <div class="nav-group">
            <button type="button" class="nav-link nav-trigger" aria-controls="nav-activities"
              :aria-expanded="activeMenu === 'activities'" @click="toggleMenu('activities')"
              @keydown="event => handleTriggerKeydown(event, 'activities')">
              活动中心
            </button>
            <div id="nav-activities" v-show="activeMenu === 'activities'" class="nav-dropdown">
              <router-link to="/activities" class="nav-dropdown-link">活动列表</router-link>
              <router-link to="/signups" class="nav-dropdown-link">报名记录</router-link>
            </div>
          </div>
          <div class="nav-group">
            <button type="button" class="nav-link nav-trigger" aria-controls="nav-volunteer"
              :aria-expanded="activeMenu === 'volunteer'" @click="toggleMenu('volunteer')"
              @keydown="event => handleTriggerKeydown(event, 'volunteer')">
              兑换中心
            </button>
            <div id="nav-volunteer" v-show="activeMenu === 'volunteer'" class="nav-dropdown">
              <router-link to="/exchange" class="nav-dropdown-link">商品列表</router-link>
            </div>
          </div>
          <div class="nav-group">
            <button type="button" class="nav-link nav-trigger" aria-controls="nav-dashboard"
              :aria-expanded="activeMenu === 'dashboard'" @click="toggleMenu('dashboard')"
              @keydown="event => handleTriggerKeydown(event, 'dashboard')">
              数据看板
            </button>
            <div id="nav-dashboard" v-show="activeMenu === 'dashboard'" class="nav-dropdown">
              <router-link to="/dashboard" class="nav-dropdown-link">看板总览</router-link>
              <router-link to="/data-import" class="nav-dropdown-link">数据导入</router-link>
              <router-link to="/data-export" class="nav-dropdown-link">数据导出</router-link>
            </div>
          </div>
          <div class="nav-group">
            <button type="button" class="nav-link nav-trigger" aria-controls="nav-admin"
              :aria-expanded="activeMenu === 'admin'" @click="toggleMenu('admin')"
              @keydown="event => handleTriggerKeydown(event, 'admin')">
              管理后台
            </button>
            <div id="nav-admin" v-show="activeMenu === 'admin'" class="nav-dropdown">
              <router-link to="/admin/volunteers" class="nav-dropdown-link">志愿者审核</router-link>
              <router-link to="/admin/activities" class="nav-dropdown-link">活动管理</router-link>
              <router-link to="/admin/points" class="nav-dropdown-link">积分管理</router-link>
              <router-link to="/admin/exchange" class="nav-dropdown-link">兑换管理</router-link>
            </div>
          </div>
        </nav>
        <div class="user-area" ref="userMenuRef">
          <button class="avatar-btn" @click="toggleUserMenu" aria-label="用户菜单">
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
                    <span>{{ isVolunteerVerified ? '已认证' : '未认证' }}</span>
                    <span class="status-badge" :class="{ 'is-verified': isVolunteerVerified }">
                      {{ isVolunteerVerified ? '已认证' : '未认证' }}
                    </span>
                  </span>
                </div>
              </div>
              <div class="user-actions">
                <template v-if="currentUser">
                  <router-link to="/user-center" class="dropdown-btn primary user-center-btn">
                    进入个人中心
                  </router-link>
                  <button class="dropdown-btn" @click="handleLogout">退出登录</button>
                </template>
                <template v-else>
                  <button class="dropdown-btn primary" @click="goToLogin">登录/注册</button>
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

    <div v-if="loading" class="global-loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import GlobalToast from './components/GlobalToast.vue';

const router = useRouter();

const loading = ref(true);
const currentUser = ref(null);
const menuOpen = ref(false);
const userMenuRef = ref(null);
const activeMenu = ref(null);

const displayName = computed(() => currentUser.value?.username || '游客');
const displayPoints = computed(() => currentUser.value?.points ?? 0);
const avatarText = computed(() => (displayName.value ? displayName.value.slice(0, 1) : '游'));
const isVolunteerVerified = computed(() => currentUser.value?.volunteerVerified ?? false);
const roleLabel = computed(() => {
  if (!currentUser.value) return '游客';
  const role = currentUser.value.role;
  const roleMap = {
    ADMIN: '管理员',
    VOLUNTEER: '志愿者',
    USER: '普通用户'
  };
  return roleMap[role] || role;
})

onMounted(() => {
  checkAuth();
  loading.value = false;
  document.addEventListener('click', handleClickOutside);
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
})

const checkAuth = () => {
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      parsedUser.value = JSON.parse(userStr);
      currentUser.value = {
        ...parsedUser,
        volunteerVerified: parsedUser.volunteerVerified ?? parsedUser.isVerified ?? false
      };
      return;
    }
  } catch (error) {
    console.error('读取用户信息失败:', error);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }
  currentUser.value = null;
}

const toggleUserMenu = () => {
  menuOpen.value = !menuOpen.value;
}

const toggleMenu = menuName => {
  activeMenu.value = activeMenu.value === menuName ? null : menuName;
}

const handleTriggerKeydown = (event, menuName) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    toggleMenu(menuName);
  }
}

const handleClickOutside = event => {
  if (!userMenuRef.value) return;
  if (!userMenuRef.value.contains(event.target)) {
    menuOpen.value = false;
  }
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    currentUser.value = null;
    menuOpen.value = false;
    router.push('/login');
  }
}

const goToLogin = () => {
  menuOpen.value = false;
  router.push('/login');
}

router.afterEach(() => {
  checkAuth();
  menuOpen.value = false;
  activeMenu.value = null;
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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

.nav-dropdown-link+.nav-dropdown-link {
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
</style>
