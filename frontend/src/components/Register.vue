<template>
  <div class="register-page">
    <div class="register-card">
      <header>
        <h2>志愿服务平台注册</h2>
        <p>完善信息后即可参与活动与积分管理</p>
      </header>
      <form class="register-form" @submit.prevent="handleRegister">
        <div class="form-row">
          <label>用户名</label>
          <div class="input-with-action">
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
            />
            <button
              v-if="form.username"
              type="button"
              class="clear-button"
              @click="form.username = ''"
            >
              ×
            </button>
          </div>
        </div>
        <div class="form-row">
          <label>手机号</label>
          <div class="input-with-action">
            <input
              v-model="form.phone"
              type="text"
              placeholder="请输入手机号"
            />
            <button
              v-if="form.phone"
              type="button"
              class="clear-button"
              @click="form.phone = ''"
            >
              ×
            </button>
          </div>
        </div>
        <div class="form-row">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </div>
        <div class="form-row">
          <label>确认密码</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
          />
        </div>
        <div class="form-row">
          <label>注册身份</label>
          <div class="role-options">
            <label class="role-option">
              <input
                v-model="form.primaryRole"
                type="radio"
                name="primary-role"
                value="USER"
                checked
              />
              <span>普通用户</span>
            </label>
            <label class="role-option">
              <input
                v-model="form.primaryRole"
                type="radio"
                name="primary-role"
                value="ADMIN"
              />
              <span>管理员</span>
            </label>
          </div>
        </div>
        <div v-if="form.primaryRole === 'USER'">
          <div class="form-row">
            <label>志愿者身份</label>
            <label class="volunteer-option">
              <input v-model="form.isVolunteer" type="checkbox" />
              <span>申请成为志愿者</span>
            </label>
          </div>
          <div v-if="form.isVolunteer" class="form-row">
            <label>真实姓名</label>
            <div class="input-with-action">
              <input
                v-model="form.realName"
                type="text"
                placeholder="请输入真实姓名"
              />
              <button
                v-if="form.realName"
                type="button"
                class="clear-button"
                @click="form.realName = ''"
              >
                ×
              </button>
            </div>
          </div>
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? "注册中..." : "提交注册" }}
        </button>
      </form>
      <footer>
        <span>已有账号？</span>
        <router-link to="/login">返回登录</router-link>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { postJson } from "../utils/api";
import { useToast } from "../utils/toast";

const router = useRouter();
const { success, error } = useToast();

const form = reactive({
  username: "",
  phone: "",
  password: "",
  confirmPassword: "",
  primaryRole: "USER",
  isVolunteer: false,
  realName: "",
});

const loading = ref(false);

const resolvedRole = computed(() => {
  if (form.primaryRole === "ADMIN") return "ADMIN";
  return "USER";
});

watch(
  () => form.primaryRole,
  (value) => {
    if (value === "ADMIN") {
      form.isVolunteer = false;
      form.realName = "";
    }
  }
);

const handleRegister = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    error("注册失败", "请输入用户名和密码");
    return;
  }

  if (form.password !== form.confirmPassword) {
    error("注册失败", "两次输入的密码不一致");
    return;
  }

  loading.value = true;

  try {
    await postJson("/api/auth/register", {
      username: form.username,
      password: form.password,
      role: resolvedRole.value,
      phone: form.phone,
      requestVolunteer: form.isVolunteer,
      ...(form.isVolunteer ? { realName: form.realName } : {}),
    });

    success("注册成功");
    await router.push("/login");
  } catch (err) {
    const message = err instanceof Error ? err.message : "注册失败，请稍后重试";
    error("注册失败", message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

.register-card {
  width: min(420px, 100%);
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.register-card header h2 {
  margin: 0 0 8px;
}

.register-card header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.register-form {
  display: grid;
  gap: 16px;
}

.form-row {
  display: grid;
  gap: 8px;
}

.form-row label {
  font-size: 14px;
  color: #374151;
}

.form-row input {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
}

.input-with-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-with-action input {
  flex: 1;
}

.clear-button {
  border: none;
  background: #e5e7eb;
  color: #6b7280;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: background 0.2s ease, color 0.2s ease;
}

.clear-button:hover {
  background: #d1d5db;
  color: #374151;
}

.role-options {
  display: flex;
  gap: 15px;
  margin-top: 4px;
}

.role-option {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.volunteer-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
}

.volunteer-option input {
  margin: 0;
}

.role-option input {
  margin-right: 8px;
}

.role-option span {
  font-size: 14px;
  color: #555;
}

.register-form button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 12px;
  font-weight: 600;
  cursor: pointer;
}

.register-form button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.register-card footer {
  display: flex;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.register-card footer a {
  color: #2563eb;
  text-decoration: none;
}
</style>
