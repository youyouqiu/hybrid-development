package com.zw.platform.controller.tree;

import com.zw.platform.service.tree.TreeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/2 17:24
 */
@RestController
@RequestMapping("/m/tree")
public class TreeController {
    private static final Logger log = LogManager.getLogger(TreeController.class);

    @Autowired
    private TreeService treeService;

    /**
     * 组织分组树
     * @param searchType       搜索类型 0:企业; 1:分组;
     * @param simpleQueryParam 搜索条件
     */
    @RequestMapping(value = "/org/assignment/tree", method = RequestMethod.POST)
    public String getOrgAssignmentTree(Integer searchType, String simpleQueryParam) {
        try {
            return treeService.getOrgAssignmentTree(searchType, simpleQueryParam);
        } catch (Exception e) {
            log.error("获取组织分组树", e);
            return null;
        }
    }

    /**
     * 组织树
     * @param simpleQueryParam 搜索条件
     */
    @RequestMapping(value = "/org/tree", method = RequestMethod.POST)
    public String getOrgTree(String simpleQueryParam) {
        try {
            return treeService.getOrgTree(simpleQueryParam);
        } catch (Exception e) {
            log.error("获取组织树", e);
            return null;
        }
    }
}
