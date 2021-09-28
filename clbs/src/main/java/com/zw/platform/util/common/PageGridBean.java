package com.zw.platform.util.common;

import com.cb.platform.util.page.ApiPageInfo;
import com.cb.platform.util.page.PageResultBean;
import com.github.pagehelper.Page;
import com.zw.platform.dto.paas.PaasCloudPageDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页表格bean
 */
@Data
public class PageGridBean implements Serializable {
    public static final boolean SUCCESS = true;
    public static final boolean FAULT = false;
    private static final long serialVersionUID = 1L;
    private boolean success;

    private long totalPages; // 总页数

    private long totalRecords; // 总记录数

    private int draw; // dataTable用 ，接收和返回必须一样

    private long recordsTotal; // dataTable用

    private long recordsFiltered; // dataTable用

    private long pageSize; // 每页数据

    private long page; // 当前页数

    private long start; // 开始记录数

    private long end; // 结束记录数

    @SuppressWarnings("rawtypes")
    private List records = new ArrayList();

    private String message;

    private Object[] searchAfter;

    public PageGridBean() {
        this.success = true;
    }

    public PageGridBean(final boolean suc) {
        this.success = suc;
    }

    public PageGridBean(final boolean suc, String message) {
        this.success = suc;
        this.message = message;
    }

    /**
     * mybatis分页插件,不加pageHelper参数 会被List那个方法覆盖
     */
    public PageGridBean(final Page<?> pageParm, boolean pageHelper) {
        this.success = true;
        this.records = pageParm.getResult();
        this.totalRecords = pageParm.getTotal();
        this.recordsTotal = pageParm.getTotal(); // dataTable用
        this.recordsFiltered = pageParm.getTotal(); // dataTable用
        this.pageSize = pageParm.getPageSize();
        this.totalPages = pageParm.getPages();
        this.page = pageParm.getPageNum();
        this.start = pageParm.getStartRow();
        this.end = pageParm.getEndRow();
    }

    public PageGridBean(final List<?> recordsList, final boolean suc, String message) {
        this(recordsList);
        this.success = suc;
        this.message = message;
    }

    public PageGridBean(final Page<?> pageParm, Object[] searchAfter) {
        this(pageParm, true);
        this.searchAfter = searchAfter;
    }

    /**
     * mybatis分页插件
     */
    public PageGridBean(final BaseQueryBean query, final Page<?> pageParm, boolean pageHelper) {
        this(pageParm, pageHelper);
        this.draw = query.getDraw();
    }

    public PageGridBean(final List<?> recordsList) {
        this.success = true;
        this.records = recordsList;
    }

    /**
     * dataTable用
     */
    public PageGridBean(final BaseQueryBean query, final List<?> recordsList) {
        this.draw = query.getDraw();
        this.success = true;
        this.records = recordsList;
    }

    public PageGridBean(final long totalRecordsNum, final List<?> recordsList) {
        this(recordsList);
        this.totalRecords = totalRecordsNum;
        this.recordsTotal = totalRecordsNum; // dataTable用
        this.recordsFiltered = totalRecordsNum; // dataTable用
    }

    /**
     * dataTable用
     */
    public PageGridBean(final BaseQueryBean query, final long totalRecordsNum, final List<?> recordsList) {
        this(query, recordsList);
        this.totalRecords = totalRecordsNum;
        this.recordsTotal = totalRecordsNum; // dataTable用
        this.recordsFiltered = totalRecordsNum; // dataTable用
    }

    public PageGridBean(final long totalRecordsNum, final long page, final List<?> recordsList) {
        this(totalRecordsNum, recordsList);
        this.page = page;
    }

    public PageGridBean(final long totalRecordsNum, final long page, final long pageSizeParm,
        final List<?> recordsList) {
        this(totalRecordsNum, page, recordsList);
        this.pageSize = pageSizeParm;
        this.totalPages = (totalRecordsNum % pageSizeParm) == 0 ? (totalRecordsNum / pageSizeParm) :
            (totalRecordsNum / pageSizeParm + 1);
    }

    public PageGridBean(PageResultBean pageResultBean) {
        ApiPageInfo apiPageInfo = pageResultBean.getPageInfo();
        if (apiPageInfo != null) {
            int page = apiPageInfo.getPage();
            int pageSize = apiPageInfo.getPageSize();
            long total = apiPageInfo.getTotal();
            this.page = page;
            this.pageSize = pageSize;
            this.start = page * (pageSize - 1L) + 1;
            this.end = (long) page * pageSize;
            this.totalRecords = total;
            this.recordsTotal = total;
            this.recordsFiltered = total;
            this.totalPages = apiPageInfo.getTotalPage();
        }

        this.records = pageResultBean.getItems();
        this.success = true;
    }

    public PageGridBean(List<?> list, PaasCloudPageDTO pageInfo) {
        if (pageInfo != null) {
            int page = pageInfo.getPage();
            int pageSize = pageInfo.getPageSize();
            long total = pageInfo.getTotal();
            this.page = page;
            this.pageSize = pageSize;
            this.start = page * (pageSize - 1L) + 1;
            this.end = (long) page * pageSize;
            this.totalRecords = total;
            this.recordsTotal = total;
            this.recordsFiltered = total;
            this.totalPages = pageInfo.getTotalPage();
        }
        this.records = list;
        this.success = true;
    }
}
