import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Register from "../components/Register.vue";
import UserCenter from "../components/UserCenter.vue";
import ActivityList from "../components/ActivityList.vue";
import SignupRecord from "../components/SignupRecord.vue";
import PointRecord from "../components/PointRecord.vue";
import ExchangePage from "../components/ExchangePage.vue";
import ExchangeRecord from "../components/ExchangeRecord.vue";
import Dashboard from "../components/Dashboard.vue";
import ImportData from "../components/ImportData.vue";
import ExportData from "../components/ExportData.vue";
import AdminActivity from "../components/AdminActivity.vue";
import AdminVolunteer from "../components/AdminVolunteer.vue";
import AdminPoints from "../components/AdminPoints.vue";
import AdminExchange from "../components/AdminExchange.vue";

const routes = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/home",
    name: "Home",
    component: Home,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/user-center",
    name: "UserCenter",
    component: UserCenter,
  },
  {
    path: "/activities",
    name: "Activities",
    component: ActivityList,
  },
  {
    path: "/signups",
    name: "Signups",
    component: SignupRecord,
  },
  {
    path: "/points",
    name: "Points",
    component: PointRecord,
  },
  {
    path: "/exchange",
    name: "Exchange",
    component: ExchangePage,
  },
  {
    path: "/exchange-records",
    name: "ExchangeRecord",
    component: ExchangeRecord,
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: Dashboard,
  },
  {
    path: "/data-import",
    name: "DataImport",
    component: ImportData,
  },
  {
    path: "/data-export",
    name: "DataExport",
    component: ExportData,
  },
  {
    path: "/admin/activities",
    name: "AdminActivities",
    component: AdminActivity,
  },
  {
    path: "/admin/volunteers",
    name: "AdminVolunteers",
    component: AdminVolunteer,
  },
  {
    path: "/admin/points",
    name: "AdminPoints",
    component: AdminPoints,
  },
  {
    path: "/admin/exchange",
    name: "AdminExchange",
    component: AdminExchange,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
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
