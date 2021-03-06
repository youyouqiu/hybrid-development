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
 * @Title: ??????service???
 * @date 2020/9/2511:02
 */
@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService, CacheService, IpAddressService {

    /**
     * ???????????????????????????????????????????????????LDAP
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
        log.info("?????????????????????");
        //????????????????????????
        RedisHelper.delByPattern(RedisKeyEnum.ORGANIZATION_INFO.of("*"));
        log.info("??????????????????????????????");
        List<OrganizationLdap> allOrganization = getAllOrganization();
        if (CollectionUtils.isEmpty(allOrganization)) {
            log.info("????????????0");
            return;
        }
        Map<RedisKey, Map<String, String>> redisKeyOrgInfoMap = Maps.newHashMap();
        for (OrganizationLdap organizationLdap : allOrganization) {
            redisKeyOrgInfoMap
                .put(RedisKeyEnum.ORGANIZATION_INFO.of(organizationLdap.getUuid()), getOrgListMap(organizationLdap));
        }
        RedisHelper.batchAddToHash(redisKeyOrgInfoMap);
        log.info("?????????????????????");
    }

    @Override
    public boolean add(OrganizationLdap organization) {
        String ou = "ORG_" + UUID.randomUUID();
        organization.setOu(ou);
        organization.setId(LdapUtils.newLdapName("ou=" + ou + "," + organization.getPid()));
        String msg = "????????????: " + organization.getName();
        try {
            Attributes attributes = setOrgAttribute(organization);
            ldapTemplate.bind(organization.getId(), null, attributes);
            //?????????uuid
            OrganizationLdap newOrganization = getByOu(organization.getOu());
            // ??????redis?????? ?????????ID???_organization_list  ?????????809??????????????????  ???F3??????
            putOrg2Redis(newOrganization);
            // ????????????
            if (StringUtils.isNotBlank(organization.getScopeOfOperationIds())) {
                List<String> scopeIds = Arrays.asList(organization.getScopeOfOperationIds().split(","));
                businessScopeService.bindBusinessScope(newOrganization.getUuid(), scopeIds, 1);
            }
            logSearchService.log(msg, "3", "", "-", "");
        } catch (Exception e) {
            log.error("??????????????????", e);
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
        //?????????????????????
        generateUpdateAttribute(newOrg, moList, oldOrg, beforeName, afterName);
        if (moList.size() == 0) {
            return true;
        }
        ModificationItem[] moArray = new ModificationItem[moList.size()];
        ldapTemplate.modifyAttributes(newOrg.getPid(), moList.toArray(moArray));
        newOrg.setUuid(orgUuid);
        if (!Objects.equals(newOrg.getScopeOfOperation(), oldOrg.getScopeOfOperation())) {
            //?????????????????????
            businessScopeService.deleteById(oldOrg.getUuid());
            // ????????????
            if (StringUtils.isNotBlank(newOrg.getScopeOfOperationIds())) {
                List<String> scopeIds = Arrays.asList(newOrg.getScopeOfOperationIds().split(","));
                businessScopeService.bindBusinessScope(oldOrg.getUuid(), scopeIds, 1);
            }
        }
        if (!flag) {
            //???????????????????????????
            maintainVehicleOrgNameCache(newOrg);
        }
        //????????????????????????
        newOrg.setId(oldOrg.getId());
        putOrg2Redis(newOrg);
        String msg;
        if (beforeName.equals(afterName)) {
            msg = "???????????? : " + afterName;
        } else {
            msg = "?????????????????? : " + beforeName + " ??? : " + afterName;
            //??????????????????????????????
            List<String> orgIds = getSuperiorOrgIdsById(orgUuid);
            Set<RedisKey> patternSet = new HashSet<>();
            for (String key : orgIds) {
                patternSet.add(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN.of(key));
            }
            RedisHelper.delBatchByPatternSets(patternSet);
        }
        // ??????-????????????????????????(0x1608)
        webClientHandleCom.send1608ByUpdateGroupByZwProtocol(orgUuid);
        // ??????-????????????????????????(0x1605)
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
     * ??????????????????????????????????????????
     * @param newOrg
     */
    private void maintainVehicleOrgNameCache(OrganizationLdap newOrg) {

        //??????orgId????????????????????????????????????
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
        //????????????????????????????????????, uuid -> userDn
        String parentDn = organization.getPid();
        Map<String, String> oldUserMap = usersBeforeAction(parentDn);
        moveChildren(parentDn, organization.getId().toString());
        // ??????????????????dn???????????????oldDn -> newDn
        Map<String, String> userMap = usersAfterAction(parentDn, oldUserMap);
        renameOrgUsers(userMap);
        return true;
    }

    /**
     * lijie
     * ??????????????????id??????????????????id??????
     * @return result
     */
    public List<String> getSuperiorOrgIdsById(String orgId) {
        List<String> orgIds = new ArrayList<>();
        // ???????????? ????????????
        List<OrganizationLdap> allOrg = getOrgChildList("ou=organization");
        // ??????????????????
        getParentOrgId(allOrg, orgId, orgIds);
        return orgIds;
    }

    /**
     * ?????????????????????????????????????????????uuid
     * @param allList    ????????????
     * @param returnList ??????list
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
        //??????????????????
        String orgId = organization.getUuid();
        try {
            //??????bind??????
            checkOrgIsBind(orgDn, orgId);
            //??????????????????
            List<String> orgIds = getSuperiorOrgIdsById(orgId);
            //?????????????????????????????????DN???
            int index = orgDn.indexOf(",");
            if (index < 0) {
                return null;
            }
            // ??????????????????????????????????????????
            businessScopeService.deleteById(orgId);
            String parentDn = orgDn.substring(index + 1);
            //????????????????????????????????????
            Map<String, String> oldUserMap = usersBeforeAction(parentDn);
            moveChildren(orgDn, parentDn);
            ldapTemplate.unbind(orgDn);
            // ??????????????????dn???????????????oldDn -> newDn
            Map<String, String> userMap = usersAfterAction(parentDn, oldUserMap);
            //??????ldap?????????????????????Dn??????
            renameOrgUsers(userMap);
            String msg = "???????????? : " + organization.getName();
            logSearchService.log(msg, "3", "", "-", "");
            // ????????????
            //??????????????????????????????
            Set<RedisKey> patternSet = new HashSet<>();
            for (String id : orgIds) {
                patternSet.add(HistoryRedisKeyEnum.ORG_MONITOR_SCORE_PATTERN.of(id));
            }
            RedisHelper.delBatchByPatternSets(patternSet);
            RedisHelper.delete(RedisKeyEnum.ORGANIZATION_INFO.of(orgId));
            return organization;
        } catch (Throwable e) {
            if (!(e instanceof OrganizationDeleteException)) {
                log.error("????????????????????????", e);
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
     * ??????????????????????????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * ????????????????????????????????????????????????id??????)????????????????????????????????????id
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
     * ???????????????????????????dn
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
            //.. ??????sim,????????????????????????????????????????????????????????? @TODO
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
            //?????????????????????????????????????????????????????????
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
        // ???????????????????????????????????????????????????
        List<UserDTO> newUsers = userService.getUserByOrgDn(parentId, SearchScope.SUBTREE);
        if (newUsers == null) {
            return Collections.emptyMap();
        }
        // ??????????????????????????????
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
            // ??????????????????????????????????????????DN???
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
     * ????????????id(??????uuid,???entryDN)?????????????????????????????????????????????
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
     * ?????????????????????????????????
     * @param searchParam   ????????????
     * @param orgId         orgId
     * @param searchSubTree searchSubTree false???????????????true????????????
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
        // ??????????????????
        if (users != null && !users.isEmpty()) {
            users.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return users;
        }
        return null;
    }

    private Map<String, String> usersBeforeAction(String dn) {
        // ???????????????????????????????????????????????????
        List<UserDTO> oldUsers = userService.getUserByOrgDn(dn, SearchScope.SUBTREE);
        if (oldUsers == null || oldUsers.isEmpty()) {
            return Collections.emptyMap();
        }

        // ?????????????????????????????????
        List<UserDTO> parentUser = userService.getUserByOrgDn(dn, SearchScope.ONELEVEL);
        if (parentUser == null || parentUser.isEmpty()) {
            return oldUsers.stream().collect(Collectors.toMap(UserDTO::getUuid, user -> user.getId().toString()));
        }
        Set<String> parentUserIdSet = parentUser.stream().map(UserDTO::getUuid).collect(Collectors.toSet());
        // ????????????????????????????????????????????????
        return oldUsers.stream().filter(user -> !parentUserIdSet.contains(user.getUuid()))
            .collect(Collectors.toMap(UserDTO::getUuid, user -> user.getId().toString()));
    }

    /**
     * ??????entryDN????????????
     * @param dn dn
     * @return result
     * @author wangying
     */
    @Override
    public OrganizationLdap getOrgByEntryDn(String dn) {
        return getOrgByEntryDn(dn, true);
    }

    /**
     * ???????????????????????????????????????uuid
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
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(",");
        String dn = userId.substring(beginIndex + 1);
        OrganizationLdap org = getOrgByEntryDn(dn);
        if (org != null) {
            uuid = org.getUuid();
        }
        return uuid;
    }

    /**
     * ??????????????????id?????????????????????uuid
     * @return String result
     * @author lijie
     */
    @Override
    public String getOrgUuidByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        // ?????????????????????????????????????????????
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
     * ??????ldap?????????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (CollectionUtils.isNotEmpty(orgs)) {
            return orgs.get(0);
        }
        return null;
    }

    /**
     * ????????????????????????????????????dn
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
     * ????????????id(??????uuid,???entryDN)?????????????????????????????????????????????
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
        return getOrganizationLdap(searchCtls, searchBase, returnedAtts, "entryUUID", uuid);
    }

    /**
     * ???????????????????????????
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

    /**
     * ???????????????????????????????????????
     * @param allList    ????????????
     * @param id         ????????????id
     * @param returnList ??????list
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
     * ??????uuidSet????????????
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
     * ???????????????????????????????????????
     * @param userDn userDn
     * @return result
     * @author wangying
     */
    @Override
    public List<String> getOrgUuidsByUser(String userDn) {
        List<String> userOrgListId = new ArrayList<>();
        if (StringUtils.isNotBlank(userDn)) {
            // ????????????id(????????????id????????????????????????)
            int beginIndex = userDn.indexOf(",");
            String orgId = userDn.substring(beginIndex + 1);
            List<OrganizationLdap> orgs = getOrgChildList(orgId);
            // ?????????????????????????????????????????????id???list
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
     * ?????? attribute
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
     * ??????????????????redis??????
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
        // ??????????????????????????????????????????????????????dn?????????
        if (orgs != null && !orgs.isEmpty()) {
            return orgs.get(0);
        }
        throw new BusinessException("???????????????");
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

        //?????????????????????????????????????????????
        Map<String, Integer> checkRepeat = new HashMap<String, Integer>();
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // ???????????????
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        // excel ????????? list
        List<OrganizationImportForm> list = importExcel.getDataList(OrganizationImportForm.class, null);
        List<OrganizationLdap> importList = new ArrayList<>();
        // ?????????????????????????????????
        Iterable<OrganizationLdap> allOrg = organizationRepo.findAll();
        Set<String> organizationCodeSet = new HashSet<>();
        for (OrganizationLdap next : allOrg) {
            if (StringUtils.isNotBlank(next.getOrganizationCode())) {
                organizationCodeSet.add(next.getOrganizationCode());
            }
        }
        //????????????
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
                // groupby ????????????
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
            msg.append("???????????? : ").append(org.getName()).append(" <br/>");
        }
        Map<String, Object> resultMap = new HashMap<>(6);
        if (importList.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "????????????0????????????");
            return resultMap;
        }
        // ???????????????????????????????????????????????????????????????????????????
        importMore(importList);
        resultInfo += "????????????" + importList.size() + "?????????,????????????" + (list.size() - importList.size()) + "????????????";
        resultMap.put("flag", 1);
        resultMap.put("errorMsg", errorMsg.toString());
        resultMap.put("resultInfo", resultInfo);
        if (!"".contentEquals(msg)) {
            logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", "????????????");
        }
        // Since 4.4.0: ?????????????????????????????????
        resultMap.put("newOrgIds", importList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList()));
        return resultMap;
    }

    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        Map<String, String[]> selectMap = new HashMap<>();
        // ??????
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("????????????????????????");
        headList.add("????????????????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("?????????????????????");
        headList.add("?????????????????????");
        headList.add("?????????????????????");
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????");
        headList.add("???????????????");
        headList.add("??????");
        headList.add("??????");

        // ????????????
        requiredList.add("????????????");

        // ????????????????????????
        exportList.add("??????????????????");
        exportList.add("67578544");
        exportList.add("621122001");
        exportList.add("400002179");
        exportList.add("");
        exportList.add("??????????????????");
        exportList.add("????????????????????????????????????");
        exportList.add("46456322266");
        List<DictionaryDO> businessLicenseType = dictionaryService.getBusinessLicenseType();
        String[] cods;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(businessLicenseType)) {
            cods = businessLicenseType.stream().map(DictionaryDO::getCode).toArray(String[]::new);
            selectMap.put("?????????????????????", cods);
            exportList.add(cods[0]);
        } else {
            exportList.add("");
        }
        exportList.add("2019-03-11");
        exportList.add("2019-03-12");
        exportList.add("??????");
        exportList.add("520102");
        exportList.add("2020-04-10");
        exportList.add("??????");
        exportList.add("13688888888");
        exportList.add("?????????");
        exportList.add("13688888888");
        exportList.add("???????????????????????????");
        exportList.add("??????");

        // ?????????????????????map
        List<Operations> operationList = operationService.findAll();
        if (CollectionUtils.isNotEmpty(operationList)) {
            String[] operations = operationList.stream().map(Operations::getOperationType).toArray(String[]::new);
            selectMap.put("????????????", operations);
        }
        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.close();
    }

    @Override
    public List<OrganizationLdap> getOrgListByUserDn(String userDn) {
        if (StringUtils.isBlank(userDn)) {
            return new ArrayList<>(1);
        }
        // ????????????id(????????????id????????????????????????)
        int beginIndex = userDn.indexOf(",");
        return getOrgChildList(userDn.substring(beginIndex + 1));
    }

    @Override
    public List<OrganizationLdap> getOrgParentAndChild(String orgDn) {
        //?????????????????????orgDn
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
            //?????????uuid
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
            //???????????????
            businessScopeService.addBusinessScope(list);
        }
        // ??????redis?????? ?????????ID???_organization_list  ?????????809??????????????????  ???F3??????
        RedisHelper.batchAddToHash(orgInfoMap);
    }

    private boolean validate(OrganizationImportForm org, Map<String, Integer> checkRepeat,
        Set<String> organizationCodeSet, Set<String> operations, Set<String> businessLicenseCodes,
        Map<String, DictionaryDO> businessScopeMap, int i, StringBuilder errorMsg) {
        //????????????
        String name = org.getName();
        if (StringUtils.isBlank(name)) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????<br/>");
            return false;
        }
        if (name.length() > 25) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????,????????????25<br/>");
            return false;
        }
        String organizationCode = org.getOrganizationCode();
        if (StringUtils.isNotBlank(organizationCode)) {
            if (checkRepeat.containsKey(organizationCode)) {
                errorMsg.append("???").append(i + 1).append("?????????,????????????????????????<br/>");
                return false;
            }
            if (organizationCode.length() > 20) {
                errorMsg.append("???").append(i + 1).append("?????????,??????????????????????????????,????????????20<br/>");
                return false;
            }
            if (organizationCodeSet.contains(organizationCode)) {
                errorMsg.append("???").append(i + 1).append("?????????,???????????????????????????<br/>");
                return false;
            }
            checkRepeat.put(organizationCode, i);
        }
        if (StringUtils.isNotBlank(org.getUpOrganizationCode()) && org.getUpOrganizationCode().length() > 20) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????????????????,????????????20<br/>");
            return false;
        }
        String managerOrganizationCode = org.getManagerOrganizationCode();
        if (StringUtils.isNotBlank(managerOrganizationCode)) {
            if (managerOrganizationCode.length() > 30) {
                errorMsg.append("???").append(i + 1).append("?????????,????????????????????????????????????,????????????30<br/>");
                return false;
            }
            if (!RegexUtils.checkIsRegularChar(managerOrganizationCode)) {
                errorMsg.append("???").append(i + 1).append("?????????,?????????????????????????????????`^*;'\\\"|,/<>?<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getBusinessLicenseType())) {
            if (!businessLicenseCodes.contains(org.getBusinessLicenseType())) {
                errorMsg.append("???").append(i + 1).append("?????????,??????????????????????????????<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getOperation()) && !operations.contains(org.getOperation())) {
            errorMsg.append("???").append(i + 1).append("?????????,?????????????????????<br/>");
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
                // ???????????????
                dictionaryInfos.add(businessScopeMap.get("??????????????????"));
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
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????,????????????20<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getIssuingOrgan()) && org.getIssuingOrgan().length() > 50) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????,????????????50<br/>");
            return false;
        }
        String licenseValidityStartDate = org.getLicenseValidityStartDate();
        Long licenseValidityStart = null;
        String licenseValidityEndDate = org.getLicenseValidityEndDate();
        if (StringUtils.isNotBlank(licenseValidityStartDate)) {
            try {
                licenseValidityStart = DateUtil.getStringToLong(licenseValidityStartDate, DateUtil.DATE_Y_M_D_FORMAT);
            } catch (Exception e) {
                errorMsg.append("???").append(i + 1).append("?????????,???????????????????????????<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(licenseValidityEndDate)) {
            try {
                Long licenseValidityEnd = DateUtil.getStringToLong(licenseValidityEndDate, DateUtil.DATE_Y_M_D_FORMAT);
                if (Objects.nonNull(licenseValidityStart) && licenseValidityEnd < licenseValidityStart) {
                    errorMsg.append("???").append(i + 1).append("?????????,???????????????????????????????????????????????????<br/>");
                    return false;
                }
            } catch (Exception e) {
                errorMsg.append("???").append(i + 1).append("?????????,???????????????????????????<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getOperatingStateStr())) {
            String operatingState = getOperatingState(org.getOperatingStateStr());
            if (StringUtils.isBlank(operatingState)) {
                errorMsg.append("???").append(i + 1).append("?????????,??????????????????<br/>");
                return false;
            }
            org.setOperatingState(operatingState);
        }
        if (StringUtils.isNotBlank(org.getRegisterDate())) {
            try {
                DateUtil.getStringToLong(licenseValidityStartDate, DateUtil.DATE_Y_M_D_FORMAT);
            } catch (Exception e) {
                errorMsg.append("???").append(i + 1).append("?????????,??????????????????<br/>");
                return false;
            }
        }
        if (StringUtils.isNotBlank(org.getAreaNumber()) && !Pattern.matches("^\\d{6}$", org.getAreaNumber())) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getPrincipal()) && org.getPrincipal().length() > 20) {
            errorMsg.append("???").append(i + 1).append("?????????,????????????????????????,????????????20<br/>");
            return false;
        }
        String principalPhone = org.getPrincipalPhone();
        if (StringUtils.isNotBlank(principalPhone)) {
            if (principalPhone.length() > 11) {
                errorMsg.append("???").append(i + 1).append("?????????,??????????????????,????????????11???<br/>");
                return false;
            } else if (principalPhone.length() == 11) {
                if (!principalPhone.matches("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$")) {
                    errorMsg.append("???").append(i + 1).append("?????????,??????????????????,??????????????????????????????<br/>");
                    return false;
                }
            } else {
                if (!principalPhone.matches("^(\\d{3,4}-?)?\\d{7,9}$")) {
                    errorMsg.append("???").append(i + 1).append("?????????,??????????????????,??????????????????????????????<br/>");
                    return false;
                }
            }
        }
        if (StringUtils.isNotBlank(org.getContactName()) && org.getContactName().length() > 20) {
            errorMsg.append("???").append(i + 1).append("?????????,?????????????????????,????????????20<br/>");
            return false;
        }
        String phone = org.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            if (phone.length() > 11) {
                errorMsg.append("???").append(i + 1).append("?????????,?????????????????????,????????????11???<br/>");
                return false;
            } else if (phone.length() == 11) {
                if (!phone.matches("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$")) {
                    errorMsg.append("???").append(i + 1).append("?????????,?????????????????????,??????????????????????????????<br/>");
                    return false;
                }
            } else {
                if (!phone.matches("^(\\d{3,4}-?)?\\d{7,9}$")) {
                    errorMsg.append("???").append(i + 1).append("?????????,?????????????????????,??????????????????????????????<br/>");
                    return false;
                }
            }
        }
        if (StringUtils.isNotBlank(org.getAddress()) && org.getAddress().length() > 50) {
            errorMsg.append("???").append(i + 1).append("?????????,??????????????????,????????????20<br/>");
            return false;
        }
        if (StringUtils.isNotBlank(org.getDescription()) && org.getDescription().length() > 50) {
            errorMsg.append("???").append(i + 1).append("?????????,??????????????????,????????????50<br/>");
            return false;
        }
        return true;
    }

    private String getOperatingState(String str) {
        String value;
        switch (str) {
            case "??????":
                value = "1";
                break;
            case "??????":
                value = "2";
                break;
            case "??????":
                value = "3";
                break;
            case "????????????":
                value = "4";
                break;
            case "??????":
                value = "5";
                break;
            case "??????":
                value = "6";
                break;
            case "??????":
                value = "7";
                break;
            default:
                value = "";
        }
        return value;
    }


    /**
     * lijie
     * ?????????????????????????????????????????????????????????id??????
     * @return result
     */
    @Override
    public List<OrganizationLdap> getSuperiorOrg() {
        List<OrganizationLdap> listGroupIds = new ArrayList<>();
        // ???????????? ????????????
        List<OrganizationLdap> allOrg = getAllOrganization();
        String orgId = userService.getCurrentUserOrg().getId().toString();
        // ??????????????????
        getParentOrgList(allOrg, orgId, listGroupIds);
        return listGroupIds;
    }

}
