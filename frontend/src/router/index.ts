import { createRouter, createWebHistory } from "vue-router";
import LoginPage from "../components/LoginPage.vue";
import HomePage from "../components/HomePage.vue";
import RegisterPage from "../components/RegisterPage.vue";
import ActivityListPage from "../components/ActivityListPage.vue";
import SignupRecordsPage from "../components/SignupRecordsPage.vue";
import ProfilePage from "../components/ProfilePage.vue";
import PointsPage from "../components/PointsPage.vue";
import ExchangePage from "../components/ExchangePage.vue";
import DashboardPage from "../components/DashboardPage.vue";
import DataImportPage from "../components/DataImportPage.vue";
import DataExportPage from "../components/DataExportPage.vue";
import AdminActivityPage from "../components/AdminActivityPage.vue";
import AdminVolunteerPage from "../components/AdminVolunteerPage.vue";
import AdminPointsPage from "../components/AdminPointsPage.vue";
import AdminExchangePage from "../components/AdminExchangePage.vue";

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
  {
    path: "/activities",
    name: "Activities",
    component: ActivityListPage,
  },
  {
    path: "/signups",
    name: "Signups",
    component: SignupRecordsPage,
  },
  {
    path: "/profile",
    name: "Profile",
    component: ProfilePage,
  },
  {
    path: "/points",
    name: "Points",
    component: PointsPage,
  },
  {
    path: "/exchange",
    name: "Exchange",
    component: ExchangePage,
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: DashboardPage,
  },
  {
    path: "/data-import",
    name: "DataImport",
    component: DataImportPage,
  },
  {
    path: "/data-export",
    name: "DataExport",
    component: DataExportPage,
  },
  {
    path: "/admin/activities",
    name: "AdminActivities",
    component: AdminActivityPage,
  },
  {
    path: "/admin/volunteers",
    name: "AdminVolunteers",
    component: AdminVolunteerPage,
  },
  {
    path: "/admin/points",
    name: "AdminPoints",
    component: AdminPointsPage,
  },
  {
    path: "/admin/exchange",
    name: "AdminExchange",
    component: AdminExchangePage,
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
