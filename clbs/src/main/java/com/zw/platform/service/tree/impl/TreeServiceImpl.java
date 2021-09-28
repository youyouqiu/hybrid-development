package com.zw.platform.service.tree.impl;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.tree.TreeService;
import com.zw.platform.util.TreeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/2 17:28
 */
@Service
public class TreeServiceImpl implements TreeService {
    @Autowired
    private UserService userService;

    @Autowired
    private GroupDao groupDao;

    @Override
    public String getOrgAssignmentTree(Integer searchType, String simpleQueryParam) {
        JSONArray jsonArrTree = new JSONArray();
        // 当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户组织id
        String currentOrgId = userId.substring(userId.indexOf(",") + 1);
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> userOwenOrgList = userService.getOrgChild(currentOrgId);
        if (CollectionUtils.isEmpty(userOwenOrgList)) {
            return jsonArrTree.toJSONString();
        }
        String userUuid = userService.getUserUuidById(userId);
        Map<String, OrganizationLdap> orgUuidAndOrgInfoMap =
            userOwenOrgList.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, Function.identity()));
        List<Assignment> orgAssignmentList =
            groupDao.findUserAssignment(userUuid, new ArrayList<>(orgUuidAndOrgInfoMap.keySet()));
        // 没有模糊搜索
        if (StringUtils.isBlank(simpleQueryParam)) {
            TreeUtils.treeAddOrg(userOwenOrgList, jsonArrTree);
            TreeUtils.treeAddAssignment(orgAssignmentList, orgUuidAndOrgInfoMap, jsonArrTree);
            return jsonArrTree.toJSONString();
        }
        // 模糊搜索企业
        if (Objects.equals(searchType, 0)) {
            List<OrganizationLdap> superiorOrgList = new ArrayList<>();
            Set<String> superiorOrgIds = new HashSet<>();
            userOwenOrgList
                .stream()
                .filter(org -> org.getName().contains(simpleQueryParam))
                .forEach(org -> {
                    String orgId = org.getId().toString();
                    // 递归获取组织的上级所有组织(包含当前组织)
                    TreeUtils.recursionGetSuperiorOrg(orgId, userOwenOrgList, superiorOrgList,
                        superiorOrgIds);
                });
            TreeUtils.treeAddOrg(superiorOrgList, jsonArrTree);
        }
        // 模糊搜索分组
        if (Objects.equals(searchType, 1)) {
            orgAssignmentList = orgAssignmentList
                .stream()
                .filter(assignment -> assignment.getName().contains(simpleQueryParam))
                .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgAssignmentList)) {
                return jsonArrTree.toJSONString();
            }
            List<OrganizationLdap> superiorOrgList = new ArrayList<>();
            Set<String> superiorOrgIds = new HashSet<>();
            for (Assignment assignment : orgAssignmentList) {
                OrganizationLdap org = orgUuidAndOrgInfoMap.get(assignment.getGroupId());
                // 递归获取分组所属组织的上级所有组织(包含当前分组所属组织)
                TreeUtils
                    .recursionGetSuperiorOrg(org.getId().toString(), userOwenOrgList, superiorOrgList, superiorOrgIds);
            }
            TreeUtils.treeAddOrg(superiorOrgList, jsonArrTree);
            TreeUtils.treeAddAssignment(orgAssignmentList, orgUuidAndOrgInfoMap, jsonArrTree);
        }
        return jsonArrTree.toJSONString();
    }

    @Override
    public String getOrgTree(String simpleQueryParam) {
        JSONArray jsonArrTree = new JSONArray();
        // 当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户组织id
        String currentOrgId = userId.substring(userId.indexOf(",") + 1);
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> userOwenOrgList = userService.getOrgChild(currentOrgId);
        if (CollectionUtils.isEmpty(userOwenOrgList)) {
            return jsonArrTree.toJSONString();
        }
        if (StringUtils.isBlank(simpleQueryParam)) {
            TreeUtils.treeAddOrg(userOwenOrgList, jsonArrTree);
            return jsonArrTree.toJSONString();
        }
        List<OrganizationLdap> superiorOrgList = new ArrayList<>();
        Set<String> superiorOrgIds = new HashSet<>();
        userOwenOrgList
            .stream()
            .filter(org -> org.getName().contains(simpleQueryParam))
            .forEach(org -> {
                String orgId = org.getId().toString();
                // 递归获取组织的上级所有组织(包含当前组织)
                TreeUtils.recursionGetSuperiorOrg(orgId, userOwenOrgList, superiorOrgList, superiorOrgIds);
            });
        TreeUtils.treeAddOrg(superiorOrgList, jsonArrTree);
        return jsonArrTree.toJSONString();
    }
}
