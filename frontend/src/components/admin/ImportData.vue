<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useToast } from "../../utils/toast";

type ImportTab = "ACTIVITY" | "ITEM";

type ActivityType =
  | "COMMUNITY_SERVICE"
  | "ENVIRONMENTAL_PROTECTION"
  | "ELDERLY_CARE"
  | "CHILDREN_TUTORING"
  | "DISABILITIES_SUPPORT"
  | "CULTURAL_EVENTS"
  | "HEALTH_PROMOTION"
  | "OTHER";

type ActivityStatus =
  | "RECRUITING"
  | "CONFIRMED"
  | "ONGOING"
  | "COMPLETED"
  | "CANCELLED";

type ItemCategory =
  | "DAILY_NECESSITIES"
  | "BOOKS"
  | "STATIONARY"
  | "FOOD"
  | "COUPON"
  | "OTHER";

type ItemStatus = "AVAILABLE" | "UNAVAILABLE" | "SOLD_OUT" | "DELETED";

const { success, error, info } = useToast();

const activeTab = ref<ImportTab>("ACTIVITY");
const tabs: { label: string; value: ImportTab }[] = [
  { label: "活动导入", value: "ACTIVITY" },
  { label: "商品导入", value: "ITEM" },
];

const activityTypeOptions: { label: string; value: ActivityType }[] = [
  { label: "社区服务", value: "COMMUNITY_SERVICE" },
  { label: "环境保护", value: "ENVIRONMENTAL_PROTECTION" },
  { label: "敬老助老", value: "ELDERLY_CARE" },
  { label: "儿童助学", value: "CHILDREN_TUTORING" },
  { label: "助残服务", value: "DISABILITIES_SUPPORT" },
  { label: "文化活动", value: "CULTURAL_EVENTS" },
  { label: "健康宣传", value: "HEALTH_PROMOTION" },
  { label: "其他", value: "OTHER" },
];

const activityStatusOptions: { label: string; value: ActivityStatus }[] = [
  { label: "招募中", value: "RECRUITING" },
  { label: "已确认", value: "CONFIRMED" },
  { label: "进行中", value: "ONGOING" },
  { label: "已结束", value: "COMPLETED" },
  { label: "已取消", value: "CANCELLED" },
];

const itemCategoryOptions: { label: string; value: ItemCategory }[] = [
  { label: "日用品", value: "DAILY_NECESSITIES" },
  { label: "书籍", value: "BOOKS" },
  { label: "文具", value: "STATIONARY" },
  { label: "食品", value: "FOOD" },
  { label: "优惠券", value: "COUPON" },
  { label: "其他", value: "OTHER" },
];

const itemStatusOptions: { label: string; value: ItemStatus }[] = [
  { label: "可兑换", value: "AVAILABLE" },
  { label: "下架", value: "UNAVAILABLE" },
  { label: "售罄", value: "SOLD_OUT" },
  { label: "已删除", value: "DELETED" },
];

const activityForm = reactive({
  title: "",
  description: "",
  type: "COMMUNITY_SERVICE" as ActivityType,
  location: "",
  startTime: "",
  endTime: "",
  status: "RECRUITING" as ActivityStatus,
  pointsPerHour: 1,
  maxParticipants: 20,
});

const itemForm = reactive({
  name: "",
  description: "",
  price: 100,
  stock: 10,
  category: "DAILY_NECESSITIES" as ItemCategory,
  status: "AVAILABLE" as ItemStatus,
  sortWeight: 0,
});

const activityImage = ref<File | null>(null);
const itemImage = ref<File | null>(null);

const activityImageText = computed(() =>
  activityImage.value
    ? activityImage.value.name
    : "未选择图片（COS 接入后上传）",
);
const itemImageText = computed(() =>
  itemImage.value ? itemImage.value.name : "未选择图片（COS 接入后上传）",
);

const pickImage = (event: Event, type: "activity" | "item") => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0] ?? null;
  if (type === "activity") {
    activityImage.value = file;
  } else {
    itemImage.value = file;
  }
  info("图片上传待接入", "已保留选择入口，后续将接入 COS 上传服务");
};

const validateActivityForm = () => {
  if (!activityForm.title.trim()) {
    error("导入失败", "请填写活动名称");
    return false;
  }
  if (!activityForm.location.trim()) {
    error("导入失败", "请填写活动地点");
    return false;
  }
  if (!activityForm.startTime || !activityForm.endTime) {
    error("导入失败", "请选择活动开始/结束时间");
    return false;
  }
  if (new Date(activityForm.startTime) >= new Date(activityForm.endTime)) {
    error("导入失败", "结束时间必须晚于开始时间");
    return false;
  }
  if (activityForm.pointsPerHour <= 0) {
    error("导入失败", "每小时积分必须大于 0");
    return false;
  }
  if (activityForm.maxParticipants <= 0) {
    error("导入失败", "活动人数上限必须大于 0");
    return false;
  }
  return true;
};

const validateItemForm = () => {
  if (!itemForm.name.trim()) {
    error("导入失败", "请填写商品名称");
    return false;
  }
  if (itemForm.price <= 0) {
    error("导入失败", "兑换积分必须大于 0");
    return false;
  }
  if (itemForm.stock < 0) {
    error("导入失败", "库存不能为负数");
    return false;
  }
  if (itemForm.sortWeight < 0) {
    error("导入失败", "排序权重不能为负数");
    return false;
  }
  return true;
};

const submitActivityImport = () => {
  if (!validateActivityForm()) {
    return;
  }

  success(
    "活动导入表单已提交",
    "当前仅完成前端录入结构，后续可直接接入后端导入接口",
  );
};

const submitItemImport = () => {
  if (!validateItemForm()) {
    return;
  }

  success(
    "商品导入表单已提交",
    "当前仅完成前端录入结构，后续可直接接入后端导入接口",
  );
};
</script>

<template>
  <div class="import-data">
    <header class="header">
      <h2>数据导入</h2>
      <p>管理员可通过表单导入活动和商品数据。</p>
    </header>

    <section class="layout">
      <aside class="tab-menu" aria-label="导入数据导航">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          class="tab-button"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </aside>

      <div class="tab-page">
        <form
          v-if="activeTab === 'ACTIVITY'"
          class="form-card"
          @submit.prevent="submitActivityImport"
        >
          <h3>活动导入</h3>
          <div class="form-grid">
            <label class="form-item">
              <span>活动名称</span>
              <input v-model="activityForm.title" type="text" maxlength="100" />
            </label>

            <label class="form-item">
              <span>活动类型</span>
              <select v-model="activityForm.type">
                <option
                  v-for="option in activityTypeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="form-item full-width">
              <span>活动描述</span>
              <textarea
                v-model="activityForm.description"
                rows="4"
                maxlength="1000"
                placeholder="选填：活动详情、注意事项等"
              />
            </label>

            <label class="form-item full-width">
              <span>活动地点</span>
              <input
                v-model="activityForm.location"
                type="text"
                maxlength="200"
              />
            </label>

            <label class="form-item">
              <span>开始时间</span>
              <input v-model="activityForm.startTime" type="datetime-local" />
            </label>

            <label class="form-item">
              <span>结束时间</span>
              <input v-model="activityForm.endTime" type="datetime-local" />
            </label>

            <label class="form-item">
              <span>活动状态</span>
              <select v-model="activityForm.status">
                <option
                  v-for="option in activityStatusOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="form-item">
              <span>每小时积分</span>
              <input
                v-model.number="activityForm.pointsPerHour"
                type="number"
                min="0.1"
                step="0.1"
              />
            </label>

            <label class="form-item">
              <span>人数上限</span>
              <input
                v-model.number="activityForm.maxParticipants"
                type="number"
                min="1"
              />
            </label>

            <div class="form-item full-width image-upload">
              <span>活动封面图（预留）</span>
              <label class="upload-trigger">
                <input
                  type="file"
                  accept="image/*"
                  @change="pickImage($event, 'activity')"
                />
                选择图片
              </label>
              <p>{{ activityImageText }}</p>
              <small>当前后端尚未接入 COS，暂不执行真实上传。</small>
            </div>
          </div>
          <button class="primary" type="submit">提交活动导入</button>
        </form>

        <form v-else class="form-card" @submit.prevent="submitItemImport">
          <h3>商品导入</h3>
          <div class="form-grid">
            <label class="form-item">
              <span>商品名称</span>
              <input v-model="itemForm.name" type="text" maxlength="50" />
            </label>

            <label class="form-item">
              <span>商品分类</span>
              <select v-model="itemForm.category">
                <option
                  v-for="option in itemCategoryOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="form-item full-width">
              <span>商品描述</span>
              <textarea
                v-model="itemForm.description"
                rows="4"
                maxlength="1000"
                placeholder="选填：商品说明、兑换须知等"
              />
            </label>

            <label class="form-item">
              <span>兑换积分</span>
              <input v-model.number="itemForm.price" type="number" min="1" />
            </label>

            <label class="form-item">
              <span>库存</span>
              <input v-model.number="itemForm.stock" type="number" min="0" />
            </label>

            <label class="form-item">
              <span>商品状态</span>
              <select v-model="itemForm.status">
                <option
                  v-for="option in itemStatusOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="form-item">
              <span>排序权重</span>
              <input
                v-model.number="itemForm.sortWeight"
                type="number"
                min="0"
              />
            </label>

            <div class="form-item full-width image-upload">
              <span>商品图片（预留）</span>
              <label class="upload-trigger">
                <input
                  type="file"
                  accept="image/*"
                  @change="pickImage($event, 'item')"
                />
                选择图片
              </label>
              <p>{{ itemImageText }}</p>
              <small>当前后端尚未接入 COS，暂不执行真实上传。</small>
            </div>
          </div>
          <button class="primary" type="submit">提交商品导入</button>
        </form>
      </div>
    </section>
  </div>
</template>

<style scoped>
.import-data {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header p {
  color: #6b7280;
}

.layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  align-items: start;
  gap: 20px;
}

.tab-menu {
  display: flex;
  flex-direction: column;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 8px;
  gap: 8px;
}

.tab-button {
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  color: #475569;
  border: none;
  border-radius: 8px;
  padding: 10px 12px;
  transition: all 0.2s ease;
}

.tab-button.active {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.tab-page {
  min-width: 0;
}

.form-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #334155;
}

.form-item.full-width {
  grid-column: 1 / -1;
}

.form-item input,
.form-item select,
.form-item textarea {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 9px 10px;
  outline: none;
  transition: all 0.2s ease;
}

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.image-upload p {
  margin: 0;
  color: #64748b;
}

.image-upload small {
  color: #94a3b8;
}

.upload-trigger {
  position: relative;
  width: fit-content;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  cursor: pointer;
  color: #334155;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 12px;
}

.upload-trigger input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.primary {
  width: fit-content;
  background: #2563eb;
  cursor: pointer;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 9px 14px;
}
</style>
