package com.zw.talkback.domain.lyxj.tsm3;

import lombok.Data;

@Data
public class PageInfo {

    /**
     * 当前页码
     */
    private int currentPageIndex;
    /**
     * 每页记录条数
     */
    private int pageSize;

    /**
     * 下一页码
     */
    private int nextPageIndex;
    /**
     * 总记录数
     */
    private int totalRecords;
    /**
     * 总页数
     */
    private int totalPages;
    /**
     * 当前页记录条数
     */
    private int currentPageRecords;
    /**
     * 上一页码
     */
    private int prePageIndex;

    private int pageIndex;
}