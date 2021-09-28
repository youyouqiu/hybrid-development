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
 * 车辆管理实现类
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
        //维护模糊查询缓存
        RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(thingDTO), buildFuzzyValue(thingDTO));
        //维护redis缓存 维护顺序和监控对象信息缓存
        RedisHelper.addToListTop(RedisKeyEnum.THING_SORT_LIST.of(), thingDTO.getId());
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(thingDTO.getId()), buildRedisInfo(thingDTO));

        //检查监控对象是否绑定,未绑定维护企业下未绑定缓存
        if (Objects.equals(Vehicle.BindType.UNBIND, thingDTO.getBindType())) {
            RedisHelper.addToHash(UNBIND_KEY.of(thingDTO.getOrgId()), thingDTO.getId(), thingDTO.getName());
        }
        String log = String.format("新增物品：%s( @%s )", thingDTO.getName(), thingDTO.getOrgName());
        logSearchService.addLog(null, log, "3", "", thingDTO.getName(), "");
        return true;
    }

    @Override
    public boolean update(ThingDTO thingDTO) {
        //检查物品编号是否重复
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

        //更新监控对象信息缓存
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), updateMonitorInfo(thingDTO));
        //未绑定，同步更新组织下未绑定车辆
        boolean unBind = Objects.equals(oldThing.getBindType(), Vehicle.BindType.UNBIND);
        thingDTO.setBindType(oldThing.getBindType());
        //组织发生改变，同步删除原来组织下的未绑定关系，新增新的组织下未绑定关系
        if (!Objects.equals(thingDTO.getOrgId(), oldThing.getOrgId()) && unBind) {
            RedisHelper.hdel(UNBIND_KEY.of(oldThing.getOrgId()), id);
            RedisHelper.addToHash(UNBIND_KEY.of(thingDTO.getOrgId()), id, thingDTO.getName());
        }

        //若物品标号发生改变，同步维护模糊查询
        ThingDTO curThing = thingDao.getDetailById(id);
        boolean thingNumberChange = !Objects.equals(oldThing.getName(), curThing.getName());
        if (thingNumberChange) {
            RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(oldThing));
            RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(curThing), buildFuzzyValue(curThing));

            // 同步协议下监控对象摘要信息缓存
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
            log = String.format("修改物品：%s( @%s ) 修改为%s( @%s )", oldThing.getName(), orgMap.get(oldThing.getOrgId()),
                curThing.getName(), orgMap.get(curThing.getOrgId()));

        } else {
            log = String.format("修改物品：%s( @%s )", oldThing.getName(), orgMap.get(oldThing.getOrgId()));
        }
        logSearchService.addLog(getIpAddress(), log, "3", "", curThing.getName(), "");
    }

    @Override
    public boolean update(String id, String name) {
        return thingDao.updateNumber(id, name);
    }

    @Override
    public List<String> getUserOwnIds(String keyword, List<String> orgIds) {
        // 获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //获取用户权限下的企业，以及未绑定的物品
        Set<String> unBindIdSet = getUnbindIds(orgIds);

        //用户权限下所有的物品
        Set<String> userOwnSet = new HashSet<>();
        userOwnSet.addAll(bindIdSet);
        userOwnSet.addAll(unBindIdSet);
        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的物品ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, userOwnSet, MonitorTypeEnum.THING);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.THING_SORT_LIST);
    }

    /**
     * 获取未绑定的物品ID
     * @param orgIds 组织ID 若为null，获取用户权限下的组织id
     * @return 未绑定的物品ID集合
     */
    private Set<String> getUnbindIds(List<String> orgIds) {
        List<String> orgIdList;
        if (CollectionUtils.isNotEmpty(orgIds)) {
            orgIdList = orgIds;
        } else {
            //获取用户权限的组织ID
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
        //获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的物品ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, bindIdSet, MonitorTypeEnum.THING);
        //进行排序和过滤
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
        //补全物品相关图片的路径
        thingDTO.convertAccessPhoto(fastDFSClient);

        // 补全企业名称
        OrganizationLdap oldOrg = orgService.getOrganizationByUuid(thingDTO.getOrgId());
        thingDTO.setOrgName(oldOrg == null ? "" : oldOrg.getName());
        //补全类型名称和类别名称
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
        //已经绑定的不允许删除
        if (Objects.isNull(thingDTO) || Objects.equals(thingDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
            return false;
        }
        //进行物品删除
        int count = thingDao.delete(Collections.singletonList(id));
        if (count <= 0) {
            return false;
        }

        //删除监控对象信息缓存
        RedisHelper.delete(RedisKeyEnum.MONITOR_INFO.of(id));

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(thingDTO));

        //删除顺序
        RedisHelper.delListItem(RedisKeyEnum.THING_SORT_LIST.of(), id);

        //同步删除未绑定缓存
        RedisHelper.hdel(UNBIND_KEY.of(thingDTO.getOrgId()), id);

        //删除物品图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), id);
        addDeleteLog(thingDTO);
        return true;
    }

    private void addDeleteLog(ThingDTO thingDTO) {
        String orgName = organizationService.getOrgNameByUuid(thingDTO.getOrgId());
        String log = String.format("删除物品：%s( @%s )", thingDTO.getName(), orgName);
        logSearchService.addLog(getIpAddress(), log, "3", "", thingDTO.getName(), "");
    }

    @Override
    public int batchDel(Collection<String> ids) {
        List<ThingDTO> thingList = thingDao.getDetailByIds(ids);
        if (CollectionUtils.isEmpty(thingList)) {
            return 0;
        }

        //封装物品管理相关的缓存，过滤掉已经绑定的监控对象，只能删除未绑定的监控对象
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

        //进行物品删除
        int count = thingDao.delete(unbindList);
        if (count <= 0) {
            return count;
        }

        //删除sort缓存中的物品
        RedisHelper.delListItem(RedisKeyEnum.THING_SORT_LIST.of(), unbindList);

        //删除监控对象信息缓存
        RedisHelper.delete(monitorInfoRedisKey);

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzyFieldList);

        //删除组织下未绑定的物品
        RedisHelper.hdel(orgUnbindMap);

        //删除物品图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), unbindList);
        addBatchLogs(thingList);
        return count;
    }

    private void addBatchLogs(List<ThingDTO> thingList) {
        StringBuilder logs = new StringBuilder();
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();
        for (ThingDTO thingDTO : thingList) {
            logs.append(String.format("删除物品：%s( @%s )</br>", thingDTO.getName(), orgMap.get(thingDTO.getOrgId())));
        }
        logSearchService.addLog(getIpAddress(), logs.toString(), "3", "batch", "批量删除物品");
    }

    @Override
    public DeleteThingDTO deleteThingInfoByBatch(String thingIds) {
        List<ThingDTO> details = thingDao.getDetailByIds(Arrays.asList(thingIds.split(",")));
        DeleteThingDTO deleteThingDTO = DeleteThingDTO.getResult(details, vehicleBrandBound);
        if (CollectionUtils.isNotEmpty(deleteThingDTO.getNotBindMonitorIds())) {
            //批量删除
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

        //获取模糊搜索的到的车辆ID
        Set<String> fuzzyIds =
            FuzzySearchUtil.scanByMonitor(MonitorTypeEnum.THING.getType(), keyword, Vehicle.BindType.UNBIND);
        if (CollectionUtils.isEmpty(fuzzyIds)) {
            return new ArrayList<>();
        }

        //把模糊搜索的结果进行排序
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

        // 获取用户权限下的组织
        List<String> orgIds = userService.getCurrentUserOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }

        //获取未绑定的物品列表
        List<RedisKey> redisKeys = orgIds.stream().map(UNBIND_KEY::of).collect(Collectors.toList());
        Map<String, String> unBindMap = RedisHelper.hgetAll(redisKeys);
        if (unBindMap.isEmpty()) {
            return new ArrayList<>();
        }

        //对未绑定的物品列表进行排序
        List<Map<String, Object>> unBindThingList = new ArrayList<>();
        for (String id : sortList) {
            String value = unBindMap.get(id);
            if (StringUtils.isBlank(value)) {
                continue;
            }

            if (value.startsWith("扫")) {
                continue;
            }
            //限制下拉框返回数量
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

        // 新增的物品ID
        List<String> addThingIds = new ArrayList<>();

        // 未绑定转换成绑定关系时，删除组织下未绑定的监控对象
        Map<RedisKey, Collection<String>> delOrgUnbindMap = new HashMap<>(16);

        //新增未绑定的物品时，添加组织未绑定缓存关系
        Map<RedisKey, Map<String, String>> addOrgUnbindMap = new HashMap<>(16);

        //未绑定物品进行绑定时,需要先删除物品原有的模糊搜索
        List<String> deleteFuzzy = new ArrayList<>();

        //新增的模糊搜索键值对
        int initialCapacity = (int) (monitorList.size() / 0.75) + 1;
        Map<String, String> addFuzzyMap = new HashMap<>(initialCapacity);

        //物品信息缓存键值对的map初始化
        Map<RedisKey, Map<String, String>> thingRedisMap = new HashMap<>(initialCapacity);

        for (ThingDTO thing : monitorList) {
            String id = thing.getId();
            //判断物品是否属于新增
            boolean isAdd = CollectionUtils.isEmpty(updateIds) || !updateIds.contains(id);
            boolean isBind = Objects.equals(thing.getBindType(), Vehicle.BindType.HAS_BIND);
            if (isAdd) {
                addThingIds.add(id);
            }
            RedisKey unBindKey = UNBIND_KEY.of(thing.getOrgId());
            if (!isBind) {
                Map<String, String> unBindMap = addOrgUnbindMap.get(unBindKey);
                if (unBindMap == null) {
                    //批量导入时，场景大多是属于同一个企业的物品
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
        //删除物品图标缓存
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
            //进行关键字模糊搜索匹配，并帅选出用户拥有权限的物品ID
            ids = new ArrayList<>(fuzzyKeyword(query.getSimpleQueryParam(), groupMonitorIds, MonitorTypeEnum.THING));
        } else {
            List<String> orgList = new ArrayList<>();
            if (StrUtil.isNotBlank(query.getGroupName())) {
                orgList.add(query.getGroupName());
            }
            //获取用户权限的的车辆Id
            ids = getUserOwnIds(query.getSimpleQueryParam(), orgList);
        }

        return getPageList(query, ids);
    }


    private Page<ThingInfoForm> getPageList(BaseQueryBean query, List<String> ids) {
        //所有满足条件的物品id，存入Redis用于导出
        RedisKey exportKey = RedisKeyEnum.USER_THING_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        RedisHelper.addToList(exportKey, ids);

        //进行分页
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

        //获取所有的组织
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();

        //获取用户权限的分组信息
        Map<String, String> groupMap = userGroupService.getGroupMap();

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<ThingDTO> thingDTOList = thingDao.getDetailByIds(ids);
        Map<String, ThingDTO> thingMap = new HashMap<>(16);

        //组装分组信息和企业信息
        for (ThingDTO thingDTO : thingDTOList) {
            //封装用户权限下的分组，非用户权限下的分组过滤掉
            Map<String, String> group = filterGroup(thingDTO.getGroupId(), groupMap);
            thingDTO.setGroupId(group.get("groupId"));
            thingDTO.setGroupName(group.get("groupName"));
            thingDTO.setOrgName(orgMap.get(thingDTO.getOrgId()));
            thingDTO.convertAccessPhoto(fastDFSClient);

            thingMap.put(thingDTO.getId(), thingDTO);
        }

        //进行排序
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
        // 获取组织下的分组及分组下的监控对象
        Set<String> bindSet = userService.getCurrentUserMonitorIds();
        Set<String> allSet = new HashSet<>();
        //获取组织下未绑定的监控对象
        Set<String> unBindIdSet = getUnbindIds(Collections.singletonList(orgId));
        allSet.addAll(unBindIdSet);
        allSet.addAll(bindSet);
        if (CollectionUtils.isEmpty(allSet)) {
            return new Page<>();
        }
        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的车辆ID
        Set<String> fuzzyThingIds = fuzzyKeyword(query.getSimpleQueryParam(), allSet, MonitorTypeEnum.THING);
        //进行排序和过滤
        List<String> sortIds = sortList(fuzzyThingIds, RedisKeyEnum.THING_SORT_LIST);
        return getPageList(query, sortIds);
    }

    @Override
    public Page<ThingInfoForm> getListByGroup(Collection<String> groupIds, BaseQueryBean query) {
        // 获取分组下的监控对象
        Set<String> idSet = groupMonitorService.getMonitorIdsByGroupId(groupIds);

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的物品ID
        Set<String> fuzzyThingIds = fuzzyKeyword(query.getSimpleQueryParam(), idSet, MonitorTypeEnum.THING);

        //进行排序和过滤
        List<String> sortIds = sortList(fuzzyThingIds, RedisKeyEnum.THING_SORT_LIST);
        return getPageList(query, sortIds);
    }

    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel("物品列表", ThingExportDTO.class, 1);
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
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public void initCache() {
        log.info("开始进行物品管理的redis初始化.");
        //维护物品顺序缓存
        List<String> sortList = thingDao.getSortList();
        cleanRedisCache();
        if (sortList.isEmpty()) {
            return;
        }
        RedisHelper.addToListTop(RedisKeyEnum.THING_SORT_LIST.of(), sortList);

        //获取所有的企业id和名称的映射关系map
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        //避免sql慢查询，导致激增mysql的cpu，采用分批处理
        List<List<String>> cutSortList = cutList(sortList);
        Map<String, Map<String, String>> orgUnBindThingMap = new HashMap<>(256);
        for (List<String> subList : cutSortList) {
            //查询物品绑定新
            List<ThingDTO> tempList = thingDao.initCacheList(subList);

            int initialCapacity = (int) (tempList.size() / 0.75) + 1;
            Map<String, String> fuzzyMap = new HashMap<>(initialCapacity);
            Map<String, String> intercomFuzzyMap = new HashMap<>(initialCapacity);
            Map<RedisKey, Map<String, String>> monitorInfoRedisMap = new HashMap<>(initialCapacity);
            //协议类型和物品信息的映射关系：协议类型-人员ID-人员编号
            Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>(16);
            for (ThingDTO thingDTO : tempList) {
                thingDTO.setOrgName(orgMap.get(thingDTO.getOrgId()));
                //封装未绑定的物品
                fuzzyMap.put(buildFuzzyField(thingDTO), buildFuzzyValue(thingDTO));
                monitorInfoRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(thingDTO.getId()), buildRedisInfo(thingDTO));
                //维护对讲对象的模糊搜索
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
                //维护物品与协议类型的关系
                String deviceType = thingDTO.getDeviceType();
                if (StringUtils.isBlank(deviceType)) {
                    continue;
                }
                RedisKey redisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                Map<String, String> monitorMap = protocolMap.getOrDefault(redisKey, new HashMap<>(initialCapacity));
                monitorMap.put(thingDTO.getId(), thingDTO.getName());
                protocolMap.put(redisKey, monitorMap);

            }
            //维护模糊搜索
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of(), fuzzyMap);
            //维护车辆信息
            RedisHelper.batchAddToHash(monitorInfoRedisMap);
            //维护对讲模糊搜索
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), intercomFuzzyMap);
            //维护物品与协议类型的关系
            RedisHelper.batchAddToHash(protocolMap);
        }

        //维护企业下未绑定物品的缓存
        int initialCapacity = (int) (orgUnBindThingMap.size() / 0.75) + 1;
        Map<RedisKey, Map<String, String>> unBindMap = new HashMap<>(initialCapacity);
        for (Map.Entry<String, Map<String, String>> entry : orgUnBindThingMap.entrySet()) {
            unBindMap.put(UNBIND_KEY.of(entry.getKey()), entry.getValue());
        }
        RedisHelper.batchAddToHash(unBindMap);
        log.info("结束物品管理的redis初始化.");
    }

    /**
     * 构建物品信息redis缓存的Map
     * @param thingDTO thingDTO
     * @return Map
     */
    private Map<String, String> buildRedisInfo(ThingDTO thingDTO) {
        BindDTO bindDTO = new BindDTO();
        BeanUtils.copyProperties(thingDTO, bindDTO);
        return MapUtil.objToMap(bindDTO);
    }

    private void cleanRedisCache() {
        //删除物品排序缓存
        RedisHelper.delete(RedisKeyEnum.THING_SORT_LIST.of());
        //删除未绑定的物品缓存
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
    @MethodLog(name = "生成物品列表模板", description = "生成物品列表模板")
    public boolean thingTemplate(HttpServletResponse response) throws Exception {

        List<Object> exportList = new ArrayList<>();
        List<String> headList = getHeadTitleList();

        // 必填字段
        List<String> requiredList = getRequireTitle();
        // 默认设置一条数据
        getDefaultRecorder(exportList);
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);
        selectMap.put("物品类型", getTypes());
        selectMap.put("物品类别", getCategoryNames());

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
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

        return true;
    }

    private String[] getTypes() {
        List<DictionaryDO> typeList = TypeCacheManger.getInstance().getDictionaryList("THING_TYPE");
        String[] types = new String[typeList.size()];
        if (typeList.size() <= 0) {
            types[0] = "其他物品";
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
            categoryNames[0] = "其他物品";
        }
        return categoryNames;
    }

    private void getDefaultRecorder(List<Object> exportList) {
        exportList.add("车C001");
        exportList.add("车载机");
        exportList.add("其他物品");
        exportList.add("其他物品");
        exportList.add("卡仕达");
        exportList.add("H000-1");
        exportList.add("塑料");
        exportList.add("2");
        exportList.add("个");
        exportList.add("卡仕达");
        exportList.add("卡仕达");
        exportList.add("重庆");
        exportList.add("2018-06-13");
        exportList.add("物品信息");
    }

    private List<String> getRequireTitle() {
        List<String> requiredList = new ArrayList<>();
        requiredList.add("物品编号");
        requiredList.add("物品类别");
        requiredList.add("物品类型");
        return requiredList;
    }

    private List<String> getHeadTitleList() {
        List<String> headList = new ArrayList<>();
        headList.add("物品编号");
        headList.add("物品名称");
        headList.add("物品类别");
        headList.add("物品类型");
        headList.add("品牌");
        headList.add("型号");
        headList.add("主要材料");
        headList.add("物品重量(kg)");
        headList.add("规格");
        headList.add("制造商");
        headList.add("经销商");
        headList.add("产地");
        headList.add("生产日期");
        headList.add("备注");
        return headList;
    }

    @Override
    @MethodLog(name = "批量导入", description = "批量导入")
    @ImportLock(value = ImportModule.THING)
    public JsonResultBean importThingInfo(MultipartFile multipartFile) throws Exception {
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        //读取文件
        List<ThingImportDTO> list = importExcel.getDataListNew(ThingImportDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return new JsonResultBean(false, "导入数据不能为空");
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

        logSearchService.addLog(getIpAddress(), "", "3", "batch", "导入物品");
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
