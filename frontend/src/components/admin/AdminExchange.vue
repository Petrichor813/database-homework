<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { deleteJson, getJson, postJson, putJson } from "../../utils/api";
import type { PageResponse } from "../../utils/page";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import Pagination from "../utils/Pagination.vue";
import COS from "cos-js-sdk-v5";

type AdminExchangeTab = "EXCHANGE_RECORDS" | "PRODUCTS";

type ExchangeStatus =
  | "REVIEWING"
  | "PROCESSING"
  | "COMPLETED"
  | "CANCELLED"
  | "REJECTED";

interface ExchangeRecord {
  id: number;
  volunteerId: number;
  volunteerName: string;
  productName: string;
  productPrice: number;
  number: number;
  totalPoints: number;
  status: ExchangeStatus;
  orderTime: string;
  processTime?: string;
  note?: string;
  recvInfo?: string;
}

interface Product {
  id: number;
  name: string;
  description?: string;
  category: string;
  price: number;
  stock: number;
  status: string;
  imageUrl?: string;
  sortWeight?: number;
}

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

const { success, error } = useToast();

const activeTab = ref<AdminExchangeTab>("EXCHANGE_RECORDS");
const tabs: { label: string; value: AdminExchangeTab }[] = [
  { label: "兑换记录管理", value: "EXCHANGE_RECORDS" },
  { label: "商品管理", value: "PRODUCTS" },
];

/*****************************************************
 ******************** 兑换记录管理 *******************
 ****************************************************/

const exchangePagination = usePagination(8);
const exchangeLoading = ref(false);
const exchangeRecords = ref<ExchangeRecord[]>([]);

const exchangeStatusFilter = ref<string>("ALL");
const exchangeStatusOptions = [
  { label: "全部", value: "ALL" },
  { label: "待处理", value: "REVIEWING" },
  { label: "处理中", value: "PROCESSING" },
  { label: "已完成", value: "COMPLETED" },
  { label: "已拒绝", value: "REJECTED" },
  { label: "已取消", value: "CANCELLED" },
];

const statusLabelMap: Record<ExchangeStatus, string> = {
  REVIEWING: "待处理",
  PROCESSING: "处理中",
  COMPLETED: "已完成",
  CANCELLED: "已取消",
  REJECTED: "已拒绝",
};

const changeExchangeStatusFilter = (value: string) => {
  if (exchangeStatusFilter.value === value) return;
  exchangeStatusFilter.value = value;
};

const fetchExchangeRecords = async (page: number) => {
  exchangeLoading.value = true;
  try {
    const data = await getJson<PageResponse<ExchangeRecord>>(
      `/api/admin/exchange-record/get?status=${exchangeStatusFilter.value}&page=${page}&size=${exchangePagination.pageObject.value.pageSize}`,
    );
    exchangeRecords.value = data.content;
    exchangePagination.updatePageState(data);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取兑换记录失败";
    error("加载失败", msg);
  } finally {
    exchangeLoading.value = false;
  }
};

/*****************************************************
 ********************** 商品管理 *********************
 ****************************************************/

const productPagination = usePagination(8);
const productLoading = ref(false);
const products = ref<Product[]>([]);
const productSearchKeyword = ref("");

const productStatusMap: Record<ProductStatus, string> = {
  AVAILABLE: "可兑换",
  SOLD_OUT: "已售罄",
  DELETED: "已删除",
};

const productTypeMap: Record<ProductType, string> = {
  DAILY_NECESSITIES: "日用品",
  BOOKS: "书籍",
  STATIONARY: "文具",
  FOOD: "食品",
  COUPON: "优惠券",
  OTHER: "其他",
};

const fetchProducts = async (page: number) => {
  productLoading.value = true;
  try {
    const query = new URLSearchParams({
      page: String(page),
      size: String(productPagination.pageObject.value.pageSize)
    });

    const keyword = productSearchKeyword.value.trim();
    if (keyword) {
      query.append("keyword", keyword);
    }

    const data = await getJson<PageResponse<Product>>(`/api/admin/product/search?${query}`);
    products.value = data.content;
    productPagination.updatePageState(data);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "获取商品列表失败";
    error("加载失败", msg);
  } finally {
    productLoading.value = false;
  }
};

const searchProducts = () => {
  fetchProducts(0);
};

const clearSearch = () => {
  productSearchKeyword.value = "";
  fetchProducts(0);
};

/*****************************************************
 ********************** 弹窗逻辑 *********************
 ****************************************************/

const showApproveDialog = ref(false);
const showRejectDialog = ref(false);
const showDetailDialog = ref(false);
const showEditExchangeRecordDialog = ref(false);
const showEditProductDialog = ref(false);
const showDeleteProductDialog = ref(false);
const curExchangeRecord = ref<ExchangeRecord | null>(null);
const processNote = ref("");
const isProcessing = ref(false);
const isExchangeRecordEditing = ref(false);

const isAnyDialogOpen = computed(() => {
  return (
    showApproveDialog.value ||
    showRejectDialog.value ||
    showDetailDialog.value ||
    showEditExchangeRecordDialog.value ||
    showEditProductDialog.value ||
    showDeleteProductDialog.value
  );
});

const exchangeRecordForm = reactive({
  number: 0,
  status: "PROCESSING" as ExchangeStatus,
});

const editStatusOptions: { label: string; value: ExchangeStatus }[] = [
  { label: "处理中", value: "PROCESSING" },
  { label: "已完成", value: "COMPLETED" },
];

const openDetailDialog = (record: ExchangeRecord) => {
  curExchangeRecord.value = record;
  showDetailDialog.value = true;
};

const closeDetailDialog = () => {
  curExchangeRecord.value = null;
  showDetailDialog.value = false;
};

const openEditExchangeRecordDialog = (record: ExchangeRecord) => {
  curExchangeRecord.value = record;
  exchangeRecordForm.number = record.number;
  exchangeRecordForm.status = record.status;
  showEditExchangeRecordDialog.value = true;
};

const closeEditExchangeRecordDialog = () => {
  curExchangeRecord.value = null;
  exchangeRecordForm.number = 0;
  exchangeRecordForm.status = "PROCESSING";
  showEditExchangeRecordDialog.value = false;
};

const updateExchange = async () => {
  if (!curExchangeRecord.value || isExchangeRecordEditing.value) return;

  if (exchangeRecordForm.number <= 0) {
    error("保存失败", "兑换数量必须大于0");
    return;
  }

  isExchangeRecordEditing.value = true;
  try {
    await putJson(`/api/admin/exchange-record/${curExchangeRecord.value.id}/update`, {
      number: exchangeRecordForm.number,
      status: exchangeRecordForm.status,
    });
    success("保存成功", "兑换记录已更新");
    closeEditExchangeRecordDialog();
    await fetchExchangeRecords(exchangePagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "更新兑换记录失败";
    error("操作失败", msg);
  } finally {
    isExchangeRecordEditing.value = false;
  }
};

const openApproveDialog = (record: ExchangeRecord) => {
  curExchangeRecord.value = record;
  processNote.value = "";
  showApproveDialog.value = true;
};

const closeApproveDialog = () => {
  curExchangeRecord.value = null;
  processNote.value = "";
  showApproveDialog.value = false;
};

const openRejectDialog = (record: ExchangeRecord) => {
  curExchangeRecord.value = record;
  processNote.value = "";
  showRejectDialog.value = true;
};

const closeRejectDialog = () => {
  curExchangeRecord.value = null;
  processNote.value = "";
  showRejectDialog.value = false;
};

const approveExchange = async () => {
  if (!curExchangeRecord.value || isProcessing.value) return;

  isProcessing.value = true;
  try {
    await postJson(
      `/api/admin/exchange-record/${curExchangeRecord.value.id}/approve`,
      {
        note: processNote.value.trim() || "管理员批准兑换",
      },
    );
    success("批准成功", "兑换申请已批准");
    closeApproveDialog();
    await fetchExchangeRecords(exchangePagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "批准兑换失败";
    error("操作失败", msg);
  } finally {
    isProcessing.value = false;
  }
};

const rejectExchange = async () => {
  if (!curExchangeRecord.value || isProcessing.value) return;

  isProcessing.value = true;
  try {
    await postJson(
      `/api/admin/exchange-record/${curExchangeRecord.value.id}/reject`,
      {
        note: processNote.value.trim() || "管理员拒绝兑换",
      },
    );
    success("拒绝成功", "兑换申请已拒绝，积分已退还");
    closeRejectDialog();
    await fetchExchangeRecords(exchangePagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "拒绝兑换失败";
    error("操作失败", msg);
  } finally {
    isProcessing.value = false;
  }
};

const isProductSaving = ref(false);
const isProductDeleting = ref(false);
const curProduct = ref<Product | null>(null);

const productForm = reactive({
  name: "",
  description: "",
  price: 0,
  stock: 0,
  category: "DAILY_NECESSITIES" as ProductType,
  status: "AVAILABLE" as ProductStatus,
  sortWeight: 0,
  imageUrl: "",
});

const productImage = ref<File | null>(null);
const productImageUrl = ref<string>("");
const isUploadingImage = ref(false);

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
      "/api/cos-sts/credential",
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
            reject(err); // 上传失败，直接抛出错误
          } else {
            resolve(); // 上传成功，继续执行
          }
        },
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

const deleteImageFromCos = async (fileUrl: string): Promise<void> => {
  try {
    await deleteJson("/api/cos-sts/delete", { fileUrl });
  } catch (err) {
    const msg = err instanceof Error ? err.message : "删除文件失败";
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
    isUploadingImage.value = true;
    const imageUrl = await uploadImageToCos(file);
    productImage.value = file;
    productImageUrl.value = imageUrl;
    productForm.imageUrl = imageUrl;
    success("上传成功", "图片已成功上传到腾讯云COS");
  } catch (err) {
    const msg = err instanceof Error ? err.message : "图片上传失败";
    error("上传失败", msg);
    target.value = "";
    productImage.value = null;
  } finally {
    isUploadingImage.value = false;
  }
};

const removeImage = async () => {
  if (productImageUrl.value) {
    try {
      await deleteImageFromCos(productImageUrl.value);
    } catch (err) {
      error("删除失败", "图片删除失败，但已清除本地选择");
    }
  }
  productImage.value = null;
  productImageUrl.value = "";
  productForm.imageUrl = "";

  const fileInput = document.querySelector(
    '.product-form input[type="file"]',
  ) as HTMLInputElement;
  if (fileInput) {
    fileInput.value = "";
  }
};

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
  { label: "已售罄", value: "SOLD_OUT" },
];

const openEditProductDialog = (product: Product) => {
  curProduct.value = product;
  productForm.name = product.name;
  productForm.description = product.description || "";
  productForm.price = product.price;
  productForm.stock = product.stock;
  productForm.category = product.category as ProductType;
  productForm.status = product.status as ProductStatus;
  productForm.sortWeight = product.sortWeight || 0;
  productForm.imageUrl = product.imageUrl || "";
  productImageUrl.value = product.imageUrl || "";
  productImage.value = null;
  showEditProductDialog.value = true;
};

const closeEditProductDialog = () => {
  showEditProductDialog.value = false;
  curProduct.value = null;
};

const validateProductForm = (): string | null => {
  const name = productForm.name.trim();
  if (!name) {
    return "商品名称不能为空";
  }
  if (name.length > 50) {
    return "商品名称长度不能超过50个字符";
  }
  if (!productForm.price || productForm.price <= 0) {
    return "商品价格必须大于0";
  }
  if (productForm.stock < 0) {
    return "库存数量不能为负数";
  }
  return null;
};

const saveProduct = async () => {
  if (isProductSaving.value) return;

  const validationError = validateProductForm();
  if (validationError) {
    error("保存失败", validationError);
    return;
  }

  isProductSaving.value = true;
  try {
    const payload = {
      name: productForm.name.trim(),
      description: productForm.description.trim() || null,
      price: productForm.price,
      stock: productForm.stock,
      category: productForm.category,
      status: productForm.status,
      sortWeight: productForm.sortWeight,
      imageUrl: productForm.imageUrl.trim() || null,
    };

    if (!curProduct.value) {
      error("保存失败", "未找到当前商品");
      return;
    }
    await putJson(`/api/admin/product/${curProduct.value.id}/update`, payload);
    success("保存成功", "商品信息已更新");

    closeEditProductDialog();
    await fetchProducts(productPagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "保存商品失败";
    error("保存失败", msg);
  } finally {
    isProductSaving.value = false;
  }
};

const openDeleteProductDialog = (product: Product) => {
  curProduct.value = product;
  showDeleteProductDialog.value = true;
};

const closeDeleteProductDialog = () => {
  showDeleteProductDialog.value = false;
  curProduct.value = null;
};

const deleteProduct = async () => {
  if (!curProduct.value || isProductDeleting.value) return;

  isProductDeleting.value = true;
  try {
    await deleteJson(`/api/admin/product/${curProduct.value.id}/delete`);
    success("删除成功", "商品已删除");
    closeDeleteProductDialog();
    await fetchProducts(productPagination.pageObject.value.curPage);
  } catch (err) {
    const msg = err instanceof Error ? err.message : "删除商品失败";
    error("删除失败", msg);
  } finally {
    isProductDeleting.value = false;
  }
};

/*****************************************************
 ********************** 全局事件 **********************
 *****************************************************/

watch(activeTab, (newTab) => {
  if (newTab === "EXCHANGE_RECORDS" && exchangeRecords.value.length === 0) {
    fetchExchangeRecords(0);
  } else if (newTab === "PRODUCTS" && products.value.length === 0) {
    fetchProducts(0);
  }
});

watch(exchangeStatusFilter, () => {
  fetchExchangeRecords(0);
});

onMounted(() => fetchExchangeRecords(0));

watch(isAnyDialogOpen, (isOpen) => {
  if (isOpen) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
});
</script>

<template>
  <div class="admin-exchange">
    <h2>商品与兑换管理</h2>

    <section class="layout">
      <aside class="tab-menu" aria-label="商品与兑换管理导航">
        <button
          type="button"
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-button"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </aside>

      <div class="tab-page">
        <section
          v-if="activeTab === 'EXCHANGE_RECORDS'"
          class="exchange-records"
        >
          <div class="filter-bar">
            <button
              type="button"
              v-for="option in exchangeStatusOptions"
              :key="option.value"
              class="filter-button"
              :class="{ active: exchangeStatusFilter === option.value }"
              @click="changeExchangeStatusFilter(option.value)"
            >
              {{ option.label }}
            </button>
          </div>

          <section class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>志愿者姓名</th>
                  <th>商品名称</th>
                  <th>兑换数量</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="exchangeLoading" class="loading-row">
                  <td colspan="5">正在加载...</td>
                </tr>
                <tr v-else-if="exchangeRecords.length === 0" class="empty-row">
                  <td colspan="5">暂无兑换记录</td>
                </tr>
                <tr v-else v-for="record in exchangeRecords" :key="record.id">
                  <td>{{ record.volunteerName }}</td>
                  <td>{{ record.productName }}</td>
                  <td>{{ record.number }}</td>
                  <td>
                    <span
                      class="status-badge"
                      :class="record.status.toLowerCase()"
                    >
                      {{ statusLabelMap[record.status] || record.status }}
                    </span>
                  </td>
                  <td>
                    <div class="actions">
                      <button
                        type="button"
                        class="info-button"
                        @click="openDetailDialog(record)"
                      >
                        详细信息
                      </button>
                      <button
                        v-if="
                          record.status === 'PROCESSING' ||
                          record.status === 'COMPLETED'
                        "
                        type="button"
                        class="edit-button"
                        @click="openEditExchangeRecordDialog(record)"
                      >
                        编辑
                      </button>
                      <template v-if="record.status === 'REVIEWING'">
                        <button
                          type="button"
                          class="approve-button"
                          @click="openApproveDialog(record)"
                        >
                          批准
                        </button>
                        <button
                          type="button"
                          class="reject-button"
                          @click="openRejectDialog(record)"
                        >
                          拒绝
                        </button>
                      </template>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <Pagination
              :page-object="exchangePagination.pageObject.value"
              :items="exchangeRecords"
              :loading="exchangeLoading"
              :page-ranges="exchangePagination.pageRanges.value"
              :go-to-page="
                (page: number) =>
                  exchangePagination.goToPage(page, fetchExchangeRecords)
              "
              :prev-page="
                () => exchangePagination.prevPage(fetchExchangeRecords)
              "
              :next-page="
                () => exchangePagination.nextPage(fetchExchangeRecords)
              "
            />
          </section>
        </section>

        <section v-else class="products">
          <div class="toolbar">
            <div class="search-box">
              <input
                v-model="productSearchKeyword"
                type="text"
                placeholder="请输入商品名称搜索"
                @keyup.enter="searchProducts"
              />
              <button
                v-if="productSearchKeyword"
                type="button"
                class="clear-button"
                @click="clearSearch"
              >
                ×
              </button>
            </div>
            <button type="button" class="search-button" @click="searchProducts">
              搜索
            </button>
          </div>

          <section class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>商品名称</th>
                  <th>商品价格(积分/个)</th>
                  <th>库存数量</th>
                  <th>分类</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="productLoading" class="loading-row">
                  <td colspan="6">正在加载...</td>
                </tr>
                <tr v-else-if="products.length === 0" class="empty-row">
                  <td colspan="6">
                    {{ productSearchKeyword ? "未找到匹配商品" : "暂无商品" }}
                  </td>
                </tr>
                <tr v-else v-for="product in products" :key="product.id">
                  <td>{{ product.name }}</td>
                  <td>{{ product.price }}</td>
                  <td>{{ product.stock }}</td>
                  <td>
                    {{
                      productTypeMap[product.category as ProductType] ||
                      product.category
                    }}
                  </td>
                  <td>
                    <span
                      class="status-badge"
                      :class="product.status.toLowerCase()"
                    >
                      {{
                        productStatusMap[product.status as ProductStatus] ||
                        product.status
                      }}
                    </span>
                  </td>
                  <td>
                    <div class="actions">
                      <button
                        type="button"
                        class="edit-button"
                        @click="openEditProductDialog(product)"
                      >
                        编辑
                      </button>
                      <button
                        type="button"
                        class="delete-button"
                        @click="openDeleteProductDialog(product)"
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <Pagination
              :page-object="productPagination.pageObject.value"
              :items="products"
              :loading="productLoading"
              :page-ranges="productPagination.pageRanges.value"
              :go-to-page="
                (page: number) =>
                  productPagination.goToPage(page, fetchProducts)
              "
              :prev-page="() => productPagination.prevPage(fetchProducts)"
              :next-page="() => productPagination.nextPage(fetchProducts)"
            />
          </section>
        </section>
      </div>
    </section>

    <div v-if="showApproveDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>批准兑换申请</h3>
        <p class="dialog-tip">
          确定要批准此兑换申请吗？批准后将自动扣减商品库存。
        </p>
        <div class="form-row">
          <label>备注（可选）</label>
          <textarea
            v-model="processNote"
            placeholder="请输入批准备注"
            rows="3"
          ></textarea>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button approve"
            :disabled="isProcessing"
            @click="approveExchange"
          >
            确认批准
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeApproveDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRejectDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>拒绝兑换申请</h3>
        <p class="dialog-tip">
          确定要拒绝此兑换申请吗？拒绝后积分将退还给志愿者。
        </p>
        <div class="form-row">
          <label>拒绝原因（可选）</label>
          <textarea
            v-model="processNote"
            placeholder="请输入拒绝原因"
            rows="3"
          ></textarea>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button reject"
            :disabled="isProcessing"
            @click="rejectExchange"
          >
            确认拒绝
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeRejectDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDetailDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>兑换记录详细信息</h3>
        <div v-if="curExchangeRecord" class="detail-content">
          <div class="detail-row">
            <span class="detail-label">志愿者姓名</span>
            <span class="detail-value">{{
              curExchangeRecord.volunteerName
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">志愿者ID</span>
            <span class="detail-value">{{
              curExchangeRecord.volunteerId
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">商品名称</span>
            <span class="detail-value">{{
              curExchangeRecord.productName
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">商品单价</span>
            <span class="detail-value"
              >{{ curExchangeRecord.productPrice.toFixed(2) }} 积分/个</span
            >
          </div>
          <div class="detail-row">
            <span class="detail-label">兑换数量</span>
            <span class="detail-value">{{ curExchangeRecord.number }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">积分消耗</span>
            <span class="detail-value">{{
              curExchangeRecord.totalPoints
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">兑换时间</span>
            <span class="detail-value">{{ curExchangeRecord.orderTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">兑换状态</span>
            <span class="detail-value">
              <span
                class="status-badge"
                :class="curExchangeRecord.status.toLowerCase()"
              >
                {{
                  statusLabelMap[curExchangeRecord.status] ||
                  curExchangeRecord.status
                }}
              </span>
            </span>
          </div>
          <div v-if="curExchangeRecord.processTime" class="detail-row">
            <span class="detail-label">处理时间</span>
            <span class="detail-value">{{
              curExchangeRecord.processTime
            }}</span>
          </div>
          <div v-if="curExchangeRecord.note" class="detail-row">
            <span class="detail-label">备注</span>
            <span class="detail-value">{{ curExchangeRecord.note }}</span>
          </div>
          <div v-if="curExchangeRecord.recvInfo" class="detail-row-block">
            <span class="detail-label">收货信息</span>
            <span class="detail-value">{{ curExchangeRecord.recvInfo }}</span>
          </div>
        </div>
        <div class="dialog-actions">
          <button type="button" class="close-button" @click="closeDetailDialog">
            关闭
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditExchangeRecordDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>编辑兑换记录</h3>
        <div v-if="curExchangeRecord" class="detail-content">
          <div class="detail-row">
            <span class="detail-label">志愿者姓名</span>
            <span class="detail-value">{{
              curExchangeRecord.volunteerName
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">志愿者ID</span>
            <span class="detail-value">{{
              curExchangeRecord.volunteerId
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">商品名称</span>
            <span class="detail-value">{{
              curExchangeRecord.productName
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">商品单价</span>
            <span class="detail-value"
              >{{ curExchangeRecord.productPrice.toFixed(2) }} 积分/个</span
            >
          </div>
          <div class="form-row">
            <label>兑换数量 <span class="required">*</span></label>
            <input
              v-model.number="exchangeRecordForm.number"
              type="number"
              min="1"
              placeholder="请输入兑换数量"
            />
          </div>
          <div class="detail-row">
            <span class="detail-label">积分消耗</span>
            <span class="detail-value">{{
              (
                curExchangeRecord.productPrice * exchangeRecordForm.number
              ).toFixed(2)
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">兑换时间</span>
            <span class="detail-value">{{ curExchangeRecord.orderTime }}</span>
          </div>
          <div class="form-row">
            <label>兑换状态 <span class="required">*</span></label>
            <select v-model="exchangeRecordForm.status">
              <option
                v-for="option in editStatusOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button"
            :disabled="isExchangeRecordEditing"
            @click="updateExchange"
          >
            确认保存
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeEditExchangeRecordDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditProductDialog" class="dialog-bg">
      <div class="dialog-body product-dialog" role="dialog" aria-modal="true">
        <h3>编辑商品</h3>
        <div class="product-form">
          <div class="form-row">
            <label>商品名称 <span class="required">*</span></label>
            <input
              v-model="productForm.name"
              type="text"
              placeholder="请输入商品名称（1-50字符）"
              maxlength="50"
            />
          </div>
          <div class="form-row">
            <label>商品描述</label>
            <textarea
              v-model="productForm.description"
              placeholder="请输入商品描述（可选）"
              rows="2"
            ></textarea>
          </div>
          <div class="form-row-inline">
            <div class="form-row half">
              <label>商品价格(积分) <span class="required">*</span></label>
              <input
                v-model.number="productForm.price"
                type="number"
                min="1"
                placeholder="请输入价格"
              />
            </div>
            <div class="form-row half">
              <label>库存数量 <span class="required">*</span></label>
              <input
                v-model.number="productForm.stock"
                type="number"
                min="0"
                placeholder="请输入库存"
              />
            </div>
          </div>
          <div class="form-row-inline">
            <div class="form-row half">
              <label>商品分类 <span class="required">*</span></label>
              <select v-model="productForm.category">
                <option
                  v-for="option in productTypeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div class="form-row half">
              <label>商品状态 <span class="required">*</span></label>
              <select v-model="productForm.status">
                <option
                  v-for="option in productStatusOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <label>排序权重</label>
            <input
              v-model.number="productForm.sortWeight"
              type="number"
              min="0"
              placeholder="请输入排序权重（数值越大越靠前）"
            />
          </div>
          <div class="form-row image-upload-row">
            <label>商品图片</label>
            <div class="image-upload-container">
              <label v-if="!productImageUrl" class="upload-trigger">
                <input
                  type="file"
                  accept="image/*"
                  @change="pickImage($event)"
                />
                <span>{{ isUploadingImage ? "上传中..." : "选择图片" }}</span>
              </label>
              <p class="image-status-text">{{ productImageText }}</p>
              <div v-if="productImageUrl" class="image-preview">
                <img :src="productImageUrl" alt="商品图片预览" />
                <button
                  type="button"
                  class="delete-image-button"
                  @click="removeImage"
                  aria-label="删除图片"
                >
                  ×
                </button>
              </div>
              <small>支持jpg、png等常见图片格式，大小不超过5MB</small>
            </div>
          </div>
        </div>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button"
            :disabled="isProductSaving"
            @click="saveProduct"
          >
            保存修改
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeEditProductDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDeleteProductDialog" class="dialog-bg">
      <div class="dialog-body" role="dialog" aria-modal="true">
        <h3>确认删除商品</h3>
        <p class="dialog-tip">
          确定要删除商品「{{ curProduct?.name }}」吗？此操作不可恢复。
        </p>
        <div class="dialog-actions">
          <button
            type="button"
            class="confirm-button delete"
            :disabled="isProductDeleting"
            @click="deleteProduct"
          >
            确认删除
          </button>
          <button
            type="button"
            class="cancel-button"
            @click="closeDeleteProductDialog"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.admin-exchange {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.layout {
  display: grid;
  align-items: start;
  grid-template-columns: 220px minmax(0, 1fr);
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
  text-align: center;
  cursor: pointer;
  font-size: 16px;
  color: #475569;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
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
  transition: all 0.2s ease;
}

.tab-button.active:hover {
  background: #dbeafe;
  color: #1e40af;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.tab-page {
  display: grid;
  min-width: 0;
  gap: 16px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.filter-button {
  min-width: 80px;
  background: white;
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  padding: 6px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-button:hover {
  background: #f8fafc;
  color: black;
  border-color: #cdcdcd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.filter-button.active {
  background: #2563eb;
  color: white;
  border: none;
}

.filter-button.active:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.table-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: center;
}

.data-table th {
  background: #f8fafc;
  color: #6b7280;
  font-size: 16px;
  font-weight: 600;
}

.data-table tbody tr {
  border-top: 1px solid #e5e7eb;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.loading-row td,
.empty-row td {
  color: #6b7280;
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.status-badge.reviewing {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.processing {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.rejected {
  background: #fee2e2;
  color: #b91c1c;
}

.status-badge.available {
  background: #dcfce7;
  color: #166534;
}

.status-badge.sold_out {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.deleted {
  background: #fee2e2;
  color: #b91c1c;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.actions button {
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.actions button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.approve-button {
  background: #22c55e;
}

.approve-button:hover:not(:disabled) {
  background: #16a34a;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.reject-button {
  background: #ef4444;
}

.reject-button:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.edit-button {
  background: #2563eb;
}

.edit-button:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.info-button {
  background: #2563eb;
}

.info-button:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.delete-button {
  background: #ef4444;
}

.delete-button:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.processed-text {
  color: #9ca3af;
  font-size: 14px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 0;
}

.search-box input {
  width: 100%;
  padding: 10px 36px 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.search-box input:hover,
.search-box input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-box input:focus {
  border-width: 2px;
}

.clear-button {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background: #e5e7eb;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.clear-button:hover {
  background: #d1d5db;
}

.search-button {
  background: #2563eb;
  color: white;
  font-weight: 600;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.search-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.dialog-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  background: rgba(15, 23, 42, 0.4);
  inset: 0;
  z-index: 100;
  padding: 20px;
  overflow: hidden;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  background: white;
  width: 420px;
  max-width: 90vw;
  max-height: 90vh;
  padding: 24px;
  gap: 16px;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
  overflow: hidden;
}

.dialog-body h3 {
  text-align: center;
  margin: 0;
  flex-shrink: 0;
}

.dialog-tip {
  font-size: 14px;
  color: #6b7280;
  text-align: center;
  margin: 0;
  line-height: 1.5;
  flex-shrink: 0;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.form-row label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-row .required {
  color: #ef4444;
}

.form-row input,
.form-row textarea,
.form-row select {
  box-sizing: border-box;
  width: 100%;
  font-size: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  transition: all 0.2s ease;
}

.form-row input:focus,
.form-row textarea:focus,
.form-row select:focus {
  border-color: #2563eb;
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-row textarea {
  resize: vertical;
}

.image-upload-row {
  flex-direction: column;
}

.image-upload-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-trigger:hover {
  background: #f1f5f9;
  border-color: #94a3b8;
}

.upload-trigger input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.image-status-text {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.image-preview {
  position: relative;
  display: inline-block;
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
}

.delete-image-button {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0;
}

.delete-image-button:hover {
  background: #dc2626;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.delete-image-button:active {
  transform: scale(0.95);
}

.image-upload-row small {
  color: #94a3b8;
  font-size: 12px;
}

.form-row-inline {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.form-row.half {
  flex: 1;
  min-width: 0;
}

.product-dialog {
  max-width: 520px;
}

.product-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 12px;
  flex: 1;
  min-height: 0;
}

.product-form::-webkit-scrollbar {
  width: 6px;
}

.product-form::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.product-form::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.product-form::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.dialog-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 8px;
  flex-shrink: 0;
}

.confirm-button {
  min-width: 100px;
  background: #22c55e;
  color: white;
  cursor: pointer;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.confirm-button:hover:not(:disabled) {
  background: #16a34a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.confirm-button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.confirm-button.approve {
  background: #22c55e;
}

.confirm-button.approve:hover:not(:disabled) {
  background: #16a34a;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.confirm-button.reject {
  background: #ef4444;
}

.confirm-button.reject:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.confirm-button.delete {
  background: #ef4444;
}

.confirm-button.delete:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.cancel-button {
  min-width: 100px;
  background: white;
  cursor: pointer;
  padding: 10px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cancel-button:hover {
  background: #efefef;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(203, 213, 215, 0.3);
}

.close-button {
  min-width: 80px;
  min-height: 44px;
  background: #9ca3af;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #6b7280;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 12px;
  flex: 1;
  min-height: 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
  gap: 12px;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row-block:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  flex-shrink: 0;
  min-width: 80px;
}

.detail-value {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
  word-break: break-word;
  overflow-wrap: break-word;
  text-align: right;
  flex: 1;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .tab-menu {
    flex-direction: row;
    overflow-x: auto;
  }

  .tab-button {
    white-space: nowrap;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

  .form-row-inline {
    flex-direction: column;
  }
}

.detail-content::-webkit-scrollbar {
  width: 6px;
}

.detail-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
