<template>
  <div class="login-container">
    <section class="login-intro">
      <p class="intro-tag">社区志愿服务平台</p>
      <h2>欢迎回到志愿服务积分系统</h2>
      <p class="intro-desc">
        统一管理志愿活动、积分与数据统计，帮助社区服务更高效。
      </p>
      <div class="intro-list">
        <div class="intro-item">
          <span>✔</span>
          <p>活动报名、审核与结算</p>
        </div>
        <div class="intro-item">
          <span>✔</span>
          <p>积分记录与兑换管理</p>
        </div>
        <div class="intro-item">
          <span>✔</span>
          <p>数据导入、分析与导出</p>
        </div>
      </div>
    </section>

    <div class="login-card">
      <div class="login-header">
        <h1>用户登录</h1>
        <p>请输入账号信息继续</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            required
            class="form-input"
          />
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? "登录中..." : "登录" }}
        </button>

        <div class="form-footer">
          <span>没有账号？</span>
          <a href="#" @click.prevent="goToRegister" class="register-link"
            >立即注册</a
          >
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { postJson } from "../utils/api";
import { useToast } from "../utils/toast";

type UserRole = "ADMIN" | "VOLUNTEER" | "USER";

interface LoginResponse {
  id: number | string;
  username: string;
  role: UserRole;
  token: string;
  volunteerStatus?: string | null;
  phone?: string | null;
}

const router = useRouter();
const { success, error } = useToast();

const form = reactive({
  username: "",
  password: "",
});

const loading = ref(false);

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    error("登录失败", "请输入用户名和密码");
    return;
  }

  loading.value = true;

  try {
    const response = await postJson<LoginResponse>("/api/auth/login", {
      username: form.username,
      password: form.password,
    });

    const userData = {
      id: response.id,
      username: response.username,
      role: response.role,
      token: response.token,
      volunteerStatus: response.volunteerStatus ?? null,
      phone: response.phone ?? null,
    };

    localStorage.setItem("user", JSON.stringify(userData));
    localStorage.setItem("token", response.token);

    await router.push("/home");
    success("登录成功");
  } catch (err) {
    const message =
      err instanceof Error ? err.message : "登录失败，请检查用户名或密码";
    error("登录失败", message);
  } finally {
    loading.value = false;
  }
};

const goToRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
.login-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  padding: 30px 0;
  align-items: center;
  min-height: calc(100vh - 120px);
}

.login-intro {
  padding: 20px;
}

.intro-tag {
  color: #2563eb;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 10px;
}

.login-intro h2 {
  font-size: 30px;
  margin-bottom: 12px;
  color: #1f2937;
}

.intro-desc {
  color: #4b5563;
  line-height: 1.7;
  margin-bottom: 20px;
}

.intro-list {
  display: grid;
  gap: 12px;
}

.intro-item {
  display: flex;
  gap: 10px;
  align-items: center;
  color: #1f2937;
}

.login-card {
  width: min(420px, 100%);
  background: white;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  overflow: hidden;
  justify-self: center;
}

.login-header {
  padding: 24px;
  text-align: center;
  background: #1f2937;
  color: white;
}

.login-header h1 {
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  opacity: 0.8;
}

.login-form {
  padding: 24px;
}

.form-group {
  margin-bottom: 18px;
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
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 10px;
}

.login-btn:hover {
  background: #1d4ed8;
}

.login-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  color: #6b7280;
  font-size: 14px;
}

.register-link {
  color: #2563eb;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
