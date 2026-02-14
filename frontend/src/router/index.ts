import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Register from "../components/Register.vue";
import UserCenter from "../components/UserCenter.vue";
import ActivityList from "../components/ActivityList.vue";
import SignupRecord from "../components/SignupRecord.vue";
import PointRecord from "../components/PointRecord.vue";
import Exchange from "../components/Exchange.vue";
import ExchangeRecord from "../components/ExchangeRecord.vue";
import Dashboard from "../components/Dashboard.vue";
import ImportData from "../components/admin/ImportData.vue";
import ExportData from "../components/ExportData.vue";
import AdminActivity from "../components/admin/AdminActivity.vue";
import AdminVolunteer from "../components/admin/AdminVolunteer.vue";
import AdminPoints from "../components/admin/AdminPoints.vue";
import AdminExchange from "../components/admin/AdminExchange.vue";

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
    component: Exchange,
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
    meta: { requiresAdmin: true },
  },
  {
    path: "/admin/volunteers",
    name: "AdminVolunteers",
    component: AdminVolunteer,
    meta: { requiresAdmin: true },
  },
  {
    path: "/admin/points",
    name: "AdminPoints",
    component: AdminPoints,
    meta: { requiresAdmin: true },
  },
  {
    path: "/admin/exchange",
    name: "AdminExchange",
    component: AdminExchange,
    meta: { requiresAdmin: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const requiresAdmin = to.matched.some((r) =>
    Boolean(r.meta && r.meta.requiresAdmin),
  );
  const requiresAuth = to.matched.some((r) =>
    Boolean(r.meta && r.meta.requiresAuth),
  );
  if (!requiresAdmin && !requiresAuth) {
    return next();
  }

  const userStr = localStorage.getItem("user");
  if (!userStr) return next("/login");

  type Role = "ADMIN" | "VOLUNTEER" | "USER";
  interface User {
    role?: Role;
  }
  let user: User;
  try {
    user = JSON.parse(userStr) as User;
  } catch (err) {
    console.error("解析用户信息失败:", err);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    return next("/login");
  }

  if (requiresAdmin && user.role !== "ADMIN") {
    return next("/home");
  }

  next();
});

export default router;
