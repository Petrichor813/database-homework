import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: LoginPage
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫（权限控制）
router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const userStr = localStorage.getItem('user')
    
    if (!userStr) {
      // 未登录，跳转到登录页
      next('/login')
      return
    }

    try {
      const user = JSON.parse(userStr)
      
      // 检查角色权限
      if (to.meta.role && user.role !== to.meta.role) {
        // 角色不匹配，根据用户角色跳转到对应首页
        switch (user.role) {
          case 'ADMIN':
            next('/admin')
            break
          case 'VOLUNTEER':
            next('/volunteer')
            break
          case 'USER':
          default:
            next('/user')
        }
        return
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      next('/login')
      return
    }
  }

  next()
})

export default router
