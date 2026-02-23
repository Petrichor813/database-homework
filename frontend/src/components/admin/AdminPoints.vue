<template>
  <div class="page-shell">
    <header class="page-header">
      <h2>积分管理</h2>
      <p>审核积分发放与调整记录（占位）。</p>
    </header>
    <section class="toolbar">
      <button class="primary">新增积分调整</button>
      <button>导出记录</button>
    </section>
    <section class="table-card">
      <div class="table-header">
        <span>志愿者</span>
        <span>变动</span>
        <span>原因</span>
        <span>时间</span>
        <span>操作</span>
      </div>
      <div v-for="record in records" :key="record.id" class="table-row">
        <span>{{ record.volunteerName }}</span>
        <span
          >{{ record.changeAmount > 0 ? "+" : ""
          }}{{ record.changeAmount }}</span
        >
        <span>{{ record.reason }}</span>
        <span>{{ formatDateTime(record.createTime) }}</span>
        <div class="actions">
          <button type="button" @click="openDetailDialog(record)">
            查看详情
          </button>
        </div>
      </div>
    </section>

    <!-- 积分记录详情弹窗 -->
    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-area">
        <h3>积分变动详情</h3>
        <p><strong>志愿者：</strong>{{ curRecord?.volunteerName }}</p>
        <p>
          <strong>变动类型：</strong>
          {{ getChangeTypeText(curRecord?.changeType || "") }}
        </p>
        <p>
          <strong>变动数量：</strong
          >{{ (curRecord?.changeAmount || 0) > 0 ? "+" : ""
          }}{{ curRecord?.changeAmount || 0 }}
        </p>
        <p><strong>变动原因：</strong>{{ curRecord?.reason }}</p>
        <p>
          <strong>关联记录类型：</strong
          >{{ getRelatedRecordTypeText(curRecord?.relatedRecordType || "") }}
        </p>
        <p><strong>关联记录ID：</strong>{{ curRecord?.relatedRecordId }}</p>
        <p>
          <strong>操作时间：</strong
          >{{ formatDateTime(curRecord?.createTime || "") }}
        </p>
        <p><strong>操作人：</strong>{{ curRecord?.operatorName }}</p>
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

interface PointRecord {
  id: number;
  volunteerName: string;
  volunteerId: number;
  changeType: "EARN" | "SPEND" | "ADJUST";
  changeAmount: number;
  reason: string;
  relatedRecordType: "ACTIVITY" | "EXCHANGE" | "MANUAL";
  relatedRecordId: number;
  createTime: string;
  operatorName: string;
}

const records = ref<PointRecord[]>([
  {
    id: 1,
    volunteerName: "张三",
    volunteerId: 1001,
    changeType: "EARN",
    changeAmount: 40,
    reason: "参与社区环境清洁活动",
    relatedRecordType: "ACTIVITY",
    relatedRecordId: 2001,
    createTime: "2025-04-10T14:30:00",
    operatorName: "系统",
  },
  {
    id: 2,
    volunteerName: "李同学",
    volunteerId: 1002,
    changeType: "SPEND",
    changeAmount: -20,
    reason: "兑换环保礼包",
    relatedRecordType: "EXCHANGE",
    relatedRecordId: 3001,
    createTime: "2025-04-08T10:15:00",
    operatorName: "系统",
  },
]);

// 积分记录详情弹窗
const showDetailDialog = ref(false);
const curRecord = ref<PointRecord | null>(null);

// 打开详情弹窗
const openDetailDialog = (record: PointRecord) => {
  curRecord.value = record;
  showDetailDialog.value = true;
};

// 关闭详情弹窗
const closeDetailDialog = () => {
  curRecord.value = null;
  showDetailDialog.value = false;
};

// 获取变动类型文本
const getChangeTypeText = (type: string) => {
  switch (type) {
    case "EARN":
      return "获得";
    case "SPEND":
      return "消费";
    case "ADJUST":
      return "调整";
    default:
      return type;
  }
};

// 获取关联记录类型文本
const getRelatedRecordTypeText = (type: string) => {
  switch (type) {
    case "ACTIVITY":
      return "活动";
    case "EXCHANGE":
      return "兑换";
    case "MANUAL":
      return "手动调整";
    default:
      return type;
  }
};

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
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
  grid-template-columns: repeat(5, 1fr);
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
