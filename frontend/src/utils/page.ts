import { computed, ref } from "vue";

export type PageResponse<T> = {
  content: T[];
  curPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
};

export interface PageState {
  curPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export function usePagination(initPageSize: number = 8) {
  const pageObject = ref<PageState>({
    curPage: 0,
    pageSize: initPageSize,
    totalElements: 0,
    totalPages: 0,
  });

  const pageRanges = computed(() => {
    const curPage = pageObject.value.curPage;
    const totalPages = pageObject.value.totalPages;
    const delta = 2;
    const range = [];

    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    // 总是显示第一页
    range.push(0);

    if (curPage > delta + 1) {
      range.push("...");
    }

    const start = Math.max(1, curPage - delta);
    const end = Math.min(totalPages - 2, curPage + delta);

    for (let i = start; i <= end; i++) {
      range.push(i);
    }

    if (end < totalPages - 2) {
      range.push("...");
    }

    // 总是显示最后一页
    range.push(totalPages - 1);

    return range;
  });

  const updatePageState = (data: PageState) => {
    pageObject.value = { ...pageObject.value, ...data };
  };

  const goToPage = (page: number, callback: (page: number) => void) => {
    if (page < 0 || page >= pageObject.value.totalPages) {
      return;
    }
    callback(page);
  };

  const prevPage = (callback: (page: number) => void) => {
    if (pageObject.value.curPage <= 0) {
      return;
    }
    callback(pageObject.value.curPage - 1);
  };

  const nextPage = (callback: (page: number) => void) => {
    if (pageObject.value.curPage >= pageObject.value.totalPages - 1) {
      return;
    }
    callback(pageObject.value.curPage + 1);
  };

  return {
    pageObject,
    pageRanges,
    updatePageState,
    goToPage,
    prevPage,
    nextPage,
  };
}
