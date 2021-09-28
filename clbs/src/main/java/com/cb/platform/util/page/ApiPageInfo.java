package com.cb.platform.util.page;

import com.github.pagehelper.Page;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
public class ApiPageInfo {
    /**
     * 总数量
     */
    private long total;

    /**
     * 当前页数量
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 当前页
     */
    private Integer page;

    public ApiPageInfo() {

    }

    public ApiPageInfo(List<?> resultList) {
        try {
            // 转换成page
            Page<?> pageList = (Page<?>) resultList;
            this.total = pageList.getTotal();
            this.pageSize = pageList.getPageSize();
            this.totalPage = pageList.getPages();
            this.page = pageList.getPageNum();
        } catch (Exception e) {
            // 转换失败,不做处理
        }
    }

}
