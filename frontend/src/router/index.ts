import { createRouter, createWebHistory } from "vue-router";
import Home from "../components/Home.vue";
import UserCenter from "../components/UserCenter.vue";
import ActivityList from "../components/activity/ActivityList.vue";
import SignupRecord from "../components/activity/SignupRecord.vue";
import AdminActivity from "../components/admin/AdminActivity.vue";
import AdminExchange from "../components/admin/AdminExchange.vue";
import AdminPoints from "../components/admin/AdminPoints.vue";
import AdminVolunteer from "../components/admin/AdminVolunteer.vue";
import ImportData from "../components/admin/ImportData.vue";
import Login from "../components/auth/Login.vue";
import Register from "../components/auth/Register.vue";
import Dashboard from "../components/data/Dashboard.vue";
import ExportData from "../components/data/ExportData.vue";
import Exchange from "../components/exchange/Exchange.vue";
import ExchangeRecord from "../components/exchange/ExchangeRecord.vue";

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
    meta: { requiresAuth: true },
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
    meta: { requiresAuth: true },
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
    meta: { requiresAuth: true },
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
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/data-export",
    name: "DataExport",
    component: ExportData,
    meta: { requiresAuth: true},
  },
  {
    path: "/admin/activities",
    name: "AdminActivities",
    component: AdminActivity,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/volunteers",
    name: "AdminVolunteers",
    component: AdminVolunteer,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/points",
    name: "AdminPoints",
    component: AdminPoints,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/exchange",
    name: "AdminExchange",
    component: AdminExchange,
    meta: { requiresAuth: true, requiresAdmin: true },
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
  const token = localStorage.getItem("token");
  if (!userStr || !token) return next("/login");

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
