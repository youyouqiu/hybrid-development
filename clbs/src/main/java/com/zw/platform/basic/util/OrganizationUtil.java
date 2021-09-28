package com.zw.platform.basic.util;

import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.collections.CollectionUtils;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织工具类
 * @author Admin
 */
public class OrganizationUtil {

    /**
     * 根据UUID过滤掉非orgIds及其对应的上级组织的组织
     * @param orgList 组织
     * @param orgIds  拥有数据的组织uuid集合
     * @return 过滤后的组织
     */
    public static List<OrganizationLdap> filterOrgListByUuid(List<OrganizationLdap> orgList,
        Collection<String> orgIds) {
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }

        Map<String, OrganizationLdap> orgIdLookup = new HashMap<>(orgList.size());
        Map<String, OrganizationLdap> orgDnLookup = new HashMap<>(orgList.size());
        for (OrganizationLdap org : orgList) {
            orgIdLookup.put(org.getUuid(), org);
            orgDnLookup.put(org.getId().toString(), org);
        }

        OrganizationLdap org;
        Set<OrganizationLdap> filterOrgList = new HashSet<>();
        for (String orgId : orgIds) {
            org = orgIdLookup.get(orgId);
            if (org == null) {
                continue;
            }
            filterOrgList.addAll(getOrgParents(org, orgDnLookup));
        }
        // 为了保证组织的顺序,这里再做一道过滤进行排序
        Set<String> filterUuidSet = filterOrgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toSet());
        return orgList.stream().filter(o -> filterUuidSet.contains(o.getUuid())).collect(Collectors.toList());
    }

    /**
     * 根据DN过滤掉非orgDnList及其对应的上级组织的组织
     * @param orgList   用户权限下的组织
     * @param orgDnList 当前拥有用数据的组织DN集合
     * @return 过滤后的组织
     */
    public static List<OrganizationLdap> filterOrgListByDn(List<OrganizationLdap> orgList, Set<String> orgDnList) {
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(orgDnList)) {
            return new ArrayList<>();
        }
        Map<String, OrganizationLdap> orgDnLookup = new HashMap<>(orgList.size());
        for (OrganizationLdap org : orgList) {
            orgDnLookup.put(org.getId().toString(), org);
        }
        Set<OrganizationLdap> filterOrgList = new HashSet<>();
        OrganizationLdap org;
        for (String orgDn : orgDnList) {
            if (!orgDnLookup.containsKey(orgDn)) {
                continue;
            }
            org = orgDnLookup.get(orgDn);
            filterOrgList.addAll(getOrgParents(org, orgDnLookup));
        }
        // 为了保证组织的顺序,这里再做一道过滤进行排序
        Set<String> filterUuidSet = filterOrgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toSet());
        return orgList.stream().filter(o -> filterUuidSet.contains(o.getUuid())).collect(Collectors.toList());
    }

    /**
     * 获取组织的当前及上级组织(包含当前组织)
     * @param org      组织
     * @param orgDnMap dn-org 的map映射关系
     * @return 上级组织列表
     */
    private static List<OrganizationLdap> getOrgParents(OrganizationLdap org, Map<String, OrganizationLdap> orgDnMap) {
        if (!orgDnMap.containsKey(org.getId().toString())) {
            return new ArrayList<>();
        }
        LdapName name = (LdapName) org.getId();
        int rdnCount = name.getRdns().size();
        List<OrganizationLdap> upOrgList = new ArrayList<>();
        OrganizationLdap orgLdap;
        String dn;
        for (int i = rdnCount; i > 1; i--) {
            dn = name.getPrefix(i).toString();
            orgLdap = orgDnMap.get(dn);
            if (orgLdap == null) {
                break;
            }
            orgDnMap.remove(dn);
            upOrgList.add(orgLdap);
        }
        return upOrgList;
    }

}
