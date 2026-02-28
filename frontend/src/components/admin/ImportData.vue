<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useToast } from "../../utils/toast";
import { getJson, postJson } from "../../utils/api";
import COS from "cos-js-sdk-v5";

type ImportTab = "ACTIVITY" | "PRODUCT";

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

type ProductStatus = "AVAILABLE" | "SOLD_OUT" | "DELETED";

type ProductType =
  | "DAILY_NECESSITIES"
  | "BOOKS"
  | "STATIONARY"
  | "FOOD"
  | "COUPON"
  | "OTHER";

interface CosCredentials {
  tmpSecretId: string;
  tmpSecretKey: string;
  sessionToken: string;
}

interface StsCredentialResponse {
  credentials: CosCredentials;
  startTime: number;
  expiredTime: number;
  bucket: string;
  region: string;
}

const { success, error, info } = useToast();

const activeTab = ref<ImportTab>("ACTIVITY");
const tabs: { label: string; value: ImportTab }[] = [
  { label: "活动导入", value: "ACTIVITY" },
  { label: "商品导入", value: "PRODUCT" },
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

const productTypeOptions: { label: string; value: ProductType }[] = [
  { label: "日用品", value: "DAILY_NECESSITIES" },
  { label: "书籍", value: "BOOKS" },
  { label: "文具", value: "STATIONARY" },
  { label: "食品", value: "FOOD" },
  { label: "优惠券", value: "COUPON" },
  { label: "其他", value: "OTHER" },
];

const productStatusOptions: { label: string; value: ProductStatus }[] = [
  { label: "可兑换", value: "AVAILABLE" },
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

const productForm = reactive({
  name: "",
  description: "",
  price: 100,
  stock: 10,
  category: "DAILY_NECESSITIES" as ProductType,
  status: "AVAILABLE" as ProductStatus,
  sortWeight: 0,
});

const productImage = ref<File | null>(null);
const productImageUrl = ref<string>("");

const productImageText = computed(() => {
  if (productImageUrl.value) {
    return `已上传: ${productImage.value?.name || "图片"}`;
  } else if (productImage.value) {
    return `已选择: ${productImage.value.name}`;
  } else {
    return "未选择图片";
  }
});

const uploadImageToCos = async (file: File): Promise<string> => {
  try {
    const credential = (await getJson(
      "/api/cos-sts/credential"
    )) as StsCredentialResponse;

    const cosClient = new COS({
      getAuthorization: function (_options, callback) {
        callback({
          TmpSecretId: credential.credentials.tmpSecretId,
          TmpSecretKey: credential.credentials.tmpSecretKey,
          XCosSecurityToken: credential.credentials.sessionToken,
          StartTime: credential.startTime,
          ExpiredTime: credential.expiredTime,
        });
      },
    });

    const fileExtension = file.name.split(".").pop() || "jpg";
    const fileName = `images/${Date.now()}-${Math.random()
      .toString(36)
      .substring(2)}.${fileExtension}`;

    await new Promise<void>((resolve, reject) => {
      cosClient.putObject(
        {
          Bucket: credential.bucket,
          Region: credential.region,
          Key: fileName,
          Body: file,
        },
        function (err, _data) {
          if (err) {
            reject(err);
          } else {
            resolve();
          }
        }
      );
    });

    const imageUrl = `https://${credential.bucket}.cos.${credential.region}.myqcloud.com/${fileName}`;
    return imageUrl;
  } catch (err) {
    const msg = err instanceof Error ? err.message : "未知错误";
    error("上传失败", msg);
    throw new Error(msg);
  }
};

const pickImage = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0] ?? null;

  if (!file) {
    target.value = "";
    return;
  }

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    error("上传失败", "请选择图片文件");
    target.value = "";
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    error("上传失败", "图片大小不能超过5MB");
    target.value = "";
    return;
  }

  try {
    info("上传中", "正在上传图片，请稍候...");
    const imageUrl = await uploadImageToCos(file);
    productImage.value = file;
    productImageUrl.value = imageUrl;
    success("上传成功", "图片已成功上传到腾讯云COS");
  } catch (err) {
    const msg = err instanceof Error ? err.message : "图片上传失败";
    error("上传失败", msg);
    target.value = "";
    productImage.value = null;
  }
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

const validateProductForm = () => {
  if (!productForm.name.trim()) {
    error("导入失败", "请填写商品名称");
    return false;
  }
  if (productForm.price <= 0) {
    error("导入失败", "兑换积分必须大于 0");
    return false;
  }
  if (productForm.stock < 0) {
    error("导入失败", "库存不能为负数");
    return false;
  }
  if (productForm.sortWeight < 0) {
    error("导入失败", "排序权重不能为负数");
    return false;
  }
  return true;
};

const submitActivityImport = async () => {
  if (!validateActivityForm()) {
    return;
  }

  try {
    const request = {
      title: activityForm.title,
      description: activityForm.description,
      type: activityForm.type,
      location: activityForm.location,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime,
      status: activityForm.status,
      pointsPerHour: activityForm.pointsPerHour,
      maxParticipants: activityForm.maxParticipants,
    };

    await postJson("/api/admin/activity/import", request);
    success("导入活动数据成功", `已导入活动 ${activityForm.title}`);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "未知错误";
    error("导入活动数据失败", msg);
  }
};

const submitProductImport = async () => {
  if (!validateProductForm()) {
    return;
  }

  try {
    const request = {
      name: productForm.name,
      description: productForm.description,
      price: productForm.price,
      stock: productForm.stock,
      category: productForm.category,
      status: productForm.status,
      sortWeight: productForm.sortWeight,
      imageUrl: productImageUrl.value || "",
    };

    await postJson("/api/admin/product/import", request);
    success("导入商品数据成功", `已导入商品 ${productForm.name}`);
    resetProductForm();
  } catch (err) {
    const msg = err instanceof Error ? err.message : "未知错误";
    error("导入商品数据失败", msg);
  }
};

const resetProductForm = () => {
  productForm.name = "";
  productForm.description = "";
  productForm.price = 100;
  productForm.stock = 10;
  productForm.category = "DAILY_NECESSITIES";
  productForm.status = "AVAILABLE";
  productForm.sortWeight = 0;
  productImage.value = null;
  productImageUrl.value = "";

  const fileInput = document.querySelector(
    'input[type="file"]'
  ) as HTMLInputElement;
  if (fileInput) {
    fileInput.value = "";
  }
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
          </div>
          <button class="submit-button" type="submit">提交活动导入</button>
        </form>

        <form v-else class="form-card" @submit.prevent="submitProductImport">
          <h3>商品导入</h3>
          <div class="form-grid">
            <label class="form-item">
              <span>商品名称</span>
              <input v-model="productForm.name" type="text" maxlength="50" />
            </label>

            <label class="form-item">
              <span>商品分类</span>
              <select v-model="productForm.category">
                <option
                  v-for="option in productTypeOptions"
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
                v-model="productForm.description"
                rows="4"
                maxlength="1000"
                placeholder="选填：商品说明、兑换须知等"
              />
            </label>

            <label class="form-item">
              <span>兑换积分</span>
              <input v-model.number="productForm.price" type="number" min="1" />
            </label>

            <label class="form-item">
              <span>库存</span>
              <input v-model.number="productForm.stock" type="number" min="0" />
            </label>

            <label class="form-item">
              <span>商品状态</span>
              <select v-model="productForm.status">
                <option
                  v-for="option in productStatusOptions"
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
                v-model.number="productForm.sortWeight"
                type="number"
                min="0"
              />
            </label>

            <div class="form-item full-width image-upload">
              <span>商品图片</span>
              <label class="upload-trigger">
                <input
                  type="file"
                  accept="image/*"
                  @change="pickImage($event)"
                />
                <span>选择图片</span>
              </label>
              <p>{{ productImageText }}</p>
              <div v-if="productImageUrl" class="image-preview">
                <img :src="productImageUrl" alt="商品图片预览" />
                <p>图片URL: {{ productImageUrl }}</p>
              </div>
              <small>支持jpg、png等常见图片格式，大小不超过5MB</small>
            </div>
          </div>
          <button class="submit-button" type="submit">提交商品导入</button>
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

.tab-button:hover {
  background: #f8fafc;
  color: black;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.tab-button.active {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.tab-button.active:hover {
  background: #dbeafe;
  color: #1e40af;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
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

.form-item input:hover,
.form-item input:focus,
.form-item select:hover,
.form-item select:focus,
.form-item textarea:hover,
.form-item textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
  border-width: 2px;
}

.form-item select {
  cursor: pointer;
}

.form-item textarea {
  resize: vertical;
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
  justify-content: center;
  align-items: center;
  background: #f8fafc;
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

.upload-trigger span {
  font-size: 16px;
}

.submit-button {
  width: fit-content;
  background: #2563eb;
  cursor: pointer;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 9px 14px;
  transition: all 0.2s ease;
}

.submit-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.image-preview {
  margin-top: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  background: #f9fafb;
}

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 6px;
  object-fit: cover;
  margin-bottom: 8px;
}

.image-preview p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  word-break: break-all;
}
</style>
