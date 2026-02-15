<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useToast } from "../../utils/toast";
import { postJson } from "../../utils/api";

const { success, error } = useToast();

const account_info = reactive({
  username: "",
  realName: "",
  phone: "",
  password: "",
  confirmPassword: "",
  userRole: "USER",
  isVolunteer: false,
});

const loading = ref(false);

const updatedRole = computed(() => {
  if (account_info.userRole === "ADMIN") {
    return "ADMIN";
  }

  return "USER";
});

watch(
  () => account_info.userRole,
  (value) => {
    if (value === "ADMIN") {
      account_info.realName = "";
      account_info.isVolunteer = false;
    }
  },
);

const handleRegister = async () => {
  if (!account_info.username.trim() || !account_info.password.trim()) {
    error("注册失败", "请输入用户名和密码");
    return;
  }

  if (account_info.password !== account_info.confirmPassword) {
    error("注册失败", "两次输入的密码不一致");
    return;
  }

  loading.value = true;

  try {
    await postJson("/api/auth/register", {
      username: account_info.username,
      password: account_info.password,
      role: updatedRole.value,
      phone: account_info.phone,
      requestVolunteer: account_info.isVolunteer,
      ...(account_info.isVolunteer ? { realName: account_info.realName } : {}),
    });
    success("注册成功");
  } catch (err) {
    const msg = err instanceof Error ? err.message : "请检查您的注册信息";
    error("注册失败", msg);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="register">
    <div class="card">
      <header>
        <h2>用户注册</h2>
        <p>注册账号并登录后即可参与活动、获取积分</p>
      </header>

      <form @submit.prevent="handleRegister">
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
          <label for="phone">手机号</label>
          <div class="input-area">
            <input
              id="username"
              v-model="account_info.phone"
              type="text"
              placeholder="请输入手机号"
              required
              class="input-box"
            />
            <button
              v-if="account_info.phone"
              type="button"
              class="clear-button"
              @click="account_info.phone = ''"
            >
              ×
            </button>
          </div>
        </div>

        <div class="form-row">
          <label for="password">密码</label>
          <div class="input-area">
            <input
              id="username"
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

        <div class="form-row">
          <label for="confirm-password">确认密码</label>
          <div class="input-area">
            <input
              id="username"
              v-model="account_info.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              required
              class="input-box"
            />
            <button
              v-if="account_info.confirmPassword"
              type="button"
              class="clear-button"
              @click="account_info.confirmPassword = ''"
            >
              ×
            </button>
          </div>
        </div>

        <div class="form-row">
          <label for="user-role">注册身份</label>
          <div class="check-role">
            <label for="normal">
              <input
                id="user-role"
                v-model="account_info.userRole"
                type="radio"
                value="USER"
                checked
              />
              <span>普通用户</span>
            </label>
            <label for="normal">
              <input
                id="user-role"
                v-model="account_info.userRole"
                type="radio"
                value="ADMIN"
              />
              <span>管理员</span>
            </label>
          </div>
        </div>

        <div v-if="account_info.userRole === 'USER'">
          <div class="form-row">
            <label for="volunteer-option-tag">志愿者身份</label>
            <label for="volunteer-option" class="check-volunteer">
              <input
                id="volunteer-role"
                v-model="account_info.isVolunteer"
                type="checkbox"
              />
              <span>申请成为志愿者</span>
            </label>
          </div>

          <div v-if="account_info.isVolunteer" class="form-row">
            <label for="real-name">真实姓名</label>
            <div class="input-area">
              <input
                id="username"
                v-model="account_info.realName"
                type="text"
                placeholder="请输入真实姓名"
                required
                class="input-box"
              />
              <button
                v-if="account_info.realName"
                type="button"
                class="clear-button"
                @click="account_info.realName = ''"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <button type="submit" :disabled="loading" class="register-button">
          {{ loading ? "注册中……" : "提交注册" }}
        </button>
      </form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped lang="css">
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

.card {
  display: flex;
  flex-direction: column;
  width: min(420px, 100%);
  background: white;
  border-radius: 16px;
  padding: 28px;
  gap: 20px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.card header h2 {
  text-align: center;
  margin: 0 0 8px;
}

.card header p {
  font-size: 14px;
  color: #6b7280;
  text-align: center;
  margin: 0;
}

form {
  display: grid;
  gap: 16px;
}

.form-row {
  display: grid;
  gap: 8px;
}

.form-row label {
  font-size: 16px;
  color: #374151;
}

.input-area {
  display: flex;
  align-items: center;
  position: relative;
}

.input-area .input-box {
  flex: 1;
  padding-right: 40px;
}

.input-area .clear-button {
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

.input-area .clear-button:hover {
  background: #d1d5db;
  color: #374151;
}

.check-role {
  display: flex;
  gap: 15px;
  margin-top: 4px;
}

.check-role label {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 10px;
}

.check-role input {
  margin-right: 8px;
}

.check-role span {
  font-size: 14px;
  color: #555555;
}

.check-volunteer {
  display: inline-flex;
  align-items: center;
  color: #374151;
  gap: 8px;
}

.check-volunteer span {
  font-size: 14px;
}

.register-footer {
  display: flex;
  justify-content: center;
  font-size: 14px;
  color: #6b7280;
  gap: 8px;
}

.register-button {
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
  transition: background 0.3s;
}

.register-button:hover {
  background: #1d4ed8;
}

.register-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.login-link {
  font-weight: 500;
  text-decoration: none;
  color: #2563eb;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
