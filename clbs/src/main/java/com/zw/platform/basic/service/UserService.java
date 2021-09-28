package com.zw.platform.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.query.UserPageQuery;
import com.zw.platform.basic.dto.result.UserMenuDTO;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.ldap.query.SearchScope;

import javax.naming.ldap.LdapName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 用户管理类
 * @date 2020/9/2418:02
 */
public interface UserService extends IpAddressService {

    /**
     * 获取目录路径
     * @return
     */
    LdapName getBaseLdapPath();

    /**
     * 新增
     * @param userDto
     * @return
     * @throws Exception
     */
    JsonResultBean add(UserDTO userDto) throws Exception;

    /**
     * 用户正常下线处理(退出,修改密码,修改组织)
     * @param userName
     * @throws Exception
     */
    void updateUserOffline(String userName) throws Exception;

    /**
     * 修改
     * @param userDto
     * @return
     * @throws Exception
     */
    JsonResultBean update(UserDTO userDto) throws Exception;

    /**
     * 通过用户dn批量获取dn
     * @param entryDns
     * @return
     */
    List<UserDTO> getUsersByEntryDns(List<String> entryDns);

    /**
     * 过期
     * @param userName
     */
    void expireUserSession(String userName);

    /**
     * 通过dn查询用户
     * @param dn
     * @return
     */
    UserDTO getUserByEntryDn(String dn);

    UserDTO getByDn(String dn);

    /**
     * 通过用户名查询
     * @param username
     * @return
     */
    UserDTO getUserByUsername(String username);

    /**
     * 根据用户dn获取当前组织及下级组织Id
     * @param userDn userDn
     * @return List<String>
     * @author wangying
     */
    List<String> getOrgIdsByUserDn(String userDn);

    /**
     * 获取当前用户下的组织名称和组织uuid的map映射
     * @return
     */
    Map<String, String> getCurrentUserOrgNameOrgIdMap();

    /**
     * 获取当前用户下的组织uuid和组织名称的map映射
     * @return
     */
    Map<String, String> getCurrentUserOrgIdOrgNameMap();

    /**
     * 获取当前用户下的所有组织Id
     * @return List<String>
     * @author wangying
     */
    List<String> getCurrentUserOrgIds();

    /**
     * 通过userDn获取该用户下的所有组织
     * @param userDn
     * @return
     */
    List<OrganizationLdap> getOrgListByUserDn(String userDn);

    /**
     * 通过企业Dn 查询用户信息
     * @param orgDn
     * @param searchScope
     * @return
     */
    List<UserDTO> getUserByOrgDn(String orgDn, SearchScope searchScope);

    /**
     * 通过用户Dn查询组织DN
     * @param userDn
     * @return
     */
    String getUserOrgDnByDn(String userDn);

    /**
     * 获取当前用户下的orgDn
     * @return string
     */
    String getCurrentUserOrgDn();

    /**
     * 删除
     * @param userDn
     * @return
     */
    UserDTO delete(String userDn);

    List<UserDTO> deleteBatch(List<String> userDns);

    /**
     * 通过用户Dn获取用户名称
     * @param userDn
     * @return
     */
    String getUsernameByUserDn(String userDn);

    /**
     * 获取分页信息
     * @param query
     * @return
     * @throws Exception
     */
    Page<UserBean> getPageByKeyword(UserPageQuery query) throws Exception;

    /**
     * 获取当期用户uuid
     * @return
     */
    String getCurrentUserUuid();

    /**
     * 批量更新
     * @param userId
     * @param userName
     * @param passWord
     * @param state
     * @param authorizationDate
     * @throws Exception
     */
    void updateBatch(String userId, String userName, String passWord, String state, String authorizationDate)
        throws Exception;

    /**
     * 更新用户的密码
     * @param password
     * @param equipmentType APP
     * @throws Exception
     */
    void updatePassword(String password, String equipmentType) throws Exception;

    /**
     * 获取所有用户的uuid
     * @return
     */
    List<UserDTO> findAllUser();

    /**
     * 通过userDn 获取userUuid
     * @param userDn
     * @return
     */
    String getUserUuidByDn(String userDn);

    /**
     * 根据用户id获取当前组织及下级组织
     * @return List<String>
     * @author wangying
     */
    List<OrganizationLdap> getCurrentUseOrgList();

    /**
     * 获取当前用户的所属企业
     * @return
     */
    OrganizationLdap getCurrentUserOrg();

    /**
     * 通过用户uuid 获取用户信息
     * @param uuids
     * @return
     */
    List<UserDTO> getUserListByUuids(Collection<String> uuids);

    JsonResultBean deleteTalkBackDispatcherRoles(String[] roleDns);

    /**
     * 更新用户的角色列表，即添加用户到对应的用户组
     * @param userId
     * @param roleIds
     * @return
     * @throws Exception
     */
    JsonResultBean updateUserRole(String userId, String roleIds) throws Exception;

    /**
     * 通过企业名称模糊搜索用户下的企业id
     * @param name
     * @return
     */
    List<String> fuzzSearchUserOrgIdsByOrgName(String name);

    /**
     * 获取当前用户所在组织 如果当前用户具有admin权限，返回其下级组织uuid，若有多个平行下级组织，则获取其第一个下级组织的uuid 如果当前用户不具有admin权限，则直接返回其组织uuid
     * @return String
     * @author Liubangquan
     */
    String getOrgIdExceptAdmin();

    /**
     * 获取当前用户下的企业名称
     * @return
     */
    List<String> getCurrentUserOrgNames();

    /**
     * 获取当前用户信息
     * @return
     */
    UserDTO getCurrentUserInfo();

    /**
     * 获取admin用户信息
     * @return UserDTO
     */
    UserDTO getAdminUserInfo();

    /**
     * 判断是否是admin
     * @return
     */
    boolean isAdminRole();

    /**
     * 通过用户dn获取角色树
     * @param id
     * @return
     */
    JSONArray getRoleTreeByUserDn(String id);

    /**
     * 比较分配角色，是否超过当前用户
     * @param toString
     * @param roleList
     * @return
     */
    boolean compareAllotRole(String toString, List<String> roleList);

    /**
     * 删除对讲的角色
     * @param id
     * @return
     */
    JsonResultBean deleteTalkBackDispatcherRole(String id);

    /**
     * 聊天组树
     * @param type
     * @param groupId
     * @return
     * @throws Exception
     */
    String getChatGroupUserList(String type, String groupId) throws Exception;

    /**
     * 获取当前用户下的所有监控对象的id,人车物
     * @return
     */
    Set<String> getCurrentUserMonitorIds();

    /**
     * 获取当前用户下的所有监控对象的id,人车物
     * @return
     */
    Set<String> getCurrentUserMonitorIds(String userName);

    /**
     * 获取去也下的所有未绑定监控对象的id，人车物
     * @return
     */
    Set<String> getCurrentUserUnbindMonitorIds(List<String> orgIds);

    /**
     * 根据用户名获取该用户下的所有监控对象的ID，人车物
     * @param userName userName
     * @return Set<String>
     */
    Set<String> getMonitorIdsByUser(String userName);

    /**
     * 获取当前用户全下的分组集合
     * @return
     */
    List<GroupDTO> getCurrentUserGroupList();

    /**
     * 获取权限内的分组id和分组名称
     * @return Map<String, String>
     */
    Map<String, String> getCurrentGroupIdAndGroupName();

    /**
     * 获取当前用户全下的分组Id
     * @return
     */
    Set<String> getCurrentUserGroupIds();

    Set<String> getUserGroupIdsByUserName(String userName);

    /**
     * 获取当前用户下的分组树
     * @return
     */
    JSONArray getCurrentGroupTree();

    /**
     * 根据条件获取权限内有效的监控对象
     * @param fuzzySign  模糊搜索标识 0:监控对象; 1:分组; 2:企业(企业下的监控对象)
     * @param fuzzyParam 模糊搜索参数
     * @param deviceType 协议类型(查询协议类型下的监控对象) 为空查询所有车辆
     *                   -1、1：808-2013所有类型； 11：808-2019所有类型； 5：BDTD-SM； 9：ASO； 10：F3超长待机
     * @param moType     监控对象类型(不传查询所有的监控对象类型) 0：车； 1：人； 2：物
     * @param isNeedSort 是否需要排序(根据BIND_SORT_LIST排序)
     * @return List<String>
     */
    List<String> getValidVehicleId(String fuzzySign, String fuzzyParam, String deviceType, String moType,
        boolean isNeedSort);

    /**
     * 根据条件获取权限内有效的监控对象
     * @param orgId            企业id(查询企业所有的分组内的监控对象)
     * @param assignmentId     分组id(查询分组内的监控对象)
     *                         企业id 和 分组id 同时只能传一个 同时传只判断分组id
     * @param deviceType       协议类型(查询协议类型下的监控对象) 为空查询所有车辆
     *                         -1、1：808-2013所有类型； 11：808-2019所有类型； 5：BDTD-SM； 9：ASO； 10：F3超长待机
     * @param simpleQueryParam 监控名称模糊搜索条件
     * @param moType           监控对象类型(不传查询所有的监控对象类型) 0：车； 1：人； 2：物
     * @param isNeedSort       是否需要排序(根据BIND_SORT_LIST排序)
     * @return List<String>
     */
    List<String> getValidVehicleId(String orgId, String assignmentId, String deviceType, String simpleQueryParam,
        String moType, boolean isNeedSort);

    /**
     * 设置绑定信息的对象类型名称
     * @param bindDtoList 绑定信息
     */
    void setObjectTypeName(Collection<BindDTO> bindDtoList);

    /**
     * 设置绑定信息的对象类型名称
     * @param bindDTO 绑定信息
     */
    void setObjectTypeName(BindDTO bindDTO);

    /**
     * 获得当前用户下级企业的相关信息
     * @return
     */
    Map<String, GroupRank> getCurrentUserOrgInfoList();

    /**
     * 角色分配给用户
     * @param userIds userIds
     * @param roleId  roleId
     * @return JsonResultBean
     */
    JsonResultBean updateUserByRole(String userIds, String roleId);

    /**
     * 获取监获取用户当前组织或admin用户第一个组织
     * @return 组织
     */
    OrganizationLdap getCurUserOrgAdminFirstOrg();

    /**
     * 模糊搜索过滤企业id
     * @param simpleQueryParam 模糊搜索条件
     * @param orgIds 需要过滤的企业id
     * @return  List<String>
     */
    Set<String> fuzzySearchFilterOrgIds(String simpleQueryParam, String orgIds);

    /**
     * 模糊搜索过滤监控对象id
     * @param simpleQueryParam 模糊搜索条件
     * @param monitorIds       需要过滤的监控对象id
     * @return List<String>
     */
    Set<String> fuzzySearchFilterMonitorIds(String simpleQueryParam, String monitorIds);

    /**
     * 根据用户名获取用户及菜单权限信息
     * @param username username
     * @return UserMenuDTO
     */
    UserMenuDTO loadUserPermission(String username);

}
