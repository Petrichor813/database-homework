<script setup lang="ts">
import { onMounted, ref } from "vue";
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

const { success, error } = useToast();
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

const handleExchange = async (prod: Product) => {
  try {
    await postJson<{ id: number; message: string }>("/api/product/exchange", {
      productId: prod.id,
      number: 1,
    });
    success("兑换成功", `您成功兑换了${prod.name}`);
    await fetchProducts(pageObject.value.curPage);
  } catch (e) {
    const msg = e instanceof Error ? e.message : "兑换失败";
    error("兑换失败", msg);
  }
};

const getCategoryLabel = (category: ProductType) => {
  const option = productTypeOptions.find((opt) => opt.value === category);
  return option ? option.label : category;
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.style.display = 'none';
  const parent = img.parentElement;
  if (parent) {
    const placeholder = parent.querySelector('.image-placeholder');
    if (placeholder) {
      (placeholder as HTMLElement).style.display = 'flex';
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
            @click="handleExchange(prod)"
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

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: #6b7280;
  padding: 40px 0;
}
</style>
