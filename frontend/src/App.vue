<template>
  <div id="app">
    <!-- 导航栏 -->
    <header v-if="showHeader" class="app-header">
      <div class="header-content">
        <div class="logo">
          <h1>志愿积分系统</h1>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-link">首页</router-link>
          <template v-if="currentUser">
            <span class="user-info">欢迎，{{ currentUser.username }} [{{ currentUser.role }}]</span>
            <button @click="handleLogout" class="logout-btn">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-link">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 全局加载 -->
    <div v-if="loading" class="global-loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(true)

// 当前用户信息
const currentUser = ref(null)

// 是否显示导航栏（登录页不显示）
const showHeader = computed(() => {
  return route.path !== '/login' && route.path !== '/register'
})

// 初始化检查登录状态
onMounted(() => {
  checkAuth()
  loading.value = false
})

// 检查本地存储的登录状态
const checkAuth = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      currentUser.value = JSON.parse(userStr)
    }
  } catch (error) {
    console.error('读取用户信息失败:', error)
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    currentUser.value = null
    router.push('/login')
  }
}

// 监听路由变化
router.afterEach(() => {
  checkAuth()
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

/* 头部样式 */
.app-header {
  background: #2c3e50;
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
  height: 60px;
}

.logo h1 {
  font-size: 20px;
  font-weight: 600;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  color: #ecf0f1;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
  font-size: 14px;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
  background: #3498db;
}

.user-info {
  font-size: 14px;
  color: #bdc3c7;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #c0392b;
}

/* 主内容区域 */
.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

/* 全局加载 */
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

/* 基本页面容器样式 */
.page-container {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #3498db;
}
</style>
