package com.zw.api.service;

import com.zw.platform.domain.core.OrganizationLdap;

public interface SwaggerLdapService {
    OrganizationLdap getUuidByOrgCode(String orgCode);

    String getCurrentUserUuid();
}
