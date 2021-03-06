package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.google.common.collect.ImmutableMap;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.BaseKvtDo;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.export.ThingExportDTO;
import com.zw.platform.basic.dto.imports.ThingImportDTO;
import com.zw.platform.basic.dto.result.DeleteThingDTO;
import com.zw.platform.basic.imports.handler.ThingImportHandler;
import com.zw.platform.basic.repository.ThingDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.basicinfo.query.ThingInfoQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ?????????????????????
 * @author XK
 * @date 2020/9/27
 */
@Service("thingService")
@Order(2)
public class ThingServiceImpl implements ThingService, CacheService {
    private static final Logger log = LogManager.getLogger(ThingServiceImpl.class);
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final RedisKeyEnum UNBIND_KEY = RedisKeyEnum.ORG_UNBIND_THING;

    @Autowired
    private ThingDao thingDao;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService orgService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

    @Override
    public boolean add(ThingDTO thingDTO) {
        if (isExistNumber(null, thingDTO.getName())) {
            return false;
        }

        ThingDO thingDO = new ThingDO(thingDTO);
        boolean flag = thingDao.insert(thingDO);
        if (!flag) {
            return false;
        }
        //????????????????????????
        RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(thingDTO), buildFuzzyValue(thingDTO));
        //??????redis?????? ???????????????????????????????????????
        RedisHelper.addToListTop(RedisKeyEnum.THING_SORT_LIST.of(), thingDTO.getId());
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(thingDTO.getId()), buildRedisInfo(thingDTO));

        //??????????????????????????????,???????????????????????????????????????
        if (Objects.equals(Vehicle.BindType.UNBIND, thingDTO.getBindType())) {
            RedisHelper.addToHash(UNBIND_KEY.of(thingDTO.getOrgId()), thingDTO.getId(), thingDTO.getName());
        }
        String log = String.format("???????????????%s( @%s )", thingDTO.getName(), thingDTO.getOrgName());
        logSearchService.addLog(null, log, "3", "", thingDTO.getName(), "");
        return true;
    }

    @Override
    public boolean update(ThingDTO thingDTO) {
        //??????????????????????????????
        String id = thingDTO.getId();
        if (isExistNumber(id, thingDTO.getName())) {
            return false;
        }

        ThingDTO oldThing = thingDao.getDetailById(id);
        if (Objects.isNull(oldThing)) {
            return false;
        }

        ThingDO thingDO = new ThingDO(thingDTO);
        thingDO.convertSavePhoto(fdfsWebServer.getWebServerUrl());
        boolean success = thingDao.update(thingDO);
        if (!success) {
            return false;
        }

        //??????????????????????????????
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), updateMonitorInfo(thingDTO));
        //????????????????????????????????????????????????
        boolean unBind = Objects.equals(oldThing.getBindType(), Vehicle.BindType.UNBIND);
        thingDTO.setBindType(oldThing.getBindType());
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (!Objects.equals(thingDTO.getOrgId(), oldThing.getOrgId()) && unBind) {
            RedisHelper.hdel(UNBIND_KEY.of(oldThing.getOrgId()), id);
            RedisHelper.addToHash(UNBIND_KEY.of(thingDTO.getOrgId()), id, thingDTO.getName());
        }

        //??????????????????????????????????????????????????????
        ThingDTO curThing = thingDao.getDetailById(id);
        boolean thingNumberChange = !Objects.equals(oldThing.getName(), curThing.getName());
        if (thingNumberChange) {
            RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(oldThing));
            RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(curThing), buildFuzzyValue(curThing));

            // ?????????????????????????????????????????????
            final String deviceType = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(id), "deviceType");
            if (StringUtils.isNotEmpty(deviceType)) {
                final RedisKey protocolKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                RedisHelper.addToHash(protocolKey, id, curThing.getName());
            }
        }
        addUpdateLog(oldThing, curThing, thingNumberChange);
        return true;
    }

    private void addUpdateLog(ThingDTO oldThing, ThingDTO curThing, boolean thingNumberChange) {
        String log;
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();
        if (thingNumberChange) {
            log = String.format("???????????????%s( @%s ) ?????????%s( @%s )", oldThing.getName(), orgMap.get(oldThing.getOrgId()),
                curThing.getName(), orgMap.get(curThing.getOrgId()));

        } else {
            log = String.format("???????????????%s( @%s )", oldThing.getName(), orgMap.get(oldThing.getOrgId()));
        }
        logSearchService.addLog(getIpAddress(), log, "3", "", curThing.getName(), "");
    }

    @Override
    public boolean update(String id, String name) {
        return thingDao.updateNumber(id, name);
    }

    @Override
    public List<String> getUserOwnIds(String keyword, List<String> orgIds) {
        // ????????????????????????????????????????????????ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //?????????????????????????????????????????????????????????
        Set<String> unBindIdSet = getUnbindIds(orgIds);

        //??????????????????????????????
        Set<String> userOwnSet = new HashSet<>();
        userOwnSet.addAll(bindIdSet);
        userOwnSet.addAll(unBindIdSet);
        //???????????????????????????????????????????????????????????????????????????ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, userOwnSet, MonitorTypeEnum.THING);
        //?????????????????????
        return sortList(fuzzyVehicleIds, RedisKeyEnum.THING_SORT_LIST);
    }

    /**
     * ????????????????????????ID
     * @param orgIds ??????ID ??????null?????????????????????????????????id
     * @return ??????????????????ID??????
     */
    private Set<String> getUnbindIds(List<String> orgIds) {
        List<String> orgIdList;
        if (CollectionUtils.isNotEmpty(orgIds)) {
            orgIdList = orgIds;
        } else {
            //???????????????????????????ID
            orgIdList =
                userService.getCurrentUseOrgList().stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(orgIdList)) {
            return new HashSet<>();
        }

        Set<String> idSet = new HashSet<>();
        for (String orgId : orgIdList) {
            Set<String> tempSet = RedisHelper.hkeys(UNBIND_KEY.of(orgId));
            if (CollectionUtils.isNotEmpty(tempSet)) {
                idSet.addAll(tempSet);
            }
        }
        return idSet;
    }

    @Override
    public List<String> getUserOwnBindIds(String keyword) {
        //????????????????????????????????????????????????ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //???????????????????????????????????????????????????????????????????????????ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, bindIdSet, MonitorTypeEnum.THING);
        //?????????????????????
        return sortList(fuzzyVehicleIds, RedisKeyEnum.THING_SORT_LIST);
    }

    @Override
    public ThingDTO getById(String id) {
        ThingDTO thingDTO = thingDao.getDetailById(id);
        if (Objects.isNull(thingDTO)) {
            return null;
        }
        Map<String, String> groupMap = userGroupService.getGroupMap();
        Map<String, String> filterGroupMap = filterGroup(thingDTO.getGroupId(), groupMap);
        thingDTO.setGroupId(filterGroupMap.get("groupId"));
        thingDTO.setGroupName(filterGroupMap.get("groupName"));
        //?????????????????????????????????
        thingDTO.convertAccessPhoto(fastDFSClient);

        // ??????????????????
        OrganizationLdap oldOrg = orgService.getOrganizationByUuid(thingDTO.getOrgId());
        thingDTO.setOrgName(oldOrg == null ? "" : oldOrg.getName());
        //?????????????????????????????????
        Map<String, String> typeMap = TypeCacheManger.getInstance().getDictCodeValueMap(DictionaryType.THING_TYPE);
        Map<String, String> categoryMap =
            TypeCacheManger.getInstance().getDictCodeValueMap(DictionaryType.THING_CATEGORY);
        thingDTO.setTypeName(typeMap.get(thingDTO.getType()));
        thingDTO.setCategoryName(categoryMap.get(thingDTO.getCategory()));
        return thingDTO;
    }

    @Override
    public List<ThingDTO> getByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        List<ThingDTO> thingList = thingDao.getDetailByIds(ids);
        Set<String> groupIds = new HashSet<>();
        thingList.forEach(thing -> {
            if (StringUtils.isNotBlank(thing.getGroupId())) {
                groupIds.addAll(Arrays.asList(thing.getGroupId().split(",")));
            }
        });
        Map<String, String> groupMap =
            groupService.getGroupsById(groupIds).stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));

        for (ThingDTO thingDTO : thingList) {
            thingDTO.setOrgName(orgMap.get(thingDTO.getOrgId()));
            Map<String, String> filterGroupMap = filterGroup(thingDTO.getGroupId(), groupMap);
            thingDTO.setGroupId(filterGroupMap.get("groupId"));
            thingDTO.setGroupName(filterGroupMap.get("groupName"));
        }
        return thingList;
    }

    @Override
    public ThingDTO getByName(String monitorName) {
        return thingDao.getDetailByNumber(monitorName);
    }

    @Override
    public boolean delete(String id) {
        ThingDTO thingDTO = thingDao.getDetailById(id);
        //??????????????????????????????
        if (Objects.isNull(thingDTO) || Objects.equals(thingDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
            return false;
        }
        //??????????????????
        int count = thingDao.delete(Collections.singletonList(id));
        if (count <= 0) {
            return false;
        }

        //??????????????????????????????
        RedisHelper.delete(RedisKeyEnum.MONITOR_INFO.of(id));

        //????????????????????????
        RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(thingDTO));

        //????????????
        RedisHelper.delListItem(RedisKeyEnum.THING_SORT_LIST.of(), id);

        //???????????????????????????
        RedisHelper.hdel(UNBIND_KEY.of(thingDTO.getOrgId()), id);

        //????????????????????????
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), id);
        addDeleteLog(thingDTO);
        return true;
    }

    private void addDeleteLog(ThingDTO thingDTO) {
        String orgName = organizationService.getOrgNameByUuid(thingDTO.getOrgId());
        String log = String.format("???????????????%s( @%s )", thingDTO.getName(), orgName);
        logSearchService.addLog(getIpAddress(), log, "3", "", thingDTO.getName(), "");
    }

    @Override
    public int batchDel(Collection<String> ids) {
        List<ThingDTO> thingList = thingDao.getDetailByIds(ids);
        if (CollectionUtils.isEmpty(thingList)) {
            return 0;
        }

        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        List<RedisKey> monitorInfoRedisKey = new ArrayList<>();
        List<String> fuzzyFieldList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgUnbindMap = new HashMap<>(16);
        List<String> unbindList = new ArrayList<>();
        for (ThingDTO thingDTO : thingList) {
            if (Objects.equals(thingDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                continue;
            }
            monitorInfoRedisKey.add(RedisKeyEnum.MONITOR_INFO.of(thingDTO.getId()));
            unbindList.add(thingDTO.getId());
            fuzzyFieldList.add(buildFuzzyField(thingDTO));
            if (!Objects.equals(thingDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                Collection<String> thingIdList;
                if (orgUnbindMap.containsKey(UNBIND_KEY.of(thingDTO.getOrgId()))) {
                    thingIdList = orgUnbindMap.get(UNBIND_KEY.of(thingDTO.getOrgId()));
                } else {
                    thingIdList = new ArrayList<>();
                }
                thingIdList.add(thingDTO.getId());
                orgUnbindMap.put(UNBIND_KEY.of(thingDTO.getOrgId()), thingIdList);
            }
        }
        if (CollectionUtils.isEmpty(unbindList)) {
            return 0;
        }

        //??????????????????
        int count = thingDao.delete(unbindList);
        if (count <= 0) {
            return count;
        }

        //??????sort??????????????????
        RedisHelper.delListItem(RedisKeyEnum.THING_SORT_LIST.of(), unbindList);

        //??????????????????????????????
        RedisHelper.delete(monitorInfoRedisKey);

        //????????????????????????
        RedisHelper.hdel(FUZZY_KEY, fuzzyFieldList);

        //?????????????????????????????????
        RedisHelper.hdel(orgUnbindMap);

        //????????????????????????
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), unbindList);
        addBatchLogs(thingList);
        return count;
    }

    private void addBatchLogs(List<ThingDTO> thingList) {
        StringBuilder logs = new StringBuilder();
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();
        for (ThingDTO thingDTO : thingList) {
            logs.append(String.format("???????????????%s( @%s )</br>", thingDTO.getName(), orgMap.get(thingDTO.getOrgId())));
        }
        logSearchService.addLog(getIpAddress(), logs.toString(), "3", "batch", "??????????????????");
    }

    @Override
    public DeleteThingDTO deleteThingInfoByBatch(String thingIds) {
        List<ThingDTO> details = thingDao.getDetailByIds(Arrays.asList(thingIds.split(",")));
        DeleteThingDTO deleteThingDTO = DeleteThingDTO.getResult(details, vehicleBrandBound);
        if (CollectionUtils.isNotEmpty(deleteThingDTO.getNotBindMonitorIds())) {
            //????????????
            batchDel(deleteThingDTO.getNotBindMonitorIds());
        }
        return deleteThingDTO;
    }

    @Override
    public ThingDO getBaseById(String id) {
        return thingDao.getById(id);
    }

    @Override
    public boolean checkThingNumberSole(String thingNumber, String id) {
        return thingDao.getThingIdByNumberAndId(thingNumber, id) == null;
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList() {
        return getUbBindSelectList(RedisHelper.getList(RedisKeyEnum.THING_SORT_LIST.of()));
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return getUbBindSelectList();
        }

        //?????????????????????????????????ID
        Set<String> fuzzyIds =
            FuzzySearchUtil.scanByMonitor(MonitorTypeEnum.THING.getType(), keyword, Vehicle.BindType.UNBIND);
        if (CollectionUtils.isEmpty(fuzzyIds)) {
            return new ArrayList<>();
        }

        //????????????????????????????????????
        List<String> ids = RedisHelper.getList(RedisKeyEnum.THING_SORT_LIST.of());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<String> sortIds = new ArrayList<>();
        for (String id : ids) {
            if (fuzzyIds.contains(id)) {
                sortIds.add(id);
            }
        }

        return getUbBindSelectList(sortIds);
    }

    private List<Map<String, Object>> getUbBindSelectList(List<String> sortList) {
        if (CollectionUtils.isEmpty(sortList)) {
            return new ArrayList<>();
        }

        // ??????????????????????????????
        List<String> orgIds = userService.getCurrentUserOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }

        //??????????????????????????????
        List<RedisKey> redisKeys = orgIds.stream().map(UNBIND_KEY::of).collect(Collectors.toList());
        Map<String, String> unBindMap = RedisHelper.hgetAll(redisKeys);
        if (unBindMap.isEmpty()) {
            return new ArrayList<>();
        }

        //???????????????????????????????????????
        List<Map<String, Object>> unBindThingList = new ArrayList<>();
        for (String id : sortList) {
            String value = unBindMap.get(id);
            if (StringUtils.isBlank(value)) {
                continue;
            }

            if (value.startsWith("???")) {
                continue;
            }
            //???????????????????????????
            if (Objects.equals(unBindThingList.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER)) {
                break;
            }
            unBindThingList.add(ImmutableMap.of("brand", value, "id", id));
        }
        return unBindThingList;
    }

    @Override
    public void addOrUpdateRedis(List<ThingDTO> monitorList, Set<String> updateIds) {
        if (CollectionUtils.isEmpty(monitorList)) {
            return;
        }

        // ???????????????ID
        List<String> addThingIds = new ArrayList<>();

        // ???????????????????????????????????????????????????????????????????????????
        Map<RedisKey, Collection<String>> delOrgUnbindMap = new HashMap<>(16);

        //???????????????????????????????????????????????????????????????
        Map<RedisKey, Map<String, String>> addOrgUnbindMap = new HashMap<>(16);

        //??????????????????????????????,??????????????????????????????????????????
        List<String> deleteFuzzy = new ArrayList<>();

        //??????????????????????????????
        int initialCapacity = (int) (monitorList.size() / 0.75) + 1;
        Map<String, String> addFuzzyMap = new HashMap<>(initialCapacity);

        //??????????????????????????????map?????????
        Map<RedisKey, Map<String, String>> thingRedisMap = new HashMap<>(initialCapacity);

        for (ThingDTO thing : monitorList) {
            String id = thing.getId();
            //??????????????????????????????
            boolean isAdd = CollectionUtils.isEmpty(updateIds) || !updateIds.contains(id);
            boolean isBind = Objects.equals(thing.getBindType(), Vehicle.BindType.HAS_BIND);
            if (isAdd) {
                addThingIds.add(id);
            }
            RedisKey unBindKey = UNBIND_KEY.of(thing.getOrgId());
            if (!isBind) {
                Map<String, String> unBindMap = addOrgUnbindMap.get(unBindKey);
                if (unBindMap == null) {
                    //???????????????????????????????????????????????????????????????
                    unBindMap = new HashMap<>(initialCapacity);
                }
                unBindMap.put(id, thing.getName());
                addOrgUnbindMap.put(unBindKey, unBindMap);
            }
            if (!isAdd && isBind) {
                deleteFuzzy.add(FuzzySearchUtil.THING_TYPE + thing.getName());

                Collection<String> bindIds = delOrgUnbindMap.get(unBindKey);
                if (bindIds == null) {
                    bindIds = new ArrayList<>();
                }
                bindIds.add(id);
                delOrgUnbindMap.put(unBindKey, bindIds);
            }

            addFuzzyMap.put(buildFuzzyField(thing), buildFuzzyValue(thing));
            thingRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(id), buildRedisInfo(thing));
        }
        RedisHelper.hdel(FUZZY_KEY, deleteFuzzy);
        RedisHelper.hdel(delOrgUnbindMap);
        RedisHelper.addToListTop(RedisKeyEnum.THING_SORT_LIST.of(), addThingIds);
        RedisHelper.batchAddToHash(addOrgUnbindMap);
        RedisHelper.batchAddToHash(thingRedisMap);
        RedisHelper.addToHash(FUZZY_KEY, addFuzzyMap);
    }

    @Override
    public ThingDTO getDefaultInfo(ConfigDTO bindDTO) {
        ThingDTO thing = new ThingDTO();
        BeanUtils.copyProperties(bindDTO, thing);
        if (StringUtils.isBlank(bindDTO.getOrgId())) {
            OrganizationLdap currentUserOrg = userService.getCurUserOrgAdminFirstOrg();
            thing.setOrgId(currentUserOrg.getUuid());
            thing.setOrgName(currentUserOrg.getName());
        }
        if (StringUtils.isBlank(thing.getType())) {
            List<DictionaryDO> categories =
                TypeCacheManger.getInstance().getDictionaryList(DictionaryType.THING_CATEGORY);
            if (!categories.isEmpty()) {
                thing.setCategory(categories.get(0).getCode());
            }
            List<DictionaryDO> typeList = TypeCacheManger.getInstance().getDictionaryList(DictionaryType.THING_TYPE);
            if (!typeList.isEmpty()) {
                thing.setType(typeList.get(0).getCode());
            }
        } else {
            thing.setCategory(bindDTO.getThingCategory());
            thing.setType(bindDTO.getThingType());
        }
        thing.setModel(bindDTO.getThingModel());
        thing.setManufacture(bindDTO.getThingManufacturer());
        return thing;
    }

    @Override
    public MonitorInfo getF3Data(String id) {
        ThingDO thingDO = thingDao.getById(id);
        if (Objects.isNull(thingDO)) {
            return null;
        }

        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setMonitorType(Integer.valueOf(MonitorTypeEnum.THING.getType()));
        monitorInfo.setMonitorId(thingDO.getId());
        monitorInfo.setMonitorName(thingDO.getThingNumber());
        monitorInfo.setGroupId(thingDO.getOrgId());
        OrganizationLdap org = organizationService.getOrganizationByUuid(thingDO.getOrgId());
        if (Objects.nonNull(org)) {
            monitorInfo.setGroupName(org.getName());
        }
        monitorInfo.setLabel(thingDO.getLabel());
        monitorInfo.setMaterial(thingDO.getMaterial());
        monitorInfo.setModel(thingDO.getModel());
        monitorInfo.setWeight(thingDO.getWeight() == null ? null : String.valueOf(thingDO.getWeight()));
        monitorInfo.setSpec(thingDO.getSpec());
        Date productDate = thingDO.getProductDate();
        String productDateStr =
            Objects.isNull(productDate) ? null : DateUtil.getDateToString(productDate, DateFormatKey.YYYY_MM_DD);
        monitorInfo.setProductDate(productDateStr);
        return monitorInfo;
    }

    @Override
    public List<MonitorBaseDTO> getByNames(Collection<String> monitorNames) {
        return thingDao.getByNumbers(monitorNames);
    }

    @Override
    public List<String> getScanByName(String afterName) {
        return thingDao.getScanByNumber(afterName);
    }

    @Override
    public boolean updateIcon(Collection<String> ids, String iconId, String iconName) {
        thingDao.updateIcon(ids, iconId);
        updateIconCache(ids, iconName);
        return true;
    }

    @Override
    public boolean deleteIcon(Collection<String> ids) {
        thingDao.updateIcon(ids, "");
        //????????????????????????
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), ids);
        return true;
    }

    @Override
    public void initIconCache() {
        List<BaseKvtDo<String, String, String>> thingIconList = thingDao.getIconList();
        if (CollectionUtils.isEmpty(thingIconList)) {
            return;
        }
        Map<String, String> iconMap = new HashMap<>(CommonUtil.ofMapCapacity(thingIconList.size()));
        for (BaseKvtDo<String, String, String> icon : thingIconList) {
            iconMap.put(icon.getKeyName(), icon.getFirstValue());
        }
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_ICON.of(), iconMap);
    }

    @Override
    public Page<ThingInfoForm> getListByKeyWord(ThingInfoQuery query) {
        List<String> ids;
        if (StrUtil.isNotBlank(query.getGroupType()) && query.getGroupType().equals("assignment")) {
            Set<String> groupMonitorIds = RedisHelper.getSet(RedisKeyEnum.GROUP_MONITOR.of(query.getGroupName()));
            //???????????????????????????????????????????????????????????????????????????ID
            ids = new ArrayList<>(fuzzyKeyword(query.getSimpleQueryParam(), groupMonitorIds, MonitorTypeEnum.THING));
        } else {
            List<String> orgList = new ArrayList<>();
            if (StrUtil.isNotBlank(query.getGroupName())) {
                orgList.add(query.getGroupName());
            }
            //??????????????????????????????Id
            ids = getUserOwnIds(query.getSimpleQueryParam(), orgList);
        }

        return getPageList(query, ids);
    }


    private Page<ThingInfoForm> getPageList(BaseQueryBean query, List<String> ids) {
        //???????????????????????????id?????????Redis????????????
        RedisKey exportKey = RedisKeyEnum.USER_THING_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        RedisHelper.addToList(exportKey, ids);

        //????????????
        Long start = query.getStart();
        Long end = Math.min(start + query.getLength(), ids.size());
        List<String> subList = ids.subList(start.intValue(), end.intValue());
        List<ThingDTO> thingDTOS = queryAndSort(subList);
        List<ThingInfoForm> result = new ArrayList<>();
        for (ThingDTO thingDTO : thingDTOS) {
            result.add(thingDTO.convert());
        }

        return RedisQueryUtil.getListToPage(result, query, ids.size());
    }

    private List<ThingDTO> queryAndSort(List<String> ids) {

        //?????????????????????
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();

        //?????????????????????????????????
        Map<String, String> groupMap = userGroupService.getGroupMap();

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<ThingDTO> thingDTOList = thingDao.getDetailByIds(ids);
        Map<String, ThingDTO> thingMap = new HashMap<>(16);

        //?????????????????????????????????
        for (ThingDTO thingDTO : thingDTOList) {
            //?????????????????????????????????????????????????????????????????????
            Map<String, String> group = filterGroup(thingDTO.getGroupId(), groupMap);
            thingDTO.setGroupId(group.get("groupId"));
            thingDTO.setGroupName(group.get("groupName"));
            thingDTO.setOrgName(orgMap.get(thingDTO.getOrgId()));
            thingDTO.convertAccessPhoto(fastDFSClient);

            thingMap.put(thingDTO.getId(), thingDTO);
        }

        //????????????
        List<ThingDTO> sortList = new ArrayList<>();
        for (String id : ids) {
            ThingDTO thingDTO = thingMap.get(id);
            if (thingDTO != null) {
                sortList.add(thingDTO);
            }
        }
        return sortList;
    }

    @Override
    public Page<ThingInfoForm> getListByOrg(String orgId, BaseQueryBean query) {
        // ???????????????????????????????????????????????????
        Set<String> bindSet = userService.getCurrentUserMonitorIds();
        Set<String> allSet = new HashSet<>();
        //???????????????????????????????????????
        Set<String> unBindIdSet = getUnbindIds(Collections.singletonList(orgId));
        allSet.addAll(unBindIdSet);
        allSet.addAll(bindSet);
        if (CollectionUtils.isEmpty(allSet)) {
            return new Page<>();
        }
        //???????????????????????????????????????????????????????????????????????????ID
        Set<String> fuzzyThingIds = fuzzyKeyword(query.getSimpleQueryParam(), allSet, MonitorTypeEnum.THING);
        //?????????????????????
        List<String> sortIds = sortList(fuzzyThingIds, RedisKeyEnum.THING_SORT_LIST);
        return getPageList(query, sortIds);
    }

    @Override
    public Page<ThingInfoForm> getListByGroup(Collection<String> groupIds, BaseQueryBean query) {
        // ??????????????????????????????
        Set<String> idSet = groupMonitorService.getMonitorIdsByGroupId(groupIds);

        //???????????????????????????????????????????????????????????????????????????ID
        Set<String> fuzzyThingIds = fuzzyKeyword(query.getSimpleQueryParam(), idSet, MonitorTypeEnum.THING);

        //?????????????????????
        List<String> sortIds = sortList(fuzzyThingIds, RedisKeyEnum.THING_SORT_LIST);
        return getPageList(query, sortIds);
    }

    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel("????????????", ThingExportDTO.class, 1);
        RedisKey exportKey = RedisKeyEnum.USER_THING_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> thingIds = RedisHelper.getList(exportKey);
        List<ThingDTO> things = queryAndSort(thingIds);

        List<ThingExportDTO> exportList = new ArrayList<>();

        Map<String, String> typeMap = TypeCacheManger.getInstance().getDictCodeValueMap("THING_TYPE");
        Map<String, String> categoryMap = TypeCacheManger.getInstance().getDictCodeValueMap("THING_CATEGORY");
        for (ThingDTO info : things) {
            ThingExportDTO form = new ThingExportDTO();

            ConvertUtils.register(form, Date.class);
            BeanUtils.copyProperties(info, form);
            form.setThingNumber(info.getName());
            form.setName(info.getAlias());
            form.setType(typeMap.get(info.getType()));
            form.setCategory(categoryMap.get(info.getCategory()));
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public void initCache() {
        log.info("???????????????????????????redis?????????.");
        //????????????????????????
        List<String> sortList = thingDao.getSortList();
        cleanRedisCache();
        if (sortList.isEmpty()) {
            return;
        }
        RedisHelper.addToListTop(RedisKeyEnum.THING_SORT_LIST.of(), sortList);

        //?????????????????????id????????????????????????map
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        //??????sql????????????????????????mysql???cpu?????????????????????
        List<List<String>> cutSortList = cutList(sortList);
        Map<String, Map<String, String>> orgUnBindThingMap = new HashMap<>(256);
        for (List<String> subList : cutSortList) {
            //?????????????????????
            List<ThingDTO> tempList = thingDao.initCacheList(subList);

            int initialCapacity = (int) (tempList.size() / 0.75) + 1;
            Map<String, String> fuzzyMap = new HashMap<>(initialCapacity);
            Map<String, String> intercomFuzzyMap = new HashMap<>(initialCapacity);
            Map<RedisKey, Map<String, String>> monitorInfoRedisMap = new HashMap<>(initialCapacity);
            //?????????????????????????????????????????????????????????-??????ID-????????????
            Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>(16);
            for (ThingDTO thingDTO : tempList) {
                thingDTO.setOrgName(orgMap.get(thingDTO.getOrgId()));
                //????????????????????????
                fuzzyMap.put(buildFuzzyField(thingDTO), buildFuzzyValue(thingDTO));
                monitorInfoRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(thingDTO.getId()), buildRedisInfo(thingDTO));
                //?????????????????????????????????
                if (StringUtils.isNotBlank(thingDTO.getIntercomDeviceNumber())) {
                    String key = String.format("%s%s&%s&%s", FuzzySearchUtil.PEOPLE_TYPE, thingDTO.getName(),
                        thingDTO.getIntercomDeviceNumber(), thingDTO.getSimCardNumber());
                    intercomFuzzyMap.put(key, buildFuzzyValue(thingDTO));
                }
                if (Objects.equals(thingDTO.getBindType(), Vehicle.BindType.UNBIND)) {
                    Map<String, String> unBindMap =
                        orgUnBindThingMap.getOrDefault(thingDTO.getOrgId(), new HashMap<>(16));
                    unBindMap.put(thingDTO.getId(), thingDTO.getName());
                    orgUnBindThingMap.put(thingDTO.getOrgId(), unBindMap);
                }
                //????????????????????????????????????
                String deviceType = thingDTO.getDeviceType();
                if (StringUtils.isBlank(deviceType)) {
                    continue;
                }
                RedisKey redisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                Map<String, String> monitorMap = protocolMap.getOrDefault(redisKey, new HashMap<>(initialCapacity));
                monitorMap.put(thingDTO.getId(), thingDTO.getName());
                protocolMap.put(redisKey, monitorMap);

            }
            //??????????????????
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of(), fuzzyMap);
            //??????????????????
            RedisHelper.batchAddToHash(monitorInfoRedisMap);
            //????????????????????????
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), intercomFuzzyMap);
            //????????????????????????????????????
            RedisHelper.batchAddToHash(protocolMap);
        }

        //???????????????????????????????????????
        int initialCapacity = (int) (orgUnBindThingMap.size() / 0.75) + 1;
        Map<RedisKey, Map<String, String>> unBindMap = new HashMap<>(initialCapacity);
        for (Map.Entry<String, Map<String, String>> entry : orgUnBindThingMap.entrySet()) {
            unBindMap.put(UNBIND_KEY.of(entry.getKey()), entry.getValue());
        }
        RedisHelper.batchAddToHash(unBindMap);
        log.info("?????????????????????redis?????????.");
    }

    /**
     * ??????????????????redis?????????Map
     * @param thingDTO thingDTO
     * @return Map
     */
    private Map<String, String> buildRedisInfo(ThingDTO thingDTO) {
        BindDTO bindDTO = new BindDTO();
        BeanUtils.copyProperties(thingDTO, bindDTO);
        return MapUtil.objToMap(bindDTO);
    }

    private void cleanRedisCache() {
        //????????????????????????
        RedisHelper.delete(RedisKeyEnum.THING_SORT_LIST.of());
        //??????????????????????????????
        RedisHelper.delByPattern(RedisKeyEnum.ORG_UNBIND_THING_PATTERN.of());
    }

    @Override
    public boolean isExistNumber(String id, String brand) {
        ThingDO thingDO = thingDao.getByBrand(brand);
        return Objects.nonNull(thingDO) && !Objects.equals(id, thingDO.getId());
    }

    @Override
    public boolean isBind(String number) {
        ThingDO thingDO = thingDao.getByBrand(number);
        if (Objects.isNull(thingDO)) {
            return false;
        }
        ThingDTO thingDTO = thingDao.getDetailById(thingDO.getId());
        return Objects.equals(thingDTO.getBindType(), Vehicle.BindType.HAS_BIND);
    }

    private Map<String, String> updateMonitorInfo(ThingDTO thingDTO) {
        Map<String, String> map = new HashMap<>();
        String alias = thingDTO.getAlias();
        map.put("alias", alias == null ? "" : alias);
        map.put("orgId", thingDTO.getOrgId());
        map.put("orgName", thingDTO.getOrgName());
        map.put("name", thingDTO.getName());
        return map;
    }

    @Override
    @MethodLog(name = "????????????????????????", description = "????????????????????????")
    public boolean thingTemplate(HttpServletResponse response) throws Exception {

        List<Object> exportList = new ArrayList<>();
        List<String> headList = getHeadTitleList();

        // ????????????
        List<String> requiredList = getRequireTitle();
        // ????????????????????????
        getDefaultRecorder(exportList);
        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<>(16);
        selectMap.put("????????????", getTypes());
        selectMap.put("????????????", getCategoryNames());

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
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

        return true;
    }

    private String[] getTypes() {
        List<DictionaryDO> typeList = TypeCacheManger.getInstance().getDictionaryList("THING_TYPE");
        String[] types = new String[typeList.size()];
        if (typeList.size() <= 0) {
            types[0] = "????????????";
        } else {
            for (int i = 0; i < typeList.size(); i++) {
                types[i] = typeList.get(i).getValue();
            }
        }
        return types;
    }

    private String[] getCategoryNames() {
        List<DictionaryDO> categoryList = TypeCacheManger.getInstance().getDictionaryList("THING_CATEGORY");
        String[] categoryNames = new String[categoryList.size()];
        if (categoryList.size() > 0) {
            for (int i = 0; i < categoryList.size(); i++) {
                categoryNames[i] = categoryList.get(i).getValue();
            }
        } else {
            categoryNames[0] = "????????????";
        }
        return categoryNames;
    }

    private void getDefaultRecorder(List<Object> exportList) {
        exportList.add("???C001");
        exportList.add("?????????");
        exportList.add("????????????");
        exportList.add("????????????");
        exportList.add("?????????");
        exportList.add("H000-1");
        exportList.add("??????");
        exportList.add("2");
        exportList.add("???");
        exportList.add("?????????");
        exportList.add("?????????");
        exportList.add("??????");
        exportList.add("2018-06-13");
        exportList.add("????????????");
    }

    private List<String> getRequireTitle() {
        List<String> requiredList = new ArrayList<>();
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        return requiredList;
    }

    private List<String> getHeadTitleList() {
        List<String> headList = new ArrayList<>();
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????");
        headList.add("??????");
        headList.add("????????????");
        headList.add("????????????(kg)");
        headList.add("??????");
        headList.add("?????????");
        headList.add("?????????");
        headList.add("??????");
        headList.add("????????????");
        headList.add("??????");
        return headList;
    }

    @Override
    @MethodLog(name = "????????????", description = "????????????")
    @ImportLock(value = ImportModule.THING)
    public JsonResultBean importThingInfo(MultipartFile multipartFile) throws Exception {
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        //????????????
        List<ThingImportDTO> list = importExcel.getDataListNew(ThingImportDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return new JsonResultBean(false, "????????????????????????");
        }

        List<ThingDTO> thingList = transformThingDTO(list);

        ThingImportHandler handler = new ThingImportHandler(thingList, this, thingDao);
        JsonResultBean resultBean;
        try (ImportCache ignored = new ImportCache(ImportModule.THING, SystemHelper.getCurrentUsername(), handler)) {
            resultBean = handler.execute();
            if (!resultBean.isSuccess()) {
                for (int i = 0; i < thingList.size(); i++) {
                    list.get(i).setErrorMsg(thingList.get(i).getErrorMsg());
                }
                ImportErrorUtil.putDataToRedis(list, ImportModule.THING);
                return resultBean;
            }
        }

        logSearchService.addLog(getIpAddress(), "", "3", "batch", "????????????");
        return resultBean;
    }

    @Override
    public boolean addByBatch(List<ThingDO> thingList) {
        return thingDao.addThingInfoByBatch(thingList);
    }

    private List<ThingDTO> transformThingDTO(List<ThingImportDTO> importList) {
        List<ThingDTO> thingList = new ArrayList<>();
        OrganizationLdap curOrg = userService.getCurUserOrgAdminFirstOrg();
        for (ThingImportDTO importDTO : importList) {
            ThingDTO thingDTO = new ThingDTO();
            BeanUtils.copyProperties(importDTO, thingDTO);
            thingDTO.setOrgId(curOrg.getUuid());
            thingDTO.setOrgName(curOrg.getName());
            thingDTO.setName(importDTO.getThingNumber());
            thingDTO.setAlias(importDTO.getName());
            thingDTO.setMonitorType(MonitorTypeEnum.THING.getType());
            thingDTO.setBindType(Vehicle.BindType.UNBIND);
            thingList.add(thingDTO);
        }
        return thingList;
    }
}
