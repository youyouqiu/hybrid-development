package com.zw.platform.util;

import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.core.OrganizationLdap;
import org.apache.commons.collections.CollectionUtils;

import javax.naming.ldap.LdapName;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Chen Feng
 * @version 1.0 2019/1/15
 */
public class OrganizationUtil {

    public static List<OrganizationLdap> filterOrgList(List<OrganizationLdap> orgList, List<Assignment> assignList) {
        if (orgList == null || orgList.isEmpty() || assignList == null || assignList.isEmpty()) {
            return new ArrayList<>();
        }
        HashMap<String, OrganizationLdap> orgIdLookup = new HashMap<>(orgList.size());
        HashMap<String, OrganizationLdap> orgDnLookup = new HashMap<>(orgList.size());
        for (OrganizationLdap org : orgList) {
            orgIdLookup.put(org.getUuid(), org);
            orgDnLookup.put(org.getId().toString(), org);
        }

        Set<OrganizationLdap> res = new LinkedHashSet<>(orgList.size());
        List<OrganizationLdap> parents;
        OrganizationLdap org;
        String orgUuid;
        for (Assignment assignment : assignList) {
            orgUuid = assignment.getGroupId();
            org = orgIdLookup.get(orgUuid);
            if (org == null) {
                continue;
            }
            res.add(org);

            parents = getOrgParents(org, orgDnLookup);
            for (OrganizationLdap parent : parents) {
                res.add(parent);
                orgIdLookup.remove(parent.getUuid());
            }
            orgIdLookup.remove(orgUuid);
        }
        // 为了保证组织的顺序,这里再做一道过滤
        List<String> resultOrgId = res.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        orgList = orgList.stream().filter(e -> resultOrgId.contains(e.getUuid())).collect(Collectors.toList());
        return orgList;
    }

    /**
     * 通过分组过滤组织
     * @param orgList
     * @param groupDTOList
     * @return
     */
    public static List<OrganizationLdap> filterOrgListByGroup(List<OrganizationLdap> orgList,
        List<GroupDTO> groupDTOList) {

        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(groupDTOList)) {
            return new ArrayList<>();
        }
        HashMap<String, OrganizationLdap> orgIdLookup = new HashMap<>(CommonUtil.ofMapCapacity(orgList.size()));
        HashMap<String, OrganizationLdap> orgDnLookup = new HashMap<>(CommonUtil.ofMapCapacity(orgList.size()));
        for (OrganizationLdap org : orgList) {
            orgIdLookup.put(org.getUuid(), org);
            orgDnLookup.put(org.getId().toString(), org);
        }
        Set<OrganizationLdap> res = new LinkedHashSet<>(orgList.size());
        List<OrganizationLdap> parents;
        OrganizationLdap org;
        String orgUuid;
        for (GroupDTO groupDTO : groupDTOList) {
            orgUuid = groupDTO.getOrgId();
            org = orgIdLookup.get(orgUuid);
            if (org == null) {
                continue;
            }
            res.add(org);
            parents = getOrgParents(org, orgDnLookup);
            for (OrganizationLdap parent : parents) {
                res.add(parent);
                orgIdLookup.remove(parent.getUuid());
            }
            orgIdLookup.remove(orgUuid);
        }
        // 为了保证组织的顺序,这里再做一道过滤
        List<String> resultOrgId = res.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        orgList = orgList.stream().filter(e -> resultOrgId.contains(e.getUuid())).collect(Collectors.toList());
        return orgList;
    }

    private static List<OrganizationLdap> getOrgParents(OrganizationLdap org,
        HashMap<String, OrganizationLdap> orgDnLookup) {
        LdapName name = (LdapName) org.getId();
        int rdnCount = name.getRdns().size();
        List<OrganizationLdap> res = new ArrayList<>();
        OrganizationLdap orgLdap;
        String dn;
        for (int i = rdnCount - 1; i > 1; i--) {
            dn = name.getPrefix(i).toString();
            orgLdap = orgDnLookup.get(dn);
            if (orgLdap == null) {
                break;
            }
            res.add(orgLdap);
            orgDnLookup.remove(dn);
        }
        return res;
    }
}
