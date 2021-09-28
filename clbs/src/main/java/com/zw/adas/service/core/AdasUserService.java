package com.zw.adas.service.core;

import com.zw.adas.domain.core.OrganizationLdapAdas;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.UserBean;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.Collection;
import java.util.List;

/***
 @Author gfw
 @Date 2019/5/24 9:48
 @Description Ldap目录数据库crud
 @version 1.0
 **/
public interface AdasUserService {
    /**
     * 根据entryDn 查询用户组织
     *
     * @param id
     * @return
     */
    OrganizationLdapAdas getOrgByEntryDN(String id);

    OrganizationLdapAdas getOrgByUuid(String uuid);

    LdapName getBaseLdapPath();

    Collection<Group> findByMember(Name member);

    /**
     * 获取用户当前及下级组织列表
     * @param userId userId
     * @return result
     * @author lijie
     */
    List<String> getOrgUuidsByUser(String userId);

    /**
     * 获取用户的uuid
     * @param userId userId
     * @return result
     */
    String getUserUuidById(String userId);

    /**
     * 根据uuid查询用户
     * @param id id
     * @return user
     * @author lijie
     */
    UserBean getUserByEntryDn(String id);

    /**
     * 根据当前用户获取其所属组织uuid
     * @return String result
     * @author lijie
     */
    String getOrgUuidByUser();

    List<OrganizationLdapAdas> getAllOrganization();
}
