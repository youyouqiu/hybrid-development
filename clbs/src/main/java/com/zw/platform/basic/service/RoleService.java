package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.zw.lkyw.domain.ReportMenu;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 角色类
 * @date 2020/9/2915:51
 */
public interface RoleService extends IpAddressService {
    /**
     * 查询ldap
     * @param name
     * @return
     */
    Collection<Group> getByMemberName(Name name);

    /**
     * 查询ldap
     * @param name
     * @return
     */
    Collection<Group> getByMemberNameStr(String name);

    @MethodLog(name = "根据role查询是否有权限", description = "根据role查询是否有权限")
    List<String> getMenuEditableByRoles(List<String> roleList) throws Exception;

    List<Resource> getMenuByRoles(List<String> roleList) throws Exception;

    Set<String> listMenuIdByRoleId(Collection<String> roleIds);

    /**
     * 更新ldap
     * @param groups
     * @param originalId
     * @param newId
     */
    void ldapGroupReferences(Collection<Group> groups, Name originalId, Name newId);

    void deleteUser(String roleDn, Name user);

    /**
     * chaxu
     * @param searchParam
     * @param checkAdminRole
     * @return
     */
    List<Group> getListByKeyword(String searchParam, boolean checkAdminRole);

    /**
     * 获取所有的角色
     * @return
     */
    List<Group> getAllGroup();

    void deleteSilentRole(LdapName name);

    void removeMemberFromGroup(String groupName, Name user);

    void addMemberToGroup(String groupName, Name user);

    List<LdapName> getMemberNameListByRoleCn(String roleCn);

    /**
     * 根据roleDn获取角色信息
     * @param roleDn roleDn
     * @return Group
     */
    Group getGroupById(String roleDn);

    /**
     * 获取该角色用户组织树
     * @param roleMembers roleMembers
     * @return JSONArray
     */
    JSONArray getRoleUserTree(Set<Name> roleMembers);

    /**
     * 获取角色权限菜单树
     * @param roleDn roleDn
     * @return JSONArray
     */
    JSONArray generateTree(String roleDn);

    /**
     * 根据新增角色权限判断当前新增的角色是否在当前用户的角色权限之内
     * @param formList formList
     * @return ture 表示不在用户的角色权限之内 false 表示存在
     */
    boolean compareUserForm(List<RoleResourceForm> formList);

    /**
     * 新增角色
     * @param group group
     */
    void addRole(Group group);

    /**
     * 新增角色资源关联信息
     * @param list 角色资源关联入参
     * @return boolean
     */
    boolean addRoleResourceByBatch(List<RoleResourceForm> list);

    /**
     * 更新某用户下的角色（用于新增角色时，分配给创建用户和admin用户）
     * @param userDn   userDn
     * @param roleName 角色name（Ldap中的cn）
     */
    void addAllotRole(String userDn, String roleName);

    /**
     * 修改角色
     * @param groupId            groupId
     * @param group              group
     * @param permissionEditTree permissionEditTree
     * @return JsonResultBean
     */
    JsonResultBean updateRole(String groupId, Group group, String permissionEditTree);

    /**
     * 根据ID删除角色
     * @param groupIds groupIds（分号隔开）
     * @return String
     */
    String deleteGroup(String groupIds);

    Map<String, List<ReportMenu>> getUserIntersectionMenu();

    /**
     * 判断当前用户所拥有的角色,是否拥有分组管理的可写权限
     * @return true:有 false:无
     */
    boolean isGroupWritePower();

}
