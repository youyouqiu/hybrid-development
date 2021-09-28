package com.zw.platform.basic.ldap.mapper;

import com.zw.platform.domain.core.OrganizationLdap;
import lombok.NoArgsConstructor;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;

/**
 * @author wanxing
 * @Title: 组织树ldapMapper
 * @date 2020/9/2717:42
 */
@NoArgsConstructor
public class OrganizationContextMapper implements ContextMapper<OrganizationLdap> {

    @Override
    public OrganizationLdap mapFromContext(Object ctx) throws NamingException {
        DirContextAdapter context = (DirContextAdapter) ctx;
        OrganizationLdap org = new OrganizationLdap();
        org.setOu(context.getStringAttribute("ou"));
        org.setDescription(context.getStringAttribute("description"));
        org.setOrganizationCode(context.getStringAttribute("postalCode"));
        org.setLicense(context.getStringAttribute("p0"));
        org.setRegisterDate(context.getStringAttribute("physicalDeliveryOfficeName"));
        org.setOperation(context.getStringAttribute("businessCategory"));
        org.setAddress(context.getStringAttribute("registeredAddress"));
        org.setPrincipal(context.getStringAttribute("st"));
        org.setPhone(context.getStringAttribute("telephoneNumber"));
        org.setName(context.getStringAttribute("l"));
        org.setUuid(context.getStringAttribute("entryUUID"));
        org.setOperatingState(context.getStringAttribute("operatingState"));
        org.setScopeOfOperation(context.getStringAttribute("scopeOfOperation"));
        org.setProvinceName(context.getStringAttribute("provinceName"));
        org.setIssuingOrgan(context.getStringAttribute("issuingOrgan"));
        org.setCityName(context.getStringAttribute("street"));
        org.setCountyName(context.getStringAttribute("countyName"));
        org.setAreaNumber(context.getStringAttribute("areaNumber"));
        org.setLicenseValidityStartDate(context.getStringAttribute("p7"));
        org.setLicenseValidityEndDate(context.getStringAttribute("p8"));
        org.setContactName(context.getStringAttribute("p9"));
        org.setUpOrganizationCode(context.getStringAttribute("upOrganizationCode"));
        org.setId(context.getDn());
        org.setEntryDN(context.getStringAttribute("entryDN"));
        org.setManagerOrganizationCode(context.getStringAttribute("k0"));
        org.setBusinessLicenseType(context.getStringAttribute("k8"));
        org.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
        org.setPrincipalPhone(context.getStringAttribute("destinationIndicator"));
        return org;
    }
}
