import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import HomePage from "../components/HomePage.vue";
import RegisterPage from "../components/RegisterPage.vue";

const routes = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/home",
    name: "Home",
    component: HomePage,
  },
  {
    path: "/login",
    name: "Login",
    component: LoginPage,
  },
  {
    path: "/register",
    name: "Register",
    component: RegisterPage,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const userStr = localStorage.getItem("user");

    if (!userStr) {
      next("/login");
      return;
    }

    try {
      const user = JSON.parse(userStr);

      if (to.meta.role && user.role !== to.meta.role) {
        switch (user.role) {
          case "ADMIN":
            next("/admin");
            break;
          case "VOLUNTEER":
            next("/volunteer");
            break;
          case "USER":
          default:
            next("/user");
        }
        return;
      }
    } catch (error) {
      console.error("解析用户信息失败:", error);
      localStorage.removeItem("user");
      localStorage.removeItem("token");
      next("/login");
      return;
    }
  }

  next();
});

export default router;
