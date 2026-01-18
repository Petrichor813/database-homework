<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 标题 -->
      <div class="login-header">
        <h1>志愿服务积分系统</h1>
        <p>用户登录</p>
      </div>

      <!-- 登录表单 -->
      <form class="login-form" @submit.prevent="handleLogin">
        <!-- 用户名 -->
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" v-model="form.username" type="text" placeholder="请输入用户名" required class="form-input" />
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label for="password">密码</label>
          <input id="password" v-model="form.password" type="password" placeholder="请输入密码" required
            class="form-input" />
        </div>

        <!-- 角色选择 -->
        <div class="form-group">
          <label>登录身份</label>
          <div class="role-options">
            <label class="role-option">
              <input v-model="form.role" type="radio" name="role" value="USER" checked />
              <span>普通用户</span>
            </label>
            <label class="role-option">
              <input v-model="form.role" type="radio" name="role" value="VOLUNTEER" />
              <span>志愿者</span>
            </label>
            <label class="role-option">
              <input v-model="form.role" type="radio" name="role" value="ADMIN" />
              <span>管理员</span>
            </label>
          </div>
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <!-- 注册链接 -->
        <div class="form-footer">
          <span>没有账号？</span>
          <a href="#" @click.prevent="goToRegister" class="register-link">立即注册</a>
        </div>
      </form>

      <!-- 系统信息 -->
      <div class="system-info">
        <p>© 2025 志愿服务平台</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const form = reactive({
  username: '',
  password: '',
  role: 'USER'
})

// 加载状态
const loading = ref(false)

// 登录处理
const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    // 模拟登录API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟登录成功数据
    const mockUserData = {
      id: 1,
      username: form.username,
      role: form.role,
      token: 'mock-jwt-token-' + Date.now()
    }

    // 保存到localStorage
    localStorage.setItem('user', JSON.stringify(mockUserData))
    localStorage.setItem('token', mockUserData.token)

    // 根据角色跳转
    switch (form.role) {
      case 'ADMIN':
        await router.push('/admin')
        break
      case 'VOLUNTEER':
        await router.push('/volunteer')
        break
      case 'USER':
      default:
        await router.push('/user')
    }

    alert('登录成功！')
  } catch (error) {
    console.error('登录失败:', error)
    alert('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-card {
  width: 400px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-header {
  padding: 30px;
  text-align: center;
  background: #2c3e50;
  color: white;
}

.login-header h1 {
  font-size: 24px;
  margin-bottom: 10px;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  opacity: 0.8;
}

.login-form {
  padding: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.role-options {
  display: flex;
  gap: 15px;
  margin-top: 5px;
}

.role-option {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.role-option input {
  margin-right: 8px;
}

.role-option span {
  font-size: 14px;
  color: #555;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 10px;
}

.login-btn:hover {
  background: #2980b9;
}

.login-btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #3498db;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

.system-info {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  color: #777;
  font-size: 12px;
  border-top: 1px solid #eee;
}
</style>
