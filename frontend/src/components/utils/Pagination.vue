<script setup lang="ts">
import type { PageState } from "../../utils/page";

const props = defineProps({
  pageObject: {
    type: Object as () => PageState,
    required: true,
  },

  items: {
    type: Array,
    required: true,
  },

  loading: {
    type: Boolean,
    required: true,
  },

  pageRanges: {
    type: Array,
    required: true,
  },

  goToPage: {
    type: Function,
    required: true,
  },

  prevPage: {
    type: Function,
    required: true,
  },

  nextPage: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div
    v-if="!loading && items.length > 0 && pageObject.totalPages > 1"
    class="page-nav"
  >
    <div class="page-info">
      显示第 {{ pageObject.curPage * pageObject.pageSize + 1 }} -
      {{
        Math.min(
          (pageObject.curPage + 1) * pageObject.pageSize,
          pageObject.totalElements,
        )
      }}
      条， 共 {{ pageObject.totalElements }} 条记录
    </div>

    <div class="page-control">
      <button
        type="button"
        class="page-button"
        :disabled="pageObject.curPage === 0"
        @click="() => prevPage()"
      >
        上一页
      </button>

      <template v-for="(page, index) in pageRanges" :key="index">
        <button
          v-if="typeof page === 'number'"
          type="button"
          class="page-button"
          :class="{ active: page === pageObject.curPage }"
          @click="() => goToPage(page)"
        >
          {{ page + 1 }}
        </button>
        <span v-else class="page-dots">...</span>
      </template>

      <button
        type="button"
        class="page-button"
        :disabled="pageObject.curPage === pageObject.totalPages - 1"
        @click="() => nextPage()"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style lang="css" scoped>
.page-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.page-info {
  font-size: 16px;
  color: #6b7280;
}

.page-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-button {
  min-width: 60px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: white;
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.page-button:hover {
  background: #f8fafc;
}

.page-button.active {
  background: #2563eb;
  color: white;
  border: none;
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-dots {
  padding: 0 8px;
  color: #6b7280;
}
</style>