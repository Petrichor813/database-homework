<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>个人资料</h2>
      <p>管理志愿者个人信息与认证状态。</p>
    </header>
    <section class="profile-grid">
      <div class="profile-card">
        <div class="avatar-placeholder">头像占位</div>
        <div class="info">
          <p class="name">张三</p>
          <p class="role">志愿者 · 待审核</p>
          <button>编辑资料</button>
        </div>
      </div>
      <div class="profile-card">
        <h3>基础信息</h3>
        <ul>
          <li>手机号：138****8888</li>
          <li>服务时长：120 小时</li>
          <li>积分余额：260</li>
        </ul>
      </div>
      <div class="profile-card">
        <h3>认证状态</h3>
        <div class="status-block">等待管理员审核</div>
        <button>提交补充材料</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-placeholder {
  height: 120px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name {
  font-weight: 600;
}

.role {
  color: #6b7280;
  font-size: 14px;
}

.status-block {
  background: #fef3c7;
  color: #92400e;
  padding: 8px 12px;
  border-radius: 8px;
  width: fit-content;
}

button {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}
</style>
