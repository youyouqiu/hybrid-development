package com.zw.platform.service.tree;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/2 17:28
 */
public interface TreeService {
    /**
     * 组织分组树
     * @param searchType       搜索类型
     * @param simpleQueryParam 搜索条件
     * @return String
     */
    String getOrgAssignmentTree(Integer searchType, String simpleQueryParam);

    /**
     * 组织树
     * @param simpleQueryParam 搜索条件
     * @return String
     */
    String getOrgTree(String simpleQueryParam);
}
