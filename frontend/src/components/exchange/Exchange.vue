<script setup lang="ts">
import { onMounted, ref, reactive } from "vue";
import { usePagination } from "../../utils/page";
import { useToast } from "../../utils/toast";
import { getJson, postJson } from "../../utils/api";
import Pagination from "../utils/Pagination.vue";
import type { PageResponse } from "../../utils/page";

// 类型定义
type ProductType =
  | "DAILY_NECESSITIES"
  | "BOOKS"
  | "STATIONARY"
  | "FOOD"
  | "COUPON"
  | "OTHER";

type ProductStatus = "AVAILABLE" | "UNAVAILABLE" | "SOLD_OUT" | "DELETED";

interface Product {
  id: number;
  name: string;
  description: string;
  category: ProductType;
  price: number;
  stock: number;
  status: ProductStatus;
  imageUrl?: string;
}

// 商品分类选项
const productTypeOptions = [
  { label: "日用品", value: "DAILY_NECESSITIES" },
  { label: "书籍", value: "BOOKS" },
  { label: "文具", value: "STATIONARY" },
  { label: "食品", value: "FOOD" },
  { label: "优惠券", value: "COUPON" },
  { label: "其他", value: "OTHER" },
];

const { success, error, info } = useToast();
const {
  pageObject,
  pageRanges,
  updatePageState,
  goToPage,
  prevPage,
  nextPage,
} = usePagination(8); // 每页8个商品，4*2布局

const loading = ref(false);
const products = ref<Product[]>([]);
const keyword = ref("");
const typeFilter = ref("ALL");

const showExchangeDialog = ref(false);
const currentProduct = ref<Product | null>(null);
const isExchanging = ref(false);
const recvInfoForm = reactive({
  name: "",
  phone: "",
  address: "",
});

const fetchProducts = async (page = 0) => {
  loading.value = true;
  try {
    const query = new URLSearchParams({
      page: String(page),
      size: String(pageObject.value.pageSize),
      category: typeFilter.value,
    });

    if (keyword.value) {
      query.append("keyword", keyword.value);
    }

    const response = await getJson<PageResponse<Product>>(
      `/api/product/get-products?${query.toString()}`
    );

    products.value = response.content;
    updatePageState({
      curPage: response.curPage,
      pageSize: response.pageSize,
      totalElements: response.totalElements,
      totalPages: response.totalPages,
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : "未知错误";
    error("商品加载失败", msg);
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  await fetchProducts(0);
};

const openExchangeDialog = (prod: Product) => {
  currentProduct.value = prod;
  recvInfoForm.name = "";
  recvInfoForm.phone = "";
  recvInfoForm.address = "";
  showExchangeDialog.value = true;
};

const closeExchangeDialog = () => {
  showExchangeDialog.value = false;
  currentProduct.value = null;
  recvInfoForm.name = "";
  recvInfoForm.phone = "";
  recvInfoForm.address = "";
};

const handleExchange = async () => {
  if (!currentProduct.value || isExchanging.value) return;

  const name = recvInfoForm.name.trim();
  const phone = recvInfoForm.phone.trim();
  const address = recvInfoForm.address.trim();

  if (!name) {
    error("兑换失败", "请填写收货人姓名");
    return;
  }
  if (!phone) {
    error("兑换失败", "请填写收货人手机号");
    return;
  }
  if (!address) {
    error("兑换失败", "请填写收货地址");
    return;
  }

  isExchanging.value = true;
  try {
    await postJson<{ id: number; message: string }>("/api/product/exchange", {
      productId: currentProduct.value.id,
      number: 1,
      recvInfo: `${name}, ${phone}, ${address}`,
    });
    success("兑换成功", `您成功兑换了${currentProduct.value.name}`);
    closeExchangeDialog();
    await fetchProducts(pageObject.value.curPage);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "兑换失败";
    
    if (msg.includes("库存不足") || msg.includes("积分不足") || msg.includes("已被其他用户兑换")) {
      info("兑换失败", msg);
    } else {
      error("兑换失败", msg);
    }
  } finally {
    isExchanging.value = false;
  }
};

const getCategoryLabel = (category: ProductType) => {
  const option = productTypeOptions.find((opt) => opt.value === category);
  return option ? option.label : category;
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.style.display = "none";
  const parent = img.parentElement;
  if (parent) {
    const placeholder = parent.querySelector(".image-placeholder");
    if (placeholder) {
      (placeholder as HTMLElement).style.display = "flex";
    }
  }
};

onMounted(() => {
  fetchProducts(0);
});
</script>

<template>
  <div class="exchange-center">
    <header class="page-header">
      <div>
        <h2>兑换商城</h2>
        <p>用积分兑换公益礼包与周边。</p>
      </div>
      <router-link to="/exchange-records" class="record-entry"
        >兑换记录</router-link
      >
    </header>

    <section class="search-box">
      <input
        v-model.trim="keyword"
        type="text"
        placeholder="搜索商品名称/描述关键词"
        @keyup.enter="handleSearch"
      />
      <button type="button" @click="handleSearch">搜索</button>
    </section>

    <section class="filters">
      <label class="filter-item">
        <span>商品分类</span>
        <select v-model="typeFilter" @change="handleSearch">
          <option value="ALL">全部分类</option>
          <option
            v-for="option in productTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </label>
    </section>

    <section class="product-grid">
      <p v-if="loading" class="empty">商品加载中...</p>
      <p v-else-if="products.length === 0" class="empty">暂无匹配商品</p>

      <article
        v-for="prod in products"
        :key="prod.id"
        class="product-card"
        :class="{ 'sold-out': prod.status === 'SOLD_OUT' }"
      >
        <div class="product-image">
          <img
            v-if="prod.imageUrl"
            :src="prod.imageUrl"
            :alt="prod.name"
            @error="handleImageError"
          />
          <div v-else class="image-placeholder">暂无图片</div>
        </div>
        <div class="product-body">
          <h3>{{ prod.name }}</h3>
          <p class="category">{{ getCategoryLabel(prod.category) }}</p>
          <p class="points">{{ prod.price }} 积分</p>
          <p class="stock">{{ prod.stock }} 件库存</p>
          <button
            :disabled="prod.status === 'SOLD_OUT'"
            @click="openExchangeDialog(prod)"
          >
            {{ prod.status === "SOLD_OUT" ? "已售罄" : "兑换" }}
          </button>
        </div>
      </article>
    </section>

    <Pagination
      :page-object="pageObject"
      :items="products"
      :loading="loading"
      :page-ranges="pageRanges"
      :go-to-page="(page: number) => goToPage(page, fetchProducts)"
      :prev-page="() => prevPage(fetchProducts)"
      :next-page="() => nextPage(fetchProducts)"
    />
  </div>

  <div v-if="showExchangeDialog" class="dialog-bg">
    <div class="dialog-body" role="dialog" aria-modal="true">
      <h3>商品兑换</h3>
      <p class="dialog-tip">确定要兑换此商品吗？兑换后将消耗相应积分。</p>
      <div v-if="currentProduct" class="product-summary">
        <div class="summary-item">
          <span class="label">商品名称</span>
          <span class="value">{{ currentProduct.name }}</span>
        </div>
        <div class="summary-item">
          <span class="label">所需积分</span>
          <span class="value">{{ currentProduct.price }} 积分</span>
        </div>
      </div>
      <div class="form-row">
        <label>收货人姓名</label>
        <input
          v-model="recvInfoForm.name"
          type="text"
          placeholder="请输入收货人姓名"
          maxlength="20"
        />
      </div>
      <div class="form-row">
        <label>收货人手机号</label>
        <input
          v-model="recvInfoForm.phone"
          type="text"
          placeholder="请输入收货人手机号"
          maxlength="11"
        />
      </div>
      <div class="form-row">
        <label>收货地址</label>
        <textarea
          v-model="recvInfoForm.address"
          placeholder="请输入详细收货地址"
          rows="3"
          maxlength="200"
        ></textarea>
      </div>
      <div class="dialog-actions">
        <button
          type="button"
          class="confirm-button"
          :disabled="isExchanging"
          @click="handleExchange"
        >
          确认兑换
        </button>
        <button
          type="button"
          class="cancel-button"
          @click="closeExchangeDialog"
        >
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exchange-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header p {
  color: #6b7280;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.record-entry {
  min-width: 80px;
  background: #2563eb;
  color: white;
  border-radius: 8px;
  padding: 8px 14px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.record-entry:hover {
  text-decoration: underline;
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(29, 78, 216, 0.3);
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.search-box input {
  flex: 1;
  font-size: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

.search-box input:hover,
.search-box input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.search-box input:focus {
  border-width: 2px;
}

.search-box button {
  min-width: 80px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-box button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(29, 78, 216, 0.3);
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item span {
  font-size: 14px;
  color: #4b5563;
  white-space: nowrap;
}

.filter-item select {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-item select:hover,
.filter-item select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.filter-item select:focus {
  border-width: 2px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

@media (min-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.product-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.2s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-card.sold-out {
  opacity: 0.7;
  background: #f9fafb;
}

.product-image {
  height: 160px;
  background: #f1f5f9;
  position: relative;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-placeholder {
  height: 160px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.product-body {
  padding: 16px;
  display: grid;
  gap: 8px;
}

.product-body h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}

.category {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.points {
  color: #2563eb;
  font-weight: 600;
  margin: 0;
}

.stock {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
}

.product-body button {
  border: none;
  background: #2563eb;
  color: white;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.product-body button:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}

.product-body button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.dialog-bg {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-body {
  background: white;
  border-radius: 12px;
  padding: 24px;
  max-width: 480px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-body h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
}

.dialog-tip {
  margin: 0 0 16px 0;
  color: #6b7280;
  font-size: 14px;
}

.product-summary {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e5e7eb;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  font-size: 14px;
  color: #6b7280;
}

.summary-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-row label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-row input,
.form-row textarea {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.form-row input:hover,
.form-row input:focus,
.form-row textarea:hover,
.form-row textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-row input:focus,
.form-row textarea:focus {
  border-width: 2px;
}

.form-row textarea {
  resize: vertical;
  min-height: 80px;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-actions button {
  min-width: 80px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.confirm-button {
  background: #2563eb;
  color: white;
  border: none;
}

.confirm-button:hover:not(:disabled) {
  background: #1d4ed8;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.confirm-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.cancel-button {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.cancel-button:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: #6b7280;
  padding: 40px 0;
}
</style>
