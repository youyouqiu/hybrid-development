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
 * ?????????????????????
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
     * ??????????????????
     */
    private static final Cache<String, OrganizationLdap> ORG_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    /**
     * ????????????
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
     * ????????????????????????????????????dn
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
        // ????????????????????????id
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
     * ????????????
     * @param password  ??????
     * @param ipAddress ipAddress
     * @author fanlu
     */
    public void updateModifyPwd(String password, String ipAddress, String equipmentType) {
        Name dn = SystemHelper.getCurrentUser().getId();
        Attribute attr = new BasicAttribute("userPassword", SecurityPasswordHelper.encodePassword(password));
        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] { item });
        String username = SystemHelper.getCurrentUsername();
        String log = "?????????" + username + " ????????????";
        String logSource;
        if (StringUtils.isNotBlank(equipmentType) && "APP".equals(equipmentType)) {
            logSource = "4";
        } else {
            logSource = "3";
        }
        logSearchServiceImpl.addLog(ipAddress, log, logSource, "", "-", "");
        updateUserOffline(ipAddress, username); // ????????????????????????
        expireUserSession(SystemHelper.getCurrentUsername());
    }

    /**
     * ???????????????????????????
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
                // Goups???????????????
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
        // ????????????
        if (cid.indexOf(",") > 0) {
            int beginIndex = cid.indexOf(",");
            obj.setCid(cid);
            obj.setPid(cid.substring(beginIndex + 1));
        } else {
            // ?????????
            obj.setCid(cid);
            obj.setPid("");
        }
    }

    /**
     * ????????????
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
        // ????????????
        String msg = "????????????: " + organizationLdap.getName();
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
     * ?????????uuid?????????
     * @param orgId ?????????id
     * @param limit ????????????
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
     * ????????????id(??????uuid,???entryDN)?????????????????????????????????????????????
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
            // ????????????
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
            org.setLicense(context.getStringAttribute("p0")); // ???????????????
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
     * ???????????????????????? ??????????????????????????????;
     * @param roleCn ?????? cn
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
     * ????????????????????????????????????????????? uuid???name??????
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
     * ??????id??????????????????
     * @param id id
     * @return OrganizationLdap
     * @author wangying
     */
    public OrganizationLdap findOrganization(String id) {
        return organizationRepo.findOne(LdapUtils.newLdapName(id));
    }

    /**
     * ??????????????????
     * @return List<Group>
     */
    public List<Group> getAllGroup() {
        return (List<Group>) groupRepo.findAll();
    }

    public LdapName getBaseLdapPath() {
        return this.baseLdapPath;
    }

    /**
     * ????????????id??????????????????????????????
     * @param member member
     * @return Collection<Group>
     * @author fanlu
     */
    public Collection<Group> findByMember(Name member) {
        return groupRepo.findByMember(member);
    }

    /**
     * ????????????id????????????????????????
     */
    public Collection<Group> findByMember(String userId) {
        LdapName name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        return groupRepo.findByMember(name);
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     * @param userId    userId
     * @param roleIds   roleIds
     * @param ipAddress ipAddress
     * @author fanlu
     */
    public JsonResultBean updateRolesByUser(String userId, String roleIds, String ipAddress) {

        String str = "";

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        LdapName name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        // ?????????????????????????????????
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

            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

            List<String> roles1 = new ArrayList<>(currentRoleNames);

            List<String> roleList1 = new LinkedList<>(Arrays.asList(roleArray));

            roleList1.removeAll(roles1);

            // ??????????????????????????????
            for (String roleName : roleList1) {
                //??????????????????????????????????????????
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = addDispatcherRoleToUser(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    break;
                }
                //???????????????????????????????????????
                if (isSilentRole(roleName)) {
                    boolean canAuthSilentRole = canAuthSilentRole(currentRoles, roleList1);
                    if (!canAuthSilentRole) {
                        return new JsonResultBean("????????????????????????????????????????????????????????????");
                    }

                }

            }

            // ??????????????????????????????????????????????????????????????????????????????
            for (String roleName : roles) {
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = removeDispatcherRole(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    //???????????????????????????????????????????????????
                    deleteSilentRole(name);
                    break;
                }
            }

            // ??????????????????????????????
            for (String roleName : roleList1) {
                boolean judge = judge(currentRoleNames, roleIds);
                if (!judge) {
                    str = ("????????????????????????????????????????????????????????????");
                    continue;
                }
                groupRepo.addMemberToGroup(roleName, name);
            }
            // ??????????????????????????????
            for (String roleName : roles) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
        } else { // ?????????????????????
            // ??????????????????????????????
            for (String roleName : currentRoleNames) {
                if (isDispatcherRole(roleName)) {
                    JsonResultBean resultBean = removeDispatcherRole(name);
                    if (resultBean != null) {
                        return resultBean;
                    }
                    //???????????????????????????????????????????????????
                    deleteSilentRole(name);
                    break;
                }
            }
            // ??????????????????????????????
            for (String roleName : currentRoleNames) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
        }
        if (ipAddress != null && !"".equals(ipAddress)) {
            // ???????????????????????????
            int commaIndex = userId.indexOf(",");
            // ???????????????????????????
            int eqIndex = userId.indexOf("=");
            // ???????????????
            String userName = userId.substring(eqIndex + 1, commaIndex);
            String log = "????????? " + userName + " ????????????";
            logSearchServiceImpl.addLog(ipAddress, log, "3", "", "-", "");
        }

        if (!str.equals("")) {
            return new JsonResultBean(str);
        }

        return new JsonResultBean(JsonResultBean.SUCCESS, "??????????????????");
    }

    public boolean judge(List<String> currentRoleNames, String roleIds) {

        boolean flag = true;

        String[] roleArray = roleIds.split(",");

        List<String> collect = Stream.of(roleArray).collect(Collectors.toList());

        List<Group> allGroup = getAllGroup();

        String jyanId = "";

        String diaoDuId = "";

        for (Group group : allGroup) {

            if (group.getRoleName().equals("????????????")) {

                jyanId = group.getName();

            }
            if (group.getRoleName().equals("???????????????")) {

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
        Group groupRole = queryRoleList("????????????", false).get(0);
        groupRepo.removeMemberFromGroup(groupRole.getName() + "", name);
    }

    /**
     * ????????????????????????
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
                //????????????????????????????????????????????????????????????userId??????ldap???
                updateModifyUserDispatcherId(getUserId(name), uid);
            } else {
                throw new Exception("????????????????????????????????????");
            }
        } catch (Exception e) {
            // ????????????????????????????????????????????????,??????????????????????????????????????????
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
                //????????????????????????????????????????????????????????????userId??????ldap???
                updateModifyUserDispatcherId(getUserId(name), null);
                updateAndRecyclePersonCallNumber(intercomIotUser.getCallNumber() + "");
            } else {
                throw new Exception("????????????????????????????????????");
            }
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
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
     * ??????id????????????
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
            // ??????????????????????????????
            for (String roleName : currentRoleNames) {
                groupRepo.removeMemberFromGroup(roleName, name);
            }
            // ???????????????????????????
            int commaIndex = item.indexOf(",");
            // ???????????????????????????
            int eqIndex = item.indexOf("=");
            String orgId = item.substring(commaIndex + 1);
            // ????????????????????????
            OrganizationLdap org = findOrganization(orgId);
            // ???????????????
            String username = item.substring(eqIndex + 1, commaIndex);
            log.info("????????????");
            expireUserSession(username);
            message.append("???????????? : ").append(username).append(" ( @").append(org.getName()).append(")")
                .append(" <br/>");
        }
        if (!message.toString().isEmpty()) {
            if (items.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "??????????????????");
            }
        }
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????????????????
     * @param searchParam   ????????????
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
        // ??????????????????
        if (users != null && !users.isEmpty()) {
            setUserRole(users);
            setUserOrg(users);
            users.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return users;
        }
        return null;
    }

    /**
     * ??????Uid??????User
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
     * ??????id????????????
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
        Group existingGroup = groupRepo.findOne(originalId); // ??????LdapName??????????????????
        if (existingGroup != null) {
            if (StringUtils.isNotBlank(group.getRoleName())) { // ???????????????
                existingGroup.setRoleName(group.getRoleName()); // ???????????????
            }
            existingGroup.setDescription(group.getDescription()); // ????????????
            Group saveGroup = updateGroupStandard(existingGroup); // ??????Ldap????????????
            if (saveGroup != null) { // ?????????????????????????????????,?????????????????????????????????
                // ???????????????Resource??????
                String newRoleId = saveGroup.getId().toString();
                // ?????????
                boolean delFlag = true;
                // ??????id???????????????????????????????????????
                List<RoleResource> ids = roleService.findIdByRoleId(groupId);
                if (ids != null && ids.size() > 0) {
                    // ?????????????????????????????????
                    delFlag = roleService.deleteByRole(groupId);
                }
                // ?????????
                List<RoleResourceForm> formList = new ArrayList<>();
                if (delFlag) {
                    // ????????????????????????
                    if (permissionEditTree != null && !permissionEditTree.isEmpty()) {
                        JSONArray resourceArray = JSON.parseArray(permissionEditTree);
                        for (Object obj : resourceArray) {
                            String id = (String) ((JSONObject) obj).get("id");
                            // ????????????
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
                // ????????????????????????ID
                String message = "???????????? : " + group.getRoleName();
                // ???????????????????????????
                logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
                msg.put("flag", 1);
                msg.put("errMsg", "???????????????");
                return new JsonResultBean(msg);
            }
        }
        msg.put("flag", 2);
        return new JsonResultBean(msg);
    }

    /**
     * ????????????
     * @param form      form
     * @param ipAddress ip??????
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
        // ????????????
        if (StringUtils.isNotBlank(form.getScopeOfOperationIds())) {
            List<String> scopeIds = Arrays.asList(form.getScopeOfOperationIds().split(","));
            businessScopeDao.bindBusinessScope(gf.getUuid(), scopeIds, 1);
        }
        updateUserAssignmentCache(gf.getName(), form.getName(), groupUuid);
        //???????????????????????????
        maintainVehicleGroupCache(form, gf);
        String beforeName = gf.getName();
        String afterName = form.getName();
        String msg;

        //????????????????????????
        form.setId(gf.getId());
        editOrgRedis(form);

        if (beforeName.equals(afterName)) {
            msg = "???????????? : " + afterName;
        } else {
            msg = "?????????????????? : " + beforeName + " ??? : " + afterName;
            //??????????????????????????????
            List<String> orgIds = this.getSuperiorGroupIdsById(groupUuid);
            orgIds.stream()
                    .map(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN::of)
                    .forEach(RedisHelper::delByPattern);
        }
        // ??????-????????????????????????(0x1608)
        webClientHandleCom.send1608ByUpdateGroupByZwProtocol(groupUuid);
        // ??????-????????????????????????(0x1605)
        webClientHandleCom.send1605ByUpdateGroupBySiChuanProtocol(groupUuid);
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    /**
     * ?????????????????????????????????
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
        //????????????????????????
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
     * ??????????????????????????????
     */

    private boolean isDispatcherRole(String roleId) {
        final List<Group> roles = queryRoleList("???????????????", false);
        if (roles == null) {
            // ????????????????????????
            return false;
        }
        Group groupRole = roles.get(0);
        if (!roleId.contains("cn=")) {
            return roleId.equals(groupRole.getName() + "");
        }

        return ("cn=" + groupRole.getName() + ",ou=Groups").equals(roleId);
    }

    /**
     * ???????????????????????????
     */
    private boolean isSilentRole(String roleId) {
        final List<Group> roles = queryRoleList("????????????", false);
        if (roles == null) {
            // ?????????????????????
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
     * ??????Group
     * @param group     group
     * @param ipAddress ip??????
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
        // ??????????????????????????????
        String message = "???????????? : " + group.getRoleName();
        // ????????????IP???message??????????????????
        logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");

    }

    /**
     * ??????id????????????
     * @param groupId   groupId
     * @param ipAddress ip??????
     * @author wangying
     */
    public String deleteGroup(String groupId, String ipAddress) throws Exception {
        String[] roleIds = groupId.split(";");
        // ????????????
        StringBuilder message = new StringBuilder();
        StringBuilder deleteMessage = new StringBuilder();
        StringBuilder notDeleteMessage = new StringBuilder();
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId) && (!"cn=ROLE_ADMIN,ou=Groups".equals(roleId))) {
                if (roleEntryDN.equals(roleId)) {
                    // ????????????: ?????????????????????
                    message.append("????????????: ?????????????????????").append(" <br/>");
                    continue;
                }
                String roleName;
                // ??????id????????????
                Group g = getGroupById(roleId);
                if (g == null) {
                    continue;
                }
                roleName = g.getRoleName();
                if (("???????????????").equals(roleName) || "????????????".equals(roleName) || "????????????".equals(roleName)) {
                    notDeleteMessage.append("?????? :???").append(g.getRoleName()).append("???").append("??????????????? <br/>");
                    continue;

                }

                Set<Name> members = g.getMembers();
                if (members.size() > 2) {
                    deleteMessage.append("?????? :???").append(g.getRoleName()).append("???").append(" <br/>");
                    continue;
                }

                message.append("???????????? : ").append(g.getRoleName()).append(" <br/>");
                ldapTemplate.unbind(roleId);
                // ??????????????????????????????????????????
                roleService.deleteByRole(roleId);

            }
        }
        if (!message.toString().isEmpty()) {
            if (roleIds.length > 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "??????????????????");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            }
        }
        if (deleteMessage.length() > 0) {
            deleteMessage.append("????????????????????????????????????????????????");
        }
        return deleteMessage.toString();
    }

    /**
     * ??????id????????????
     * @param groupId   sroupId
     * @param ipAddress ip??????
     */
    public void deleteOrganizationLdap(String groupId, String ipAddress) {
        OrganizationLdap ldap = getOrganizationById(groupId);
        if (ldap != null) {
            ldapTemplate.unbind(groupId);
            // ????????????
            // delGroupRedis(ldap);
            String msg = "???????????? : " + ldap.getName();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        }

    }

    private Name buildGroupDn(String groupName) {
        return LdapNameBuilder.newInstance("ou=Groups").add("cn", groupName).build();
    }

    /**
     * ?????????????????????????????????
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
        // ???????????????????????????????????? gfw 20180907
        // ???????????????admin?????? admin ??????????????????
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
            // ??????????????????
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * ?????????????????????ADMIN??????
     * @return boolean
     * @author wangying
     */
    public boolean isADMINRole() {
        boolean adminFlag = false;
        // ???????????????????????????
        String userName = SystemHelper.getCurrentUsername();
        // ???????????????????????????id
        String userId = getUserDetails(userName).getId().toString();
        // ????????????id???????????? ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userId + "," + getBaseLdapPath().toString());
        List<Group> userGroup = (List<Group>) findByMember(name);
        // ?????????????????????admin??????
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
     * admin ??????????????????admin?????????power???????????????power?????????admin??????
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
     * ?????????????????????????????????
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
     * ???????????????????????????????????????
     * @return result
     * @author FanLu
     */
    public List<String> getOrgByUser() {
        String orgId = getOrgIdByUser();
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        return userOrgListId;
    }

    /**
     * ????????????id?????????????????????????????????id
     * @param userId userId
     * @return List<String>
     * @author wangying
     */
    public List<String> getOrgIdsByUser(String userId) {
        // ???????????????????????????????????????
        String orgId = getOrgIdByUserId(userId);
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        return userOrgListId;
    }

    /**
     * ???????????????????????????????????????id
     * @return String
     * @author wangying
     */
    public String getOrgIdByUser() {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(",");
        return userId.substring(beginIndex + 1);
    }

    /**
     * ????????????id(????????????id????????????????????????)
     * @param userId ??????id
     * @return ??????id
     */
    public String getOrgIdByUserId(String userId) {
        int beginIndex = userId.indexOf(",");
        return userId.substring(beginIndex + 1);
    }

    /**
     * ?????????????????????????????????????????????id
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
     * ?????????????????????????????? ????????????????????????admin??????????????????????????????uuid????????????????????????????????????????????????????????????????????????uuid ???????????????????????????admin?????????????????????????????????uuid
     * @return String
     * @author Liubangquan
     */
    public String getOrgIdExceptAdmin() {
        // ?????????????????????????????????????????????????????????
        String orgId;
        boolean adminFlag = isADMINRole();
        // ??????????????????admin??????
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
     * ???????????????????????????????????????
     * @param allList    ????????????
     * @param id         ????????????id
     * @param returnList ??????list
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
     * ????????????
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
     * ?????????????????????????????????????????????uuid
     * @param allList    ????????????
     * @param returnList ??????list
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
     * ???????????????????????????????????????????????????????????????????????????
     * @return result
     */
    public List<String> getFenceOrg() {
        List<String> fenceOrg = new ArrayList<>();
        // ???????????????????????????
        String parentOrg = getParentOrgIdByUser();
        // ???????????? ????????????
        List<OrganizationLdap> allOrg = getOrgChild("ou=organization");
        // ??????????????????
        getParentOrg(allOrg, parentOrg, fenceOrg);
        // ???????????????????????????
        List<String> childOrg = getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        fenceOrg.addAll(childOrg);
        return fenceOrg;
    }

    /**
     * lijie
     * ??????????????????id??????????????????id??????
     * @return result
     */
    public List<String> getSuperiorGroupIdsById(String groupId) {
        List<String> listGroupIds = new ArrayList<>();
        // ???????????? ????????????
        List<OrganizationLdap> allOrg = getOrgChild("ou=organization");
        // ??????????????????
        getParentOrg1(allOrg, groupId, listGroupIds);
        return listGroupIds;
    }

    /**
     * ??????uuid????????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * ??????uuid????????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    /**
     * ??????uuid????????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * ??????uuidSet????????????
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
     * ??????????????????????????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * ??????entryDN????????????
     * @param id id
     * @return result
     * @author wangying
     */
    public OrganizationLdap getOrgByEntryDN(String id) {
        return getOrgByEntryDN(id, true);
    }

    /**
     * ??????entryDN????????????
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
        // ??????????????????????????????????????????????????????dn?????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * ???????????????????????????????????????
     * @param userId userId
     * @return result
     * @author wangying
     */
    public List<String> getOrgUuidsByUser(String userId) {
        List<String> userOrgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            // ????????????id(????????????id????????????????????????)
            int beginIndex = userId.indexOf(",");
            String orgId = userId.substring(beginIndex + 1);
            List<OrganizationLdap> orgs = getOrgChild(orgId);
            // ?????????????????????????????????????????????id???list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdap org : orgs) {
                    userOrgListId.add(org.getUuid());
                }
            }
        }
        return userOrgListId;
    }

    /**
     * ??????????????????????????????????????????????????????(???????????????)
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
     * ???????????????????????????????????????uuid
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
        // ?????????????????????????????????????????????
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
            // ?????????????????????????????????????????????id???list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdap org : orgs) {
                    orgListId.add(org.getUuid());
                }
            }
        }
        return orgListId;
    }

    /**
     * ???????????????uuid???????????????????????????
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
     * ???????????????????????????uuid
     */

    public String getCurrentUserUuid() {
        UserLdap user = SystemHelper.getCurrentUser();
        List<UserBean> userBeans = getUserByUid(user.getUid());
        UserBean userBean = userBeans.get(0);
        return userBean.getUuid();
    }

    /**
     * ???????????????uuid
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
     * ??????????????????
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
     * ????????????????????????
     * @param assignList ????????????
     * @return result
     */
    public Set<String> getAssignVehicleForRedis(List<String> assignList) {
        // ?????????????????????id list
        final List<RedisKey> keys =
                assignList.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toList());
        return RedisHelper.batchGetSet(keys);
    }

    /**
     * ????????????????????????(??????,????????????,????????????)
     */
    public void updateUserOffline(String ipAddress, String username) {
        String msg = "?????? : " + username + " ????????????";
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    public String getChatGroupUserList(String type, String groupId) throws Exception {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(","); // ????????????id(????????????id????????????????????????)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = getOrgChild(orgId);
        // ????????????????????????
        List<String> checkUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(groupId)) {
            // ??????????????????????????????id list
            checkUserList = chatGroupUserService.findGroupUserByGroupId(groupId);
        }
        int userCount = 0;
        // ?????????????????????????????????????????????id???list
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
                if (StringUtils.isBlank(groupId) && userId.equals(userBean.getId().toString())) { // ?????????????????????????????????
                    userObj.put("checked", true);
                } else { // ??????????????????????????????????????????
                    if (checkUserList != null && !checkUserList.isEmpty()) {
                        if (checkUserList.contains(uid)) { // ???????????????????????????
                            userObj.put("checked", true);
                        }
                    }
                }
                result.add(userObj);
            }
        }
        // ?????????????????????
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
     * ????????????id????????????????????????????????????????????????????????????????????????????????????
     * @param userId
     * @param formList
     * @return ture ??????????????????????????????????????? false ????????????
     * @Author gfw
     * @Date 20180904
     */
    public boolean compareUserForm(String userId, List<RoleResourceForm> formList) {
        boolean flag = true;
        log.info("??????id:{}", userId);
        log.info("????????????:{}", formList);
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
     * ????????????????????????????????????????????????????????????admin??????
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
     * ?????? ???????????????????????????????????????????????????
     * @param userId
     * @param roleList
     * @return true ???????????? false ???????????????
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
     * ????????????ID??????????????????
     * @param vehicleId vehicleId
     * @return groupName
     */
    public String getGroupNameByVehicleId(String vehicleId) {
        return RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "orgName");
    }

    /**
     * ??????????????????????????????
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
     * ???????????????id
     * @param userName
     * @return APP2.0.0??????
     */
    public Object generatingClientId(String userName) {
        String clientId = userName + "_" + UUID.randomUUID().toString();
        WebSubscribeManager.getInstance().addUserClientId(userName, clientId);
        return clientId;
    }

    /**
     * ???????????????id????????????
     * @param userName
     * @param nowClientId
     * @return APP2.0.0??????
     */
    public Boolean checkClientId(String userName, String nowClientId) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(nowClientId)) {
            return false;
        }
        String oldClientId = WebSubscribeManager.getInstance().getUserClientId(userName);
        return Objects.equals(nowClientId, oldClientId);
    }

    /**
     * ?????????????????????????????????
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
        // ???????????????????????????????????? gfw 20180907
        // ???????????????admin?????? admin ??????????????????
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
            // ??????????????????
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * ???????????????????????????????????????(?????????????????????????????????)
     * @param orgId
     * @return
     */
    public List<OrganizationLdap> getUserOwnAuthorityOrganizeInfo(String orgId) {
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = getOrgChild(orgId);
        return CollectionUtils.isNotEmpty(userOwnAuthorityOrganizeInfo) ? userOwnAuthorityOrganizeInfo :
            new ArrayList<>();
    }

}
