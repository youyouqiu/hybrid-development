package com.zw.api.service.impl;

import com.zw.api.service.SwaggerLdapService;
import com.zw.platform.basic.ldap.mapper.OrganizationContextMapper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.List;

@Service
public class SwaggerLdapServiceImpl implements SwaggerLdapService {
    @Autowired
    private LdapTemplate ldapTemplate;

    private static final String BASE = "ou=organization";

    @Override
    public OrganizationLdap getUuidByOrgCode(String orgCode) {
        String[] uuidAttrs = { "entryUUID", "l" };
        SearchControls searchCtls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, uuidAttrs, false, false);
        String filter = "(&(objectClass=organizationalUnit)(postalCode=" + orgCode + "))";
        List<OrganizationLdap> uuids = ldapTemplate.search(BASE, filter, searchCtls, new OrganizationContextMapper());
        if (uuids.isEmpty()) {
            throw new RuntimeException("组织不存在");
        }
        return uuids.get(0);
    }

    @Override
    public String getCurrentUserUuid() {
        String[] uuidAttrs = { "entryUUID" };
        SearchControls searchCtls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, uuidAttrs, false, false);
        String userId = SystemHelper.getCurrentUserId();
        String filter = "(&(objectClass=person)(uid=" + userId + "))";
        List<String> uuids = ldapTemplate.search(BASE, filter, searchCtls,
            (AttributesMapper<String>) attributes -> attributes.get("entryUUID").get().toString());
        if (uuids.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return uuids.get(0);
    }
}
