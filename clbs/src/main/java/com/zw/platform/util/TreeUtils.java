package com.zw.platform.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/7/11 18:05
 @Description 树通用工具类
 @version 1.0
 **/
public class TreeUtils {

    public static List<OrganizationLdap> getFilterWholeOrgList(List<OrganizationLdap> orgs,
        List<OrganizationLdap> filterList) {
        List<String> orgIdList = new ArrayList<>();
        Set<OrganizationLdap> orgResult = new HashSet<>();
        if (CollectionUtils.isNotEmpty(filterList)) {
            for (OrganizationLdap org : filterList) {
                orgIdList.add(org.getUuid());
                getLowerOrg(orgResult, orgs, org);
            }
        }
        return new ArrayList<>(orgResult);

    }

    public static void getLowerOrg(Set<OrganizationLdap> resultOrgList, List<OrganizationLdap> allOrgList,
        OrganizationLdap filterOrg) {
        for (OrganizationLdap org : allOrgList) {
            if (org.getId().toString().equals(filterOrg.getId().toString())) {
                resultOrgList.add(org);
            }
            if (org.getId().toString().equals(filterOrg.getPid())) {
                resultOrgList.add(org);
                getLowerOrg(resultOrgList, allOrgList, org);
            }
        }
    }

    public static void filterOrgByGroupOrgId(Set<OrganizationLdap> resultOrgList, List<OrganizationLdap> allOrgList,
        String groupOrgId) {
        for (OrganizationLdap org : allOrgList) {
            if (org.getId().toString().equals(groupOrgId)) {
                resultOrgList.add(org);
                filterOrgByGroupOrgId(resultOrgList, allOrgList, org.getPid());
            }
        }
    }

    /**
     * 用组织id在组织列表中查询下级组织返回
     * @param resultOrgSet 返回的组织集合
     * @param orgList      组织列表
     * @param groupId      组织id
     */
    public static void getLowerOrg(Set<OrganizationLdap> resultOrgSet, List<OrganizationLdap> orgList, String groupId) {
        for (OrganizationLdap org : orgList) {
            if (org.getId().toString().equals(groupId)) {
                resultOrgSet.add(org);
                getLowerOrg(resultOrgSet, orgList, org.getPid());
            }
        }
    }

    /**
     * 组装分组树
     */
    public static void treeAddAssignment(Collection<Assignment> assignmentList,
        Map<String, OrganizationLdap> orgUuidAndOrgInfoMap, JSONArray tree) {
        if (CollectionUtils.isEmpty(assignmentList)) {
            return;
        }
        for (Assignment assignment : assignmentList) {
            JSONObject assignmentTreeNode = new JSONObject();
            assignmentTreeNode.put("id", assignment.getAssignmentId());
            String groupId = assignment.getGroupId();
            OrganizationLdap org = orgUuidAndOrgInfoMap.get(groupId);
            assignmentTreeNode.put("pId", org.getId().toString());
            assignmentTreeNode.put("name", assignment.getName());
            assignmentTreeNode.put("type", "assignment");
            assignmentTreeNode.put("iconSkin", "assignmentSkin");
            assignmentTreeNode.put("pName", org.getName());
            tree.add(assignmentTreeNode);
        }
    }

    /**
     * 组装分组树
     */
    public static void treeAddOrg(Collection<OrganizationLdap> orgList, JSONArray tree) {
        if (CollectionUtils.isEmpty(orgList)) {
            return;
        }
        for (OrganizationLdap org : orgList) {
            JSONObject orgTreeNode = new JSONObject();
            orgTreeNode.put("id", org.getCid());
            orgTreeNode.put("pId", org.getPid());
            orgTreeNode.put("name", org.getName());
            orgTreeNode.put("iconSkin", "groupSkin");
            orgTreeNode.put("type", "group");
            orgTreeNode.put("open", true);
            orgTreeNode.put("uuid", org.getUuid());
            if ("ou=organization".equals(org.getCid())) {
                orgTreeNode.put("pId", "0");
            }
            tree.add(orgTreeNode);
        }
    }

    /**
     * 递归获取组织的上级所有组织(包含当前组织)
     * @param nowOrgId 当前的组织id 不是uuid
     * @param orgList 权限内的所有组织
     * @param superiorOrgList 上级的所有组织
     * @param superiorOrgIds 上级的所有组织Id 不是uuid
     */
    public static void recursionGetSuperiorOrg(String nowOrgId, List<OrganizationLdap> orgList,
        List<OrganizationLdap> superiorOrgList, Set<String> superiorOrgIds) {
        for (OrganizationLdap org : orgList) {
            String orgId = org.getId().toString();
            if (superiorOrgIds.contains(orgId) || !Objects.equals(nowOrgId, orgId)) {
                continue;
            }
            superiorOrgList.add(org);
            superiorOrgIds.add(orgId);
            recursionGetSuperiorOrg(org.getPid(), orgList, superiorOrgList, superiorOrgIds);
        }
    }
}
