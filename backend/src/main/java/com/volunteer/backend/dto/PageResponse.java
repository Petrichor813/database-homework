package com.volunteer.backend.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int curPage;
    private int pageSize; // 这里指每页展示多少条记录
    private long totalElements;
    private int totalPages;

    public PageResponse(List<T> content, int curPage, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.curPage = curPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
