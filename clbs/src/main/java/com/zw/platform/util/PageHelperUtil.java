package com.zw.platform.util;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.util.common.BaseQueryBean;

import java.util.Objects;

/**
 * PageHelper分页工具类
 * github地址: https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md
 * @author zhouzongbo on 2019/11/11 11:19
 */
public class PageHelperUtil {

    public static <T> Page<T> doSelect(BaseQueryBean query, ISelect select) {
        if (Objects.isNull(query)) {
            return new Page<>(0, 0, false);
        }
        return PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue()).doSelectPage(select);
    }


    public static <T, R> Page<R> copyPage(Page<T> page) {
        Page<R> resultPage  = new Page<>();
        resultPage.setTotal(page.getTotal());
        resultPage.setPageNum(page.getPageNum());
        resultPage.setPageSize(page.getPageSize());
        resultPage.setStartRow(page.getStartRow());
        resultPage.setEndRow(page.getEndRow());
        return resultPage;
    }
}
