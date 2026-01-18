<template>
  <div class="register-page">
    <div class="register-card">
      <header>
        <h2>志愿服务平台注册</h2>
        <p>完善信息后即可参与活动与积分管理</p>
      </header>
      <form class="register-form">
        <div class="form-row">
          <label>用户名</label>
          <input type="text" placeholder="请输入用户名" />
        </div>
        <div class="form-row">
          <label>手机号</label>
          <input type="text" placeholder="请输入手机号" />
        </div>
        <div class="form-row">
          <label>密码</label>
          <input type="password" placeholder="请输入密码" />
        </div>
        <div class="form-row">
          <label>确认密码</label>
          <input type="password" placeholder="再次输入密码" />
        </div>
        <button type="button">提交注册</button>
      </form>
      <footer>
        <span>已有账号？</span>
        <router-link to="/login">返回登录</router-link>
      </footer>
    </div>
  </div>
</template>

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

.register-form button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 12px;
  font-weight: 600;
  cursor: pointer;
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
