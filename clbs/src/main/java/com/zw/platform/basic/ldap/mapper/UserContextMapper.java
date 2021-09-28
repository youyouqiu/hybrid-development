package com.zw.platform.basic.ldap.mapper;

import com.zw.platform.basic.dto.UserDTO;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;

/**
 * @author wanxing
 * @Title: 用户ldap mapper
 * @date 2020/9/2911:58
 */
public class UserContextMapper implements ContextMapper<UserDTO> {

    @Override
    public UserDTO mapFromContext(Object ctx) throws NamingException {
        DirContextAdapter context = (DirContextAdapter) ctx;
        UserDTO p = new UserDTO();
        p.setFullName(transParameter(context.getStringAttribute("givenName")));
        p.setUsername(context.getStringAttribute("uid"));
        p.setMail(transParameter(context.getStringAttribute("mail")));
        p.setMobile(transParameter(context.getStringAttribute("mobile")));
        p.setId(context.getDn());
        p.setUserId(context.getDn().toString());
        p.setMember(LdapUtils.newLdapName(context.getStringAttribute("entryDN")));
        p.setCreateTimestamp(transParameter(context.getStringAttribute("createTimestamp")));
        p.setGender(transParameter(context.getStringAttribute("employeeType")));
        p.setState(transParameter(context.getStringAttribute("st")));
        p.setAuthorizationDate(transParameter(context.getStringAttribute("carLicense")));
        p.setUuid(context.getStringAttribute("entryuuid"));
        p.setIdentity(transParameter(context.getStringAttribute("employeeNumber")));
        p.setIndustry(transParameter(context.getStringAttribute("businessCategory")));
        p.setDuty(transParameter(context.getStringAttribute("departmentNumber")));
        p.setAdministrativeOffice(transParameter(context.getStringAttribute("displayName")));
        p.setDispatcherId(transParameter(context.getStringAttribute("telexNumber")));
        p.setSocialSecurityNumber(transParameter(context.getStringAttribute("socialSecurityNumber")));
        p.setIdentityNumber(transParameter(context.getStringAttribute("identityNumber")));
        return p;
    }

    /**
     * 转换参数
     * @param parameter
     * @return
     */
    private String transParameter(String parameter) {
        if ("null".equals(parameter)) {
            return null;
        }
        return parameter;
    }
}
