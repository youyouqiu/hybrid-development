package com.zw.platform.basic.ldap.mapper;

import com.zw.platform.domain.leaderboard.EnterpriseLdapInfo;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * @author Administrator
 */
public class EnterpriseLdapInfoMapper implements ContextMapper<EnterpriseLdapInfo> {
    @Override
    public EnterpriseLdapInfo mapFromContext(Object ctx) throws org.springframework.ldap.NamingException {
        DirContextAdapter context = (DirContextAdapter) ctx;
        EnterpriseLdapInfo e = new EnterpriseLdapInfo();
        e.setGid(context.getStringAttribute("entryUUID"));
        e.setProvinceCode(context.getStringAttribute("myproc"));
        e.setCityCode(context.getStringAttribute("mycic"));
        e.setCountyCode(context.getStringAttribute("mycoc"));
        e.setLdapAddress(context.getStringAttribute("area"));
        e.setName(context.getStringAttribute("l"));
        return e;
    }
}