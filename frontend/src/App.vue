<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <router-link to="/home" class="logo">
          <h1>志愿服务平台</h1>
        </router-link>
        <nav class="nav-menu">
          <router-link to="/home" class="nav-link">首页</router-link>
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
              <div class="user-stats">
                <div class="stat">
                  <span class="stat-label">积分余额</span>
                  <span class="stat-value">{{ displayPoints }}</span>
                </div>
              </div>
              <div class="user-actions">
                <template v-if="currentUser">
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import GlobalToast from './components/GlobalToast.vue'

const router = useRouter()

const loading = ref(true)
const currentUser = ref(null)
const menuOpen = ref(false)
const userMenuRef = ref(null)

const displayName = computed(() => currentUser.value?.username || '游客')
const displayPoints = computed(() => currentUser.value?.points ?? 0)
const avatarText = computed(() => (displayName.value ? displayName.value.slice(0, 1) : '游'))
const roleLabel = computed(() => {
  if (!currentUser.value) return '游客'
  const role = currentUser.value.role
  const roleMap = {
    ADMIN: '管理员',
    VOLUNTEER: '志愿者',
    USER: '普通用户'
  }
  return roleMap[role] || role
})

onMounted(() => {
  checkAuth()
  loading.value = false
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

const checkAuth = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      currentUser.value = JSON.parse(userStr)
      return
    }
  } catch (error) {
    console.error('读取用户信息失败:', error)
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }
  currentUser.value = null
}

const toggleUserMenu = () => {
  menuOpen.value = !menuOpen.value
}

const handleClickOutside = event => {
  if (!userMenuRef.value) return
  if (!userMenuRef.value.contains(event.target)) {
    menuOpen.value = false
  }
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    currentUser.value = null
    menuOpen.value = false
    router.push('/login')
  }
}

const goToLogin = () => {
  menuOpen.value = false
  router.push('/login')
}

router.afterEach(() => {
  checkAuth()
  menuOpen.value = false
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
