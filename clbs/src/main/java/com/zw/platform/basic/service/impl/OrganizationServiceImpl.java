package com.zw.platform.basic.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BusinessScopeDO;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.ldap.mapper.EnterpriseLdapInfoMapper;
import com.zw.platform.basic.ldap.mapper.OrganizationContextMapper;
import com.zw.platform.basic.ldap.mapper.UserContextMapper;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.OrganizationRepo;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.form.OrganizationImportForm;
import com.zw.platform.domain.leaderboard.EnterpriseLdapInfo;
import com.zw.platform.exception.OrganizationDeleteException;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.service.basicinfo.DictionaryService;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 组织service类
 * @date 2020/9/2511:02
 */
@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService, CacheService, IpAddressService {

    /**
     * 可能存在一点延迟，但不需要频繁存取LDAP
     */
    private static final Cache<String, OrganizationLdap> ORG_CACHE = Caffeine.newBuilder()
            .initialCapacity(2 << 10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .softValues()
            .build();

    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private WebClientHandleCom webClientHandleCom;

    @Autowired
    private BusinessScopeService businessScopeService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private FenceService fenceService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ProfessionalService professionalService;

    @Value("${group.exist.vehicle}")
    private String orgExistMonitor;

    @Value("${group.exist.employee}")
    private String orgExistEmployee;

    @Value("${group.exist.device}")
    private String orgExistDevice;

    @Value("${group.exist.sim}")
    private String orgExistSim;

    @Value("${group.exist.assignment}")
    private String orgExistAssignment;

    @Value("${group.exist.fence}")
    private String orgExistFence;

    @Value("${group.exist.user}")
    private String orgExistUser;

    @Value("${ldap.base}")
    private String ldapBase;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private DictionaryService dictionaryService;

    @Override
    public String getCurrentUserOrgTree() {
        return null;
    }

    @Override
    public void initCache() {
        log.info("开始初始化组织");
        //先删除之前的缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORGANIZATION_INFO.of("*"));
        log.info("删除原有组织缓存功能");
        List<OrganizationLdap> allOrganization = getAllOrganization();
        if (CollectionUtils.isEmpty(allOrganization)) {
            log.info("组织数为0");
            return;
        }
        Map<RedisKey, Map<String, String>> redisKeyOrgInfoMap = Maps.newHashMap();
        for (OrganizationLdap organizationLdap : allOrganization) {
            redisKeyOrgInfoMap
                .put(RedisKeyEnum.ORGANIZATION_INFO.of(organizationLdap.getUuid()), getOrgListMap(organizationLdap));
        }
        RedisHelper.batchAddToHash(redisKeyOrgInfoMap);
        log.info("初始化组织成功");
    }

    @Override
    public boolean add(OrganizationLdap organization) {
        String ou = "ORG_" + UUID.randomUUID();
        organization.setOu(ou);
        organization.setId(LdapUtils.newLdapName("ou=" + ou + "," + organization.getPid()));
        String msg = "新增组织: " + organization.getName();
        try {
            Attributes attributes = setOrgAttribute(organization);
            ldapTemplate.bind(organization.getId(), null, attributes);
            //查询到uuid
            OrganizationLdap newOrganization = getByOu(organization.getOu());
            // 维护redis缓存 “组织ID”_organization_list  为川标809协议扩展字段  供F3使用
            putOrg2Redis(newOrganization);
            // 经营范围
            if (StringUtils.isNotBlank(organization.getScopeOfOperationIds())) {
                List<String> scopeIds = Arrays.asList(organization.getScopeOfOperationIds().split(","));
                businessScopeService.bindBusinessScope(newOrganization.getUuid(), scopeIds, 1);
            }
            logSearchService.log(msg, "3", "", "-", "");
        } catch (Exception e) {
            log.error("新增企业异常", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean update(OrganizationLdap newOrg) throws BusinessException {

        List<ModificationItem> moList = new ArrayList<>();
        OrganizationLdap oldOrg = getOrgByEntryDn(newOrg.getPid());
        String orgUuid = oldOrg.getUuid();
        String beforeName = oldOrg.getName();
        String afterName = newOrg.getName();
        boolean flag = beforeName.equals(afterName);
        //构造更新的字段
        generateUpdateAttribute(newOrg, moList, oldOrg, beforeName, afterName);
        if (moList.size() == 0) {
            return true;
        }
        ModificationItem[] moArray = new ModificationItem[moList.size()];
        ldapTemplate.modifyAttributes(newOrg.getPid(), moList.toArray(moArray));
        newOrg.setUuid(orgUuid);
        if (!Objects.equals(newOrg.getScopeOfOperation(), oldOrg.getScopeOfOperation())) {
            //变更了经营范围
            businessScopeService.deleteById(oldOrg.getUuid());
            // 经营范围
            if (StringUtils.isNotBlank(newOrg.getScopeOfOperationIds())) {
                List<String> scopeIds = Arrays.asList(newOrg.getScopeOfOperationIds().split(","));
                businessScopeService.bindBusinessScope(oldOrg.getUuid(), scopeIds, 1);
            }
        }
        if (!flag) {
            //维护车和组织的缓存
            maintainVehicleOrgNameCache(newOrg);
        }
        //维护组织信息缓存
        newOrg.setId(oldOrg.getId());
        putOrg2Redis(newOrg);
        String msg;
        if (beforeName.equals(afterName)) {
            msg = "修改组织 : " + afterName;
        } else {
            msg = "修改组织名称 : " + beforeName + " 为 : " + afterName;
            //维护监控对象评分缓存
            List<String> orgIds = getSuperiorOrgIdsById(orgUuid);
            Set<RedisKey> patternSet = new HashSet<>();
            for (String key : orgIds) {
                patternSet.add(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN.of(key));
            }
            RedisHelper.delBatchByPatternSets(patternSet);
        }
        // 中位-企业静态信息同步(0x1608)
        webClientHandleCom.send1608ByUpdateGroupByZwProtocol(orgUuid);
        // 四川-企业静态信息同步(0x1605)
        webClientHandleCom.send1605ByUpdateGroupBySiChuanProtocol(orgUuid);
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        return true;
    }

    private void generateUpdateAttribute(OrganizationLdap newOrg, List<ModificationItem> moList,
        OrganizationLdap oldOrg, String beforeName, String afterName) {
        setLdapUpdateItem("l", afterName, beforeName, moList);
        setLdapUpdateItem("postalCode", newOrg.getOrganizationCode(), oldOrg.getOrganizationCode(), moList);
        setLdapUpdateItem("physicalDeliveryOfficeName", newOrg.getRegisterDate(), oldOrg.getRegisterDate(), moList);
        setLdapUpdateItem("businessCategory", newOrg.getOperation(), oldOrg.getOperation(), moList);
        setLdapUpdateItem("p0", newOrg.getLicense(), oldOrg.getLicense(), moList);
        setLdapUpdateItem("st", newOrg.getPrincipal(), oldOrg.getPrincipal(), moList);
        setLdapUpdateItem("telephoneNumber", newOrg.getPhone(), oldOrg.getPhone(), moList);
        setLdapUpdateItem("registeredAddress", newOrg.getAddress(), oldOrg.getAddress(), moList);
        setLdapUpdateItem("issuingOrgan", newOrg.getIssuingOrgan(), oldOrg.getIssuingOrgan(), moList);
        setLdapUpdateItem("operatingState", newOrg.getOperatingState(), oldOrg.getOperatingState(), moList);
        setLdapUpdateItem("provinceName", newOrg.getProvinceName(), oldOrg.getProvinceName(), moList);
        setLdapUpdateItem("countyName", newOrg.getCountyName(), oldOrg.getCountyName(), moList);
        setLdapUpdateItem("street", newOrg.getCityName(), oldOrg.getCityName(), moList);
        setLdapUpdateItem("areaNumber", newOrg.getAreaNumber(), oldOrg.getAreaNumber(), moList);
        setLdapUpdateItem("description", newOrg.getDescription(), oldOrg.getDescription(), moList);
        setLdapUpdateItem("p7", newOrg.getLicenseValidityStartDate(), oldOrg.getLicenseValidityStartDate(), moList);
        setLdapUpdateItem("p8", newOrg.getLicenseValidityEndDate(), oldOrg.getLicenseValidityEndDate(), moList);
        setLdapUpdateItem("p9", newOrg.getContactName(), oldOrg.getContactName(), moList);
        setLdapUpdateItem("upOrganizationCode", newOrg.getUpOrganizationCode(), oldOrg.getUpOrganizationCode(), moList);
        setLdapUpdateItem("k0", newOrg.getManagerOrganizationCode(), oldOrg.getManagerOrganizationCode(), moList);
        setLdapUpdateItem("k8", newOrg.getBusinessLicenseType(), oldOrg.getBusinessLicenseType(), moList);
        setLdapUpdateItem("destinationIndicator", newOrg.getPrincipalPhone(), oldOrg.getPrincipalPhone(), moList);
    }

    /**
     * 维护监控对象信息中的企业名称
     * @param newOrg
     */
    private void maintainVehicleOrgNameCache(OrganizationLdap newOrg) {

        //通过orgId获取组织下的直属监控对象
        List<String> monitorIds = monitorService.getMonitorIdByOrgId(newOrg.getUuid());
        if (monitorIds.isEmpty()) {
            return;
        }
        List<RedisKey> keys = new ArrayList<>(monitorIds.size());
        monitorIds.forEach(o -> keys.add(RedisKeyEnum.MONITOR_INFO.of(o)));
        Map<String, String> values = new HashMap<>(1);
        values.put("orgName", newOrg.getName());
        RedisHelper.batchAddToHash(keys, values);
    }

    @Override
    public boolean insert(OrganizationLdap organization) {

        if (!add(organization)) {
            return false;
        }
        //获取插入组织前的用户列表, uuid -> userDn
        String parentDn = organization.getPid();
        Map<String, String> oldUserMap = usersBeforeAction(parentDn);
        moveChildren(parentDn, organization.getId().toString());
        // 保存组织用户dn值的变化，oldDn -> newDn
        Map<String, String> userMap = usersAfterAction(parentDn, oldUserMap);
        renameOrgUsers(userMap);
        return true;
    }

    /**
     * lijie
     * 获取当前组织id的上级的组织id集合
     * @return result
     */
    public List<String> getSuperiorOrgIdsById(String orgId) {
        List<String> orgIds = new ArrayList<>();
        // 所有组织 当前组织
        List<OrganizationLdap> allOrg = getOrgChildList("ou=organization");
        // 直属上级组织
        getParentOrgId(allOrg, orgId, orgIds);
        return orgIds;
    }

    /**
     * 递归获取指定组织的上级组织根据uuid
     * @param allList    所有组织
     * @param returnList 上级list
     * @author lijie
     */
    private void getParentOrgId(List<OrganizationLdap> allList, String orgId, List<String> returnList) {

        if (CollectionUtils.isEmpty(allList)) {
            return;
        }
        for (OrganizationLdap org : allList) {
            if (!org.getUuid().equals(orgId)) {
                continue;
            }
            returnList.add(org.getUuid());
            if (StringUtils.isEmpty(org.getPid())) {
                continue;
            }
            getParentOrgId(allList, org.getPid(), returnList);
        }
    }

    @Override
    public OrganizationLdap delete(String orgDn) {
        OrganizationLdap organization = getOrgByEntryDn(orgDn);
        if (organization == null) {
            return null;
        }
        //检查能否删除
        String orgId = organization.getUuid();
        try {
            //检查bind关系
            checkOrgIsBind(orgDn, orgId);
            //所有上级组织
            List<String> orgIds = getSuperiorOrgIdsById(orgId);
            //获取当前组织的父节点的DN值
            int index = orgDn.indexOf(",");
            if (index < 0) {
                return null;
            }
            // 删除企业与经营范围的绑定关系
            businessScopeService.deleteById(orgId);
            String parentDn = orgDn.substring(index + 1);
            //获取删除组织前的用户列表
            Map<String, String> oldUserMap = usersBeforeAction(parentDn);
            moveChildren(orgDn, parentDn);
            ldapTemplate.unbind(orgDn);
            // 保存组织用户dn值的变化，oldDn -> newDn
            Map<String, String> userMap = usersAfterAction(parentDn, oldUserMap);
            //改变ldap角色下的用户的Dn路径
            renameOrgUsers(userMap);
            String msg = "删除组织 : " + organization.getName();
            logSearchService.log(msg, "3", "", "-", "");
            // 维护缓存
            //维护监控对象评分缓存
            Set<RedisKey> patternSet = new HashSet<>();
            for (String id : orgIds) {
                patternSet.add(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN.of(id));
            }
            RedisHelper.delBatchByPatternSets(patternSet);
            RedisHelper.delete(RedisKeyEnum.ORGANIZATION_INFO.of(orgId));
            return organization;
        } catch (Throwable e) {
            if (!(e instanceof OrganizationDeleteException)) {
                log.error("删除组织信息异常", e);
            }
            throw e;
        }
    }

    @Override
    public List<OrganizationLdap> fuzzyOrgList(String keyword) {

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] returnedAtts =
            { "entryDN", "entryUUID", "givenName", "createTimestamp", "uid", "mail", "mobile", "employeeType", "st",
                "carLicense", "employeeNumber", "businessCategory", "departmentNumber", "displayName" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        if (StringUtils.isNotEmpty(keyword)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("l", "*" + keyword + "*"));
            andfilter.append(orfilter);
        }
        String orgDn = userService.getCurrentUserOrgDn();
        return ldapTemplate.search(orgDn, andfilter.encode(), searchCtls, new OrganizationContextMapper());
    }


    /**
     * 根据组织名称查询组织
     */
    @Override
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
     * 通过去企业名字模糊搜索（指定企业id范围)，得到所有复合条件的企业id
     * @param name
     * @return
     */
    @Override
    public List<String> getOrgIdsByOrgName(String name, Set<String> orgIdSet) {
        List<String> groupIds = new ArrayList<>();
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts = { "myproc", "mycic", "mycoc", "entryUUID", "l" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new NotFilter(new EqualsFilter("objectclass", "OpenLDAPou")));
        filter.and(new LikeFilter("l", "*" + name + "*"));
        List<EnterpriseLdapInfo> enterpriseLdapInfos =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new EnterpriseLdapInfoMapper());
        for (EnterpriseLdapInfo enterpriseLdapInfo : enterpriseLdapInfos) {
            if (orgIdSet != null && !orgIdSet.contains(enterpriseLdapInfo.getGid())) {
                continue;
            }
            groupIds.add(enterpriseLdapInfo.getGid());
        }
        return groupIds;
    }

    @Override
    public List<OrganizationLdap> getOrgListByUuid(String uuid) {
        OrganizationLdap orgByUuid = getOrganizationByUuid(uuid);
        if (orgByUuid == null) {
            return new ArrayList<>();
        }
        OrganizationLdap organizationLdap = Optional.of(orgByUuid).get();
        return getOrgChildList(organizationLdap.getId().toString());
    }

    @Override
    public List<String> getChildOrgIdByUuid(String parentUuid) {
        List<OrganizationLdap> orgList = getOrgListByUuid(parentUuid);
        return orgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
    }

    /**
     * 重命名角色下用户的dn
     * @param userDnMap
     */
    private void renameOrgUsers(Map<String, String> userDnMap) {
        if (userDnMap.size() == 0) {
            return;
        }
        Iterable<Group> roles = roleService.getAllGroup();
        boolean found;
        for (Group role : roles) {
            DirContextOperations ctx = ldapTemplate.lookupContext(role.getId());
            Set<Name> members = role.getMembers();
            found = false;
            for (Name member : members) {
                String newMemberDn = userDnMap.get(member.toString());
                if (newMemberDn == null) {
                    continue;
                }
                found = true;
                ctx.removeAttributeValue("member", member);
                ctx.addAttributeValue("member", LdapUtils.newLdapName(newMemberDn));
            }
            if (!found) {
                continue;
            }
            ldapTemplate.modifyAttributes(ctx);
        }
    }

    private void checkOrgIsBind(String orgDn, String orgId) {

        if (!monitorService.getMonitorIdByOrgId(orgId).isEmpty()) {
            throw new OrganizationDeleteException(orgExistMonitor);
            //.. 调用sim,终端，从业人员的接口判断是否能删除组织 @TODO
        } else if (!simCardService.getOrgSimCardIds(orgId).isEmpty()) {
            throw new OrganizationDeleteException(orgExistSim);
        } else if (!professionalService.getProfessionalsByOrgId(orgId).isEmpty()) {
            throw new OrganizationDeleteException(orgExistEmployee);
        } else if (!deviceService.getOrgDeviceIds(orgId).isEmpty()) {
            throw new OrganizationDeleteException(orgExistDevice);
        } else if (!groupService.getGroupsByOrgId(orgId).isEmpty()) {
            throw new OrganizationDeleteException(orgExistAssignment);
        } else if (fenceService.checkBindByOrgId(orgId) != 0) {
            throw new OrganizationDeleteException(orgExistFence);
        } else {
            //只早直属的，下属的企业，要提升一个层级
            List<UserDTO> users = userService.getUserByOrgDn(orgDn, SearchScope.ONELEVEL);
            if (users != null && users.size() > 0) {
                throw new OrganizationDeleteException(orgExistUser);
            }
        }
    }

    private Map<String, String> usersAfterAction(String parentId, Map<String, String> oldUserMap) {
        if (oldUserMap.size() == 0) {
            return Collections.emptyMap();
        }
        // 获取指定组织及其下级组织的所有用户
        List<UserDTO> newUsers = userService.getUserByOrgDn(parentId, SearchScope.SUBTREE);
        if (newUsers == null) {
            return Collections.emptyMap();
        }
        // 找到需要重命名的用户
        Map<String, String> userMap = new HashMap<>(newUsers.size());
        for (UserDTO newUser : newUsers) {
            String oldUserDn = oldUserMap.get(newUser.getUuid());
            if (oldUserDn == null) {
                continue;
            }
            userMap.put(oldUserDn + "," + ldapBase, newUser.getId().toString() + "," + ldapBase);
            userService.expireUserSession(newUser.getUsername());
        }
        return userMap;
    }

    private void moveChildren(String oldParentId, String newParentId) {
        String searchFilter = "objectClass=organizationalUnit";
        List<OrganizationLdap> children = ldapTemplate
            .search(oldParentId, searchFilter, SearchControls.ONELEVEL_SCOPE, (ContextMapper<OrganizationLdap>) ctx -> {
                DirContextAdapter context = (DirContextAdapter) ctx;
                OrganizationLdap org = new OrganizationLdap();
                org.setId(context.getDn());
                return org;
            });
        for (OrganizationLdap child : children) {
            String dn = child.getId().toString();
            if (dn.equals(newParentId)) {
                continue;
            }
            // 获取节点移动到新父节点下后的DN值
            int index = dn.indexOf(",");
            if (index < 0) {
                continue;
            }
            String newDn = dn.substring(0, index + 1) + newParentId;
            ldapTemplate.rename(child.getId().toString(), newDn);
        }
    }


    /**
     * getOrgChild
     * 根据组织id(不是uuid,是entryDN)查询企业与其所有下级企业的信息
     * @param orgDn orgDn
     * @return List<OrganizationLdap>
     */
    @Override
    public List<String> getOrgChildUUidList(String orgDn) {
        SearchControls searchCtls = new SearchControls();
        String[] returnedAtts = { "entryUUID" };
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        String searchFilter = "objectClass=organizationalUnit";
        List<String> list;
        list = ldapTemplate.search(orgDn, searchFilter, searchCtls, (ContextMapper<String>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            return context.getStringAttribute("entryuuid");
        });
        return list;
    }



    /**
     * 根据关键字模糊查询用户
     * @param searchParam   查询参数
     * @param orgId         orgId
     * @param searchSubTree searchSubTree false当前一级，true递归子级
     * @return List<UserBean>
     */
    @Override
    public List<UserDTO> fuzzyUsersByOrgDn(String searchParam, String orgId, boolean searchSubTree) {
        List<UserDTO> users;
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
            users.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return users;
        }
        return null;
    }

    private Map<String, String> usersBeforeAction(String dn) {
        // 获取指定组织和其下级组织的所有用户
        List<UserDTO> oldUsers = userService.getUserByOrgDn(dn, SearchScope.SUBTREE);
        if (oldUsers == null || oldUsers.isEmpty()) {
            return Collections.emptyMap();
        }

        // 获取指定组织的所有用户
        List<UserDTO> parentUser = userService.getUserByOrgDn(dn, SearchScope.ONELEVEL);
        if (parentUser == null || parentUser.isEmpty()) {
            return oldUsers.stream().collect(Collectors.toMap(UserDTO::getUuid, user -> user.getId().toString()));
        }
        Set<String> parentUserIdSet = parentUser.stream().map(UserDTO::getUuid).collect(Collectors.toSet());
        // 返回指定组织的下级组织的所有用户
        return oldUsers.stream().filter(user -> !parentUserIdSet.contains(user.getUuid()))
            .collect(Collectors.toMap(UserDTO::getUuid, user -> user.getId().toString()));
    }

    /**
     * 根据entryDN查询组织
     * @param dn dn
     * @return result
     * @author wangying
     */
    @Override
    public OrganizationLdap getOrgByEntryDn(String dn) {
        return getOrgByEntryDn(dn, true);
    }

    /**
     * 根据当前用户获取其所属组织uuid
     * @return String result
     * @author wangying
     */
    @Override
    public String getCurrentUserOrgUuid() {
        String uuid = "";
        UserLdap user = SystemHelper.getCurrentUser();
        String userId = user == null ? null : user.getId().toString();
        if (user == null) {
            return null;
        }
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String dn = userId.substring(beginIndex + 1);
        OrganizationLdap org = getOrgByEntryDn(dn);
        if (org != null) {
            uuid = org.getUuid();
        }
        return uuid;
    }

    /**
     * 根据当前用户id获取其所属组织uuid
     * @return String result
     * @author lijie
     */
    @Override
    public String getOrgUuidByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(",");
        String dn = userId.substring(beginIndex + 1);
        OrganizationLdap org = getOrgByEntryDn(dn);
        if (org != null) {
            return org.getUuid();
        }
        return null;
    }

    @Override
    public OrganizationLdap getOrgByEntryDn(String dn, boolean useCache) {
        return useCache ? ORG_CACHE.get(dn, orgId -> getOrganizationLdap(dn)) : getOrganizationLdap(dn);
    }

    private OrganizationLdap getOrganizationLdap(String dn) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "telephoneNumber", "scopeOfOperation",
                "issuingOrgan", "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        return getOrganizationLdap(searchCtls, searchBase, returnedAtts, "entryDN", dn + ",dc=zwlbs,dc=com");
    }

    /**
     * 获取ldap的组织
     * @param searchCtls
     * @param searchBase
     * @param returnedAtts
     * @param entryDn
     * @param s
     * @return
     */
    private OrganizationLdap getOrganizationLdap(SearchControls searchCtls, String searchBase, String[] returnedAtts,
        String entryDn, String s) {
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
        filter.and(new EqualsFilter(entryDn, s));
        List<OrganizationLdap> orgs =
            ldapTemplate.search(searchBase, filter.encode(), searchCtls, new OrganizationContextMapper());
        // 下面这段代码也可以实现根据用户名查询dn的功能
        if (CollectionUtils.isNotEmpty(orgs)) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * 根据用户名和组织部门构建dn
     * @param userName userName
     * @param orgId    orgId
     * @return Name
     * @author fanlu
     */
    @Override
    public Name bindDn(String userName, String orgId) {
        return LdapUtils.newLdapName("uid=" + userName + "," + orgId);
    }

    /**
     * getOrgChild
     * 根据组织id(不是uuid,是entryDN)查询企业与其所有下级企业的信息
     * @param orgDn orgDn
     * @return List<OrganizationLdap>
     */
    @Override
    public List<OrganizationLdap> getOrgChildList(String orgDn) {
        SearchControls searchCtls = new SearchControls();
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8", "p9",
                "upOrganizationCode", "k0", "k8", "createTimestamp", "givenName", "uid", "mail", "mobile",
                "employeeType", "carLicense", "employeeNumber", "departmentNumber", "displayName" };
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        String searchFilter = "objectClass=organizationalUnit";
        List<OrganizationLdap> list;
        list = ldapTemplate.search(orgDn, searchFilter, searchCtls, (ContextMapper<OrganizationLdap>) ctx -> {
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
            org.setCreateTimestamp(context.getStringAttribute("createTimestamp"));
            org.setEntryDN(context.getStringAttribute("entryDN").replace(",dc=zwlbs,dc=com", ""));
            return org;
        });
        if (list != null && !list.isEmpty()) {
            return list;
        }
        return null;
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
        return getOrganizationLdap(searchCtls, searchBase, returnedAtts, "entryUUID", uuid);
    }

    /**
     * 查询所有的组织架构
     * @return List<OrganizationLdap>
     * @author wangying
     */
    @Override
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

    /**
     * 递归获取指定组织的上级组织
     * @param allList    所有组织
     * @param id         指定组织id
     * @param returnList 上级list
     * @author wangying
     */
    @Override
    public void getParentOrgList(List<OrganizationLdap> allList, String id, Collection<OrganizationLdap> returnList) {
        if (CollectionUtils.isEmpty(allList)) {
            return;
        }
        for (OrganizationLdap org : allList) {
            if (!org.getId().toString().equals(id)) {
                continue;
            }
            returnList.add(org);
            String parentId = org.getPid();
            if (StringUtils.isEmpty(parentId)) {
                continue;
            }
            getParentOrgList(allList, org.getPid(), returnList);
        }
    }

    @Override
    public OrganizationLdap getOrganizationByUuid(String uuid) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "scopeOfOperation", "issuingOrgan",
                "operatingState", "provinceName", "countyName", "street", "areaNumber", "telephoneNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8" };
        searchCtls.setReturningAttributes(returnedAtts);
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

    @Override
    public String getOrgNameByUuid(String uuid) {
        OrganizationLdap organization = getOrganizationByUuid(uuid);
        if (organization != null) {
            return organization.getName();
        }
        return "";
    }

    /**
     * 根据uuidSet查询组织
     * @param uuidSet uuidSet
     */
    @Override
    public Map<String, OrganizationLdap> getOrgByUuids(Set<String> uuidSet) {
        Map<String, OrganizationLdap> organizationLdapMap = new HashMap<>(200);
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
     * 获取用户当前及下级组织列表
     * @param userDn userDn
     * @return result
     * @author wangying
     */
    @Override
    public List<String> getOrgUuidsByUser(String userDn) {
        List<String> userOrgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(userDn)) {
            // 获取组织id(根据用户id得到用户所在部门)
            int beginIndex = userDn.indexOf(",");
            String orgId = userDn.substring(beginIndex + 1);
            List<OrganizationLdap> orgs = getOrgChildList(orgId);
            // 遍历得到当前用户组织及下级组织id的list
            if (orgs != null && orgs.size() > 0) {
                for (OrganizationLdap org : orgs) {
                    userOrgListId.add(org.getUuid());
                }
            }
        }
        return userOrgListId;
    }

    private Attributes setOrgAttribute(OrganizationLdap organization) {
        Attribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("organizationalUnit");

        Attributes attributes = new BasicAttributes();
        attributes.put(objectClass);
        attributes.put("ou", organization.getOu());
        addAttribute(attributes, "l", organization.getName());
        addAttribute(attributes, "postalCode", organization.getOrganizationCode());
        addAttribute(attributes, "physicalDeliveryOfficeName", organization.getRegisterDate());
        addAttribute(attributes, "businessCategory", organization.getOperation());
        addAttribute(attributes, "p0", organization.getLicense());
        addAttribute(attributes, "st", organization.getPrincipal());
        addAttribute(attributes, "telephoneNumber", organization.getPhone());
        addAttribute(attributes, "registeredAddress", organization.getAddress());
        addAttribute(attributes, "description", organization.getDescription());
        addAttribute(attributes, "operatingState", organization.getOperatingState());
        addAttribute(attributes, "issuingOrgan", organization.getIssuingOrgan());
        addAttribute(attributes, "provinceName", organization.getProvinceName());
        addAttribute(attributes, "street", organization.getCityName());
        addAttribute(attributes, "countyName", organization.getCountyName());
        addAttribute(attributes, "areaNumber", organization.getAreaNumber());
        addAttribute(attributes, "P7", organization.getLicenseValidityStartDate());
        addAttribute(attributes, "P8", organization.getLicenseValidityEndDate());
        addAttribute(attributes, "P9", organization.getContactName());
        addAttribute(attributes, "upOrganizationCode", organization.getUpOrganizationCode());
        addAttribute(attributes, "k0", organization.getManagerOrganizationCode());
        addAttribute(attributes, "k8", organization.getBusinessLicenseType());
        addAttribute(attributes, "destinationIndicator", organization.getPrincipalPhone());
        return attributes;
    }

    /**
     * 添加 attribute
     * @param attributes
     * @param field
     * @param value
     */
    private void addAttribute(Attributes attributes, String field, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        attributes.put(field, value);
    }

    /**
     * 设置企业信息redis缓存
     */
    private void putOrg2Redis(OrganizationLdap organization) {
        RedisKey redisKey = RedisKeyEnum.ORGANIZATION_INFO.of(organization.getUuid());
        Map<String, String> orgListMap = getOrgListMap(organization);
        RedisHelper.addToHash(redisKey, orgListMap);
    }

    private Map<String, String> getOrgListMap(OrganizationLdap organization) {
        Map<String, String> orgListMap = new HashMap<>(40);
        String organizationCode = organization.getOrganizationCode();
        if (StringUtils.isNotEmpty(organizationCode)) {
            orgListMap.put("organizationCode", organizationCode);
        }
        String name = organization.getName();
        if (StringUtils.isNotEmpty(name)) {
            orgListMap.put("name", name);
        }
        String operation = organization.getOperation();
        if (StringUtils.isNotEmpty(operation)) {
            orgListMap.put("operation", operation);
        }
        String upOrganizationCode = organization.getUpOrganizationCode();
        if (StringUtils.isNotEmpty(upOrganizationCode)) {
            orgListMap.put("upOrganizationCode", upOrganizationCode);
        }
        String managerOrganizationCode = organization.getManagerOrganizationCode();
        if (StringUtils.isNotEmpty(managerOrganizationCode)) {
            orgListMap.put("managerOrganizationCode", managerOrganizationCode);
        }
        String businessLicenseType = organization.getBusinessLicenseType();
        if (StringUtils.isNotEmpty(businessLicenseType)) {
            orgListMap.put("businessLicenseType", businessLicenseType);
        }
        String issuingOrgan = organization.getIssuingOrgan();
        if (StringUtils.isNotEmpty(issuingOrgan)) {
            orgListMap.put("issuingOrgan", issuingOrgan);
        }
        String areaNumber = organization.getAreaNumber();
        if (StringUtils.isNotEmpty(areaNumber)) {
            orgListMap.put("areaNumber", areaNumber);
        }
        String license = organization.getLicense();
        if (StringUtils.isNotEmpty(license)) {
            orgListMap.put("license", license);
        }
        String scopeOfOperation = organization.getScopeOfOperation();
        if (StringUtils.isNotEmpty(scopeOfOperation)) {
            orgListMap.put("scopeOfOperation", scopeOfOperation);
        }
        String scopeOfOperationIds = organization.getScopeOfOperationIds();
        if (StringUtils.isNotEmpty(scopeOfOperationIds)) {
            orgListMap.put("scopeOfOperationIds", scopeOfOperationIds);
        }
        String licenseValidityStartDate = organization.getLicenseValidityStartDate();
        if (StringUtils.isNotEmpty(licenseValidityStartDate)) {
            orgListMap.put("licenseValidityStartDate", licenseValidityStartDate);
        }
        String licenseValidityEndDate = organization.getLicenseValidityEndDate();
        if (StringUtils.isNotEmpty(licenseValidityEndDate)) {
            orgListMap.put("licenseValidityEndDate", licenseValidityEndDate);
        }
        String address = organization.getAddress();
        if (StringUtils.isNotEmpty(address)) {
            orgListMap.put("address", address);
        }
        String principal = organization.getPrincipal();
        if (StringUtils.isNotEmpty(principal)) {
            orgListMap.put("principal", principal);
        }
        String contactName = organization.getContactName();
        if (StringUtils.isNotEmpty(contactName)) {
            orgListMap.put("contactName", contactName);
        }
        String phone = organization.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            orgListMap.put("phone", phone);
        }
        String operatingState = organization.getOperatingState();
        if (StringUtils.isNotEmpty(operatingState)) {
            orgListMap.put("operatingState", operatingState);
        }
        String uuid = organization.getUuid();
        if (StringUtils.isNotEmpty(uuid)) {
            orgListMap.put("id", uuid);
        }
        String areaName = organization.getAreaName();
        if (StringUtils.isNotEmpty(areaName) && StringUtils.isNotEmpty(areaName.trim())) {
            orgListMap.put("areaName", areaName);
        }
        String principalPhone = organization.getPrincipalPhone();
        if (StringUtils.isNotEmpty(principalPhone)) {
            orgListMap.put("principalPhone", principalPhone);
        }
        return orgListMap;
    }

    @Override
    public OrganizationLdap getByOu(String ou) throws BusinessException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=organization";
        String[] returnedAtts =
            { "entryDN", "entryUUID", "ou", "description", "postalCode", "p0", "physicalDeliveryOfficeName",
                "businessCategory", "registeredAddress", "st", "l", "telephoneNumber", "scopeOfOperation",
                "issuingOrgan", "operatingState", "provinceName", "countyName", "street", "areaNumber", "p7", "p8",
                "p9", "upOrganizationCode", "k0", "k8", "destinationIndicator" };
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
        throw new BusinessException("企业不存在");
    }

    private void setLdapUpdateItem(String id, String value, String defaultValue, List<ModificationItem> itemList) {
        if (StringUtil.isNotEmpty(value)) {
            itemList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(id, value)));
        } else if (StringUtil.isNotEmpty(defaultValue)) {
            itemList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(id, defaultValue)));
        }
    }

    @Override
    public Map<String, Object> importOrg(MultipartFile file, String parentDn) throws Exception {

        //用于检验文件中品牌名称是否重复
        Map<String, Integer> checkRepeat = new HashMap<String, Integer>();
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        // excel 转换成 list
        List<OrganizationImportForm> list = importExcel.getDataList(OrganizationImportForm.class, null);
        List<OrganizationLdap> importList = new ArrayList<>();
        // 组织结构代码唯一性校验
        Iterable<OrganizationLdap> allOrg = organizationRepo.findAll();
        Set<String> organizationCodeSet = new HashSet<>();
        for (OrganizationLdap next : allOrg) {
            if (StringUtils.isNotBlank(next.getOrganizationCode())) {
                organizationCodeSet.add(next.getOrganizationCode());
            }
        }
        //查询所有
        List<Operations> allOperation = operationService.findAll();
        Set<String> operations = new HashSet<>(16);
        if (CollectionUtils.isNotEmpty(allOperation)) {
            operations = allOperation.stream().map(Operations::getOperationType).collect(Collectors.toSet());
        }
        List<DictionaryDO> businessLicenseType = dictionaryService.getBusinessLicenseType();
        Set<String> businessLicenseCodes =
            businessLicenseType.stream().map(DictionaryDO::getCode).collect(Collectors.toSet());
        List<DictionaryDO> businessScope = dictionaryService.getBusinessScope();
        Map<String, DictionaryDO> businessScopeMap =
            businessScope.stream().collect(Collectors.groupingBy(DictionaryDO::getValue,
                // groupby 取第一条
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))));
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            OrganizationImportForm org = list.get(i);
            if (!validate(org, checkRepeat, organizationCodeSet, operations, businessLicenseCodes, businessScopeMap, i,
                errorMsg)) {
                continue;
            }
            OrganizationLdap organization = new OrganizationLdap();
            BeanUtils.copyProperties(org, organization);
            organization.setPid(java.net.URLDecoder.decode(parentDn, "UTF-8"));
            String ou = "ORG_" + UUID.randomUUID();
            organization.setOu(ou);
            organization.setId(LdapUtils.newLdapName("ou=" + ou + "," + organization.getPid()));
            importList.add(organization);
            msg.append("导入企业 : ").append(org.getName()).append(" <br/>");
        }
        Map<String, Object> resultMap = new HashMap<>(6);
        if (importList.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
        importMore(importList);
        resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
        resultMap.put("flag", 1);
        resultMap.put("errorMsg", errorMsg.toString());
        resultMap.put("resultInfo", resultInfo);
        if (!"".contentEquals(msg)) {
            logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", "导入企业");
        }
        // Since 4.4.0: 用于维护查岗额外接收人
        resultMap.put("newOrgIds", importList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList()));
        return resultMap;
    }

    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        Map<String, String[]> selectMap = new HashMap<>();
        // 表头
        headList.add("企业名称");
        headList.add("组织机构代码");
        headList.add("上级组织机构代码");
        headList.add("管理组织机构代码");
        headList.add("行业类别");
        headList.add("经营范围");
        headList.add("发证机关");
        headList.add("经营许可证号");
        headList.add("经营许可证字别");
        headList.add("许可证有效期起");
        headList.add("许可证有效期止");
        headList.add("经营状态");
        headList.add("行政区域代码");
        headList.add("注册日期");
        headList.add("企业法人");
        headList.add("法人电话");
        headList.add("联系人");
        headList.add("联系人电话");
        headList.add("地址");
        headList.add("备注");

        // 必填字段
        requiredList.add("企业名称");

        // 默认设置一条数据
        exportList.add("北京中位科技");
        exportList.add("67578544");
        exportList.add("621122001");
        exportList.add("400002179");
        exportList.add("");
        exportList.add("道路旅客运输");
        exportList.add("中华人民共和国交通运输部");
        exportList.add("46456322266");
        List<DictionaryDO> businessLicenseType = dictionaryService.getBusinessLicenseType();
        String[] cods;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(businessLicenseType)) {
            cods = businessLicenseType.stream().map(DictionaryDO::getCode).toArray(String[]::new);
            selectMap.put("经营许可证字别", cods);
            exportList.add(cods[0]);
        } else {
            exportList.add("");
        }
        exportList.add("2019-03-11");
        exportList.add("2019-03-12");
        exportList.add("营业");
        exportList.add("520102");
        exportList.add("2020-04-10");
        exportList.add("张总");
        exportList.add("13688888888");
        exportList.add("李经理");
        exportList.add("13688888888");
        exportList.add("贵州省贵阳市南明区");
        exportList.add("备注");

        // 组装有下拉框的map
        List<Operations> operationList = operationService.findAll();
        if (CollectionUtils.isNotEmpty(operationList)) {
            String[] operations = operationList.stream().map(Operations::getOperationType).toArray(String[]::new);
            selectMap.put("行业类别", operations);
        }
        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
    }

    @Override
    public List<OrganizationLdap> getOrgListByUserDn(String userDn) {
        if (StringUtils.isBlank(userDn)) {
            return new ArrayList<>(1);
        }
        // 获取组织id(根据用户id得到用户所在部门)
        int beginIndex = userDn.indexOf(",");
        return getOrgChildList(userDn.substring(beginIndex + 1));
    }

    @Override
    public List<OrganizationLdap> getOrgParentAndChild(String orgDn) {
        //获取上级组织的orgDn
        List<OrganizationLdap> allOrgList = getOrgChildList("ou=organization");
        if (Objects.equals(orgDn, "ou=organization")) {
            return allOrgList;
        }
        String parentOrgDn = orgDn.substring(orgDn.indexOf(",") + 1);
        List<OrganizationLdap> parentOrgList = new ArrayList<>();
        getParentOrgList(allOrgList, parentOrgDn, parentOrgList);
        parentOrgList.addAll(getOrgChildList(orgDn));
        return parentOrgList;
    }

    private void importMore(List<OrganizationLdap> importList) throws BusinessException {

        OrganizationLdap newOrganization;
        Map<RedisKey, Map<String, String>> orgInfoMap = new HashMap<>(CommonUtil.ofMapCapacity(importList.size()));
        List<BusinessScopeDO> list = Lists.newArrayList();
        String[] scopeOfOperationIds;
        for (OrganizationLdap organization : importList) {
            Attributes attributes = setOrgAttribute(organization);
            ldapTemplate.bind(organization.getId(), null, attributes);
            //查询到uuid
            newOrganization = getByOu(organization.getOu());
            orgInfoMap
                .put(RedisKeyEnum.ORGANIZATION_INFO.of(newOrganization.getUuid()), getOrgListMap(newOrganization));
            if (StringUtils.isEmpty(organization.getScopeOfOperationIds())) {
                continue;
            }
            scopeOfOperationIds = organization.getScopeOfOperationIds().split(",");
            for (String scopeOfOperationId : scopeOfOperationIds) {
                if (StringUtils.isEmpty(scopeOfOperationId)) {
                    continue;
                }
                list.add(new BusinessScopeDO(newOrganization.getUuid(), scopeOfOperationId, "1"));
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            //绑定数据库
            businessScopeService.addBusinessScope(list);
        }
        // 维护redis缓存 “组织ID”_organization_list  为川标809协议扩展字段  供F3使用
        RedisHelper.batchAddToHash(orgInfoMap);
    }

    private boolean validate(OrganizationImportForm org, Map<String, Integer> checkRepeat,
        Set<String> organizationCodeSet, Set<String> operations, Set<String> businessLicenseCodes,
        Map<String, DictionaryDO> businessScopeMap, int i, StringBuilder errorMsg) {
        //品牌名称
        String name = org.getName();
        if (StringUtils.isBlank(name)) {
            errorMsg.append("第").append(i + 1).append("条数据,企业名称不能为空<br/>");
            return false;
        }
        if (name.length() > 25) {
            errorMsg.append("第").append(i + 1).append("条数据,企业名称长度错误,应小等于25<br/>");
            return false;
        }
        String organizationCode = org.getOrganizationCode();
        if (StringUtils.isNotBlank(organizationCode)) {
            if (checkRepeat.containsKey(organizationCode)) {
                errorMsg.append("第").append(i + 1).append("条数据,组织机构代码重复<br/>");
                return false;
            }
            if (organizationCode.length() > 20) {
                errorMsg.append("第").append(i + 1).append("条数据,组织机构代码长度错误,应小等于20<br/>");
                return false;
            }
            if (organizationCodeSet.contains(organizationCode)) {
                errorMsg.append("第").append(i + 1).append("条数据,组织机构代码已存在<br/>");
                return false;
            }
            checkRepeat.put(organizationCode, i);
        }
        if (StringUtils.isNotBlank(org.getUpOrganizationCode()) && org.getUpOrganizationCode().length() > 20) {
            errorMsg.append("第").append(i + 1).append("条数据,上级组织机构代码长度错误,应小等于20<br/>");
            return false;
        }
        String managerOrganizationCode = org.getManagerOrganizationCode();
        if (StringUtils.isNotBlank(managerOrganizationCode)) {
            if (managerOrganizationCode.length() > 30) {
                errorMsg.append("第").append(i + 1).append("条数据,上级组织机构代码长度错误,应小等于30<br/>");
                return false;
            }
            if (!RegexUtils.checkIsRegularChar(managerOrganizationCode)) {
                errorMsg.append("第").append(i + 1).append("条数据,请不要输入空格、换行和`^*;'\\\"|,/<>?<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getBusinessLicenseType())) {
            if (!businessLicenseCodes.contains(org.getBusinessLicenseType())) {
                errorMsg.append("第").append(i + 1).append("条数据,经营许可证字别不存在<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getOperation()) && !operations.contains(org.getOperation())) {
            errorMsg.append("第").append(i + 1).append("条数据,行业类别不存在<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getScopeOfOperation())) {
            List<String> scopeOfOperations = Arrays.asList(org.getScopeOfOperation().split(","));
            Set<DictionaryDO> dictionaryInfos = new HashSet<>();
            scopeOfOperations.forEach(o -> {
                if (businessScopeMap.containsKey(o)) {
                    dictionaryInfos.add(businessScopeMap.get(o));
                }
            });
            if (CollectionUtils.isEmpty(dictionaryInfos)) {
                // 设置默认值
                dictionaryInfos.add(businessScopeMap.get("道路旅客运输"));
            }
            String businessScopeIds =
                dictionaryInfos.stream().map(DictionaryDO::getId).collect(Collectors.joining(","));
            String businessScopes =
                dictionaryInfos.stream().map(DictionaryDO::getValue).collect(Collectors.joining(","));
            String businessScopeCodes =
                dictionaryInfos.stream().map(DictionaryDO::getCode).collect(Collectors.joining(","));
            org.setScopeOfOperation(businessScopes);
            org.setScopeOfOperationIds(businessScopeIds);
            org.setScopeOfOperationCodes(businessScopeCodes);
        }
        if (StringUtils.isNotBlank(org.getLicense()) && org.getLicense().length() > 20) {
            errorMsg.append("第").append(i + 1).append("条数据,许可证号长度错误,应小等于20<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getIssuingOrgan()) && org.getIssuingOrgan().length() > 50) {
            errorMsg.append("第").append(i + 1).append("条数据,发证机关长度错误,应小等于50<br/>");
            return false;
        }
        String licenseValidityStartDate = org.getLicenseValidityStartDate();
        Long licenseValidityStart = null;
        String licenseValidityEndDate = org.getLicenseValidityEndDate();
        if (StringUtils.isNotBlank(licenseValidityStartDate)) {
            try {
                licenseValidityStart = DateUtil.getStringToLong(licenseValidityStartDate, DateUtil.DATE_Y_M_D_FORMAT);
            } catch (Exception e) {
                errorMsg.append("第").append(i + 1).append("条数据,许可证有效期起错误<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(licenseValidityEndDate)) {
            try {
                Long licenseValidityEnd = DateUtil.getStringToLong(licenseValidityEndDate, DateUtil.DATE_Y_M_D_FORMAT);
                if (Objects.nonNull(licenseValidityStart) && licenseValidityEnd < licenseValidityStart) {
                    errorMsg.append("第").append(i + 1).append("条数据,许可证有效期止要大于许可证有效期起<br/>");
                    return false;
                }
            } catch (Exception e) {
                errorMsg.append("第").append(i + 1).append("条数据,许可证有效期止错误<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getOperatingStateStr())) {
            String operatingState = getOperatingState(org.getOperatingStateStr());
            if (StringUtils.isBlank(operatingState)) {
                errorMsg.append("第").append(i + 1).append("条数据,经营状态错误<br/>");
                return false;
            }
            org.setOperatingState(operatingState);
        }
        if (StringUtils.isNotBlank(org.getRegisterDate())) {
            try {
                DateUtil.getStringToLong(licenseValidityStartDate, DateUtil.DATE_Y_M_D_FORMAT);
            } catch (Exception e) {
                errorMsg.append("第").append(i + 1).append("条数据,注册日期错误<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getAreaNumber()) && !Pattern.matches("^\\d{6}$", org.getAreaNumber())) {
            errorMsg.append("第").append(i + 1).append("条数据,行政区域代码错误<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getPrincipal()) && org.getPrincipal().length() > 20) {
            errorMsg.append("第").append(i + 1).append("条数据,企业法人长度错误,应小等于20<br/>");
            return false;
        }
        String principalPhone = org.getPrincipalPhone();
        if (StringUtils.isNotBlank(principalPhone)) {
            if (principalPhone.length() > 11) {
                errorMsg.append("第").append(i + 1).append("条数据,法人电话错误,长度超过11位<br/>");
                return false;
            } else if (principalPhone.length() == 11) {
                if (!principalPhone.matches("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$")) {
                    errorMsg.append("第").append(i + 1).append("条数据,电话号码错误,请输入正确的电话号码<br/>");
                    return false;
                }
            } else {
                if (!principalPhone.matches("^(\\d{3,4}-?)?\\d{7,9}$")) {
                    errorMsg.append("第").append(i + 1).append("条数据,电话号码错误,请输入正确的电话号码<br/>");
                    return false;
                }
            }
        }
        if (StringUtils.isNotBlank(org.getContactName()) && org.getContactName().length() > 20) {
            errorMsg.append("第").append(i + 1).append("条数据,联系人长度错误,应小等于20<br/>");
            return false;
        }
        String phone = org.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            if (phone.length() > 11) {
                errorMsg.append("第").append(i + 1).append("条数据,联系人电话错误,长度超过11位<br/>");
                return false;
            } else if (phone.length() == 11) {
                if (!phone.matches("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$")) {
                    errorMsg.append("第").append(i + 1).append("条数据,联系人电话错误,请输入正确的电话号码<br/>");
                    return false;
                }
            } else {
                if (!phone.matches("^(\\d{3,4}-?)?\\d{7,9}$")) {
                    errorMsg.append("第").append(i + 1).append("条数据,联系人电话错误,请输入正确的电话号码<br/>");
                    return false;
                }
            }
        }
        if (StringUtils.isNotBlank(org.getAddress()) && org.getAddress().length() > 50) {
            errorMsg.append("第").append(i + 1).append("条数据,地址长度错误,应小等于20<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getDescription()) && org.getDescription().length() > 50) {
            errorMsg.append("第").append(i + 1).append("条数据,备注长度错误,应小等于50<br/>");
            return false;
        }
        return true;
    }

    private String getOperatingState(String str) {
        String value;
        switch (str) {
            case "营业":
                value = "1";
                break;
            case "停业":
                value = "2";
                break;
            case "整改":
                value = "3";
                break;
            case "停业整顿":
                value = "4";
                break;
            case "歇业":
                value = "5";
                break;
            case "注销":
                value = "6";
                break;
            case "其他":
                value = "7";
                break;
            default:
                value = "";
        }
        return value;
    }


    /**
     * lijie
     * 获取当前登录用户所属企业直接上级的组织id集合
     * @return result
     */
    @Override
    public List<OrganizationLdap> getSuperiorOrg() {
        List<OrganizationLdap> listGroupIds = new ArrayList<>();
        // 所有组织 当前组织
        List<OrganizationLdap> allOrg = getAllOrganization();
        String orgId = userService.getCurrentUserOrg().getId().toString();
        // 直属上级组织
        getParentOrgList(allOrg, orgId, listGroupIds);
        return listGroupIds;
    }

}
