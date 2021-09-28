package com.zw.platform.util.common;

import com.github.pagehelper.Page;

import java.util.List;

public class RedisQueryUtil {

    /**
     * 分页处理
     * @param list     显示数据
     * @param query    页面传入参数
     * @param listSize 总条数
     * @return page 返回page对象
     */
    public static <T> Page<T> getListToPage(List<T> list, BaseQueryBean query, int listSize) {

        Page<T> result = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
        if (list != null && list.size() > 0) {
            result.addAll(list);
            result.setTotal(listSize);
        } else {
            result.setTotal(0);
        }
        return result;
    }

    /**
     * 获取最终分页要查询的数据库的id
     * @param allIds
     * @param queryBean
     * @return
     */
    public static List<String> getPageListIds(List<String> allIds, BaseQueryBean queryBean) {
        //构建分页
        int listSize = allIds.size();
        // 当前页
        int curPage = queryBean.getPage().intValue();
        // 每页条数
        int pageSize = queryBean.getLimit().intValue();
        // 遍历开始条数
        int startIndex = (curPage - 1) * pageSize;
        // 遍历条数
        int loopEntries = pageSize > (listSize - startIndex) ? listSize : (pageSize * curPage);
        //组装返回的数据列表
        return allIds.subList(startIndex, loopEntries);
    }
}
