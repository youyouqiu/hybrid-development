package com.zw.platform.domain.core.impl;

import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.GroupRepoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class GroupRepoImpl implements GroupRepoExtension, BaseLdapNameAware {
    private static final LdapName ADMIN_USER = LdapUtils.newLdapName("cn=ZW Admin,ou=IT,ou=zw,ou=organization");

    private final LdapTemplate ldapTemplate;
    private LdapName baseLdapPath;

    @Autowired
    public GroupRepoImpl(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    @Override
    public List<String> getAllGroupNames() {
        LdapQuery query = query().attributes("cn")
                .where("objectclass").is("groupOfNames");

        return ldapTemplate.search(query, new AttributesMapper<String>() {
            @Override
            public String mapFromAttributes(Attributes attributes) throws NamingException {
                return (String) attributes.get("cn").get();
            }
        });
    }

    @Override
    public void create(Group group) {
        group.addMember(LdapUtils.prepend(ADMIN_USER, baseLdapPath));
        ldapTemplate.create(group);
    }
    /**
     * 添加用户到用户组
     * @param groupName
     * @param user
     * @author FanLu
     */
    @Override
    public void addMemberToGroup(String groupName, Name user) {
        Name groupDn = buildGroupDn(groupName);
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("member", user);

        ldapTemplate.modifyAttributes(ctx);
    }
    /**
     * 从用户组移除用户
     * @param groupName
     * @param user
     * @author FanLu
     */
    @Override
    public void removeMemberFromGroup(String groupName, Name user) {
        Name groupDn = buildGroupDn(groupName);
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.removeAttributeValue("member", user);

        ldapTemplate.modifyAttributes(ctx);
    }
    /**
     * 构建group的Name
     * @param groupName
     * @return
     * @author FanLu
     */
     
    private Name buildGroupDn(String groupName) {
        return LdapNameBuilder.newInstance("ou=Groups")
            .add("cn", groupName).build();
    }

}
