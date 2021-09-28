package com.zw.platform.service.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.service.ChatGroupUserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.BusinessScopeDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.GroupRepo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.OrganizationRepo;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.UserRepo;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.Reflections;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.intercom.form.IntercomIotUserForm;
import com.zw.talkback.repository.mysql.CallNumberDao;
import com.zw.talkback.repository.mysql.IntercomIotUserDao;
import com.zw.talkback.util.CallNumberUtil;
import com.zw.talkback.util.TalkCallUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 用户公共方法类
 * @deprecated  since 4.4.0  replaced by com.zw.platform.basic.service.UserService
 * @link #com.zw.platform.basic.service.UserService
 */
@Service("oldUserService")
public class UserService implements BaseLdapNameAware {

    private static final Logger log = LogManager.getLogger(UserService.class);

    private final UserRepo userRepo;

    private final GroupRepo groupRepo;

    private final OrganizationRepo organizationRepo;

    private LdapName baseLdapPath;

    private final LdapTemplate ldapTemplate;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private RoleService roleService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private BusinessScopeDao businessScopeDao;

    @Autowired
    private ChatGroupUserService chatGroupUserService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private TalkCallUtil talkCallUtils;

    @Autowired
    private CallNumberDao callNumberDao;

    @Autowired
    private IntercomIotUserDao intercomIotUserDao;

    @Autowired
    private WebClientHandleCom webClientHandleCom;

    @Value("${experience.role.entryDN}")
    private String roleEntryDN;

    /**
     * 组织信息缓存
     */
    private static final Cache<String, OrganizationLdap> ORG_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    /**
     * 构造方法
     * @param userRepo         userRepo
     * @param groupRepo        groupRepo
     * @param organizationRepo organizationRepo
     * @param ldapTemplate     ldapTemplate
     */
    @Autowired
    public UserService(UserRepo userRepo, GroupRepo groupRepo, OrganizationRepo organizationRepo,
        LdapTemplate ldapTemplate) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.organizationRepo = organizationRepo;
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    /**
     * findAll
     * @return alluser
     */
    public Iterable<UserBean> findAll() {
        return userRepo.findAll();
    }

    /**
     * findUser
     * @param userId userId
     * @return user
     */
    public UserBean findUser(String userId) {
        return userRepo.findOne(LdapUtils.newLdapName(userId));
    }


    /**
     * 根据用户名和组织部门构建dn
     * @param userName userName
     * @param groupId  groupId
     * @return Name
     * @author fanlu
     */
    private static Name bindDN(String userName, String groupId) {
        return LdapUtils.newLdapName("uid=" + userName + "," + groupId);
    }

    /**
     * toAbsoluteDn
     * @param relativeName relativeName
     * @return LdapName
     */
    public LdapName toAbsoluteDn(Name relativeName) {
        return LdapNameBuilder.newInstance(baseLdapPath).add(relativeName).build();
    }

    /**
     * This method expects absolute DNs of group members. In order to find the actual users the DNs need to have the
     * base LDAP path removed.
     * @param absoluteIds absoluteIds
     * @return Set<UserBean>
     */
    public Set<UserBean> findAllMembers(Iterable<Name> absoluteIds) {
        return Sets.newLinkedHashSet(userRepo.findAll(toRelativeIds(absoluteIds)));
    }

    /**
     * toRelativeIds
     * @param absoluteIds absoluteIds
     * @return Iterable<Name>
     */
    private Iterable<Name> toRelativeIds(Iterable<Name> absoluteIds) {
        return StreamSupport.stream(absoluteIds.spliterator(), false)
            .map(input -> LdapUtils.removeFirst(input, baseLdapPath)).collect(Collectors.toList());
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
    }

    /**
     * 修改密码
     * @param password  密码
     * @param ipAddress ipAddress
     * @author fanlu
     */
    public void updateModifyPwd(String password, String ipAddress, String equipmentType) {
        Name dn = SystemHelper.getCurrentUser().getId();
        Attribute attr = new BasicAttribute("userPassword", SecurityPasswordHelper.encodePassword(password));
        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] { item });
        String username = SystemHelper.getCurrentUsername();
        String log = "用户：" + username + " 修改密码";
        String logSource;
        if (StringUtils.isNotBlank(equipmentType) && "APP".equals(equipmentType)) {
            logSource = "4";
        } else {
            logSource = "3";
        }
        logSearchServiceImpl.addLog(ipAddress, log, logSource, "", "-", "");
        updateUserOffline(ipAddress, username); // 用户下线时长记录
        expireUserSession(SystemHelper.getCurrentUsername());
    }

    /**
     * 查询所有的组织架构
     * @return List<OrganizationLdap>
     * @author wangying
     */
    public List<OrganizationLdap> getAllOrganization() {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = "objectClass=organizationalUnit";
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
        List<OrganizationLdap> list =
            ldapTemplate.search(searchBase, searchFilter, searchCtls, new OrganizationContextMapper());
        List<OrganizationLdap> orgList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (OrganizationLdap obj : list) {
                // Goups是用户权限
                if ("Groups".equals(obj.getOu())) {
                    orgList.remove(obj);
                    continue;
                }
                Name id = obj.getId();
                String cid = id.toString();
                setcpId(obj, cid);
                orgList.add(obj);
            }
        }
        return orgList;
    }

    private void setcpId(OrganizationLdap obj, String cid) {
        // 有父节点
        if (cid.indexOf(",") > 0) {
            int beginIndex = cid.indexOf(",");
            obj.setCid(cid);
            obj.setPid(cid.substring(beginIndex + 1));
        } else {
            // 根节点
            obj.setCid(cid);
            obj.setPid("");
        }
    }

    /**
     * 新增组织
     * @param organizationLdap organizationLdap
     * @param ipAddress        ipAddress
     */
    public void addCreateGroup(OrganizationLdap organizationLdap, String ipAddress) {
        Attribute objectClass = new BasicAttribute("objectClass");
        {
            objectClass.add("top");
            objectClass.add("organizationalUnit");
        }
        Attributes attributes = new BasicAttributes();
        attributes.put(objectClass);
        String ou = "ORG_" + UUID.randomUUID();
        organizationLdap.setOu(ou);
        attributes.put("ou", ou);
        if (organizationLdap.getName() != null && !organizationLdap.getName().isEmpty()) {
            attributes.put("l", organizationLdap.getName());
        }
        if (organizationLdap.getOrganizationCode() != null && !organizationLdap.getOrganizationCode().isEmpty()) {
            attributes.put("postalCode", organizationLdap.getOrganizationCode());
        }
        if (organizationLdap.getRegisterDate() != null && !organizationLdap.getRegisterDate().isEmpty()) {
            attributes.put("physicalDeliveryOfficeName", organizationLdap.getRegisterDate());
        }
        if (organizationLdap.getOperation() != null && !organizationLdap.getOperation().isEmpty()) {
            attributes.put("businessCategory", organizationLdap.getOperation());
        }
        if (organizationLdap.getLicense() != null && !organizationLdap.getLicense().isEmpty()) {
            attributes.put("p0", organizationLdap.getLicense());
        }
        if (organizationLdap.getPrincipal() != null && !organizationLdap.getPrincipal().isEmpty()) {
            attributes.put("st", organizationLdap.getPrincipal());
        }
        if (organizationLdap.getPhone() != null && !organizationLdap.getPhone().isEmpty()) {
            attributes.put("telephoneNumber", organizationLdap.getPhone());
        }
        if (organizationLdap.getAddress() != null && !organizationLdap.getAddress().isEmpty()) {
            attributes.put("registeredAddress", organizationLdap.getAddress());
        }
        if (organizationLdap.getDescription() != null && !organizationLdap.getDescription().isEmpty()) {
            attributes.put("description", organizationLdap.getDescription());
        }
        if (organizationLdap.getOperatingState() != null && !organizationLdap.getOperatingState().isEmpty()) {
            attributes.put("operatingState", organizationLdap.getOperatingState());
        }
        if (organizationLdap.getIssuingOrgan() != null && !organizationLdap.getIssuingOrgan().isEmpty()) {
            attributes.put("issuingOrgan", organizationLdap.getIssuingOrgan());
        }
        if (organizationLdap.getProvinceName() != null && !organizationLdap.getProvinceName().isEmpty()) {
            attributes.put("provinceName", organizationLdap.getProvinceName());
        }
        if (organizationLdap.getCityName() != null && !organizationLdap.getCityName().isEmpty()) {
            attributes.put("street", organizationLdap.getCityName());
        }
        if (organizationLdap.getCountyName() != null && !organizationLdap.getCountyName().isEmpty()) {
            attributes.put("countyName", organizationLdap.getCountyName());
        }
        if (organizationLdap.getAreaNumber() != null && !organizationLdap.getAreaNumber().isEmpty()) {
            attributes.put("areaNumber", organizationLdap.getAreaNumber());
        }
        if (organizationLdap.getLicenseValidityStartDate() != null && !organizationLdap.getLicenseValidityStartDate()
            .isEmpty()) {
            attributes.put("p7", organizationLdap.getLicenseValidityStartDate());
        }
        if (organizationLdap.getLicenseValidityEndDate() != null && !organizationLdap.getLicenseValidityEndDate()
            .isEmpty()) {
            attributes.put("p8", organizationLdap.getLicenseValidityEndDate());
        }
        if (organizationLdap.getContactName() != null && !organizationLdap.getContactName().isEmpty()) {
            attributes.put("p9", organizationLdap.getContactName());
        }
        if (organizationLdap.getUpOrganizationCode() != null && !organizationLdap.getUpOrganizationCode().isEmpty()) {
            attributes.put("upOrganizationCode", organizationLdap.getUpOrganizationCode());
        }
        Name name = bindDNS(organizationLdap);
        ldapTemplate.bind(name, null, attributes);
        organizationLdap.setId(name);
        // 维护缓存
        String msg = "新增组织: " + organizationLdap.getName();
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    public void addGroupNameRedis(OrganizationLdap organizationLdap) {
        final RedisKey key = RedisKeyEnum.ORGANIZATION_INFO.of(organizationLdap.getUuid());
        Map<String, String> orgMap = new HashMap<>();
        orgMap.put("id", organizationLdap.getUuid());
        orgMap.put("name", organizationLdap.getName());
        RedisHelper.addToHash(key, orgMap);
    }

    private static javax.naming.Name bindDNS(OrganizationLdap o) {
        return LdapUtils.newLdapName("ou=" + o.getOu() + "," + o.getPid());
    }

    /**
     * 查询子uuid和名称
     * @param orgId 父节点id
     * @param limit 查询数量
     * @return
     */
    public List<OrganizationLdap> getChildUuidAndName(String orgId, int limit) {
        String searchFilter = "objectClass=organizationalUnit";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] { "entryUUID", "l" });
        searchControls.setCountLimit(limit);
        return ldapTemplate.search(orgId, searchFilter, searchControls, (ContextMapper<OrganizationLdap>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            OrganizationLdap org = new OrganizationLdap();
            org.setUuid(context.getStringAttribute("entryUUID"));
            org.setName(context.getStringAttribute("l"));
            return org;
        });
    }

    /**
     * getOrgChild
     * 根据组织id(不是uuid,是entryDN)查询企业与其所有下级企业的信息
     * @param orgId orgId
     * @return List<OrganizationLdap>
     */
    public List<OrganizationLdap> getOrgChild(String orgId) {
        SearchControls searchCtls = new SearchControls();
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8", "p9",
                "upOrganizationCode", "k0", "k8", "createTimestamp" };
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        String searchFilter = "objectClass=organizationalUnit";
        List<OrganizationLdap> list;
        list = ldapTemplate.search(orgId, searchFilter, searchCtls, (ContextMapper<OrganizationLdap>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            OrganizationLdap org = new OrganizationLdap();
            String cid = context.getDn().toString();
            org.setId(context.getDn());
            // 有父节点
            setcpId(org, cid);
            org.setOu(context.getStringAttribute("ou"));
            org.setAddress(context.getStringAttribute("description"));
            org.setName(context.getStringAttribute("l"));
            org.setUuid(context.getStringAttribute("entryuuid"));
            org.setOperatingState(context.getStringAttribute("operatingState"));
            org.setScopeOfOperation(context.getStringAttribute("scopeOfOperation"));
            org.setProvinceName(context.getStringAttribute("provinceName"));
            org.setIssuingOrgan(context.getStringAttribute("issuingOrgan"));
            org.setCityName(context.getStringAttribute("street"));
            org.setCountyName(context.getStringAttribute("countyName"));
            org.setAreaNumber(context.getStringAttribute("areaNumber"));
            org.setLicense(context.getStringAttribute("p0")); // 经营许可证
            org.setLicenseValidityStartDate(context.getStringAttribute("p7"));
            org.setLicenseValidityEndDate(context.getStringAttribute("p8"));
            org.setContactName(context.getStringAttribute("p9"));
            org.setUpOrganizationCode(context.getStringAttribute("upOrganizationCode"));
            org.setManagerOrganizationCode(context.getStringAttribute("k0"));
            org.setBusinessLicenseType(context.getStringAttribute("k8"));
            org.setEntryDN(context.getStringAttribute("entryDN").replace(",dc=zwlbs,dc=com", ""));
            org.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
            return org;
        });
        if (list != null && !list.isEmpty()) {
            return list;
        }
        return null;
    }

    /**
     * 通过角色名称查询 拥有该角色的成员列表;
     * @param roleCn 角色 cn
     */
    public List<LdapName> getMemberNameListByRoleCn(String roleCn) {
        List<LdapName> result = new ArrayList<>();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returningAttributes = { "member" };
        searchControls.setReturningAttributes(returningAttributes);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "groupOfNames"));
        filter.and(new EqualsFilter("cn", roleCn));
        ldapTemplate.search(searchBase, filter.encode(), searchControls, (ContextMapper<Group>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            SortedSet<String> memberList = context.getAttributeSortedStringSet("member");
            for (String member : memberList) {
                result.add(LdapUtils.newLdapName(member));
            }
            return null;
        });
        return result;
    }

    /**
     * 当前用户所属组织的所有下级企业 uuid和name关系
     * @return
     */
    public Map<String, String> getUserOrgNameMap() {
        Map<String, String> map = new HashMap<>();
        String orgId = getOrgIdByUser();
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        for (OrganizationLdap org : orgs) {
            map.put(org.getUuid(), org.getName());
        }
        return map;
    }

    /**
     * 根据id查询组织架构
     * @param id id
     * @return OrganizationLdap
     * @author wangying
     */
    public OrganizationLdap findOrganization(String id) {
        return organizationRepo.findOne(LdapUtils.newLdapName(id));
    }

    /**
     * 获取所有组织
     * @return List<Group>
     */
    public List<Group> getAllGroup() {
        return (List<Group>) groupRepo.findAll();
    }

    public LdapName getBaseLdapPath() {
        return this.baseLdapPath;
    }

    /**
     * 根据用户id查询其所有的角色列表
     * @param member member
     * @return Collection<Group>
     * @author fanlu
     */
    public Collection<Group> findByMember(Name member) {
        return groupRepo.findByMember(member);
    }

    /**
     * 根据用户id查询其所有的角色
     */
    public Collection<Group> findByMember(String userId) {
        LdapName name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        return groupRepo.findByMember(name);
    }

    /**
     * 更新用户的角色列表，即添加用户到对应的用户组
     * @param userId    userId
     * @param roleIds   roleIds
     * @param ipAddress ipAddress
     * @author fanlu
     */
    public JsonResultBean updateRolesByUser(String userId, String roleIds, String ipAddress) {

        String str = "";

        // 取页面传来的角色列表和用户目前拥有的角色列表的差集，即用户已有角色列表中不存在于页面传来的角色列表中的角色，将用户从对应用户组中移除
        LdapName name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        // 获取用户所有的角色列表
        List<Group> currentRoles = (List<Group>) findByMember(name);

        List<String> currentRoleNames = new ArrayList<>();
        for (Group group : currentRoles) {
            currentRoleNames.add(group.getName());
        }
        if (StringUtils.isNotBlank(roleIds)) {

            String[] roleArray = roleIds.split(",");

            List<String> roleList = Arrays.asList(roleArray);

            List<String> roles = new ArrayList<>(currentRoleNames);

            roles.removeAll(roleList);

            // 取页面传来的角色列表和用户目前拥有的角色列表的差集，即页面传来的角色列表中的角色不存在于用户已有角色列表中，将用户添加至对应用户组中

            List<String> roles1 = new ArrayList<>(currentRoleNames);

            List<String> roleList1 = new LinkedList<>(Arrays.asList(roleArray));

            roleList1.removeAll(roles1);

            // 循环添加用户至用户组
            for (String roleName : roleList1) {
                //判断时候是调度员角色，则新增
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = addDispatcherRoleToUser(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    break;
                }
                //新增的角色是否包含禁言角色
                if (isSilentRole(roleName)) {
                    boolean canAuthSilentRole = canAuthSilentRole(currentRoles, roleList1);
                    if (!canAuthSilentRole) {
                        return new JsonResultBean("当前用户没有调度员角色，无法分配禁言角色");
                    }

                }

            }

            // 循环从用户组移除用户，并判断是否是调度员角色，则删除
            for (String roleName : roles) {
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = removeDispatcherRole(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    //删除调度员角色，一定会删除禁言角色
                    deleteSilentRole(name);
                    break;
                }
            }

            // 循环添加用户至用户组
            for (String roleName : roleList1) {
                boolean judge = judge(currentRoleNames, roleIds);
                if (!judge) {
                    str = ("当前用户没有调度员角色，无法分配禁言角色");
                    continue;
                }
                groupRepo.addMemberToGroup(roleName, name);
            }
            // 循环从用户组移除用户
            for (String roleName : roles) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
        } else { // 若选择角色为空
            // 循环从用户组移除用户
            for (String roleName : currentRoleNames) {
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = removeDispatcherRole(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    //删除调度员角色，一定会删除禁言角色
                    deleteSilentRole(name);
                    break;
                }
            }
            // 循环从用户组移除用户
            for (String roleName : currentRoleNames) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
        }
        if (ipAddress != null && !"".equals(ipAddress)) {
            // 第一个逗号所在下标
            int commaIndex = userId.indexOf(",");
            // 第一个等号所在下表
            int eqIndex = userId.indexOf("=");
            // 截取用户名
            String userName = userId.substring(eqIndex + 1, commaIndex);
            String log = "为用户 " + userName + " 分配角色";
            logSearchServiceImpl.addLog(ipAddress, log, "3", "", "-", "");
        }

        if (!str.equals("")) {
            return new JsonResultBean(str);
        }

        return new JsonResultBean(JsonResultBean.SUCCESS, "分配角色成功");
    }

    public boolean judge(List<String> currentRoleNames, String roleIds) {

        boolean flag = true;

        String[] roleArray = roleIds.split(",");

        List<String> collect = Stream.of(roleArray).collect(Collectors.toList());

        List<Group> allGroup = getAllGroup();

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

                    flag = collect.contains(diaoDuId);

                }

            }
        }

        return flag;
    }

    private void deleteSilentRole(LdapName name) {
        Group groupRole = queryRoleList("禁言角色", false).get(0);
        groupRepo.removeMemberFromGroup(groupRole.getName() + "", name);
    }

    /**
     * 能否授权禁言角色
     * @param currentRoles
     * @param roleList1
     * @return
     */
    private boolean canAuthSilentRole(List<Group> currentRoles, List<String> roleList1) {
        boolean containsDispatcherRole = false;
        if (roleList1.size() >= 2) {
            for (String role : roleList1) {
                if (isDispatcherRole(role)) {
                    containsDispatcherRole = true;
                    break;
                }
            }
        }
        return containsDispatcherRole || containsDispatcherRole(currentRoles);
    }

    private boolean containsDispatcherRole(Collection<Group> currentRoles) {
        boolean isDispatcherRole = false;
        for (Group group : currentRoles) {
            isDispatcherRole = isDispatcherRole(group.getName());
            if (isDispatcherRole) {
                break;
            }

        }
        return isDispatcherRole;
    }

    private JsonResultBean addDispatcherRoleToUser(LdapName name) {
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
        } catch (Exception e) {
            // 异常情况，进行个呼号码的集体回收,并删除已经创建好的调度员账号
            if (StrUtil.isNotBlank(addCallNumber)) {
                updateAndRecyclePersonCallNumber(addCallNumber);
            }
            if (iotUser != null) {
                iotUser.setFlag(0);
                intercomIotUserDao.updateIntercomIotUser(iotUser);
                updateModifyUserDispatcherId(getUserId(name), null);
            }
            return new JsonResultBean(JsonResultBean.FAULT, result.getString("message"));
        }
        return null;
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

    private void updateAndRecyclePersonCallNumber(String callNumber) {
        CallNumberUtil.recyclePersonCallNumber(callNumber);
        callNumberDao.updatePersonCallNumber(callNumber, (byte) 1);
    }

    public String updateAndReturnPersonCallNumber() throws Exception {
        String personCallNumber = CallNumberUtil.popPersonCallNumber();
        callNumberDao.updatePersonCallNumber(personCallNumber, (byte) 0);
        return personCallNumber;
    }

    /**
     * 根据id删除用户
     * @param userId    userId
     * @param ipAddress ipAddress
     * @author FanLu
     */
    public void delete(String userId, String ipAddress) {

        String[] items = userId.split(";");
        StringBuilder message = new StringBuilder();
        for (String item : items) {
            LdapName name = LdapUtils.newLdapName(item);
            ldapTemplate.unbind(name);
            name = LdapUtils.newLdapName(name.toString() + "," + getBaseLdapPath().toString());
            List<Group> currentRoles = (List<Group>) findByMember(name);
            List<String> currentRoleNames = new ArrayList<>();
            for (Group group : currentRoles) {
                currentRoleNames.add(group.getName());
            }
            // 循环从用户组移除用户
            for (String roleName : currentRoleNames) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
            // 第一个逗号所在下标
            int commaIndex = item.indexOf(",");
            // 第一个等号所在下表
            int eqIndex = item.indexOf("=");
            String orgId = item.substring(commaIndex + 1);
            // 获得部门组织架构
            OrganizationLdap org = findOrganization(orgId);
            // 截取用户名
            String username = item.substring(eqIndex + 1, commaIndex);
            log.info("删除用户");
            expireUserSession(username);
            message.append("删除用户 : ").append(username).append(" ( @").append(org.getName()).append(")")
                .append(" <br/>");
        }
        if (!message.toString().isEmpty()) {
            if (items.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除用户");
            }
        }
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

    /**
     * listUserByOrgId
     * @param orgId orgId
     * @return List<UserBean>
     */
    public List<UserBean> listUserByOrgId(String orgId) {
        return ldapTemplate.search(LdapQueryBuilder.query().base(orgId).searchScope(SearchScope.ONELEVEL)
            .attributes("entryDN", "entryUUID", "givenName", "uid", "createTimestamp", "telexNumber")
            .where("objectclass").is("inetOrgPerson"), new UserContextMapper());
    }

    public List<UserBean> findFuzzyUserByOrgId(String orgId, String queryParam) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        String[] returnAttrs = { "entryDN", "entryUUID", "givenName", "uid", "createTimestamp", "telexNumber" };
        searchCtls.setReturningAttributes(returnAttrs);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        if (StringUtils.isNotEmpty(queryParam)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("uid", "*" + queryParam + "*"));
            andfilter.append(orfilter);
        }
        return ldapTemplate.search(orgId, andfilter.encode(), searchCtls, new UserContextMapper());
    }

    private void setUserOrg(List<UserBean> users) {
        List<OrganizationLdap> allOrgs = getAllOrganization();
        Map<String, String> allOrgsMap = new HashMap<>();
        for (OrganizationLdap org : allOrgs) {
            allOrgsMap.put(org.getId().toString(), org.getName());
        }
        String orgId;
        for (UserBean user : users) {
            orgId = getOrgIdByUserId(user.getId().toString());
            user.setGroupName(allOrgsMap.getOrDefault(orgId, ""));
        }
    }

    private void setUserRole(List<UserBean> users) {
        List<Group> allGroup = getAllGroup();
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

        for (UserBean user : users) {
            user.setRoleName(allGroupMap.getOrDefault(user.getId().toString() + ",dc=zwlbs,dc=com", ""));
        }
    }

    /**
     * 根据关键字模糊查询用户
     * @param searchParam   查询参数
     * @param orgId         orgId
     * @param searchSubTree searchSubTree
     * @return List<UserBean>
     */
    public List<UserBean> getUserList(String searchParam, String orgId, boolean searchSubTree) {
        List<UserBean> users;
        SearchControls searchCtls = new SearchControls();
        if (searchSubTree) {
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        } else {
            searchCtls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense", "employeeNumber", "businessCategory", "departmentNumber", "displayName",
                "socialSecurityNumber", "identityNumber"};
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        if (searchParam != null && !Objects.equals(searchParam, "")) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("mail", "*" + searchParam + "*"));
            orfilter.or(new LikeFilter("mobile", "*" + searchParam + "*"));
            orfilter.or(new LikeFilter("uid", "*" + searchParam + "*"));
            orfilter.or(new LikeFilter("givenName", "*" + searchParam + "*"));
            andfilter.append(orfilter);
        }
        users = ldapTemplate.search(orgId, andfilter.encode(), searchCtls, new UserContextMapper());
        // 用户列表排序
        if (users != null && !users.isEmpty()) {
            setUserRole(users);
            setUserOrg(users);
            users.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return users;
        }
        return null;
    }

    /**
     * 根据Uid查询User
     * @param urId userId
     * @return List<UserBean>
     * @author wangying
     */
    public List<UserBean> getUserByUid(String urId) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        if (urId != null && !Objects.equals(urId, "")) {
            andfilter.and(new EqualsFilter("uid", urId));
        }
        List<UserBean> users = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new UserContextMapper());
        if (users != null && !users.isEmpty()) {
            return users;
        }
        return null;
    }

    /**
     * 根据id获取角色
     * @param id id
     * @return Group
     * @author wangying
     */
    public Group getGroupById(String id) {
        return groupRepo.findOne(LdapUtils.newLdapName(id));
    }

    /**
     * getOrganizationById
     * @param id id
     * @return result
     */
    public OrganizationLdap getOrganizationById(String id) {
        return organizationRepo.findOne(LdapUtils.newLdapName(id));
    }

    /**
     * updateGroup
     * @param groupId   groupId
     * @param group     group
     * @param ipAddress ipAddress
     * @return result
     */
    public JsonResultBean updateGroup(String groupId, Group group, String permissionEditTree, String ipAddress)
        throws Exception {
        JSONObject msg = new JSONObject();
        LdapName originalId = LdapUtils.newLdapName(groupId);
        Group existingGroup = groupRepo.findOne(originalId); // 根据LdapName查询分组实体
        if (existingGroup != null) {
            if (StringUtils.isNotBlank(group.getRoleName())) { // 获取角色名
                existingGroup.setRoleName(group.getRoleName()); // 设置角色名
            }
            existingGroup.setDescription(group.getDescription()); // 设置描述
            Group saveGroup = updateGroupStandard(existingGroup); // 修改Ldap中的角色
            if (saveGroup != null) { // 修改角色名和描述成功后,修改角色的分组操作权限
                // 修改角色与Resource关联
                String newRoleId = saveGroup.getId().toString();
                // 先删除
                boolean delFlag = true;
                // 根据id查询角色的所有分组操作权限
                List<RoleResource> ids = roleService.findIdByRoleId(groupId);
                if (ids != null && ids.size() > 0) {
                    // 删除角色的分组操作权限
                    delFlag = roleService.deleteByRole(groupId);
                }
                // 再添加
                List<RoleResourceForm> formList = new ArrayList<>();
                if (delFlag) {
                    // 用户所选可写权限
                    if (permissionEditTree != null && !permissionEditTree.isEmpty()) {
                        JSONArray resourceArray = JSON.parseArray(permissionEditTree);
                        for (Object obj : resourceArray) {
                            String id = (String) ((JSONObject) obj).get("id");
                            // 是否可写
                            boolean edit = (boolean) ((JSONObject) obj).get("edit");
                            RoleResourceForm roleResource = new RoleResourceForm();
                            roleResource.setRoleId(newRoleId);
                            roleResource.setResourceId(id);
                            roleResource.setEditable(0);
                            if (edit) {
                                roleResource.setEditable(1);
                            }
                            formList.add(roleResource);
                        }

                    }
                }
                roleService.addRoleResourceByBatch(formList);
                // 获取被修改角色的ID
                String message = "修改角色 : " + group.getRoleName();
                // 修改角色时记录日志
                logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");
                return new JsonResultBean(msg);
            }
        }
        msg.put("flag", 2);
        return new JsonResultBean(msg);
    }

    /**
     * 修改组织
     * @param form      form
     * @param ipAddress ip地址
     */
    public void update(OrganizationLdap form, String ipAddress) {
        List<ModificationItem> moList = new ArrayList<>();
        OrganizationLdap gf = getOrgByEntryDN(form.getPid());
        String groupUuid = gf.getUuid();
        addLdapUpdateItem("l", form.getName(), gf.getName(), moList);
        addLdapUpdateItem("postalCode", form.getOrganizationCode(), gf.getOrganizationCode(), moList);
        addLdapUpdateItem("physicalDeliveryOfficeName", form.getRegisterDate(), gf.getRegisterDate(), moList);
        addLdapUpdateItem("businessCategory", form.getOperation(), gf.getOperation(), moList);
        addLdapUpdateItem("p0", form.getLicense(), gf.getLicense(), moList);
        addLdapUpdateItem("st", form.getPrincipal(), gf.getPrincipal(), moList);
        addLdapUpdateItem("telephoneNumber", form.getPhone(), gf.getPhone(), moList);
        addLdapUpdateItem("registeredAddress", form.getAddress(), gf.getAddress(), moList);
        addLdapUpdateItem("issuingOrgan", form.getIssuingOrgan(), gf.getIssuingOrgan(), moList);
        addLdapUpdateItem("operatingState", form.getOperatingState(), gf.getOperatingState(), moList);
        addLdapUpdateItem("provinceName", form.getProvinceName(), gf.getProvinceName(), moList);
        addLdapUpdateItem("countyName", form.getCountyName(), gf.getCountyName(), moList);
        addLdapUpdateItem("street", form.getCityName(), gf.getCityName(), moList);
        addLdapUpdateItem("areaNumber", form.getAreaNumber(), gf.getAreaNumber(), moList);
        addLdapUpdateItem("description", form.getDescription(), gf.getDescription(), moList);
        addLdapUpdateItem("p7", form.getLicenseValidityStartDate(), gf.getLicenseValidityStartDate(), moList);
        addLdapUpdateItem("p8", form.getLicenseValidityEndDate(), gf.getLicenseValidityEndDate(), moList);
        addLdapUpdateItem("p9", form.getContactName(), gf.getContactName(), moList);
        addLdapUpdateItem("upOrganizationCode", form.getUpOrganizationCode(), gf.getUpOrganizationCode(), moList);
        addLdapUpdateItem("k0", form.getManagerOrganizationCode(), gf.getManagerOrganizationCode(), moList);
        addLdapUpdateItem("k8", form.getBusinessLicenseType(), gf.getBusinessLicenseType(), moList);
        addLdapUpdateItem("destinationIndicator", form.getPrincipalPhone(), gf.getPrincipalPhone(), moList);
        if (moList.size() > 0) {
            ModificationItem[] moArray = new ModificationItem[moList.size()];
            ldapTemplate.modifyAttributes(form.getPid(), moList.toArray(moArray));
            form.setUuid(groupUuid);
            if (!gf.getName().equals(form.getName())) {
                addGroupNameRedis(form);
            }
        }
        businessScopeDao.deleteById(gf.getUuid());
        // 经营范围
        if (StringUtils.isNotBlank(form.getScopeOfOperationIds())) {
            List<String> scopeIds = Arrays.asList(form.getScopeOfOperationIds().split(","));
            businessScopeDao.bindBusinessScope(gf.getUuid(), scopeIds, 1);
        }
        updateUserAssignmentCache(gf.getName(), form.getName(), groupUuid);
        //维护车和组织的缓存
        maintainVehicleGroupCache(form, gf);
        String beforeName = gf.getName();
        String afterName = form.getName();
        String msg;

        //维护组织信息缓存
        form.setId(gf.getId());
        editOrgRedis(form);

        if (beforeName.equals(afterName)) {
            msg = "修改组织 : " + afterName;
        } else {
            msg = "修改组织名称 : " + beforeName + " 为 : " + afterName;
            //维护监控对象评分缓存
            List<String> orgIds = this.getSuperiorGroupIdsById(groupUuid);
            orgIds.stream()
                    .map(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN::of)
                    .forEach(RedisHelper::delByPattern);
        }
        // 中位-企业静态信息同步(0x1608)
        webClientHandleCom.send1608ByUpdateGroupByZwProtocol(groupUuid);
        // 四川-企业静态信息同步(0x1605)
        webClientHandleCom.send1605ByUpdateGroupBySiChuanProtocol(groupUuid);
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    /**
     * 修改时维护企业信息缓存
     */
    private void editOrgRedis(OrganizationLdap organizationLdap) {
        Map<String, String> orgListMap = new HashMap<>();
        orgListMap.put("organizationCode", organizationLdap.getOrganizationCode());
        orgListMap.put("name", organizationLdap.getName());
        orgListMap.put("operation", organizationLdap.getOperation());
        orgListMap.put("upOrganizationCode", organizationLdap.getUpOrganizationCode());
        orgListMap.put("issuingOrgan", organizationLdap.getIssuingOrgan());
        orgListMap.put("areaNumber", organizationLdap.getAreaNumber());
        orgListMap.put("license", organizationLdap.getLicense());
        orgListMap.put("scopeOfOperation", organizationLdap.getScopeOfOperation());
        orgListMap.put("scopeOfOperationIds", organizationLdap.getScopeOfOperationIds());
        orgListMap.put("scopeOfOperationCodes", organizationLdap.getScopeOfOperationCodes());
        orgListMap.put("licenseValidityStartDate", organizationLdap.getLicenseValidityStartDate());
        orgListMap.put("licenseValidityEndDate", organizationLdap.getLicenseValidityEndDate());
        orgListMap.put("address", organizationLdap.getAddress());
        orgListMap.put("principal", organizationLdap.getPrincipal());
        orgListMap.put("contactName", organizationLdap.getContactName());
        orgListMap.put("phone", organizationLdap.getPhone());
        orgListMap.put("operatingState", organizationLdap.getOperatingState());
        orgListMap.put("id", organizationLdap.getUuid());
        orgListMap.put("areaName", organizationLdap.getAreaName());
        orgListMap.put("managerOrganizationCode", organizationLdap.getManagerOrganizationCode());
        orgListMap.put("businessLicenseType", organizationLdap.getBusinessLicenseType());
        RedisKey redisKey = RedisKeyEnum.ORGANIZATION_INFO.of(organizationLdap.getUuid());
        RedisHelper.addToHash(redisKey, orgListMap);
    }

    private void maintainVehicleGroupCache(OrganizationLdap form, OrganizationLdap gf) {
        //如果不变就不操作
        if (form.getName().equals(gf.getName())) {
            return;
        }
        List<String> ids = newVehicleDao.findVehicleIdsByGroupId(gf.getUuid());
        final List<RedisKey> keys = ids.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        RedisHelper.batchAddToHash(keys, ImmutableMap.of("orgName", form.getName()));
    }

    private String getUserId(Name name) {
        return name.toString().replace(",dc=zwlbs,dc=com", "");
    }

    /**
     * 判断是否是调度员角色
     */

    private boolean isDispatcherRole(String roleId) {
        final List<Group> roles = queryRoleList("调度员角色", false);
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
     * 判断是否是禁言角色
     */
    private boolean isSilentRole(String roleId) {
        final List<Group> roles = queryRoleList("禁言角色", false);
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

    private String getUserName(Name name) {
        return name.toString().split(",")[0].split("uid=")[1];
    }

    private void addLdapUpdateItem(String id, String value, String defaultValue, List<ModificationItem> itemList) {
        if (StringUtil.isNotEmpty(value)) {
            itemList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(id, value)));
        } else if (StringUtil.isNotEmpty(defaultValue)) {
            itemList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(id, defaultValue)));
        }
    }

    private void updateUserAssignmentCache(String oldName, String newName, String uuid) {
        if (oldName.equals(newName)) {
            return;
        }
    }

    /**
     * Update the user and - if its id changed - update all group references to the user.
     * @param existingGroup the user, populated with new data
     * @return the updated entry
     */
    private Group updateGroupStandard(Group existingGroup) {
        return groupRepo.save(existingGroup);
    }

    /**
     * 新增Group
     * @param group     group
     * @param ipAddress ip地址
     * @author wangying
     */
    public void addRole(Group group, String ipAddress) {
        Name dn = buildGroupDn(group.getName());
        DirContextAdapter context = new DirContextAdapter(dn);
        LdapName userDN = toAbsoluteDn(LdapUtils.newLdapName("uid=admin,ou=organization"));
        context.setAttributeValues("objectclass", new String[] { "groupOfNames", "top" });
        context.setAttributeValue("cn", group.getName());
        if (StringUtils.isNotBlank(group.getDescription())) {
            context.setAttributeValue("description", group.getDescription());
        }
        if (StringUtils.isNotBlank(group.getRoleName())) {
            context.setAttributeValue("o", group.getRoleName());
        }
        context.setAttributeValue("member", userDN);

        ldapTemplate.bind(context);
        // 获取到新增角色的名称
        String message = "新增角色 : " + group.getRoleName();
        // 把用户的IP和message作为参数传入
        logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");

    }

    /**
     * 根据id删除角色
     * @param groupId   groupId
     * @param ipAddress ip地址
     * @author wangying
     */
    public String deleteGroup(String groupId, String ipAddress) throws Exception {
        String[] roleIds = groupId.split(";");
        // 用户操作
        StringBuilder message = new StringBuilder();
        StringBuilder deleteMessage = new StringBuilder();
        StringBuilder notDeleteMessage = new StringBuilder();
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId) && (!"cn=ROLE_ADMIN,ou=Groups".equals(roleId))) {
                if (roleEntryDN.equals(roleId)) {
                    // 即刻体验: 角色不能被删除
                    message.append("即刻体验: 角色不能被删除").append(" <br/>");
                    continue;
                }
                String roleName;
                // 根据id查询角色
                Group g = getGroupById(roleId);
                if (g == null) {
                    continue;
                }
                roleName = g.getRoleName();
                if (("调度员角色").equals(roleName) || "禁言角色".equals(roleName) || "监听角色".equals(roleName)) {
                    notDeleteMessage.append("角色 :【").append(g.getRoleName()).append("】").append("不能被删除 <br/>");
                    continue;

                }

                Set<Name> members = g.getMembers();
                if (members.size() > 2) {
                    deleteMessage.append("角色 :【").append(g.getRoleName()).append("】").append(" <br/>");
                    continue;
                }

                message.append("删除角色 : ").append(g.getRoleName()).append(" <br/>");
                ldapTemplate.unbind(roleId);
                // 数据库删除角色与资源关联关系
                roleService.deleteByRole(roleId);

            }
        }
        if (!message.toString().isEmpty()) {
            if (roleIds.length > 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除角色");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            }
        }
        if (deleteMessage.length() > 0) {
            deleteMessage.append("删除失败，存在其他用户拥有该角色");
        }
        return deleteMessage.toString();
    }

    /**
     * 根据id删除组织
     * @param groupId   sroupId
     * @param ipAddress ip地址
     */
    public void deleteOrganizationLdap(String groupId, String ipAddress) {
        OrganizationLdap ldap = getOrganizationById(groupId);
        if (ldap != null) {
            ldapTemplate.unbind(groupId);
            // 维护缓存
            // delGroupRedis(ldap);
            String msg = "删除组织 : " + ldap.getName();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        }

    }

    private Name buildGroupDn(String groupName) {
        return LdapNameBuilder.newInstance("ou=Groups").add("cn", groupName).build();
    }

    /**
     * 根据关键字模糊查询角色
     * @param searchParam searchParam
     * @return result
     * @author wangying
     */
    public List<Group> queryRoleList(String searchParam) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returnedAtts = { "o", "createTimestamp", "cn", "description" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "groupOfNames"));
        // 只查询当前用户的角色列表 gfw 20180907
        // 如果用户是admin用户 admin 可以查询所有
        if (!isADMINRole()) {
            LdapName name =
                LdapUtils.newLdapName(SystemHelper.getCurrentUser().getId() + "," + getBaseLdapPath().toString());
            andfilter.and(new EqualsFilter("member", name.toString()));
        }
        if (StringUtils.isNotBlank(searchParam)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("o", "*" + searchParam + "*"));
            // orfilter.or(new LikeFilter("description", "*" + searchParam + "*"));
            andfilter.append(orfilter);
        }
        List<Group> groups = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new GroupContextMapper());
        if (groups != null && !groups.isEmpty()) {
            // 角色列表排序
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * 校验用户是否是ADMIN权限
     * @return boolean
     * @author wangying
     */
    public boolean isADMINRole() {
        boolean adminFlag = false;
        // 获取当前用户的角色
        String userName = SystemHelper.getCurrentUsername();
        // 根据用户名获取用户id
        String userId = getUserDetails(userName).getId().toString();
        // 根据用户id获取角色 ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        List<Group> userGroup = (List<Group>) findByMember(name);
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

    /**
     * admin 权限的不显示admin权限，power权限不显示power权限和admin权限
     * @param roles roles
     * @return List
     * @author wangying
     */
    public List<Group> getCurRole(List<Group> roles) {
        // copy list
        List<Group> roleList = new ArrayList<>();
        CollectionUtils.addAll(roleList, new Object[roles.size()]);
        Collections.copy(roleList, roles);

        return roleList;
    }

    /**
     * 根据关键字模糊查询角色
     * @param groupName groupName
     * @return result
     */
    public List<Group> getGroupByName(String groupName) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "groupOfNames"));
        if (StringUtils.isNotBlank(groupName)) {
            andfilter.and(new EqualsFilter("o", groupName));
        }
        List<Group> groups = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new GroupContextMapper());
        if (groups != null && !groups.isEmpty()) {
            return groups;
        }
        return null;
    }

    /**
     * UserContextMapper
     * @author wangying
     * @version 1.0
     */
    static class UserContextMapper implements ContextMapper<UserBean> {
        @Override
        public UserBean mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter) ctx;
            UserBean p = new UserBean();
            p.setFullName(context.getStringAttribute("givenName"));
            p.setUsername(context.getStringAttribute("uid"));
            p.setMail(context.getStringAttribute("mail"));
            p.setMobile(context.getStringAttribute("mobile"));
            p.setId(context.getDn());
            p.setMember(LdapUtils.newLdapName(context.getStringAttribute("entryDN")));
            p.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
            p.setGender(context.getStringAttribute("employeeType"));
            p.setState(context.getStringAttribute("st"));
            p.setAuthorizationDate(context.getStringAttribute("carLicense"));
            p.setUuid(context.getStringAttribute("entryuuid"));
            p.setIdentity(context.getStringAttribute("employeeNumber"));
            p.setIndustry(context.getStringAttribute("businessCategory"));
            p.setDuty(context.getStringAttribute("departmentNumber"));
            p.setAdministrativeOffice(context.getStringAttribute("displayName"));
            p.setDispatcherId(context.getStringAttribute("telexNumber"));
            p.setSocialSecurityNumber(context.getStringAttribute("socialSecurityNumber"));
            p.setIdentityNumber(context.getStringAttribute("identityNumber"));
            return p;
        }
    }

    /**
     * OrganizationContextMapper
     * @author wangying
     * @version 1.0
     */
    static class OrganizationContextMapper implements ContextMapper<OrganizationLdap> {
        @Override
        public OrganizationLdap mapFromContext(Object ctx) {
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
            org.setUuid(context.getStringAttribute("entryuuid"));
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
            org.setManagerOrganizationCode(context.getStringAttribute("k0"));
            org.setBusinessLicenseType(context.getStringAttribute("k8"));
            org.setUpOrganizationCode(context.getStringAttribute("upOrganizationCode"));
            org.setId(context.getDn());
            org.setEntryDN(context.getStringAttribute("entryDN"));
            return org;
        }
    }

    /**
     * GroupContextMapper
     * @author wangying
     * @version 1.0
     */
    static class GroupContextMapper implements ContextMapper<Group> {
        @Override
        public Group mapFromContext(Object ctx) {
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

    /**
     * 获取用户当前及下级组织列表
     * @return result
     * @author FanLu
     */
    public List<String> getOrgByUser() {
        String orgId = getOrgIdByUser();
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        return userOrgListId;
    }

    /**
     * 根据用户id获取当前组织及下级组织id
     * @param userId userId
     * @return List<String>
     * @author wangying
     */
    public List<String> getOrgIdsByUser(String userId) {
        // 获取用户所在组织及下级组织
        String orgId = getOrgIdByUserId(userId);
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        return userOrgListId;
    }

    /**
     * 根据当前用户获取其所属组织id
     * @return String
     * @author wangying
     */
    public String getOrgIdByUser() {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        return userId.substring(beginIndex + 1);
    }

    /**
     * 获取组织id(根据用户id得到用户所在部门)
     * @param userId 用户id
     * @return 组织id
     */
    public String getOrgIdByUserId(String userId) {
        int beginIndex = userId.indexOf(",");
        return userId.substring(beginIndex + 1);
    }

    /**
     * 根据当前用户获取其直接上级组织id
     * @return String
     * @author fanlu
     */
    public String getParentOrgIdByUser() {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String orgId = "";
        int secondIndex = userId.indexOf(",", userId.indexOf(",") + 1);
        if (secondIndex > 0) {
            orgId = userId.substring(secondIndex + 1);
        }
        return orgId;
    }

    /**
     * 获取当前用户所在组织 如果当前用户具有admin权限，返回其下级组织uuid，若有多个平行下级组织，则获取其第一个下级组织的uuid 如果当前用户不具有admin权限，则直接返回其组织uuid
     * @return String
     * @author Liubangquan
     */
    public String getOrgIdExceptAdmin() {
        // 默认将设备分组设为当前登录用户所在分组
        String orgId;
        boolean adminFlag = isADMINRole();
        // 当前用户具有admin权限
        if (adminFlag) {
            List<OrganizationLdap> ols = getOrgChild(getOrgIdByUser());
            if (null != ols && ols.size() > 1) {
                orgId = Converter.toBlank(ols.get(1).getUuid());
            } else {
                orgId = Converter.toBlank(getOrgUuidByUser());
            }
        } else {
            orgId = Converter.toBlank(getOrgUuidByUser());
        }
        return orgId;
    }

    /**
     * 递归获取指定组织的上级组织
     * @param allList    所有组织
     * @param id         指定组织id
     * @param returnList 上级list
     * @author wangying
     */
    private void getParentOrg(List<OrganizationLdap> allList, String id, List<String> returnList) {
        if (allList != null && allList.size() > 0) {
            for (OrganizationLdap org : allList) {
                if (org.getId().toString().equals(id)) {
                    returnList.add(org.getUuid());
                    String parentId = org.getPid();
                    if (parentId != null && !"".equals(parentId)) {
                        getParentOrg(allList, org.getPid(), returnList);
                    }
                }
            }
        }
    }

    /**
     * 递归上级
     * @param allList
     * @param id
     * @param orgList
     */
    public void getParentOrg(List<OrganizationLdap> allList, String id, Set<OrganizationLdap> orgList) {
        if (allList != null && allList.size() > 0) {
            for (OrganizationLdap org : allList) {
                if (org.getId().toString().equals(id)) {
                    if (orgList.add(org)) {
                        String parentId = org.getPid();
                        if (parentId != null && !"".equals(parentId)) {
                            getParentOrg(allList, org.getPid(), orgList);
                        }
                    }

                }
            }
        }
    }

    /**
     * 递归获取指定组织的上级组织根据uuid
     * @param allList    所有组织
     * @param returnList 上级list
     * @author lijie
     */
    private void getParentOrg1(List<OrganizationLdap> allList, String groupId, List<String> returnList) {
        String id = null;
        if (allList != null && allList.size() > 0) {
            for (OrganizationLdap org : allList) {
                if (org.getUuid().equals(groupId)) {
                    id = org.getId().toString();
                }
                if (id != null) {
                    if (org.getId().toString().equals(id)) {
                        returnList.add(org.getUuid());
                        String parentId = org.getPid();
                        if (parentId != null && !"".equals(parentId)) {
                            getParentOrg(allList, org.getPid(), returnList);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取当前登录用户所属企业直接上级以及下级的组织集合
     * @return result
     */
    public List<String> getFenceOrg() {
        List<String> fenceOrg = new ArrayList<>();
        // 当前组织的上级组织
        String parentOrg = getParentOrgIdByUser();
        // 所有组织 当前组织
        List<OrganizationLdap> allOrg = getOrgChild("ou=organization");
        // 直属上级组织
        getParentOrg(allOrg, parentOrg, fenceOrg);
        // 当前组织及下级组织
        List<String> childOrg = getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        fenceOrg.addAll(childOrg);
        return fenceOrg;
    }

    /**
     * lijie
     * 获取当前组织id的上级的组织id集合
     * @return result
     */
    public List<String> getSuperiorGroupIdsById(String groupId) {
        List<String> listGroupIds = new ArrayList<>();
        // 所有组织 当前组织
        List<OrganizationLdap> allOrg = getOrgChild("ou=organization");
        // 直属上级组织
        getParentOrg1(allOrg, groupId, listGroupIds);
        return listGroupIds;
    }

    /**
     * 根据uuid查询用户
     * @param uuid uuif
     * @return user
     * @author wangying
     */
    public UserBean getUserByUuid(String uuid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        filter.and(new EqualsFilter("entryUUID", uuid));
        List<UserBean> users = ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * 根据uuid查询用户
     * @param id id
     * @return user
     * @author wangying
     */
    public UserBean getUserByEntryDn(String id) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "inetOrgPerson"));
        filter.and(new EqualsFilter("entryDN", id + ",dc=zwlbs,dc=com"));
        List<UserBean> users = ldapTemplate.search(searchBase, filter.encode(), searchCtls, new UserContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * 根据uuid查询组织
     * @param uuid uuid
     * @return result
     * @author wangying
     */
    public OrganizationLdap getOrgByUuid(String uuid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new EqualsFilter("entryUUID", uuid));
        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * 根据uuidSet查询组织
     * @param uuidSet uuidSet
     */
    public Map<String, OrganizationLdap> getOrgByUuids(Set<String> uuidSet) {
        Map<String, OrganizationLdap> organizationLdapMap = new HashMap<>();
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        OrFilter orFilter = new OrFilter();
        for (String uuid : uuidSet) {
            orFilter.or(new EqualsFilter("entryUUID", uuid));
        }
        filter.and(orFilter);
        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        for (OrganizationLdap organizationLdap : orgs) {
            organizationLdapMap.put(organizationLdap.getUuid(), organizationLdap);
        }
        return organizationLdapMap;
    }

    /**
     * 根据组织名称查询组织
     */
    public OrganizationLdap getOrgInfoByName(String name) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8", "p9",
                "upOrganizationCode" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new EqualsFilter("l", name));
        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * 根据entryDN查询组织
     * @param id id
     * @return result
     * @author wangying
     */
    public OrganizationLdap getOrgByEntryDN(String id) {
        return getOrgByEntryDN(id, true);
    }

    /**
     * 根据entryDN查询组织
     * @param id id
     * @return result
     * @author wangying
     */
    public OrganizationLdap getOrgByEntryDN(String id, boolean useCache) {
        if (useCache) {
            return ORG_CACHE.get(id, k -> getOrgByEntryDN(k, false));
        }
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "telephoneNumber", "scopeOfOperation",
                "issuingOrgan", "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new EqualsFilter("entryDN", id + ",dc=zwlbs,dc=com"));
        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    public OrganizationLdap findGroupByOu(String ou) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "telephoneNumber", "scopeOfOperation",
                "issuingOrgan", "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        if (StringUtil.isNotEmpty(ou)) {
            filter.and(new EqualsFilter("ou", ou));
        }

        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * 获取用户当前及下级组织列表
     * @param userId userId
     * @return result
     * @author wangying
     */
    public List<String> getOrgUuidsByUser(String userId) {
        List<String> userOrgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            // 获取组织id(根据用户id得到用户所在部门)
            int beginIndex = userId.indexOf(",");
            String orgId = userId.substring(beginIndex + 1);
            List<OrganizationLdap> orgs = getOrgChild(orgId);
            // 遍历得到当前用户组织及下级组织id的list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdap org : orgs) {
                    userOrgListId.add(org.getUuid());
                }
            }
        }
        return userOrgListId;
    }

    /**
     * 获得当前用户所属企业及其下级企业名称(根节点除外)
     * @return
     */
    public List<String> getOrgNamesByUser() {
        List<String> groupNames = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<OrganizationLdap> organizations = getOrgChild(getOrgIdByUserId(userId));
        if (CollectionUtils.isNotEmpty(organizations)) {
            groupNames.addAll(organizations.stream().filter(
                org -> (!"organization".equals(org.getOu())) && (!"ou=organization".equals(org.getId().toString())))
                .map(OrganizationLdap::getName).collect(Collectors.toList()));
        }
        return groupNames;
    }

    /**
     * 根据当前用户获取其所属组织uuid
     * @return String result
     * @author wangying
     */
    public String getOrgUuidByUser() {
        String uuid = "";
        UserLdap user = SystemHelper.getCurrentUser();
        String userId = user == null ? null : user.getId().toString();
        if (user == null) {
            return null;
        }
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String orgId = userId.substring(beginIndex + 1);
        OrganizationLdap org = getOrgByEntryDN(orgId);
        if (org != null) {
            uuid = org.getUuid();
        }
        return uuid;
    }

    public List<String> getOrgChildByOrgUuid(String groupUuid) {
        List<String> orgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(groupUuid)) {
            OrganizationLdap organization = getOrganizationByUuid(groupUuid);
            String orgId = organization.getId().toString();
            List<OrganizationLdap> orgs = getOrgChild(orgId);
            // 遍历得到当前用户组织及下级组织id的list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdap org : orgs) {
                    orgListId.add(org.getUuid());
                }
            }
        }
        return orgListId;
    }

    /**
     * 根据企业的uuid获取企业的详情信息
     * @param uuid
     * @return
     */
    public OrganizationLdap getOrganizationByUuid(String uuid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        if (StringUtils.isNotBlank(uuid)) {
            andfilter.and(new EqualsFilter("entryUUID", uuid));
        }
        List<OrganizationLdap> organizationLdaps =
            ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new OrganizationContextMapper());
        if (organizationLdaps != null && !organizationLdaps.isEmpty()) {
            return organizationLdaps.get(0);
        }
        return null;
    }

    /**
     * 获取当前登录用户的uuid
     */

    public String getCurrentUserUuid() {
        UserLdap user = SystemHelper.getCurrentUser();
        List<UserBean> userBeans = getUserByUid(user.getUid());
        UserBean userBean = userBeans.get(0);
        return userBean.getUuid();
    }

    /**
     * 获取用户的uuid
     * @param userId userId
     * @return result
     */
    public String getUserUuidById(String userId) {
        String uuid = "";
        if (StringUtils.isNotBlank(userId)) {
            UserBean user = getUserByEntryDn(userId);
            if (user != null) {
                uuid = user.getUuid();
            }
        }
        return uuid;
    }

    /**
     * setGroupNameByGroupId
     * @param result result
     * @param <T>    t
     */
    public <T> void setGroupNameByGroupId(List<T> result) {
        if (null != result && result.size() > 0) {
            List<OrganizationLdap> orgLdapList = getAllOrganization();
            for (T obj : result) {
                String groupId = (String) Reflections.invokeGetter(obj, "groupId");
                if (groupId != null && !"".equals(groupId)) {
                    for (OrganizationLdap orgLdap : orgLdapList) {
                        if (Converter.toBlank(orgLdap.getUuid()).equals(groupId)) {
                            Reflections.invokeSetter(obj, "groupName", orgLdap.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询所有用户
     * @return result
     */

    public List<UserBean> findAllUserUUID() {
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
     * 获取组织车辆信息
     * @param assignList 组织列表
     * @return result
     */
    public Set<String> getAssignVehicleForRedis(List<String> assignList) {
        // 存储带权限的车id list
        final List<RedisKey> keys =
                assignList.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toList());
        return RedisHelper.batchGetSet(keys);
    }

    /**
     * 用户正常下线处理(退出,修改密码,修改组织)
     */
    public void updateUserOffline(String ipAddress, String username) {
        String msg = "用户 : " + username + " 退出登录";
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    public String getChatGroupUserList(String type, String groupId) throws Exception {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // 讨论组字段不为空
        List<String> checkUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(groupId)) {
            // 根据讨论组获取用户的id list
            checkUserList = chatGroupUserService.findGroupUserByGroupId(groupId);
        }
        int userCount = 0;
        // 遍历得到当前用户组织及下级组织id的list
        if (orgs != null && orgs.size() > 0) {
            List<UserBean> allUsers = getUserList(null, orgId, true);
            userCount = allUsers.size();
            for (UserBean userBean : allUsers) {
                JSONObject userObj = new JSONObject();
                String uid = String.valueOf(userBean.getUuid());
                userObj.put("id", uid);
                userObj.put("name", userBean.getUsername());
                if (userBean.getFullName() != null) {
                    userObj.put("count", userBean.getFullName());
                }
                userObj.put("type", "user");
                userObj.put("iconSkin", "userSkin");
                userObj.put("pId", getUserOrgId(userBean));
                if (StringUtils.isBlank(groupId) && userId.equals(userBean.getId().toString())) { // 新增，默认勾选当前用户
                    userObj.put("checked", true);
                } else { // 修改，默认勾选讨论组下的用户
                    if (checkUserList != null && !checkUserList.isEmpty()) {
                        if (checkUserList.contains(uid)) { // 勾选讨论组中的用户
                            userObj.put("checked", true);
                        }
                    }
                }
                result.add(userObj);
            }
        }
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, type));
        obj.put("tree", result);
        obj.put("size", userCount);
        return obj.toJSONString();
    }

    private String getUserOrgId(UserBean user) {
        String userDn = user.getId().toString();
        int index = userDn.indexOf(',');
        if (index == -1) {
            return "";
        }
        return userDn.substring(index + 1);
    }

    /**
     * 根据用户id和新增角色权限判断当前新增的角色是否在用户的角色权限之内
     * @param userId
     * @param formList
     * @return ture 表示不在用户的角色权限之内 false 表示存在
     * @Author gfw
     * @Date 20180904
     */
    public boolean compareUserForm(String userId, List<RoleResourceForm> formList) {
        boolean flag = true;
        log.info("用户id:{}", userId);
        log.info("权限列表:{}", formList);
        Collection<Group> groups = findByMember(userId);
        List<String> roleIds = new ArrayList<>();
        for (Group group : groups) {
            roleIds.add(group.getId().toString());
        }
        List<String> addList = new ArrayList<>();
        for (RoleResourceForm roleResourceForm : formList) {
            addList.add(roleResourceForm.getResourceId());
        }
        List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
        int i = 0;
        int size = addList.size();
        for (String addValue : addList) {
            for (String curResource : curResources) {
                if (addValue.equals(curResource)) {
                    i++;
                    break;
                }
            }
        }
        if (size == i) {
            flag = false;
        }
        return flag;
    }

    /**
     * 将用户心新增的角色放入分配给创建用户以及admin用户
     * @param userId
     * @param roleId
     * @param ipAddress
     * @Author gfw
     * @Date 20180904
     */
    public void addAllotRole(String userId, String roleId, String ipAddress) {
        try {
            List<Group> groupList = (List<Group>) findByMember(userId);
            StringBuilder builder = new StringBuilder();
            for (Group group : groupList) {
                builder.append(group.getName()).append(",");
            }
            builder.append(roleId);
            updateRolesByUser(userId, builder.toString(), ipAddress);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断 分配用户角色是否属于操作用户的角色
     * @param userId
     * @param roleList
     * @return true 表示属于 false 表示不属于
     * @Author gfw
     * @Date 20180904
     */
    public boolean compareAllotRole(String userId, List<String> roleList) {
        boolean flag = true;
        Collection<Group> groups = findByMember(userId);
        int i = 0;
        int length = roleList.size();
        for (Group group : groups) {
            for (String id : roleList) {
                if (group.getName().equals(id)) {
                    i++;
                }
            }
        }
        if (i != length) {
            flag = false;
        }
        return flag;
    }

    /**
     * 根据车辆ID获取组织名称
     * @param vehicleId vehicleId
     * @return groupName
     */
    public String getGroupNameByVehicleId(String vehicleId) {
        return RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "orgName");
    }

    /**
     * 根据角色名称查询角色
     */
    public int queryRoleUserSize(String searchParam) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returnedAtts = { "member" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "groupOfNames"));
        if (StringUtils.isNotBlank(searchParam)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("o", searchParam));
            andfilter.append(orfilter);
        }
        List<Integer> roleUsers =
            ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, (ContextMapper<Integer>) ctx -> {
                DirContextAdapter context = (DirContextAdapter) ctx;
                Set<String> roleUsers1 = context.getAttributeSortedStringSet("member");
                return (int) roleUsers1.stream().filter(userName -> !userName.contains("admin")).count();
            });

        return roleUsers.size() == 0 ? 0 : roleUsers.get(0);
    }

    /**
     * 生成客户端id
     * @param userName
     * @return APP2.0.0新增
     */
    public Object generatingClientId(String userName) {
        String clientId = userName + "_" + UUID.randomUUID().toString();
        WebSubscribeManager.getInstance().addUserClientId(userName, clientId);
        return clientId;
    }

    /**
     * 检查客户端id是否改变
     * @param userName
     * @param nowClientId
     * @return APP2.0.0新增
     */
    public Boolean checkClientId(String userName, String nowClientId) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(nowClientId)) {
            return false;
        }
        String oldClientId = WebSubscribeManager.getInstance().getUserClientId(userName);
        return Objects.equals(nowClientId, oldClientId);
    }

    /**
     * 根据关键字模糊查询角色
     * @param searchParam searchParam
     * @return result
     * @author xiaoyun
     */
    public List<Group> queryRoleList(String searchParam, boolean checkAdminRole) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returnedAtts = { "o", "createTimestamp", "cn", "description" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "groupOfNames"));
        // 只查询当前用户的角色列表 gfw 20180907
        // 如果用户是admin用户 admin 可以查询所有
        if (checkAdminRole && !isADMINRole()) {
            LdapName name =
                LdapUtils.newLdapName(SystemHelper.getCurrentUser().getId() + "," + getBaseLdapPath().toString());
            andfilter.and(new EqualsFilter("member", name.toString()));
        }
        if (StringUtils.isNotBlank(searchParam)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("o", "*" + searchParam + "*"));
            // orfilter.or(new LikeFilter("description", "*" + searchParam + "*"));
            andfilter.append(orfilter);
        }
        List<Group> groups = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new GroupContextMapper());
        if (groups != null && !groups.isEmpty()) {
            // 角色列表排序
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * 查询用户拥有权限的组织信息(当前组织和所属下级组织)
     * @param orgId
     * @return
     */
    public List<OrganizationLdap> getUserOwnAuthorityOrganizeInfo(String orgId) {
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = getOrgChild(orgId);
        return CollectionUtils.isNotEmpty(userOwnAuthorityOrganizeInfo) ? userOwnAuthorityOrganizeInfo :
            new ArrayList<>();
    }

}
