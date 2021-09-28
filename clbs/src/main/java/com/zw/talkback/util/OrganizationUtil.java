package com.zw.talkback.util;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.talkback.domain.basicinfo.Cluster;
import org.apache.commons.collections.CollectionUtils;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/15
 */
public class OrganizationUtil {

    public static Set<OrganizationLdap> filterOrgList(List<OrganizationLdap> orgList, List<Cluster> assignList) {
        if (orgList == null || orgList.isEmpty() || assignList == null || assignList.isEmpty()) {
            return new HashSet<>();
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
        for (Cluster cluster : assignList) {
            orgUuid = cluster.getGroupId();
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
        return res;
    }

    public static Set<OrganizationLdap> filterOrgListNew(List<OrganizationLdap> orgList, List<Cluster> assignList) {
        Set<OrganizationLdap> res = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(assignList)) {
            return new HashSet<>();
        }
        Map<String, OrganizationLdap> orgDnLookup =
            orgList.stream().collect(Collectors.toMap(info -> info.getId().toString(), info -> info));
        for (OrganizationLdap ldap : orgList) {
            String ldapUuid = ldap.getUuid();
            boolean isAlreadyExist = res.stream().anyMatch(info -> Objects.equals(info.getUuid(), ldapUuid));
            if (isAlreadyExist) {
                continue;
            }
            List<Cluster> filterClusterList =
                assignList.stream().filter(info -> Objects.equals(ldapUuid, info.getGroupId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filterClusterList)) {
                res.add(ldap);
                res.addAll(getOrgParents(ldap, orgDnLookup));
            }
        }
        return res;
    }

    private static List<OrganizationLdap> getOrgParents(OrganizationLdap org,
        Map<String, OrganizationLdap> orgDnLookup) {
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
