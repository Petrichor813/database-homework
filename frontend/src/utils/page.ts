export type PageResponse<T> = {
  content: T[];
  curPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
};