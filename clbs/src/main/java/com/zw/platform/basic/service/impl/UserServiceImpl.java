package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.service.ChatGroupUserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.CommonConstants;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.UserPageQuery;
import com.zw.platform.basic.dto.result.UserMenuDTO;
import com.zw.platform.basic.ldap.mapper.UserContextMapper;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.basic.repository.ThingDao;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.CustomTokenServices;
import com.zw.platform.commons.CustomTokenStore;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.UserRepo;
import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.repository.modules.OrgInspectionExtraUserDAO;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.OrganizationUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Constants;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.talkback.domain.intercom.form.IntercomIotUserForm;
import com.zw.talkback.repository.mysql.CallNumberDao;
import com.zw.talkback.repository.mysql.IntercomIotUserDao;
import com.zw.talkback.util.CallNumberExhaustException;
import com.zw.talkback.util.CallNumberUtil;
import com.zw.talkback.util.TalkCallUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wanxing
 * @Title: 用户管理类
 * @date 2020/9/259:16
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService, BaseLdapNameAware {

    private LdapName baseLdapPath;

    @Override
    public LdapName getBaseLdapPath() {
        return baseLdapPath;
    }

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OperationService operationService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private IntercomIotUserDao intercomIotUserDao;

    @Autowired
    private TalkCallUtil talkCallUtils;

    @Autowired
    private CallNumberDao callNumberDao;

    @Autowired
    private ChatGroupUserService chatGroupUserService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private ThingDao thingDao;

    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;

    @Autowired
    private OrgInspectionExtraUserDAO orgInspectionExtraUserDAO;

    @Autowired
    private CustomTokenServices tokenServices;

    @Autowired
    private CustomTokenStore tokenStore;


    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    @Override
    public JsonResultBean add(UserDTO userDto) throws Exception {
        Attributes userAttributes = getUserAttributes(userDto);
        Name userDn = organizationService.bindDn(userDto.getUsername(), userDto.getOrgDn());
        //插入ldap
        try {
            ldapTemplate.bind(userDn, null, userAttributes);
        } catch (Exception e) {
            return new JsonResultBean(false, "用户已存在");
        }
        String log = "新增用户：" + userDto.getUsername() + "(@" + userDto.getOrgName() + ")";
        // 添加新增用户日志
        logSearchService.addLog(getIpAddress(), log, "3", "", "-", "");
        return new JsonResultBean(true, "创建用户成功");
    }

    /**
     * 用户正常下线处理(退出,修改密码,修改组织)
     */
    @Override
    public void updateUserOffline(String userName) throws Exception {
        updateUserOffline(getIpAddress(), userName);
    }

    /**
     * 创建用户的attribute
     * @param userDto
     * @return
     */
    private Attributes getUserAttributes(UserDTO userDto) {
        Attribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("person");
        objectClass.add("organizationalPerson");
        objectClass.add("inetOrgPerson");
        Attributes userAttributes = new BasicAttributes();
        userAttributes.put(objectClass);
        userAttributes.put("cn", userDto.getUsername());
        userAttributes.put("sn", userDto.getUsername());
        if (StringUtils.isNotBlank(userDto.getMail())) {
            userAttributes.put("mail", userDto.getMail());
        }
        if (StringUtils.isNotBlank(userDto.getMobile())) {
            userAttributes.put("mobile", userDto.getMobile());
        }
        if (StringUtils.isNotBlank(userDto.getGender())) {
            userAttributes.put("employeeType", userDto.getGender());
        }
        if (StringUtils.isNotBlank(userDto.getFullName())) {
            userAttributes.put("givenName", userDto.getFullName());
        }
        if (StringUtils.isNotBlank(userDto.getState())) {
            userAttributes.put("st", userDto.getState());
        }
        if (StringUtils.isNotBlank(userDto.getAuthorizationDate())) {
            userAttributes.put("carLicense", userDto.getAuthorizationDate());
        }
        if (StringUtils.isNotEmpty(userDto.getIdentity())) {
            userAttributes.put("employeeNumber", userDto.getIdentity());
        }
        if (StringUtils.isNotEmpty(userDto.getIndustry())) {
            userAttributes.put("businessCategory", userDto.getIndustry());
        }
        if (StringUtils.isNotEmpty(userDto.getDuty())) {
            userAttributes.put("departmentNumber", userDto.getDuty());
        }
        if (StringUtils.isNotEmpty(userDto.getAdministrativeOffice())) {
            userAttributes.put("displayName", userDto.getAdministrativeOffice());
        }
        if (StringUtils.isNotEmpty(userDto.getIdentityNumber())) {
            userAttributes.put("identityNumber", userDto.getIdentityNumber());
        }
        if (StringUtils.isNotEmpty(userDto.getSocialSecurityNumber())) {
            userAttributes.put("socialSecurityNumber", userDto.getSocialSecurityNumber());
        }
        userAttributes.put("userPassword", SecurityPasswordHelper.encodePassword(userDto.getPassword()));
        return userAttributes;
    }

    @Override
    public JsonResultBean update(UserDTO userDto) throws Exception {
        String userDn = userDto.getUserId();
        LdapName dn = LdapUtils.newLdapName(userDn);
        UserBean beforeUser = userRepo.findOne(dn);
        if (beforeUser == null) {
            return new JsonResultBean(false, "用户不存在");
        }
        // 对比修改前后用户名
        String beforeName = beforeUser.getUsername();
        //如果修改前后用户名不一致则不允许修改
        if (!beforeName.equals(userDto.getUsername())) {
            return new JsonResultBean(JsonResultBean.FAULT, "用户名不能修改");
        }
        boolean passwordChangeFlag = modifyPasswordFlag(userDto);
        boolean orgChangeFlag = false;
        //绑定企业
        Name newDn = organizationService.bindDn(userDto.getUsername(), userDto.getOrgDn());
        // 如果用户修改了部门，则需要将用户移到新的部门
        if (!dn.equals(newDn)) {
            orgChangeFlag = true;
            passwordChangeFlag = true;
            ldapTemplate.rename(dn, newDn);
            // 修改角色里面的member
            LdapName oldUser = LdapUtils.newLdapName(userDn + "," + baseLdapPath.toString());
            LdapName newUser = LdapUtils.newLdapName(newDn.toString() + "," + baseLdapPath.toString());
            //用户角色
            Collection<Group> groups = roleService.getByMemberName(oldUser);
            roleService.ldapGroupReferences(groups, oldUser, newUser);
            userDto.setId(newDn);
            // 删除查岗额外接收人设置
            orgInspectionExtraUserDAO.deleteByUsernameIn(Collections.singleton(userDto.getUsername()));
        }
        DirContextOperations context = ldapTemplate.lookupContext(newDn);
        checkNullField(userDto, beforeUser, context);
        String ipAddress = getIpAddress();
        // 日志记录
        String log;
        // 获得部门组织架构
        OrganizationLdap org = organizationService.getOrgByEntryDn(getUserOrgDnByDn(userDn));
        if (orgChangeFlag) {
            //修改了企业后删除用户的用户分组表数据
            deleteUserGroup(newDn);
            log = "修改用户：" + userDto.getUsername() + "(@" + org.getName() + ")" + " 修改为 " + userDto.getUsername() + "(@"
                + userDto.getOrgName() + ")";
            //删除用户缓存
            RedisHelper.delete(RedisKeyEnum.USER_GROUP.of(userDto.getUsername()));
        } else {
            log = "修改用户：" + userDto.getUsername() + "(@" + org.getName() + ")";
        }
        logSearchService.addLog(ipAddress, log, "3", "", "-", "");
        // 用户模块修改他人密码后，如果此用户在线，需要记录用户的下线时间和重新登录
        if (passwordChangeFlag) {
            //更新用户离线信息
            updateUserOffline(ipAddress, userDto.getUsername());
            //过期session
            expireUserSession(userDto.getUsername());
        }
        ldapTemplate.modifyAttributes(context);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public List<UserDTO> getUsersByEntryDns(List<String> entryDns) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        OrFilter orFilter = new OrFilter();
        for (String entryDn : entryDns) {
            orFilter.or(new EqualsFilter("entryDN", entryDn + ",dc=zwlbs,dc=com"));
        }
        filter.and(orFilter);
        List<UserDTO> userList = ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList;
    }

    /**
     * 删除用户-分组的关系
     * @param newDn
     */
    private void deleteUserGroup(Name newDn) {
        //删除用户分组关系
        UserDTO userDTO = getUserByEntryDn(newDn.toString());
        String username = userDTO.getUsername();
        String userId = userDTO.getUuid();
        boolean result = userGroupService.deleteByUserId(userId);
        if (result) {
            RedisKey userGroupKey = RedisKeyEnum.USER_GROUP.of(username);
            RedisHelper.delete(userGroupKey);
        }
    }

    /**
     * 过期session
     * @param userName
     */
    @Override
    public void expireUserSession(String userName) {
        List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            if (!(principal instanceof UserLdap)) {
                continue;
            }
            final UserLdap authUser = (UserLdap) principal;
            if (!userName.equals(authUser.getUsername())) {
                continue;
            }
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                session.expireNow();
            }
        }
        removeOAuth2Token("web_1", userName);
        removeOAuth2Token("mobile_1", userName);
    }

    private void removeOAuth2Token(String clientId, String userName) {
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(clientId, userName);
        for (OAuth2AccessToken token : tokens) {
            tokenServices.revokeToken(token.getValue());
        }
    }

    /**
     * 根据用户DN查询用户
     * @param dn
     * @return user
     * @author wangying
     */
    @Override
    public UserDTO getUserByEntryDn(String dn) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense", "employeeNumber", "businessCategory", "departmentNumber", "displayName",
                "socialSecurityNumber", "identityNumber", "street" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        filter.and(new EqualsFilter("entryDN", dn + ",dc=zwlbs,dc=com"));
        List<UserDTO> users = ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        UserDTO user = users.get(0);
        String userId = user.getId().toString();
        // 根据用户id得到用户所属组织
        int beginIndex = userId.indexOf(',');
        String orgDn = userId.substring(beginIndex + 1);
        OrganizationLdap org = organizationService.getOrgByEntryDn(orgDn);
        if (org == null) {
            return null;
        }
        user.setOrgDn(orgDn);
        user.setOrgName(org.getName());
        user.setOrgId(org.getUuid());
        return user;
    }

    /**
     * 根据uuid查询User
     * @param username username
     * @return List<UserBean>
     * @author wangying
     */
    @Override
    public UserDTO getUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return new UserDTO();
        }
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        //历史原因，uid实际上存放的是用户名
        andfilter.and(new EqualsFilter("uid", username));
        List<UserDTO> result = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new UserContextMapper());
        if (!CollectionUtils.isEmpty(result)) {
            return result.get(0);
        }
        return new UserDTO();
    }

    /**
     * 根据用户id获取当前组织及下级组织Dn
     * @param userDn userDn
     * @return List<String>
     * @author wangying
     */
    @Override
    public List<String> getOrgIdsByUserDn(String userDn) {
        // 获取用户所在组织及下级组织
        List<OrganizationLdap> orgs = getOrgListByUserDn(userDn);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        return userOrgListId;
    }

    /**
     * 获取当前用户下的组织名称和组织uuid的map映射
     * @return
     */
    @Override
    public Map<String, String> getCurrentUserOrgNameOrgIdMap() {
        List<OrganizationLdap> orgs = getOrgListByUserDn(SystemHelper.getCurrentUId());
        Map<String, String> map = new HashMap<>(orgs.size());
        for (OrganizationLdap org : orgs) {
            map.put(org.getName(), org.getUuid());
        }
        return map;
    }

    /**
     * 获取当前用户下的组织uuid和组织名称的map映射
     * @return
     */
    @Override
    public Map<String, String> getCurrentUserOrgIdOrgNameMap() {
        List<OrganizationLdap> orgs = getOrgListByUserDn(SystemHelper.getCurrentUId());
        Map<String, String> map = new HashMap<>(orgs.size());
        for (OrganizationLdap org : orgs) {
            map.put(org.getUuid(), org.getName());
        }
        return map;
    }

    /**
     * 获取当前组织及下级组织dn
     * @return List<String>
     * @author wangying
     */
    @Override
    public List<String> getCurrentUserOrgIds() {
        return getOrgIdsByUserDn(SystemHelper.getCurrentUId());
    }

    @Override
    public List<OrganizationLdap> getOrgListByUserDn(String userDn) {
        String orgDn = getUserOrgDnByDn(userDn);
        return organizationService.getOrgChildList(orgDn);
    }

    /**
     * 通过企业Dn获取用户
     * @param orgDn       企业Dn
     * @param searchScope 等级
     * @return
     */
    @Override
    public List<UserDTO> getUserByOrgDn(String orgDn, SearchScope searchScope) {
        return ldapTemplate.search(LdapQueryBuilder.query().base(orgDn).searchScope(searchScope)
            .attributes("entryDN", "entryUUID", "givenName", "uid", "createTimestamp", "telexNumber")
            .where("objectclass").is("inetOrgPerson"), new UserContextMapper());
    }

    private boolean modifyPasswordFlag(UserDTO userDto) {
        return StringUtil.isNotEmpty(userDto.getPassword());
    }

    /**
     * 用户正常下线处理(退出,修改密码,修改组织)
     */
    public void updateUserOffline(String ipAddress, String userName) throws Exception {
        String msg = "用户 : " + userName + " 退出登录";
        logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
    }

    /**
     * 根据用户Dn获取组织的Dn
     * @param userDn
     * @return
     */
    @Override
    public String getUserOrgDnByDn(String userDn) {
        if (null == userDn) {
            return "";
        }
        int beginIndex = userDn.indexOf(",");
        if (beginIndex == -1) {
            return "";
        }
        return userDn.substring(beginIndex + 1);
    }

    @Override
    public String getCurrentUserOrgDn() {
        String userDn = SystemHelper.getCurrentUser().getId().toString();
        return getUserOrgDnByDn(userDn);
    }

    private void checkNullField(UserDTO userDto, UserBean existingUser, DirContextOperations context) {
        //密码
        if (StringUtil.isNotEmpty(userDto.getPassword())) {
            context.setAttributeValue("userPassword", SecurityPasswordHelper.encodePassword(userDto.getPassword()));
        }
        // 真实姓名
        if (StringUtil.isNotEmpty(userDto.getFullName())) {
            context.setAttributeValue("givenName", userDto.getFullName());
        } else {
            context.setAttributeValue("givenName", "null");
        }
        // 邮箱
        if (StringUtil.isNotEmpty(userDto.getMail())) {
            context.setAttributeValue("mail", userDto.getMail());
        } else {
            context.setAttributeValue("mail", "null");
        }
        // 电话
        if (StringUtil.isNotEmpty(userDto.getMobile())) {
            context.setAttributeValue("mobile", userDto.getMobile());
        } else {
            context.setAttributeValue("mobile", "null");
        }
        // 性别
        if (StringUtil.isNotEmpty(userDto.getGender())) {
            context.setAttributeValue("employeeType", userDto.getGender());
        }
        // 启停状态
        if (StringUtil.isNotEmpty(userDto.getState())) {
            context.setAttributeValue("st", userDto.getState());
        } else if (StringUtil.isNotEmpty(existingUser.getMail())) {
            context.setAttributeValue("st", existingUser.getState());
        }
        String userName = SystemHelper.getCurrentUsername();
        if (!userName.equals(userDto.getUsername())) {
            // 授权截止日期
            if (StringUtil.isNotEmpty(userDto.getAuthorizationDate())) {
                context.setAttributeValue("carLicense", userDto.getAuthorizationDate());
            } else if ("".equals(userDto.getAuthorizationDate()) || userDto.getAuthorizationDate() == null) {
                context.setAttributeValue("carLicense", "null");
            }
        }
        if (CommonConstants.ADMIN_USER.equals(userDto.getUsername())) {
            String sendDownCommand = userDto.getSendDownCommand();
            boolean flag = ("".equals(sendDownCommand) || sendDownCommand == null) && StringUtils
                .isEmpty(existingUser.getSendDownCommand());
            // 下发口令
            if (StringUtil.isNotEmpty(sendDownCommand)) {
                context.setAttributeValue("street", SecurityPasswordHelper.encodePassword(sendDownCommand));
            } else if (flag) {
                // 因为Ldap不能存空值,所以存入"null"字符串代表该字段没有值
                context.setAttributeValue("street", "null");
            }
        }
        // 身份
        if (StringUtils.isNotEmpty(userDto.getIdentity())) {
            context.setAttributeValue("employeeNumber", userDto.getIdentity());
        } else {
            context.setAttributeValue("employeeNumber", "null");
        }
        // 行业
        if (StringUtils.isNotEmpty(userDto.getIndustry())) {
            context.setAttributeValue("businessCategory", userDto.getIndustry());
        } else {
            context.setAttributeValue("businessCategory", "null");
        }
        // 职务
        if (StringUtils.isNotEmpty(userDto.getDuty())) {
            context.setAttributeValue("departmentNumber", userDto.getDuty());
        } else {
            context.setAttributeValue("departmentNumber", "null");
        }
        // 科室
        if (StringUtils.isNotEmpty(userDto.getAdministrativeOffice())) {
            context.setAttributeValue("displayName", userDto.getAdministrativeOffice());
        } else {
            context.setAttributeValue("displayName", "null");
        }
        // 社会保险号
        if (StringUtils.isNotEmpty(userDto.getSocialSecurityNumber())) {
            context.setAttributeValue("socialSecurityNumber", userDto.getSocialSecurityNumber());
        } else {
            context.setAttributeValue("socialSecurityNumber", "null");
        }

        // 生份证号
        if (StringUtils.isNotEmpty(userDto.getIdentityNumber())) {
            context.setAttributeValue("identityNumber", userDto.getIdentityNumber());
        } else {
            context.setAttributeValue("identityNumber", "null");
        }
    }

    @Override
    public UserDTO delete(String userDn) {
        if (StringUtils.isEmpty(userDn)) {
            return null;
        }
        StringBuilder message = new StringBuilder();
        String ipAddress = getIpAddress();
        LdapName name = LdapUtils.newLdapName(userDn);
        ldapTemplate.unbind(name);
        name = LdapUtils.newLdapName(name.toString() + "," + baseLdapPath.toString());
        Collection<Group> currentRoles = roleService.getByMemberName(name);
        if (CollectionUtils.isNotEmpty(currentRoles)) {
            // 循环从用户组移除用户
            for (Group role : currentRoles) {
                roleService.deleteUser(role.getName(), name);
            }
        }
        // 第一个逗号所在下标
        int commaIndex = userDn.indexOf(",");
        // 第一个等号所在下表
        int eqIndex = userDn.indexOf("=");
        String orgDn = userDn.substring(commaIndex + 1);
        // 截取用户名
        String userName = userDn.substring(eqIndex + 1, commaIndex);
        UserDTO userDTO = getUserByEntryDn(userDn);
        if (userDTO != null) {
            //删除用户-分组数据库
            userGroupService.deleteByUserId(userDTO.getUuid());
        }
        //删除用户-分组缓存
        RedisHelper.delete(RedisKeyEnum.USER_GROUP.of(userName));
        expireUserSession(userName);
        // 获得部门组织架构
        OrganizationLdap org = organizationService.getOrgByEntryDn(orgDn);
        message.append("删除用户 : ").append(userName).append(" ( @").append(org.getName()).append(")").append(" <br/>");
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "删除用户");
        return userDTO;
    }

    @Override
    public List<UserDTO> deleteBatch(List<String> userDns) {
        if (CollectionUtils.isEmpty(userDns)) {
            return Collections.emptyList();
        }
        StringBuilder message = new StringBuilder();
        LdapName name;
        UserDTO userDTO;
        Collection<Group> currentRoles;
        OrganizationLdap org;
        // 删除用户和车的关联
        Set<String> userUuidSet = new HashSet<>();
        Set<RedisKey> userNameCache = new HashSet<>();
        List<UserDTO> deleted = new ArrayList<>(userDns.size());
        for (String userDn : userDns) {
            name = LdapUtils.newLdapName(userDn);
            userDTO = getUserByEntryDn(userDn);
            if (userDTO != null) {
                deleted.add(userDTO);
                userUuidSet.add(userDTO.getUuid());
                userNameCache.add(RedisKeyEnum.USER_GROUP.of(userDTO.getUsername()));
            }
            ldapTemplate.unbind(name);
            name = LdapUtils.newLdapName(name.toString() + "," + baseLdapPath.toString());
            currentRoles = roleService.getByMemberName(name);
            if (CollectionUtils.isNotEmpty(currentRoles)) {
                // 循环从用户组移除用户
                for (Group role : currentRoles) {
                    roleService.deleteUser(role.getName(), name);
                }
            }
            // 第一个逗号所在下标
            int commaIndex = userDn.indexOf(",");
            // 第一个等号所在下表
            int eqIndex = userDn.indexOf("=");
            String orgDn = userDn.substring(commaIndex + 1);
            // 截取用户名
            String userName = userDn.substring(eqIndex + 1, commaIndex);
            //过期用户
            expireUserSession(userName);
            // 获得部门组织架构
            org = organizationService.getOrgByEntryDn(orgDn);
            message.append("删除用户 : ").append(userName).append(" ( @").append(org.getName()).append(")")
                .append(" <br/>");
        }
        //删除用户-分组数据库
        userGroupService.deleteByUserIds(userUuidSet);
        // 删除用户-分组缓存
        RedisHelper.batchDelete(userNameCache);
        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "批量删除用户");
        return deleted;
    }

    @Override
    public String getUsernameByUserDn(String userDn) {
        if (StringUtils.isEmpty(userDn)) {
            return null;
        }
        // 第一个逗号所在下标
        int commaIndex = userDn.indexOf(",");
        // 第一个等号所在下表
        int eqIndex = userDn.indexOf("=");
        String orgDn = userDn.substring(commaIndex + 1);
        // 截取用户名
        return userDn.substring(eqIndex + 1, commaIndex);
    }

    @Override
    public Page<UserBean> getPageByKeyword(UserPageQuery query) throws Exception {

        Page<UserBean> userPage = new Page<>(query.getPage().intValue(), query.getLimit().intValue());
        SearchControls searchCtls = new SearchControls();
        String keyword = query.getSimpleQueryParam();
        if (query.isSearchSubFlag()) {
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        } else {
            searchCtls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense", "employeeNumber", "businessCategory", "departmentNumber", "displayName",
                "socialSecurityNumber", "identityNumber" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        if (keyword != null && !Objects.equals(keyword, "")) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("mail", "*" + keyword + "*"));
            orfilter.or(new LikeFilter("mobile", "*" + keyword + "*"));
            orfilter.or(new LikeFilter("uid", "*" + keyword + "*"));
            orfilter.or(new LikeFilter("givenName", "*" + keyword + "*"));
            andfilter.append(orfilter);
        }
        List<UserDTO> users =
            ldapTemplate.search(query.getOrgDn(), andfilter.encode(), searchCtls, new UserContextMapper());
        if (CollectionUtils.isEmpty(users)) {
            return userPage;
        }
        generateUserRole(users);
        generateUserOrg(users);
        // 用户列表排序
        users.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
        int size = users.size();
        userPage.setTotal(size);
        int endIndex = Math.min(size, userPage.getEndRow());
        if (size > userPage.getPageSize()) {
            users = users.subList(userPage.getStartRow(), endIndex);
        }
        //复制
        BeanCopier beanCopier = BeanCopier.create(UserDTO.class, UserBean.class, false);
        for (UserDTO user : users) {
            userPage.add(user.transform(user, beanCopier));
        }
        List<Operations> operationList = operationService.findAll();
        if (CollectionUtils.isEmpty(operationList)) {
            return userPage;
        }
        Map<String, String> operationMap =
            operationList.stream().collect(Collectors.toMap(Operations::getId, Operations::getOperationType));
        for (UserBean userBean : userPage) {
            String industryName = operationMap.get(userBean.getIndustry());
            userBean.setIndustryName(industryName);
        }
        return userPage;
    }

    private void generateUserOrg(List<UserDTO> users) {
        List<OrganizationLdap> allOrgs = organizationService.getAllOrganization();
        Map<String, String> allOrgsMap = new HashMap<>();
        for (OrganizationLdap org : allOrgs) {
            allOrgsMap.put(org.getId().toString(), org.getName());
        }
        String orgId;
        for (UserDTO user : users) {
            orgId = getUserOrgDnByDn(user.getId().toString());
            user.setOrgName(allOrgsMap.getOrDefault(orgId, ""));
        }
    }

    private void generateUserRole(List<UserDTO> users) {
        List<Group> allGroup = roleService.getAllGroup();
        Map<String, String> allGroupMap = new HashMap<>();
        String role;
        for (Group group : allGroup) {
            for (Name member : group.getMembers()) {
                role = allGroupMap.getOrDefault(member.toString(), "");
                if (!role.isEmpty()) {
                    role = role + ",";
                }
                role = role + group.getRoleName();
                allGroupMap.put(member.toString(), role);
            }
        }

        for (UserDTO user : users) {
            user.setRoleName(allGroupMap.getOrDefault(user.getId().toString() + ",dc=zwlbs,dc=com", ""));
        }
    }

    @Override
    public UserDTO getByDn(String id) {
        UserBean userBean = userRepo.findOne(LdapUtils.newLdapName(id));
        UserDTO userDto = new UserDTO();
        BeanUtils.copyProperties(userBean, userDto);
        checkNullOfString(userDto);
        return userDto;
    }

    @Override
    public String getCurrentUserUuid() {
        UserLdap user = SystemHelper.getCurrentUser();
        UserDTO userDto = getUserByUsername(user.getUid());
        return userDto.getUuid();
    }

    /**
     * 批量修改用户
     * @param userId userId
     * @throws Exception e
     * @author fanlu
     */
    @Override
    public void updateBatch(String userId, String userName, String passWord, String state, String authorizationDate)
        throws Exception {
        String[] userIds = userId.split(";");
        String[] userNames = userName.split(";");
        Map<String, OrganizationLdap> map = Maps.newHashMap();
        for (int i = 0; i < userIds.length; i++) {
            LdapName dn = LdapUtils.newLdapName(userIds[i]);
            boolean offlineFlag = false;
            DirContextOperations context = ldapTemplate.lookupContext(dn);
            if (StringUtil.isNotEmpty(passWord)) {
                context.setAttributeValue("userPassword", SecurityPasswordHelper.encodePassword(passWord));
                offlineFlag = true;
            }
            // 启停状态
            if (StringUtil.isNotEmpty(state)) {
                context.setAttributeValue("st", state);
            }
            String userN = SystemHelper.getCurrentUsername();
            if (!userN.equals(userNames[i])) {
                if (StringUtil.isNotEmpty(authorizationDate)) {
                    // 授权截止日期
                    context.setAttributeValue("carLicense", authorizationDate);
                }
            }
            ldapTemplate.modifyAttributes(context);
            String ipAddress = getIpAddress();
            // 用户企业Id
            String orgDn = getUserOrgDnByDn(userIds[i]);
            // 获得部门组织架构
            OrganizationLdap org = map.get(orgDn);
            if (org == null) {
                org = organizationService.getOrgByEntryDn(orgDn);
                map.put(orgDn, org);
            }
            String log = "修改用户：" + userNames[i] + "(@" + org.getName() + ")";
            logSearchService.addLog(ipAddress, log, "3", "", "-", "");
            if (StringUtils.isNotBlank(state)) {
                // 将用户的启停状态存入redis
                RedisKey userStateKey = HistoryRedisKeyEnum.USER_STATE.of(userNames[i]);
                RedisHelper.setString(userStateKey, state);
            }
            if (offlineFlag || "0".equals(state)) {
                //过期Session
                expireUserSession(userNames[i]);
            }
            if (offlineFlag) {
                // 用户模块修改他人密码后，如果此用户在线，需要记录用户的下线时间和重新登录
                updateUserOffline(ipAddress, userNames[i]);
            }
        }
    }

    /**
     * 修改密码
     * @param password      密码
     * @param equipmentType APP
     * @author fanlu
     */
    @Override
    public void updatePassword(String password, String equipmentType) throws Exception {
        Name dn = SystemHelper.getCurrentUser().getId();
        Attribute attr = new BasicAttribute("userPassword", SecurityPasswordHelper.encodePassword(password));
        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] { item });
        String userName = SystemHelper.getCurrentUsername();
        String log = "用户：" + userName + " 修改密码";
        String logSource;
        if (StringUtils.isNotBlank(equipmentType) && "APP".equals(equipmentType)) {
            logSource = "4";
        } else {
            logSource = "3";
        }
        String ipAddress = getIpAddress();
        // 用户下线时长记录
        updateUserOffline(ipAddress, userName);
        logSearchService.addLog(ipAddress, log, logSource, "", "-", "");
        expireUserSession(userName);
    }

    private void checkNullOfString(UserDTO userDto) {
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getAuthorizationDate())) {
            userDto.setAuthorizationDate(null);
        }
        if (CommonConstants.ADMIN_USER.equals(userDto.getUsername()) && CommonConstants.NULL_OF_STRING
            .equals(userDto.getSendDownCommand())) {
            userDto.setSendDownCommand(null);
        }
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getMobile())) {
            userDto.setMobile("");
        }
        /**
         *  真实姓名
         */
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getFullName())) {
            userDto.setFullName("");
        }
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getMail())) {
            userDto.setMail("");
        }
        // 身份
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getIdentity())) {
            userDto.setIdentity("");
        }
        //行业
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getIndustry())) {
            userDto.setIndustry("");
        }
        // 职务
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getDuty())) {
            userDto.setDuty("");
        }
        // 科室
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getAdministrativeOffice())) {
            userDto.setAdministrativeOffice("");
        }
        // 社会保险号
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getSocialSecurityNumber())) {
            userDto.setSocialSecurityNumber("");
        }
        // 生份证号
        if (CommonConstants.NULL_OF_STRING.equals(userDto.getIdentityNumber())) {
            userDto.setIdentityNumber("");
        }
    }

    /**
     * @param roleDn
     * @return
     */
    @Override
    public JsonResultBean deleteTalkBackDispatcherRole(String roleDn) {
        LdapName name = LdapUtils.newLdapName(roleDn + "," + baseLdapPath.toString());
        Collection<Group> currentRoles = roleService.getByMemberName(name);
        if (containsDispatcherRole(currentRoles)) {
            return removeDispatcherRole(name);
        }
        return null;
    }

    @Override
    public String getChatGroupUserList(String type, String orgId) throws Exception {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userDn = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userDn.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgDn = userDn.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = organizationService.getOrgChildList(orgDn);
        if (CollectionUtils.isEmpty(orgs)) {
            return obj.toJSONString();
        }
        // 讨论组字段不为空
        List<String> checkUserList = new ArrayList<>();
        boolean flag = false;
        if (StringUtils.isNotBlank(orgId)) {
            // 根据讨论组获取用户的id list
            checkUserList = chatGroupUserService.findGroupUserByGroupId(orgId);
            flag = true;
        }
        // 遍历得到当前用户组织及下级组织id的list
        List<UserDTO> allUsers = organizationService.fuzzyUsersByOrgDn(null, orgDn, true);
        int userCount = allUsers.size();
        JSONObject userObj;
        boolean b;
        for (UserDTO userDTO : allUsers) {
            userObj = new JSONObject();
            userObj.put("id", userDTO.getUuid());
            userObj.put("name", userDTO.getUsername());
            if (userDTO.getFullName() != null) {
                userObj.put("count", userDTO.getFullName());
            }
            userObj.put("type", "user");
            userObj.put("iconSkin", "userSkin");
            userObj.put("pId", getUserOrgDnByDn(userDTO.getId().toString()));
            // 新增，默认勾选当前用户,修改，默认勾选讨论组下的用户
            b = StringUtils.isBlank(orgId) && userDn.equals(userDTO.getId().toString()) || (flag && checkUserList
                .contains(userDTO.getUuid()));
            if (b) {
                userObj.put("checked", true);
            }
            result.add(userObj);
        }
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, type));
        obj.put("tree", result);
        obj.put("size", userCount);
        return obj.toJSONString();
    }

    @Override
    public JsonResultBean deleteTalkBackDispatcherRoles(String[] roleDns) {
        JsonResultBean jsonResultBean;
        for (int i = 0; i < roleDns.length; i++) {
            jsonResultBean = deleteTalkBackDispatcherRole(roleDns[i]);
            if (jsonResultBean != null) {
                return jsonResultBean;

            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean removeDispatcherRole(LdapName name) {
        try {
            String userName = getUserName(name);
            IntercomIotUserForm intercomIotUser = intercomIotUserDao.getIntercomIotUsersByUserName(userName);
            JSONObject result = talkCallUtils.deleteIotUser(intercomIotUser.getUserId() + "");
            if (result.getIntValue("result") == 0) {
                intercomIotUser.setFlag(0);
                intercomIotUserDao.updateIntercomIotUser(intercomIotUser);
                //分配角色的时候，新增调度员成功后，需要把userId存到ldap中
                updateModifyUserDispatcherId(getUserId(name), null);
                updateAndRecyclePersonCallNumber(intercomIotUser.getCallNumber() + "");
            } else {
                throw new Exception("删除调度员角色接口异常了");
            }
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "授权异常！");
        }
        return null;
    }

    private String getUserName(Name name) {
        return name.toString().split(",")[0].split("uid=")[1];
    }

    private String getUserId(Name name) {
        return name.toString().replace(",dc=zwlbs,dc=com", "");
    }

    void updateModifyUserDispatcherId(String userId, String dispatcherId) {
        LdapName dn = LdapUtils.newLdapName(userId);
        DirContextOperations context = ldapTemplate.lookupContext(dn);
        // 对讲平台的调度员id
        if (StrUtil.isNotBlank(dispatcherId)) {
            context.setAttributeValue("telexNumber", dispatcherId);
        } else {
            context.setAttributeValue("telexNumber", null);
        }
        ldapTemplate.modifyAttributes(context);
    }

    private void updateAndRecyclePersonCallNumber(String callNumber) throws Exception {
        CallNumberUtil.recyclePersonCallNumber(callNumber);
        callNumberDao.updatePersonCallNumber(callNumber, (byte) 1);
    }

    private boolean containsDispatcherRole(Collection<Group> currentRoles) {
        if (CollectionUtils.isEmpty(currentRoles)) {
            return false;
        }
        boolean isDispatcherRole = false;
        for (Group group : currentRoles) {
            isDispatcherRole = isDispatcherRole(group.getName());
            if (isDispatcherRole) {
                break;
            }

        }
        return isDispatcherRole;
    }

    private boolean isDispatcherRole(String roleId) {
        final List<Group> roles = roleService.getListByKeyword("调度员角色", false);
        if (roles == null) {
            // 不存在调度员角色
            return false;
        }
        Group groupRole = roles.get(0);
        if (!roleId.contains("cn=")) {
            return roleId.equals(groupRole.getName() + "");
        }

        return ("cn=" + groupRole.getName() + ",ou=Groups").equals(roleId);
    }

    /**
     * 根据用户名查询用户
     * @param uid uid
     * @return user
     * @author FanLu
     */
    public UserBean getUserDetails(String uid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts = { "entryDN", "entryUUID", "givenName" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        filter.and(new EqualsFilter("uid", uid));
        List<UserBean> users =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, (ContextMapper<UserBean>) ctx -> {
                DirContextAdapter context = (DirContextAdapter) ctx;
                UserBean p = new UserBean();
                p.setFullName(context.getStringAttribute("givenName"));
                p.setUsername(context.getStringAttribute("uid"));
                p.setMail(context.getStringAttribute("mail"));
                p.setMobile(context.getStringAttribute("mobile"));
                p.setId(context.getDn());
                p.setUuid(context.getStringAttribute("entryuuid"));
                return p;
            });
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    //标记
    @Override
    public JsonResultBean updateUserRole(String userDn, String roleIds) throws Exception {

        String str = "";
        // 取页面传来的角色列表和用户目前拥有的角色列表的差集，即用户已有角色列表中不存在于页面传来的角色列表中的角色，将用户从对应用户组中移除
        LdapName name = LdapUtils.newLdapName(userDn + "," + baseLdapPath.toString());
        // 获取用户所有的角色列表
        Collection<Group> currentRoles = roleService.getByMemberName(name);
        List<String> currentRoleNames = currentRoles.stream().map(Group::getName).collect(Collectors.toList());
        List<Group> dispatcherRoleList = roleService.getListByKeyword("调度员角色", false);
        boolean dispatcherRoleFlag = false;
        Group groupRole = null;
        if (CollectionUtils.isNotEmpty(dispatcherRoleList)) {
            dispatcherRoleFlag = true;
            groupRole = dispatcherRoleList.get(0);
        }
        if (StringUtils.isNotBlank(roleIds)) {
            List<String> allotRoleList = Arrays.asList(roleIds.split(","));
            //需要删除的角色
            List<String> deleteRoles = new ArrayList<>(currentRoleNames);
            deleteRoles.removeAll(allotRoleList);
            //需要新增的角色
            List<String> roles = new ArrayList<>(currentRoleNames);
            List<String> addRoles = new LinkedList<>(allotRoleList);
            addRoles.removeAll(roles);
            // 循环添加用户至用户组
            List<Group> noTalkRoleList = roleService.getListByKeyword("禁言角色", false);
            boolean noTalkRoleFlag = false;
            Group noTalkRole = null;
            if (CollectionUtils.isNotEmpty(noTalkRoleList)) {
                noTalkRoleFlag = true;
                noTalkRole = noTalkRoleList.get(0);
            }
            boolean canAuthSilentRoleFlag = canAuthSilentRole(currentRoles, addRoles);
            for (String roleDn : addRoles) {
                if (dispatcherRoleFlag && isEquals(groupRole, roleDn)) {
                    //判断时候是调度员角色，则新增
                    JsonResultBean resultBean = addDispatcherRoleToUser(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    break;
                }
                //新增的角色是否包含禁言角色
                if (noTalkRoleFlag && isEquals(noTalkRole, roleDn) && !canAuthSilentRoleFlag) {
                    return new JsonResultBean("当前用户没有调度员角色，无法分配禁言角色");
                }
            }
            // 循环从用户组移除用户，并判断是否是调度员角色，则删除
            if (dispatcherRoleFlag) {
                JsonResultBean resultBean = judgeRemoveDispatcherRole(name, dispatcherRoleList, deleteRoles);
                if (resultBean != null) {
                    return resultBean;
                }
            }
            boolean judge = judge(currentRoleNames, roleIds);
            if (!judge) {
                str = ("当前用户没有调度员角色，无法分配禁言角色");
            } else {
                // 循环添加用户至用户组
                for (String roleDn : addRoles) {
                    roleService.addMemberToGroup(roleDn, name);
                }
            }
            // 循环从用户组移除用户
            for (String roleDn : deleteRoles) {
                roleService.removeMemberFromGroup(roleDn, name);
            }
        } else {
            // 若选择角色为空
            // 循环从用户组移除用户
            if (dispatcherRoleFlag) {
                JsonResultBean resultBean = judgeRemoveDispatcherRole(name, dispatcherRoleList, currentRoleNames);
                if (resultBean != null) {
                    return resultBean;
                }
            }
            // 循环从用户组移除用户
            for (String roleDn : currentRoleNames) {
                roleService.removeMemberFromGroup(roleDn, name);
            }
        }
        // 第一个逗号所在下标
        int commaIndex = userDn.indexOf(",");
        // 第一个等号所在下表
        int eqIndex = userDn.indexOf("=");
        // 截取用户名
        String userName = userDn.substring(eqIndex + 1, commaIndex);
        String log = "为用户 " + userName + " 分配角色";
        logSearchService.addLog(getIpAddress(), log, "3", "", "-", "");
        if (!"".equals(str)) {
            return new JsonResultBean(str);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, "分配角色成功");
    }

    private boolean isEquals(Group groupRole, String roleDn) {

        if (!roleDn.contains("cn=")) {
            return roleDn.equals(groupRole.getName() + "");
        }
        return ("cn=" + groupRole.getName() + ",ou=Groups").equals(roleDn);
    }

    /**
     * 循环从用户组移除用户，并判断是否是调度员角色，则删除
     * @param name
     * @param dispatcherRoleList
     * @param roles
     * @return
     */
    private JsonResultBean judgeRemoveDispatcherRole(LdapName name, List<Group> dispatcherRoleList,
        List<String> roles) {
        Group groupRole = dispatcherRoleList.get(0);
        for (String roleDn : roles) {
            //判断时候是调度员角色，则新增
            if (isEquals(groupRole, roleDn)) {
                JsonResultBean resultBean = removeDispatcherRole(name);
                if (resultBean != null) {
                    return resultBean;
                }
                //删除调度员角色，一定会删除禁言角色
                roleService.deleteSilentRole(name);
                break;
            }
        }
        return null;
    }

    private JsonResultBean addDispatcherRoleToUser(LdapName name) throws Exception {
        IntercomIotUserForm iotUser = null;
        String addCallNumber = null;
        JSONObject result = null;
        try {
            addCallNumber = updateAndReturnPersonCallNumber();
            String userName = getUserName(name);
            result = talkCallUtils.addIotUser(userName, "2", addCallNumber);
            if (result.getIntValue("result") == 0) {
                String uid = result.getJSONObject("data").getString("userId");
                iotUser = new IntercomIotUserForm(userName, uid, addCallNumber);
                intercomIotUserDao.updateIntercomIotUser(iotUser);
                //分配角色的时候，新增调度员成功后，需要把userId存到ldap中
                updateModifyUserDispatcherId(getUserId(name), uid);
            } else {
                throw new Exception("调用新增调度员接口异常了");
            }
        } catch (CallNumberExhaustException e) {
            return new JsonResultBean(JsonResultBean.FAULT, "个呼号码不够用了，请减少要分配用户");
        } catch (Exception e) {
            /**
             * 异常情况，进行个呼号码的集体回收,并删除已经创建好的调度员账号
             */
            if (StrUtil.isNotBlank(addCallNumber)) {
                updateAndRecyclePersonCallNumber(addCallNumber);
            }
            if (iotUser != null) {
                iotUser.setFlag(0);
                intercomIotUserDao.updateIntercomIotUser(iotUser);
                updateModifyUserDispatcherId(getUserId(name), null);
            }
            String message = result == null ? "" : result.getString("message");
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
        return null;
    }

    public String updateAndReturnPersonCallNumber() throws Exception {
        String personCallNumber = CallNumberUtil.popPersonCallNumber();
        callNumberDao.updatePersonCallNumber(personCallNumber, (byte) 0);
        return personCallNumber;
    }

    /**
     * 能否授权禁言角色
     * @param currentRoles
     * @param addRoles
     * @return
     */
    private boolean canAuthSilentRole(Collection<Group> currentRoles, List<String> addRoles) {
        boolean containsDispatcherRole = false;
        if (addRoles.size() >= 2) {
            for (String role : addRoles) {
                if (isDispatcherRole(role)) {
                    containsDispatcherRole = true;
                    break;
                }
            }
        }
        return containsDispatcherRole || containsDispatcherRole(currentRoles);
    }

    public boolean judge(List<String> currentRoleNames, String roleIds) {

        boolean flag = true;
        String[] roleArray = roleIds.split(",");
        List<String> collect = Stream.of(roleArray).collect(Collectors.toList());
        List<Group> allGroup = roleService.getAllGroup();
        String jyanId = "";
        String diaoDuId = "";
        for (Group group : allGroup) {
            if (group.getRoleName().equals("禁言角色")) {
                jyanId = group.getName();
            }
            if (group.getRoleName().equals("调度员角色")) {
                diaoDuId = group.getName();
            }
        }
        for (String currentRoleName : currentRoleNames) {
            if (currentRoleName.equals(diaoDuId)) {
                if (collect.contains(jyanId)) {
                    if (collect.contains(diaoDuId)) {
                        flag = true;
                    } else {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 查询所有用户
     * @return result
     */
    @Override
    public List<UserDTO> findAllUser() {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        return ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
    }

    /**
     * 获取用户的uuid
     * @param userDn
     * @return result
     */
    @Override
    public String getUserUuidByDn(String userDn) {
        String uuid = "";
        if (StringUtils.isNotBlank(userDn)) {
            UserDTO user = getUserByEntryDn(userDn);
            if (user != null) {
                uuid = user.getUuid();
            }
        }
        return uuid;
    }

    /**
     * 根据用户id获取当前组织及下级组织
     * @return List<String>
     * @author wangying
     */
    @Override
    public List<OrganizationLdap> getCurrentUseOrgList() {
        // 获取用户所在组织及下级组织
        List<OrganizationLdap> orgList = getOrgListByUserDn(SystemHelper.getCurrentUId());
        // 遍历得到当前用户组织及下级组织id的list
        return orgList == null ? new ArrayList<>() : orgList;
    }

    @Override
    public OrganizationLdap getCurrentUserOrg() {
        String currentUserDn = SystemHelper.getCurrentUserDn();
        final OrganizationLdap org = organizationService.getOrgByEntryDn(getUserOrgDnByDn(currentUserDn));
        return Optional.ofNullable(org).orElseGet(() -> {
            final OrganizationLdap absent = new OrganizationLdap();
            try {
                absent.setId(new LdapName(""));
            } catch (InvalidNameException e) {
                // ignore
            }
            absent.setUuid("");
            absent.setName("");
            return absent;
        });
    }

    /**
     * 根据uuids查询用户
     * @param uuids
     * @return user
     * @author wangying
     */
    @Override
    public List<UserDTO> getUserListByUuids(Collection<String> uuids) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        OrFilter orFilter = new OrFilter();
        for (String uuid : uuids) {
            orFilter.or(new EqualsFilter("entryUUID", uuid));
        }
        filter.and(orFilter);
        return ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
    }

    /**
     * 通过去企业名字模糊搜索，得到所有复合条件的企业id
     * @param name
     * @return
     */
    @Override
    public List<String> fuzzSearchUserOrgIdsByOrgName(String name) {

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnAttribute = { "entryUUID" };
        searchControls.setReturningAttributes(returnAttribute);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new NotFilter(new EqualsFilter("objectclass", "OpenLDAPou")));
        if (StringUtil.isNotEmpty(name)) {
            filter.and(new LikeFilter("l", "*" + name + "*"));
        } else {
            filter.and(new LikeFilter("l", "*"));
        }
        ContextMapper<String> contextMapper = o -> {
            DirContextAdapter context = (DirContextAdapter) o;
            return context.getStringAttribute("entryUUID");
        };
        return ldapTemplate.search(searchBase, filter.encode(), searchControls, contextMapper);

    }

    @Override
    public List<String> getCurrentUserOrgNames() {
        List<OrganizationLdap> currentUseOrgList = getCurrentUseOrgList();
        List<String> names = new ArrayList<>(currentUseOrgList.size());
        for (OrganizationLdap organizationLdap : currentUseOrgList) {
            names.add(organizationLdap.getName());
        }
        return names;
    }

    @Override
    public UserDTO getCurrentUserInfo() {
        return getUserByEntryDn(SystemHelper.getCurrentUser().getId().toString());
    }

    @Override
    public UserDTO getAdminUserInfo() {
        UserDTO userBean = getUserByEntryDn("uid=admin,ou=organization");
        List<OrganizationLdap> orgList = getOrgListByUserDn(userBean.getId().toString());
        if (CollectionUtils.isNotEmpty(orgList)) {
            userBean.setOrgName(orgList.get(0).getName());
        }
        return userBean;
    }

    /**
     * 获取当前用户所在组织 如果当前用户具有admin权限，返回其下级组织uuid，若有多个平行下级组织，则获取其第一个下级组织的uuid 如果当前用户不具有admin权限，则直接返回其组织uuid
     * @return String
     * @author Liubangquan
     */
    @Override
    public String getOrgIdExceptAdmin() {
        // 默认将设备分组设为当前登录用户所在分组
        String orgId;
        boolean adminFlag = isAdminRole();
        // 当前用户具有admin权限
        if (adminFlag) {
            List<OrganizationLdap> ols = organizationService.getOrgChildList(getCurrentUserOrgDn());
            if (null != ols && ols.size() > 1) {
                orgId = Converter.toBlank(ols.get(1).getUuid());
            } else {
                orgId = Converter.toBlank(getCurrentUserOrg().getUuid());
            }
        } else {
            orgId = Converter.toBlank(getCurrentUserOrg().getUuid());
        }
        return orgId;
    }

    /**
     * 校验用户是否是ADMIN权限
     * @return boolean
     * @author wangying
     */
    @Override
    public boolean isAdminRole() {
        boolean adminFlag = false;
        // 获取当前用户的角色
        String userName = SystemHelper.getCurrentUsername();
        // 根据用户名获取用户id
        String userId = getUserDetails(userName).getId().toString();
        // 根据用户id获取角色 ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        List<Group> userGroup = (List<Group>) roleService.getByMemberName(name);
        // 判断用户是否有admin权限
        if (userGroup != null && userGroup.size() > 0) {
            for (Group group : userGroup) {
                if ("ROLE_ADMIN".equals(group.getName())) {
                    adminFlag = true;
                    break;
                }
            }
        }
        return adminFlag;
    }

    @Override
    public JSONArray getRoleTreeByUserDn(@PathVariable String id) {

        List<Group> currentUserRoles =
            (List<Group>) roleService.getByMemberNameStr(SystemHelper.getCurrentUser().getId().toString());
        if (CollectionUtils.isEmpty(currentUserRoles)) {
            return new JSONArray(1);
        }
        Iterator<Group> iterator = currentUserRoles.iterator();
        Group next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if ("ROLE_ADMIN".equals(next.getName())) {
                //剔除admin角色
                iterator.remove();
                break;
            }
        }
        // 根据用户id获取角色
        List<Group> userGroup = (List<Group>) roleService.getByMemberNameStr(id);
        Set<String> set = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userGroup)) {
            for (Group userGro : userGroup) {
                set.add(userGro.getId().toString());
            }
        }
        JSONArray roleList = new JSONArray();
        for (Group group : currentUserRoles) {
            JSONObject curRole = new JSONObject();
            curRole.put("id", group.getId());
            curRole.put("name", group.getRoleName());
            curRole.put("cn", group.getName());
            curRole.put("checked", false);
            curRole.put("readonly", false);
            if (set.contains(group.getId().toString())) {
                curRole.put("checked", true);
            }
            roleList.add(curRole);
        }
        return roleList;
    }

    @Override
    public boolean compareAllotRole(String name, List<String> roleList) {
        Collection<Group> groups =
            roleService.getByMemberName(LdapUtils.newLdapName(name + "," + baseLdapPath.toString()));
        if (CollectionUtils.isEmpty(groups)) {
            return false;
        }
        if (roleList.size() > groups.size()) {
            return false;
        }
        Set<String> set = groups.stream().map(Group::getName).collect(Collectors.toSet());
        for (String id : roleList) {
            if (!set.contains(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<String> getCurrentUserMonitorIds() {
        String currentUsername = SystemHelper.getCurrentUsername();
        return getMonitorIdsByUser(currentUsername);
    }

    @Override
    public Set<String> getCurrentUserMonitorIds(String currentUsername) {
        if (StringUtils.isEmpty(currentUsername)) {
            return new HashSet<>();
        }
        return getMonitorIdsByUser(currentUsername);
    }

    /**
     * 获取当前用户下的所有监控对象的id,人车物
     * @param orgIds
     * @return
     */
    @Override
    public Set<String> getCurrentUserUnbindMonitorIds(List<String> orgIds) {
        List<RedisKey> redisKeys = new ArrayList<>();
        for (String orgId : orgIds) {
            redisKeys.add(RedisKeyEnum.ORG_UNBIND_VEHICLE.of(orgId));
            redisKeys.add(RedisKeyEnum.ORG_UNBIND_THING.of(orgId));
            redisKeys.add(RedisKeyEnum.ORG_UNBIND_PEOPLE.of(orgId));
        }
        List<Map<String, String>> mapList = RedisHelper.batchGetHashMap(redisKeys);
        Set<String> ids = new HashSet<>();
        for (Map<String, String> map : mapList) {
            ids.addAll(map.keySet());
        }
        return ids;
    }

    @Override
    public Set<String> getMonitorIdsByUser(String userName) {
        Set<String> groupSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(userName));
        if (groupSet == null || groupSet.isEmpty()) {
            return new HashSet<>(1);
        }
        List<RedisKey> redisKeys = Lists.newLinkedList();
        for (String monitorId : groupSet) {
            redisKeys.add(RedisKeyEnum.GROUP_MONITOR.of(monitorId));
        }
        return RedisHelper.batchGetSet(redisKeys);
    }

    @Override
    public List<GroupDTO> getCurrentUserGroupList() {
        String currentUsername = SystemHelper.getCurrentUsername();
        Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(currentUsername));
        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>(1);
        }
        return groupService.getGroupsById(groupIds);
    }

    @Override
    public Map<String, String> getCurrentGroupIdAndGroupName() {
        return getCurrentUserGroupList().stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
    }

    @Override
    public Set<String> getCurrentUserGroupIds() {
        String currentUsername = SystemHelper.getCurrentUsername();
        return getUserGroupIdsByUserName(currentUsername);
    }

    @Override
    public Set<String> getUserGroupIdsByUserName(String userName) {
        RedisKey redisKey = RedisKeyEnum.USER_GROUP.of(userName);
        return RedisHelper.getSet(redisKey);
    }

    /**
     * 组装分组树结构(查询)
     */
    @Override
    public JSONArray getCurrentGroupTree() {
        // 获取当前用户所在组织及下级组织
        JSONArray result = new JSONArray();
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> currentUseOrgList = getCurrentUseOrgList();
        // 遍历得到当前用户组织及下级组织id的list
        List<String> orgIdList = currentUseOrgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // 分组
        List<GroupDTO> groupDTOList = userGroupService.getByGroupIdsAndUserId(getCurrentUserUuid(), orgIdList);
        generateGroupTree(groupDTOList, result, currentUseOrgList);
        currentUseOrgList = OrganizationUtil.filterOrgListByGroup(currentUseOrgList, groupDTOList);
        currentUseOrgList.sort(Comparator.comparing(OrganizationLdap::getCreateTimestamp));
        result.addAll(JsonUtil.getOrgTree(currentUseOrgList, "multiple"));
        return result;
    }

    @Override
    public List<String> getValidVehicleId(String fuzzySign, String fuzzyParam, String deviceType, String moType,
        boolean isNeedSort) {
        // 获取权限下的车辆
        String currentUsername = SystemHelper.getCurrentUsername();
        Set<String> groupSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(currentUsername));
        if (CollectionUtils.isEmpty(groupSet)) {
            return new ArrayList<>();
        }
        boolean fuzzyNameNotBlank = StringUtils.isNotBlank(fuzzyParam);
        // 模糊搜索分组
        if (Objects.equals(fuzzySign, "1") && fuzzyNameNotBlank) {
            Set<String> filterGroupIds =
                groupService.getListByFuzzyName(fuzzyParam).stream().map(GroupDTO::getId).collect(Collectors.toSet());
            groupSet.retainAll(filterGroupIds);
            if (CollectionUtils.isEmpty(groupSet)) {
                return new ArrayList<>();
            }
        }
        List<RedisKey> groupMonitorKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupSet);
        Set<String> groupMonitorIds = RedisHelper.batchGetSet(groupMonitorKeys);
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new ArrayList<>();
        }
        // 模糊搜索监控对象
        if (Objects.equals(fuzzySign, "0") && fuzzyNameNotBlank) {
            fuzzyParam = com.zw.platform.util.StringUtil.mysqlLikeWildcardTranslation(fuzzyParam);
            Set<String> filterMoIds = newConfigDao.getMoIdsByFuzzyMoName(fuzzyParam);
            groupMonitorIds.retainAll(filterMoIds);
            // 模糊搜索企业
        } else if (Objects.equals(fuzzySign, "2") && fuzzyNameNotBlank) {
            List<OrganizationLdap> filterOrgList = organizationService.fuzzyOrgList(fuzzyParam);
            Set<String> filterOrgIds = CollectionUtils.isEmpty(filterOrgList) ? new HashSet<>() :
                filterOrgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toSet());
            Set<String> filterMoIds =
                CollectionUtils.isEmpty(filterOrgIds) ? new HashSet<>() : newConfigDao.getMoIdsByOrgIds(filterOrgIds);
            groupMonitorIds.retainAll(filterMoIds);
        }
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new ArrayList<>();
        }

        if (StringUtils.isNotBlank(deviceType)) {
            List<String> deviceTypes = ProtocolTypeUtil.getProtocolTypes(deviceType);
            // 协议类型下的车辆id
            Set<String> moIdsByProtocolTypes =
                CollectionUtils.isEmpty(deviceTypes) ? new HashSet<>() : newConfigDao.getMoIdByDeviceTypes(deviceTypes);
            // 交集
            groupMonitorIds.retainAll(moIdsByProtocolTypes);
            if (CollectionUtils.isEmpty(groupMonitorIds)) {
                return new ArrayList<>();
            }
        }

        if (moType != null) {
            groupMonitorIds = newConfigDao.filterMoIdsByMoType(groupMonitorIds, moType);
        }
        return isNeedSort ? VehicleUtil.sortVehicles(groupMonitorIds) : new ArrayList<>(groupMonitorIds);
    }

    @Override
    public List<String> getValidVehicleId(String orgId, String assignmentId, String deviceType, String simpleQueryParam,
        String moType, boolean isNeedSort) {
        // 获取权限下的车辆
        String currentUsername = SystemHelper.getCurrentUsername();
        Set<String> groupSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(currentUsername));
        if (CollectionUtils.isEmpty(groupSet)) {
            return new ArrayList<>();
        }
        if (StringUtils.isNotBlank(assignmentId)) {
            groupSet = groupSet.contains(assignmentId) ? Sets.newHashSet(assignmentId) : new HashSet<>();
        } else if (StringUtils.isNotBlank(orgId)) {
            Set<String> orgGroupIds = RedisHelper.getSet(RedisKeyEnum.ORG_GROUP.of(orgId));
            orgGroupIds = orgGroupIds == null ? new HashSet<>() : orgGroupIds;
            groupSet.retainAll(orgGroupIds);
        }
        List<RedisKey> groupMonitorKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupSet);
        Set<String> groupMonitorIds = RedisHelper.batchGetSet(groupMonitorKeys);
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new ArrayList<>();
        }

        if (StringUtils.isNotBlank(deviceType)) {
            List<String> deviceTypes = ProtocolTypeUtil.getProtocolTypes(deviceType);
            // 协议类型下的车辆id
            Set<String> moIdsByProtocolTypes =
                CollectionUtils.isEmpty(deviceTypes) ? new HashSet<>() : newConfigDao.getMoIdByDeviceTypes(deviceTypes);
            // 交集
            groupMonitorIds.retainAll(moIdsByProtocolTypes);
            if (CollectionUtils.isEmpty(groupMonitorIds)) {
                return new ArrayList<>();
            }
        }

        if (StringUtils.isNotBlank(simpleQueryParam)) {
            simpleQueryParam = com.zw.platform.util.StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam);
            Set<String> filterMoIds = newConfigDao.getMoIdsByFuzzyMoName(simpleQueryParam);
            // 交集
            groupMonitorIds.retainAll(filterMoIds);
            if (CollectionUtils.isEmpty(groupMonitorIds)) {
                return new ArrayList<>();
            }
        }
        if (moType != null) {
            groupMonitorIds = newConfigDao.filterMoIdsByMoType(groupMonitorIds, moType);
        }
        return isNeedSort ? VehicleUtil.sortVehicles(groupMonitorIds) : new ArrayList<>(groupMonitorIds);
    }

    @Override
    public void setObjectTypeName(Collection<BindDTO> bindDtoList) {
        if (CollectionUtils.isEmpty(bindDtoList)) {
            return;
        }
        // 车辆id
        Set<String> vehicleIdSet =
            bindDtoList.stream().filter(obj -> Objects.equals(obj.getMonitorType(), MonitorTypeEnum.VEHICLE.getType()))
                .map(BindDTO::getId).collect(Collectors.toSet());
        // 车id -> 车辆类型id
        Map<String, String> vehicleIdAndTypeMap = CollectionUtils.isEmpty(vehicleIdSet) ? new HashMap<>(4) :
            newVehicleDao.getVehicleByIds(vehicleIdSet).stream()
                .collect(Collectors.toMap(VehicleInfo::getId, VehicleInfo::getVehicleType));
        // 车辆类型id -> 车辆类型名称
        Map<String, String> vehicleTypeIdAndNameMap = vehicleIdAndTypeMap.isEmpty() ? new HashMap<>(4) :
            newVehicleTypeDao.getByIds(new HashSet<>(vehicleIdAndTypeMap.values())).stream()
                .collect(Collectors.toMap(VehicleTypeDTO::getId, VehicleTypeDTO::getType));
        // 物品id
        Set<String> thingIdSet =
            bindDtoList.stream().filter(obj -> Objects.equals(obj.getMonitorType(), MonitorTypeEnum.THING.getType()))
                .map(BindDTO::getId).collect(Collectors.toSet());
        // 物品id -> 物品类型code
        Map<String, String> thingIdAndTypeCodeMap = CollectionUtils.isEmpty(thingIdSet) ? new HashMap<>(4) :
            thingDao.getByIds(thingIdSet).stream().collect(Collectors.toMap(ThingInfo::getId, ThingInfo::getType));
        // 物品类型code -> 物品类型名称
        Map<String, String> thingTypeCodeAndNameMap =
            TypeCacheManger.getInstance().getDictCodeValueMap(DictionaryType.THING_TYPE);
        for (BindDTO bindDTO : bindDtoList) {
            String moId = bindDTO.getId();
            String monitorType = bindDTO.getMonitorType();
            String objectTypeName = "";
            // 车
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                objectTypeName = vehicleTypeIdAndNameMap.get(vehicleIdAndTypeMap.get(moId));
                // 物
            } else if (Objects.equals(monitorType, MonitorTypeEnum.THING.getType())) {
                objectTypeName = thingTypeCodeAndNameMap.get(thingIdAndTypeCodeMap.get(moId));
                // 人
            } else if (Objects.equals(monitorType, MonitorTypeEnum.PEOPLE.getType())) {
                objectTypeName = "-";
            }
            bindDTO.setObjectTypeName(objectTypeName);
        }
    }

    @Override
    public void setObjectTypeName(BindDTO bindDTO) {
        if (bindDTO == null) {
            return;
        }
        String id = bindDTO.getId();
        String monitorType = bindDTO.getMonitorType();
        String objectTypeName = "";
        if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
            VehicleTypeDO vehicleType = newVehicleTypeDao.getByVehicleId(id);
            objectTypeName = vehicleType == null ? null : vehicleType.getVehicleType();
        } else if (Objects.equals(monitorType, MonitorTypeEnum.THING.getType())) {
            ThingDO thingInfo = thingDao.getById(id);
            // 物品类型code -> 物品类型名称
            Map<String, String> thingTypeCodeAndNameMap =
                TypeCacheManger.getInstance().getDictCodeValueMap(DictionaryType.THING_TYPE);
            objectTypeName = thingInfo == null ? null : thingTypeCodeAndNameMap.get(thingInfo.getType());
        } else if (Objects.equals(monitorType, MonitorTypeEnum.PEOPLE.getType())) {
            objectTypeName = "-";
        }
        bindDTO.setObjectTypeName(objectTypeName);
    }

    /**
     * 获得当前用户下级企业的相关信息
     * @return
     */
    @Override
    public Map<String, GroupRank> getCurrentUserOrgInfoList() {
        List<OrganizationLdap> currentUseOrgList = getCurrentUseOrgList();
        if (CollectionUtils.isEmpty(currentUseOrgList)) {
            return new HashMap<>(1);
        }
        Map<String, GroupRank> orgInfoMap = new HashMap<>(CommonUtil.ofMapCapacity(currentUseOrgList.size()));
        for (OrganizationLdap org : currentUseOrgList) {
            if (Constants.GROUP_UUID.equals(org.getUuid())) {
                continue;
            }
            GroupRank groupRank = new GroupRank();
            groupRank.setGroupName(org.getName());
            groupRank.setGroupId(org.getUuid());
            String provinceName = org.getProvinceName() != null ? org.getProvinceName() : "-";
            String countyName = org.getCountyName() != null ? org.getCountyName() : "-";
            String cityName = org.getCityName() != null ? org.getCityName() : "-";
            groupRank.setArea(provinceName + "," + cityName + "," + countyName);
            orgInfoMap.put(org.getUuid(), groupRank);
        }
        return orgInfoMap;
    }

    /**
     * 组装树
     * @param groupDTOList
     * @param result
     */
    private void generateGroupTree(List<GroupDTO> groupDTOList, JSONArray result, List<OrganizationLdap> orgList) {
        if (CollectionUtils.isEmpty(groupDTOList) || CollectionUtils.isEmpty(orgList)) {
            return;
        }
        Map<String, OrganizationLdap> orgMap = new HashMap<>(CommonUtil.ofMapCapacity(orgList.size()));
        for (OrganizationLdap org : orgList) {
            orgMap.put(org.getUuid(), org);
        }
        try {
            //分组Id
            OrganizationLdap organization;
            JSONObject groupObj;
            Set<String> groupIds = groupDTOList.stream().map(GroupDTO::getId).collect(Collectors.toSet());
            Map<String, Set<String>> groupMonitorMap =
                RedisHelper.batchGetSetReturnMap(RedisKeyEnum.GROUP_MONITOR.ofs(groupIds));
            for (GroupDTO groupDTO : groupDTOList) {
                organization = orgMap.get(groupDTO.getOrgId());
                if (organization == null) {
                    continue;
                }
                // 组装分组树
                groupObj = new JSONObject();
                // 信息配置分组是否能勾选
                Set<String> monitorIds = groupMonitorMap.get(groupDTO.getId());
                groupObj.put("canCheck", CollectionUtils.isNotEmpty(monitorIds) ? monitorIds.size() : 0);
                groupObj.put("id", groupDTO.getId());
                groupObj.put("pId", organization.getId().toString());
                groupObj.put("name", groupDTO.getName());
                groupObj.put("type", "assignment");
                groupObj.put("iconSkin", "assignmentSkin");
                groupObj.put("pName", organization.getName());
                result.add(groupObj);
            }
        } catch (Exception e) {
            log.error("分组数异常", e);
        }
    }

    @Override
    public JsonResultBean updateUserByRole(String userIds, String roleId) {
        List<LdapName> addRoleUsers = new ArrayList<>();
        //拿到当前用户所属企业及下级企业所有用户的member
        Set<Name> members = getAllUserMember();
        Group role = roleService.getGroupById(roleId);
        //修改前有该角色的用户
        Set<Name> roleMembers = role.getMembers();
        //传进来的用户
        Set<Name> userMembers = new HashSet<>();
        String roleName = role.getName();
        //循环添加用户到角色members
        if (userIds != null) {
            List<String> userIdList = Arrays.asList(userIds.split(","));
            List<UserDTO> userListByUuids = getUserListByUuids(userIdList);
            for (UserDTO userDTO : userListByUuids) {
                LdapName name = LdapUtils.newLdapName(userDTO.getId() + "," + getBaseLdapPath().toString());
                userMembers.add(name);
                if (roleMembers.contains(name)) {
                    continue;
                }
                addRoleUsers.add(name);
            }
        }
        UserDTO currentUserInfo = getCurrentUserInfo();
        LdapName currentName = LdapUtils.newLdapName(currentUserInfo.getId() + "," + getBaseLdapPath().toString());
        //需要删除的角色
        members.removeAll(userMembers);
        members.retainAll(roleMembers);

        //新增的角色是否包含禁言角色
        if (isSilentRole(roleName)) {
            for (LdapName ldapName : addRoleUsers) {
                List<Group> currentRoles = (List<Group>) roleService.getByMemberName(ldapName);
                if (!containsDispatcherRole(currentRoles)) {
                    return new JsonResultBean(getUserName(ldapName) + "该用户没有调度员角色，无法分配禁言角色");
                }
            }
        }
        for (Name name : members) {
            if (name.equals(currentName)) {
                continue;
            }
            roleService.removeMemberFromGroup(roleName, name);
        }

        boolean isDispatcherRole = isDispatcherRole(roleId);
        if (isDispatcherRole) {
            return dealDisPatcher(addRoleUsers, members, roleName, currentName);
        } else {
            updateRoleUser(addRoleUsers, members, roleName, currentName);
            return new JsonResultBean("角色授权成功！");
        }

    }

    @Override
    public OrganizationLdap getCurUserOrgAdminFirstOrg() {
        String currentUserDn = SystemHelper.getCurrentUserDn();
        boolean isAdminRole = isAdminRole();
        if (isAdminRole) {
            List<OrganizationLdap> orgList = getOrgListByUserDn(SystemHelper.getCurrentUId());
            if (orgList != null && orgList.size() > 1) {
                return orgList.get(1);
            }
        }
        return organizationService.getOrgByEntryDn(getUserOrgDnByDn(currentUserDn));
    }

    @Override
    public Set<String> fuzzySearchFilterOrgIds(String simpleQueryParam, String orgIds) {
        if (StringUtils.isBlank(orgIds)) {
            return Collections.emptySet();
        }
        if (StringUtils.isBlank(simpleQueryParam)) {
            return Arrays.stream(orgIds.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        }
        List<String> fuzzSearchOrgIdsByOrgName = fuzzSearchUserOrgIdsByOrgName(simpleQueryParam);
        if (CollectionUtils.isEmpty(fuzzSearchOrgIdsByOrgName)) {
            return Collections.emptySet();
        }
        Set<String> fuzzySearchOrgIdSet = new HashSet<>(fuzzSearchOrgIdsByOrgName);
        return Arrays.stream(orgIds.split(","))
            .filter(orgId -> StringUtils.isNotBlank(orgId) && fuzzySearchOrgIdSet.contains(orgId))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> fuzzySearchFilterMonitorIds(String simpleQueryParam, String monitorIds) {
        if (StringUtils.isBlank(monitorIds)) {
            return Collections.emptySet();
        }
        if (StringUtils.isBlank(simpleQueryParam)) {
            return Arrays.stream(monitorIds.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        }
        Set<String> moIdsByFuzzyMoName = newConfigDao
            .getMoIdsByFuzzyMoName(com.zw.platform.util.StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        if (CollectionUtils.isEmpty(moIdsByFuzzyMoName)) {
            return Collections.emptySet();
        }
        return Arrays.stream(monitorIds.split(","))
            .filter(monitorId -> StringUtils.isNotBlank(monitorId) && moIdsByFuzzyMoName.contains(monitorId))
            .collect(Collectors.toSet());
    }

    @Override
    public UserMenuDTO loadUserPermission(String username) {
        final UserDTO user = getUserByUsername(username);
        final String userDn = user.getId().toString();
        final Collection<Group> roles = roleService.getByMemberNameStr(userDn);
        final List<String> roleDns = CollectionUtils.isEmpty(roles)
                ? Collections.emptyList()
                : roles.stream().map(Group::getId).map(Object::toString).collect(Collectors.toList());
        final Set<String> menuIds = roleService.listMenuIdByRoleId(roleDns);
        return new UserMenuDTO(user, menuIds);
    }

    /**
     * 更新角色用户
     * @param addRoleUsers 要新增用户
     * @param members      要删除的用户
     * @param roleName
     * @param currentName  当前用户的Name
     */
    private void updateRoleUser(List<LdapName> addRoleUsers, Set<Name> members, String roleName, LdapName currentName) {
        for (LdapName ldapName : addRoleUsers) {
            roleService.addMemberToGroup(roleName, ldapName);
        }
        for (Name name : members) {
            if (name.equals(currentName)) {
                continue;
            }
            roleService.removeMemberFromGroup(roleName, name);
        }
    }

    private Set<Name> getAllUserMember() {
        Set<Name> set = new HashSet<>();
        List<UserDTO> userInfos = new ArrayList<>();
        List<OrganizationLdap> organizationLdapList = getCurrentUseOrgList();
        for (OrganizationLdap group : organizationLdapList) {
            userInfos.addAll(getUserByOrgDn(group.getId().toString(), SearchScope.ONELEVEL));
        }
        for (UserDTO userDTO : userInfos) {
            set.add(userDTO.getMember());
        }
        return set;
    }

    /**
     * 判断是否是禁言角色
     */
    private boolean isSilentRole(String roleId) {
        final List<Group> roles = roleService.getListByKeyword("禁言角色", false);
        if (roles == null) {
            // 不存在禁言角色
            return false;
        }
        Group groupRole = roles.get(0);
        if (!roleId.contains("cn=")) {
            return roleId.equals(groupRole.getName() + "");
        }

        return ("cn=" + groupRole.getName() + ",ou=Groups").equals(roleId);
    }

    private JsonResultBean dealDisPatcher(List<LdapName> addRoleUsers, Set<Name> members, String roleName,
        LdapName currentName) {
        List<String> callNumbers = new ArrayList<>();
        List<IntercomIotUserForm> iotUsers = new ArrayList<>();
        Set<String> usedCallNumber = new HashSet<>();
        Set<String> recycleCallNumber = new HashSet<>();
        Map<String, String> userIdMap = new HashMap<>();
        List<IntercomIotUserForm> deleteIotUsers = new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(addRoleUsers)) {
                callNumbers = updateAndReturnPersonCallNumbers(addRoleUsers.size());
            }
            for (int i = 0, len = callNumbers.size(); i < len; i++) {
                String userName = getUserName(addRoleUsers.get(i));
                JSONObject result = talkCallUtils.addIotUser(userName, "2", callNumbers.get(i));
                if (result.getIntValue("result") == 0) {
                    String userId = result.getJSONObject("data").getString("userId");
                    iotUsers.add(new IntercomIotUserForm(userName, userId, callNumbers.get(i)));
                    usedCallNumber.add(callNumbers.get(i));
                    //分配角色的时候，新增调度员成功后，需要把userId存到ldap中
                    userIdMap.put(getUserId(addRoleUsers.get(i)), userId);
                } else {
                    recycleCallNumber.add(callNumbers.get(i));
                }
            }

            List<String> userNames = new ArrayList<>();
            List<Name> deleteUsers = new ArrayList<>(members);
            for (Name deleteUser : deleteUsers) {
                if (deleteUser.equals(currentName)) {
                    continue;
                }
                userNames.add(getUserName(deleteUser));
                //删除调度员角色的时候，需要清空调度员在ldap中的userId
                userIdMap.put(getUserId(deleteUser), "");
            }

            if (CollectionUtils.isNotEmpty(userNames)) {
                deleteIotUsers = intercomIotUserDao.getIntercomIotUsersByUserNames(userNames);
                for (IntercomIotUserForm deleteIotUser : deleteIotUsers) {
                    JSONObject result = talkCallUtils.deleteIotUser(deleteIotUser.getUserId() + "");
                    if (result.getIntValue("result") == 0) {
                        recycleCallNumber.add(deleteIotUser.getCallNumber() + "");
                        //将调度
                        // 员账号标志设置成0，代表删除该调度员
                        deleteIotUser.setFlag(0);
                    }
                }
            }

            // 新增运调度员角色
            if (CollectionUtils.isNotEmpty(iotUsers)) {
                intercomIotUserDao.updateIntercomIotUsers(iotUsers);
            }

            // 删除运调度员角色
            if (CollectionUtils.isNotEmpty(deleteIotUsers)) {
                intercomIotUserDao.updateIntercomIotUsers(deleteIotUsers);
            }

            // 回收个呼号码
            if (CollectionUtils.isNotEmpty(recycleCallNumber)) {
                updateAndRecyclePersonCallNumberBatch(recycleCallNumber);
            }

            // 更新ldap的调度账号id
            for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
                updateModifyUserDispatcherId(entry.getKey(), entry.getValue());
            }

            // 删除调度员角色的时候，一定要删除禁言角色
            deleteSilenceRolesUser(members, currentName);

            // 更新ladp相关字段信息
            updateRoleUser(addRoleUsers, members, roleName, currentName);

            return new JsonResultBean("角色授权成功！");

        } catch (CallNumberExhaustException e) {
            return new JsonResultBean(JsonResultBean.FAULT, "个呼号码不够用了，请减少要分配用户");
        } catch (Exception e) {
            // 异常情况，进行个呼号码的集体回收,并删除已经创建好的调度员账号
            recyclePersonCallNumber(callNumbers);
            deleteIotUsers(iotUsers);
            return new JsonResultBean(JsonResultBean.FAULT, "授权异常！");
        }
    }

    /**
     * 当异常的时候，需要删除之前的异常数据
     * @param iotUsers iotUsers
     */
    private void deleteIotUsers(List<IntercomIotUserForm> iotUsers) {
        if (CollectionUtils.isNotEmpty(iotUsers)) {
            for (IntercomIotUserForm user : iotUsers) {
                talkCallUtils.deleteIotUser(user.getUserId() + "");
                updateModifyUserDispatcherId(user.getUserId() + "", null);
                user.setFlag(0);
            }
            intercomIotUserDao.updateIntercomIotUsers(iotUsers);

        }
    }

    private void deleteSilenceRolesUser(Set<Name> members, LdapName currentName) {
        String silenceRole = roleService.getListByKeyword("禁言角色", false).get(0).getName() + "";
        for (Name name : members) {
            if (name.equals(currentName)) {
                continue;
            }
            roleService.removeMemberFromGroup(silenceRole, name);
        }
    }

    private List<String> updateAndReturnPersonCallNumbers(int length) throws Exception {
        Set<String> personCallNumbers = CallNumberUtil.popLengthPersonCallNumber(length);
        callNumberDao.updatePersonCallNumberBatch(personCallNumbers, (byte) 0);
        return new ArrayList<>(personCallNumbers);
    }

    private void updateAndRecyclePersonCallNumberBatch(Collection<String> personNumbers) {
        if (CollectionUtils.isEmpty(personNumbers)) {
            return;
        }
        String[] personNumberArr = new String[personNumbers.size()];
        personNumbers.toArray(personNumberArr);
        CallNumberUtil.recycleGroupCallNumber(personNumberArr);
        callNumberDao.updatePersonCallNumberBatch(personNumbers, (byte) 1);
    }

    private void recyclePersonCallNumber(List<String> callNumbers) {
        if (CollectionUtils.isNotEmpty(callNumbers)) {
            String[] personNumberArr = new String[callNumbers.size()];
            callNumbers.toArray(personNumberArr);
            CallNumberUtil.recycleGroupCallNumber(personNumberArr);
            callNumberDao.updatePersonCallNumberBatch(callNumbers, (byte) 1);
        }
    }
}
