package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.OrganizationGroupDO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.basic.dto.imports.GroupImportDTO;
import com.zw.platform.basic.dto.query.GroupPageQuery;
import com.zw.platform.basic.imports.handler.GroupImportHandler;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.repository.UserGroupDao;
import com.zw.platform.basic.service.ConfigMessageService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.event.UpdateAssignmentEvent;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: ??????service
 * @date 2020/10/2614:21
 */
@Service
@Slf4j
public class GroupServiceImpl implements BaseLdapNameAware, GroupService, IpAddressService {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private LdapName baseLdapPath;

    @Autowired
    private UserGroupDao userGroupDao;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private GroupMonitorDao groupMonitorDao;

    @Autowired
    private ConfigMessageService configMessageService;

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    @Override
    public boolean add(GroupDO groupDo, OrganizationGroupDO organizationGroupDO) {

        //???????????????mysql
        groupDao.add(groupDo);
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        OrganizationLdap org = organizationService.getOrganizationByUuid(groupDo.getOrgId());
        if (org == null) {
            throw new RuntimeException("????????????");
        }
        //????????????????????????????????????
        giveGroup2Superior(groupDo, org);
        //????????????-??????ID??????
        RedisKey redisKey = RedisKeyEnum.ORG_GROUP.of(groupDo.getOrgId());
        RedisHelper.addToSet(redisKey, groupDo.getId());
        //????????????
        String msg = "???????????????" + groupDo.getName() + "( @" + org.getName() + " )";
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        return true;
    }

    public void giveGroup2Superior(GroupDO groupDO, OrganizationLdap organization) {
        //??????Id
        String groupId = groupDO.getId();
        List<UserGroupDO> userGroupList = new ArrayList<>();
        // ???????????????????????????????????????????????????
        List<LdapName> memberNameList = roleService.getMemberNameListByRoleCn("POWER_USER");
        List<LdapName> roleAdmin = roleService.getMemberNameListByRoleCn("ROLE_ADMIN");
        memberNameList.addAll(roleAdmin);

        //????????????????????????????????????
        List<UserDTO> allUser = userService.findAllUser();
        Map<String, List<UserDTO>> orgIdAndUserListMap = allUser.stream()
            .collect(Collectors.groupingBy(user -> userService.getUserOrgDnByDn(user.getId().toString())));

        // ???????????????????????????list
        List<OrganizationLdap> currentAndSuperiorOrgList = new ArrayList<>();
        // ???????????????????????????????????????list
        List<OrganizationLdap> orgChildList = organizationService.getOrgChildList("ou=organization");
        organizationService.getParentOrgList(orgChildList, organization.getId().toString(), currentAndSuperiorOrgList);

        // ??????????????????????????????????????????UUID?????????
        Map<String, String> currentAndSuperiorUserUuidAndNameMap = new HashMap<>(16);
        // ????????????
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String currentUsername = currentUser.getUsername();
        String currentUserUuid = userService.getUserUuidByDn(currentUser.getId().toString());
        currentAndSuperiorUserUuidAndNameMap.put(currentUserUuid, currentUsername);
        for (OrganizationLdap org : currentAndSuperiorOrgList) {
            String orgDn = org.getId().toString();
            // ????????????????????????
            List<UserDTO> orgUserList = orgIdAndUserListMap.get(orgDn);
            if (CollectionUtils.isEmpty(orgUserList)) {
                continue;
            }
            for (UserDTO user : orgUserList) {
                String userUuid = user.getUuid();
                LdapName roleName = LdapUtils.newLdapName(user.getId().toString() + "," + baseLdapPath.toString());
                if (!memberNameList.contains(roleName)) {
                    continue;
                }
                currentAndSuperiorUserUuidAndNameMap.put(userUuid, user.getUsername());
            }
        }
        for (Map.Entry<String, String> uuidAndNameEntry : currentAndSuperiorUserUuidAndNameMap.entrySet()) {
            userGroupList.add(new UserGroupDO(groupId, uuidAndNameEntry.getKey(), currentUsername));
        }
        //????????????mysql
        Lists.partition(userGroupList, 1000).parallelStream().forEach(userGroupService::batchAddToDb);
        //??????redis
        currentAndSuperiorUserUuidAndNameMap.values()
            .forEach(o -> RedisHelper.addToSet(RedisKeyEnum.USER_GROUP.of(o), groupId));
    }

    @Override
    public boolean update(GroupDTO groupDTO) throws BusinessException {

        //@TODO wanxing ???????????????????????????
        String id = groupDTO.getId();
        GroupDO oldGroupDO = groupDao.getById(id);
        if (oldGroupDO == null) {
            throw new BusinessException("???????????????");
        }
        GroupDO groupDO = groupDTO.copyDTO2DO();
        boolean result = groupDao.update(groupDO);
        if (!result) {
            return true;
        }
        //????????????
        StringBuilder message = new StringBuilder();
        String oldName = oldGroupDO.getName();
        String newName = groupDTO.getName();
        String orgId = groupDTO.getOrgId();
        String orgName = organizationService.getOrganizationByUuid(orgId).getName();
        if (!oldName.equals(newName)) {
            //????????????????????????
            operateGroupNameOfMonitorCache(id, oldName, newName);
            message.append("???????????? : ").append(oldName).append(" ( @").append(orgName).append(" ????????? : ").append(newName)
                .append(" ( @").append(orgName).append(" )");
        } else {
            message.append("???????????? : ").append(oldName).append(" ( @").append(orgName).append(")");
        }
        //????????????
        logSearchService.addLog(getIpAddress(), message.toString(), "3", "", "-", "");
        //???????????????
        return true;
    }

    /**
     * ???????????????????????????????????????
     * @param groupId
     */
    private void operateGroupNameOfMonitorCache(String groupId, String oldName, String newName) {
        Set<String> monitorIds = RedisHelper.getSet(RedisKeyEnum.GROUP_MONITOR.of(groupId));
        if (CollectionUtils.isEmpty(monitorIds)) {
            return;
        }
        if (!Objects.equals(oldName, newName)) {
            //??????F3
            configMessageService.sendToF3(monitorIds);
        }
    }

    /**
     * ??????????????????
     * @param form
     */
    private void syncTalkBackGroupEvent(AssignmentForm form) {
        publisher.publishEvent(new UpdateAssignmentEvent(this, form));
    }

    @Override
    public String delete(String id) {

        //@TODO wanxing ????????????????????????
        GroupDO groupDO = groupDao.getById(id);
        if (groupDO == null) {
            return "????????????";
        }
        //??????????????????(???????????????)
        Set<String> groupIds = groupMonitorDao.judgeGroupsHaveMonitor(Collections.singletonList(id));
        if (!groupIds.isEmpty()) {
            return "?????????????????????????????????????????????";
        }
        //???????????????
        groupDao.delete(id);
        List<String> userIdList = userGroupService.getUserIdsByGroupId(id);
        //???????????????????????????????????????
        userGroupService.deleteByGroupId(id);
        //????????????????????????????????????
        List<UserDTO> userList = userService.getUserListByUuids(userIdList);
        RedisKey redisKey;
        if (!CollectionUtils.isEmpty(userList)) {
            for (UserDTO userDTO : userList) {
                redisKey = RedisKeyEnum.USER_GROUP.of(userDTO.getUsername());
                RedisHelper.delSetItem(redisKey, id);
            }
        }
        //???????????????????????????????????????
        String orgId = groupDO.getOrgId();
        RedisHelper.delSetItem(RedisKeyEnum.ORG_GROUP.of(orgId), id);
        OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(orgId);
        if (organizationLdap == null) {
            return "?????????????????????";
        }
        String msg = "???????????? ???" + groupDO.getName() + "( @" + organizationLdap.getName() + " )";
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        return "";
    }

    @Override
    public Page<GroupDTO> getPageByKeyword(GroupPageQuery query) {
        // ?????????????????????????????????????????????
        UserLdap user = SystemHelper.getCurrentUser();
        List<String> orgIds = organizationService.getOrgUuidsByUser(user.getId().toString());
        if (CollectionUtils.isEmpty(orgIds)) {
            return new Page<>();
        }
        query.setOrgIds(orgIds);
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            //??????????????????
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        Page<GroupDO> page;
        if ("admin".equals(user.getUsername())) {
            PageMethod.startPage(query.getPage().intValue(), query.getLimit().intValue());
            page = PageHelperUtil.doSelect(query, () -> groupDao.getListByKeyword(query));
        } else {
            String userId = userService.getCurrentUserUuid();
            page = PageHelperUtil.doSelect(query, () -> groupDao.getListByKeywordAndUerId(query, userId));
        }
        // ??????result??????groupId?????????groupName???list??????????????????
        if (CollectionUtils.isEmpty(page)) {
            return new Page<>();
        }
        //??????????????????
        return generateOrgName(page);
    }


    private Page<GroupDTO> generateOrgName(Page<GroupDO> page) {
        Set<String> orgIdSet = page.stream().map(GroupDO::getOrgId).collect(Collectors.toSet());
        Map<String, OrganizationLdap> orgMaps = organizationService.getOrgByUuids(orgIdSet);
        Page<GroupDTO> result = PageHelperUtil.copyPage(page);
        GroupDTO groupDTO;
        OrganizationLdap organizationLdap;
        for (GroupDO groupDO : page) {
            groupDTO = groupDO.copyDO2DTO();
            result.add(groupDTO);
            organizationLdap = orgMaps.get(groupDO.getOrgId());
            if (organizationLdap == null) {
                groupDTO.setOrgName("???????????????????????????");
                continue;
            }
            groupDTO.setOrgName(organizationLdap.getName());
        }
        return result;
    }

    @Override
    public List<GroupDTO> getGroupsByOrgId(String orgId) {
        return groupDao.getGroupsByOrgId(orgId);
    }

    @Override
    public List<GroupDTO> getListByFuzzyName(String fuzzyName) {
        return groupDao.getListByFuzzyName(fuzzyName).stream().map(GroupDO::copyDO2DTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupDTO> getGroupsByOrgIds(Collection<String> orgIds) {
        return groupDao.getGroupsByOrgIds(orgIds);
    }

    @Override
    @ImportLock(value = ImportModule.ASSIGNMENT)
    public JsonResultBean importGroup(MultipartFile multipartFile) throws Exception {
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel ????????? list
        List<GroupImportDTO> groupImportDTOList = importExcel.getDataListNew(GroupImportDTO.class);
        if (CollectionUtils.isEmpty(groupImportDTOList)) {
            return new JsonResultBean(false, "????????????????????????");
        }
        // ?????????????????????
        StringBuilder msg = new StringBuilder();
        // ?????????????????????
        Map<String, String> groupNameIdMap = validate(groupImportDTOList, msg);
        if (groupNameIdMap.isEmpty()) {
            ImportErrorUtil.putDataToRedis(groupImportDTOList, ImportModule.ASSIGNMENT);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        GroupImportHandler handler =
            new GroupImportHandler(groupImportDTOList, groupNameIdMap, groupDao, userService, userGroupDao, roleService,
                organizationService);
        String username = SystemHelper.getCurrentUsername();
        try (ImportCache ignored = new ImportCache(ImportModule.ASSIGNMENT, username, handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                ImportErrorUtil.putDataToRedis(groupImportDTOList, ImportModule.ASSIGNMENT);
                return jsonResultBean;
            }
        }
        if (!"".equals(msg.toString())) {
            String monitoringOperation = "????????????";
            logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", monitoringOperation);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private Map<String, String> validate(List<GroupImportDTO> groupImportDTOList, StringBuilder msg) {
        List<OrganizationLdap> organizationLdapList =
            organizationService.getOrgChildList(userService.getCurrentUserOrgDn());
        // key:orgId,value:orgName
        Map<String, String> orgIdAndNameMap = organizationLdapList.stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        // key:orgName,value:OrganizationLdap ???????????? ???????????????
        Map<String, OrganizationLdap> orgNameMap = organizationLdapList.stream()
            .collect(Collectors.toMap(OrganizationLdap::getName, Function.identity(), (o, n) -> n));
        int size = groupImportDTOList.size();
        Map<String, String> groupNameIdMap = new HashMap<>(CommonUtil.ofMapCapacity(size));
        // ????????????id???????????????
        GroupImportDTO groupImportDTO;
        for (int i = 0, len = size; i < len; i++) {
            groupImportDTO = groupImportDTOList.get(i);
            if (StringUtils.isNotBlank(groupImportDTO.getErrorMsg())) {
                continue;
            }
            GroupImportDTO group;
            for (int j = size - 1; j > i; j--) {
                group = groupImportDTOList.get(j);
                if (StringUtils.isNotBlank(group.getErrorMsg())) {
                    continue;
                }
                // ???????????????, ??????????????????, ???????????????
                String inOrgName = group.getOrgName();
                String outOrgName = groupImportDTO.getOrgName();
                if (inOrgName.equals(outOrgName) && group.getName().equals(groupImportDTO.getName())) {
                    group.setErrorMsg("??????????????????");
                }
            }
        }
        String patternMobileRegex = "^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$";
        String patternTelRegex = "^(\\d{3,4}-?)?\\d{7,9}$";
        for (GroupImportDTO group : groupImportDTOList) {
            if (StringUtils.isNotBlank(group.getErrorMsg())) {
                continue;
            }
            String orgName = group.getOrgName();
            OrganizationLdap organizationLdap = orgNameMap.get(orgName);
            // ??????????????????????????????
            if (organizationLdap == null) {
                group.setErrorMsg("?????????????????????/?????????????????????");
                continue;
            }
            String orgId = organizationLdap.getUuid();
            groupNameIdMap.put(orgName, orgId);
            // ????????????????????????
            if (Converter.toBlank(group.getName()).length() > 30) {
                group.setErrorMsg("????????????????????????30???");
                continue;
            }
            // ???????????????????????????????????????????????????
            if (Converter.toBlank(group.getContacts()).length() > 20) {
                group.setContacts("");
            }
            if (Converter.toBlank(group.getDescription()).length() > 50) {
                group.setDescription("");
            }
            // ??????????????????
            String telephone = group.getTelephone();
            if (StringUtils.isNotBlank(telephone)) {
                // ?????? Pattern ??????
                // ???????????? matcher ??????
                boolean matche1 = telephone.matches(patternMobileRegex);
                boolean matche2 = telephone.matches(patternTelRegex);
                if (!(matche2 || (telephone.length() == 11 && matche1))) {
                    group.setTelephone("");
                }
            }
            msg.append("?????? : ").append(group.getName()).append(" ( @").append(orgIdAndNameMap.get(orgId))
                .append(" ) <br/>");
        }
        return groupNameIdMap;
    }


    @Override
    public boolean export(String title, int type,
                          HttpServletResponse response, AssignmentQuery assignmentQuery) throws IOException {
        ExportExcel export = new ExportExcel(title, GroupDTO.class, type);
        // ?????????????????????
        GroupPageQuery query = GroupPageQuery.transform(assignmentQuery);
        query.setPage(0L);
        query.setLimit(0L);
        Page<GroupDTO> result = getPageByKeyword(query);
        export.setDataList(result.getResult());
        // ???????????????
        try (OutputStream out = response.getOutputStream()) {
            // ????????????????????????????????????
            export.write(out);
        }
        return true;
    }

    @Override
    public List<GroupDTO> getGroupsById(Collection<String> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>(1);
        }
        List<GroupDO> groupList = groupDao.getByIds(ids);
        if (groupList.isEmpty()) {
            return new ArrayList<>(1);
        }
        Set<String> orgIds = groupList.stream().map(GroupDO::getOrgId).collect(Collectors.toSet());
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        GroupDTO groupDTO;
        List<GroupDTO> data = new ArrayList<>(groupList.size());
        OrganizationLdap organizationLdap;
        for (GroupDO groupDO : groupList) {
            groupDTO = new GroupDTO();
            BeanUtils.copyProperties(groupDO, groupDTO);
            organizationLdap = orgMap.get(groupDO.getOrgId());
            if (organizationLdap != null) {
                groupDTO.setOrgName(organizationLdap.getName());
                groupDTO.setOrgDn(organizationLdap.getId().toString());
            }
            groupDTO.setOrgId(groupDO.getOrgId());
            data.add(groupDTO);
        }
        return data;
    }

    @Override
    public GroupDTO getById(String id) throws BusinessException {
        GroupDO groupDO = groupDao.getById(id);
        if (groupDO == null) {
            throw new BusinessException("???????????????????????????????????????id??????" + id);
        }
        //??????????????????
        String orgId = groupDO.getOrgId();
        OrganizationLdap orgLdap = organizationService.getOrganizationByUuid(orgId);
        if (orgLdap == null) {
            throw new BusinessException("???????????????????????????");
        }
        String name = orgLdap.getName();
        GroupDTO groupDTO = groupDO.copyDO2DTO();
        groupDTO.setOrgName(name);
        return groupDTO;
    }

    @Override
    public String deleteBatch(Collection<String> ids) {
        Set<String> set = groupMonitorDao.judgeGroupsHaveMonitor(ids);
        if (!set.isEmpty()) {
            return "???????????????????????????????????????????????????";
        }
        List<GroupDO> list = groupDao.getByIds(ids);
        if (list.isEmpty()) {
            return "????????????";
        }
        //???????????????
        groupDao.delBatch(ids);
        List<UserGroupDTO> userIdGroupIdList = userGroupService.getUserIdsByGroupIds(ids);
        Map<RedisKey, Collection<String>> map = new HashMap<>(200);
        if (!userIdGroupIdList.isEmpty()) {
            //???????????????????????????????????????
            userGroupService.deleteByGroupIds(ids);
            Map<String, Set<String>> userIdGroupIdsMap = new HashMap<>(100);
            Set<String> userId = Sets.newHashSet();
            userIdGroupIdList.forEach(o -> {
                userIdGroupIdsMap.computeIfAbsent(o.getUserId(), key -> new HashSet<>()).add(o.getGroupId());
                userId.add(o.getUserId());
            });
            //????????????????????????????????????
            List<UserDTO> userList = userService.getUserListByUuids(userId);
            if (!CollectionUtils.isEmpty(userList)) {
                Set<String> groupIds;
                for (UserDTO userDTO : userList) {
                    groupIds = userIdGroupIdsMap.get(userDTO.getUuid());
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        map.put(RedisKeyEnum.USER_GROUP.of(userDTO.getUsername()), groupIds);
                    }
                }
            }
        }
        //???????????????????????????????????????
        Set<String> orgIds = Sets.newHashSet();
        for (GroupDO groupDO : list) {
            orgIds.add(groupDO.getOrgId());
        }
        Map<String, OrganizationLdap> orgMaps = organizationService.getOrgByUuids(orgIds);
        if (orgMaps.isEmpty()) {
            return "??????????????????";
        }
        RedisKey redisKey;
        OrganizationLdap organizationLdap;
        StringBuilder stringBuilder = new StringBuilder();
        for (GroupDO groupDO : list) {
            redisKey = RedisKeyEnum.ORG_GROUP.of(groupDO.getOrgId());
            organizationLdap = orgMaps.get(groupDO.getOrgId());
            if (organizationLdap == null) {
                continue;
            }
            stringBuilder.append("???????????? : ").append(groupDO.getName()).append(" ( @").append(organizationLdap.getName())
                .append(" ) <br/>");
            map.computeIfAbsent(redisKey, o -> new HashSet<>()).add(groupDO.getId());
        }
        RedisHelper.batchDeleteSet(map);
        logSearchService.addLog(getIpAddress(), stringBuilder.toString(), "3", "", "-", "");
        return "";
    }

    @Override
    public List<String> getNamesByIds(Collection<String> groupIdList) {
        return groupDao.getNamesByIds(groupIdList);
    }

    @Override
    public boolean addByBatch(List<GroupDO> groupList) {
        return groupDao.batchAdd(groupList);
    }

    @Override
    public void addToRedis(List<GroupDO> groupList) {
        if (CollectionUtils.isEmpty(groupList)) {
            return;
        }
        Map<String, Set<String>> orgIdAndGroupIdSetMap = new HashMap<>(16);
        groupList.forEach(group -> {
            Set<String> groupIdSet = orgIdAndGroupIdSetMap.getOrDefault(group.getOrgId(), new HashSet<>());
            groupIdSet.add(group.getId());
            orgIdAndGroupIdSetMap.put(group.getOrgId(), groupIdSet);
        });
        Map<RedisKey, Collection<String>> setKeyValueMap = new HashMap<>(16);
        orgIdAndGroupIdSetMap.forEach((orgId, groupSet) -> {
            setKeyValueMap.put(RedisKeyEnum.ORG_GROUP.of(orgId), groupSet);
        });
        RedisHelper.batchAddToSet(setKeyValueMap);
    }

    @Override
    public List<UserGroupDTO> getNewGroupOwnUser(List<GroupDO> newGroupList) {
        if (CollectionUtils.isEmpty(newGroupList)) {
            return new ArrayList<>();
        }
        // ???????????????????????????????????????????????????
        Set<LdapName> memberSet = new HashSet<>(roleService.getMemberNameListByRoleCn("POWER_USER"));
        memberSet.addAll(roleService.getMemberNameListByRoleCn("ROLE_ADMIN"));

        //??????????????????
        List<OrganizationLdap> allOrgList = organizationService.getOrgChildList("ou=organization");

        // ????????????
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String curUsername = currentUser.getUsername();
        String curUserUuid = userService.getUserUuidByDn(currentUser.getId().toString());

        //????????????????????????????????????
        List<UserDTO> allUser = userService.findAllUser();
        Map<String, List<UserDTO>> orgAndUserListMap = allUser.stream()
            .collect(Collectors.groupingBy(user -> userService.getUserOrgDnByDn(user.getId().toString())));

        //?????????????????????????????????????????????
        Set<String> orgIds = newGroupList.stream().map(GroupDO::getOrgId).collect(Collectors.toSet());
        Map<String, List<OrganizationLdap>> orgIdAndParentOrgMap = getOrgParentOrgMap(orgIds, allOrgList);
        List<UserGroupDTO> userGroupList = new ArrayList<>();
        for (GroupDO group : newGroupList) {
            List<OrganizationLdap> curAndUpOrg = orgIdAndParentOrgMap.get(group.getOrgId());
            //???????????????????????????????????????uuid?????????
            Map<String, String> curAndUpOrgUserMap = new HashMap<>(16);
            curAndUpOrgUserMap.put(curUserUuid, curUsername);
            for (OrganizationLdap org : curAndUpOrg) {
                String orgDn = org.getId().toString();
                List<UserDTO> orgUserList = orgAndUserListMap.get(orgDn);
                if (CollectionUtils.isEmpty(orgUserList)) {
                    continue;
                }
                for (UserDTO userDTO : orgUserList) {
                    String userUuid = userDTO.getUuid();
                    LdapName roleName = LdapUtils.newLdapName(userDTO.getId().toString() + "," + baseLdapPath);
                    if (memberSet.contains(roleName)) {
                        curAndUpOrgUserMap.put(userUuid, userDTO.getUsername());
                    }
                }
            }
            curAndUpOrgUserMap.forEach((userId, userName) -> {
                userGroupList.add(new UserGroupDTO(userId, group.getId(), userName));
            });

        }

        return userGroupList;
    }

    @Override
    public List<String> getGroupIdsByOrgId(String orgId) {
        return groupDao.getGroupIdsByOrgId(orgId);
    }

    @Override
    public boolean checkNameExist(String name, String orgId, String groupId) {
        boolean flag = groupDao.checkNameExist(name, orgId, groupId) > 0;
        return !flag;
    }

    @Override
    public void exportTemplate(HttpServletResponse response) throws IOException {

        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????");
        headList.add("????????????");
        headList.add("??????");
        // ????????????
        requiredList.add("????????????");
        requiredList.add("????????????");
        // ?????????????????????????????????????????????
        List<String> orgNames = userService.getCurrentUserOrgNames();
        exportList.add("??????1???");
        if (CollectionUtils.isNotEmpty(orgNames)) {
            exportList.add(orgNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("??????");
        exportList.add("13658965874");
        exportList.add("??????");

        // ?????????????????????
        Map<String, String[]> selectMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(orgNames)) {
            String[] orgNameArr = new String[orgNames.size()];
            orgNames.toArray(orgNameArr);
            selectMap.put("????????????", orgNameArr);
        }
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        try (OutputStream out = response.getOutputStream()) {
            // ????????????????????????????????????
            export.write(out);
        }
    }

    @Override
    public int getCurrentUserMonitorCount(String id) {
        Set<String> currentUserGroupIds = userService.getCurrentUserGroupIds();
        if (CollectionUtils.isEmpty(currentUserGroupIds) || !currentUserGroupIds.contains(id)) {
            return 0;
        }
        Set<RedisKey> set = new HashSet<>(CommonUtil.ofMapCapacity(currentUserGroupIds.size()));
        for (String groupId : currentUserGroupIds) {
            set.add(RedisKeyEnum.GROUP_MONITOR.of(groupId));
        }
        return RedisHelper.batchGetSet(set).size();
    }

    @Override
    public GroupDTO getMonitorCountById(String id) {
        return groupDao.getMonitorCountById(id);
    }

    @Override
    public List<GroupDTO> getMonitorCountOrgId(String orgDn) {
        List<OrganizationLdap> orgList = organizationService.getOrgChildList(orgDn);
        if (CollectionUtils.isEmpty(orgList)) {
            return new ArrayList<>();
        }
        Set<String> orgIds = orgList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toSet());
        return groupDao.getMonitorCountByOrgId(orgIds, userService.getCurrentUserUuid());
    }

    @Override
    public JSONArray buildTreeNodes(List<GroupDTO> groupList, List<OrganizationLdap> orgList, String type,
        boolean isBigData, boolean needMonitorCount) {
        Map<String, OrganizationLdap> orgMap = AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid);
        JSONArray result = new JSONArray();
        for (GroupDTO group : groupList) {
            if (!orgMap.containsKey(group.getOrgId())) {
                continue;
            }
            JSONObject treeNode = new JSONObject();
            Integer monitorCount = group.getMonitorCount();
            if (needMonitorCount) {
                treeNode.put("count", monitorCount);
            }
            treeNode.put("canCheck", monitorCount);
            Integer onLineMonitorCount = group.getOnLineMonitorCount();
            if (Objects.nonNull(onLineMonitorCount)) {
                treeNode.put("onLine", onLineMonitorCount);
                treeNode.put("offLine", monitorCount - onLineMonitorCount);
            }
            treeNode.put("id", group.getId());
            treeNode.put("pId", orgMap.get(group.getOrgId()).getId().toString());
            treeNode.put("name", group.getName());
            treeNode.put("type", "assignment");
            treeNode.put("iconSkin", "assignmentSkin");
            treeNode.put("pName", orgMap.get(group.getOrgId()).getName());
            if ("single".equals(type)) {
                treeNode.put("nocheck", true);
            }
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (isBigData) {
                treeNode.put("isParent", true);
            }
            result.add(treeNode);
        }
        return result;
    }

    @Override
    public void getGroupMonitorCount(List<GroupDTO> groupList, Map<String, Set<String>> groupMonitorMap,
        Set<String> onLineMonitorIds) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(groupList)) {
            return;
        }
        Set<String> monitorIds;
        int onlineNum;
        for (GroupDTO group : groupList) {
            monitorIds = groupMonitorMap.getOrDefault(group.getId(), new HashSet<>());
            group.setMonitorCount(monitorIds.size());
            if (onLineMonitorIds == null) {
                continue;
            }
            onlineNum = 0;
            for (String monitorId : monitorIds) {
                if (onLineMonitorIds.contains(monitorId)) {
                    onlineNum++;
                }
            }
            group.setOnLineMonitorCount(onlineNum);
        }
    }

    @Override
    public List<GroupDTO> getUserGroupByOrgDn(String orgDn) {
        //??????????????????????????????????????????
        List<OrganizationLdap> orgChildList = organizationService.getOrgChildList(orgDn);
        List<String> orgIdList = orgChildList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        return groupDao.getUserGroupList(orgIdList, userService.getCurrentUserUuid(), null);
    }

    private Map<String, List<OrganizationLdap>> getOrgParentOrgMap(Set<String> orgIds,
        List<OrganizationLdap> allOrgList) {
        //????????????uuid???id???????????????
        Map<String, Name> orgUuidAndIdMap =
            AssembleUtil.collectionToMap(allOrgList, OrganizationLdap::getUuid, OrganizationLdap::getId);

        int initialCapacity = Math.max((int) (orgIds.size() / .75f) + 1, 16);
        Map<String, List<OrganizationLdap>> orgIdAndParentOrgMap = new HashMap<>(initialCapacity);
        for (String orgId : orgIds) {
            if (Objects.isNull(orgUuidAndIdMap.get(orgId))) {
                continue;
            }
            List<OrganizationLdap> currentAndUpOrgList = new ArrayList<>();
            String orgDn = orgUuidAndIdMap.get(orgId).toString();
            //???????????????????????????
            organizationService.getParentOrgList(allOrgList, orgDn, currentAndUpOrgList);
            orgIdAndParentOrgMap.put(orgId, currentAndUpOrgList);
        }
        return orgIdAndParentOrgMap;
    }
}
