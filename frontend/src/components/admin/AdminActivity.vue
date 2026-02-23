<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>活动管理</h2>
      <p>发布、编辑、上下架活动（占位）。</p>
    </header>
    <section class="toolbar">
      <button class="primary">新建活动</button>
      <button>批量导入</button>
    </section>
    <section class="table-card">
      <div class="table-header">
        <span>活动名称</span>
        <span>时间</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div v-for="item in activities" :key="item.id" class="table-row">
        <span>{{ item.title }}</span>
        <span>{{ formatDateTime(item.startTime) }}</span>
        <span>{{ getStatusText(item.status) }}</span>
        <div class="actions">
          <button type="button" @click="openDetailDialog(item)">编辑</button>
          <button type="button" @click="openDetailDialog(item)">查看报名</button>
        </div>
      </div>
    </section>
    
    <!-- 活动详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>活动详情</h3>
        <p><strong>活动名称：</strong>{{ curActivity?.title }}</p>
        <p><strong>活动类型：</strong>{{ getTypeText(curActivity?.type || 'OTHER') }}</p>
        <p><strong>活动地点：</strong>{{ curActivity?.location }}</p>
        <p><strong>开始时间：</strong>{{ formatDateTime(curActivity?.startTime || '') }}</p>
        <p><strong>结束时间：</strong>{{ formatDateTime(curActivity?.endTime || '') }}</p>
        <p><strong>活动状态：</strong>{{ getStatusText(curActivity?.status || 'RECRUITING') }}</p>
        <p><strong>活动描述：</strong>{{ curActivity?.description || "暂无描述" }}</p>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeDetailDialog">
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

type ActivityStatus = "RECRUITING" | "CONFIRMED" | "ONGOING" | "COMPLETED" | "CANCELLED";
type ActivityType = "COMMUNITY_SERVICE" | "ENVIRONMENTAL_PROTECTION" | "ELDERLY_CARE" | "CHILDREN_TUTORING" | "DISABILITIES_SUPPORT" | "CULTURAL_EVENTS" | "HEALTH_PROMOTION" | "OTHER";

interface Activity {
  id: number;
  title: string;
  description: string;
  type: ActivityType;
  location: string;
  startTime: string;
  endTime: string;
  status: ActivityStatus;
}

const activities = ref<Activity[]>([
  { 
    id: 1,
    title: "社区环境清洁行动", 
    description: "组织志愿者清理社区垃圾，美化环境",
    type: "ENVIRONMENTAL_PROTECTION",
    location: "社区中心广场",
    startTime: "2025-04-12T09:00:00",
    endTime: "2025-04-12T12:00:00",
    status: "RECRUITING" 
  },
  { 
    id: 2,
    title: "银龄陪伴计划", 
    description: "陪伴社区老人，提供生活帮助和精神慰藉",
    type: "ELDERLY_CARE",
    location: "阳光养老院",
    startTime: "2025-04-20T14:00:00",
    endTime: "2025-04-20T17:00:00",
    status: "ONGOING" 
  },
]);

// 活动详情弹窗
const showDetailDialog = ref(false);
const curActivity = ref<Activity | null>(null);

// 打开详情弹窗
const openDetailDialog = (activity: Activity) => {
  curActivity.value = activity;
  showDetailDialog.value = true;
};

// 关闭详情弹窗
const closeDetailDialog = () => {
  curActivity.value = null;
  showDetailDialog.value = false;
};

// 获取状态文本
const getStatusText = (status: ActivityStatus) => {
  switch (status) {
    case "RECRUITING":
      return "报名中";
    case "CONFIRMED":
      return "已确认";
    case "ONGOING":
      return "进行中";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    default:
      return status;
  }
};

// 获取类型文本
const getTypeText = (type: ActivityType) => {
  switch (type) {
    case "COMMUNITY_SERVICE":
      return "社区服务";
    case "ENVIRONMENTAL_PROTECTION":
      return "环境保护";
    case "ELDERLY_CARE":
      return "敬老助老";
    case "CHILDREN_TUTORING":
      return "儿童助学";
    case "DISABILITIES_SUPPORT":
      return "助残服务";
    case "CULTURAL_EVENTS":
      return "文化活动";
    case "HEALTH_PROMOTION":
      return "健康宣传";
    case "OTHER":
      return "其他";
    default:
      return type;
  }
};

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.toolbar {
  display: flex;
  gap: 12px;
}

.toolbar button {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  background: white;
  cursor: pointer;
}

.toolbar .primary {
  background: #2563eb;
  color: white;
  border: none;
}

.table-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-header,
.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 14px 16px;
  align-items: center;
}

.table-header {
  background: #f8fafc;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

.table-row {
  border-top: 1px solid #e5e7eb;
}

.actions {
  display: flex;
  gap: 8px;
}

.actions button {
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
}

/* 弹窗样式 */
.dialog-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 2;
}

.dialog-area {
  display: flex;
  flex-direction: column;
  width: min(420px, 90vw);
  background: white;
  padding: 20px;
  gap: 12px;
  border-radius: 12px;
}

.dialog-area h3 {
  text-align: center;
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.dialog-actions button {
  min-width: 80px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.dialog-actions button:hover {
  cursor: pointer;
  transform: translateY(-1px);
}

.close-button {
  background: #2563eb;
  color: white;
  border: none;
}

.close-button:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}
</style>