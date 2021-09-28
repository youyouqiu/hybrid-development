package com.zw.platform.basic.ldap.mapper;

import com.zw.platform.domain.core.Group;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class GroupContextMapper implements ContextMapper<Group> {

    @Override
    public Group mapFromContext(Object ctx) throws NamingException {
        DirContextAdapter context = (DirContextAdapter) ctx;
        Group p = new Group();
        p.setRoleName(context.getStringAttribute("o"));
        p.setName(context.getStringAttribute("cn"));
        p.setDescription(context.getStringAttribute("description"));
        p.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
        p.setId(context.getDn());
        return p;
    }
}