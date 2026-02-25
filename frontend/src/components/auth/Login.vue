<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "../../utils/toast";
import { postJson } from "../../utils/api";

type UserRole = "ADMIN" | "VOLUNTEER" | "USER";

interface LoginResponse {
  id: number | string;
  username: string;
  role: UserRole;
  phone?: string | null;
  token: string;
  volunteerStatus?: string | null;
}

const router = useRouter();
const { success, error } = useToast();

const account_info = reactive({
  username: "",
  password: "",
});

const loading = ref(false);

const handleLogin = async () => {
  if (!account_info.username.trim() || !account_info.password.trim()) {
    error("登录失败", "请输入用户名和密码");
    return;
  }

  loading.value = true;

  try {
    const response = await postJson<LoginResponse>("/api/auth/login", {
      username: account_info.username,
      password: account_info.password,
    }, false);

    const userInfo = {
      id: response.id,
      username: response.username,
      role: response.role,
      token: response.token,
      volunteerStatus: response.volunteerStatus ?? null,
      phone: response.phone ?? null,
    };

    localStorage.setItem("user", JSON.stringify(userInfo));
    localStorage.setItem("token", response.token);

    await router.push("/home");
    success("登录成功");
  } catch (err) {
    const msg = err instanceof Error ? err.message : "请检查用户名或密码";
    error("登录失败", msg);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login">
    <section class="intro">
      <p class="intro-tag">社区志愿服务平台</p>
      <h2>欢迎回到志愿服务平台</h2>
      <p class="intro-description">
        统一管理志愿活动、积分与数据统计，帮助社区服务更高效。
      </p>
      <div class="intro-list">
        <div class="intro-list-item">
          <span>✔</span>
          <p>活动报名、审核与结算</p>
        </div>
        <div class="intro-list-item">
          <span>✔</span>
          <p>积分记录与兑换管理</p>
        </div>
        <div class="intro-list-item">
          <span>✔</span>
          <p>数据导入、分析与导出</p>
        </div>
      </div>
    </section>

    <div class="login-area">
      <div class="header">
        <h1>用户登录</h1>
        <p>请输入账号信息以继续</p>
      </div>

      <form class="form" @submit.prevent="handleLogin">
        <div class="form-row">
          <label for="username">用户名</label>
          <div class="input-area">
            <input
              id="username"
              v-model="account_info.username"
              type="text"
              placeholder="请输入用户名"
              required
              class="input-box"
            />
            <button
              v-if="account_info.username"
              type="button"
              class="clear-button"
              @click="account_info.username = ''"
            >
              ×
            </button>
          </div>
        </div>

        <div class="form-row">
          <label for="password">密码</label>
          <div class="input-area">
            <input
              id="password"
              v-model="account_info.password"
              type="password"
              placeholder="请输入密码"
              required
              class="input-box"
            />
            <button
              v-if="account_info.password"
              type="button"
              class="clear-button"
              @click="account_info.password = ''"
            >
              ×
            </button>
          </div>
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? "登录中..." : "登录" }}
        </button>

        <div class="form-footer">
          <span>没有账号？</span>
          <router-link to="/register" class="register-link">
            立即注册
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<style lang="css" scoped>
.login {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  align-items: center;
  gap: 30px;
  padding: 30px 0;
  min-height: calc(100vh - 120px);
}

.intro {
  padding: 20px;
}

.intro-tag {
  color: #2563eb;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 10px;
}

.intro h2 {
  font-size: 30px;
  margin-bottom: 12px;
  color: #1f2937;
}

.intro-description {
  color: #4b5563;
  line-height: 1.7;
  margin-bottom: 20px;
}

.intro-list {
  display: grid;
  gap: 12px;
}

.intro-list-item {
  display: flex;
  align-items: center;
  color: #1f2937;
  gap: 10px;
}

.login-area {
  width: min(420px, 100%);
  justify-self: center;
  overflow: hidden;
  background: white;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.header {
  background: linear-gradient(135deg, #e9f2ff 0%, #f8fbff 100%);
  text-align: center;
  color: black;
  padding: 24px;
}

.header h1 {
  font-size: 30px;
  font-weight: 600;
  margin-bottom: 8px;
}

.header p {
  font-size: 16px;
  opacity: 0.8;
}

.form {
  padding: 24px;
}

.form-row {
  margin-bottom: 18px;
}

.form-row label {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #333333;
}

.input-area {
  position: relative;
}

.input-area .input-box {
  padding-right: 40px;
}

.clear-button {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 1;
  background: #e5e7eb;
  color: #6b7280;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  padding: 0;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.clear-button:hover {
  background: #d1d5db;
  color: #374151;
}

.input-box {
  width: 100%;
  font-size: 14px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.input-box:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.login-button {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: #2563eb;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.2s ease;
}

.login-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.login-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.register-link {
  font-weight: 500;
  text-decoration: none;
  color: #2563eb;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
