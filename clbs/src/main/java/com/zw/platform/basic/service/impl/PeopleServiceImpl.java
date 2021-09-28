package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.domain.JobDO;
import com.zw.platform.basic.domain.PeopleBasicDO;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.domain.SkillDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.export.IntercomPeopleExportDTO;
import com.zw.platform.basic.dto.export.PeopleExportDTO;
import com.zw.platform.basic.dto.imports.IntercomPeopleImportDTO;
import com.zw.platform.basic.dto.imports.PeopleImportDTO;
import com.zw.platform.basic.dto.query.PeopleQuery;
import com.zw.platform.basic.imports.handler.PeopleImportHandler;
import com.zw.platform.basic.repository.JobDao;
import com.zw.platform.basic.repository.PeopleDao;
import com.zw.platform.basic.repository.SkillManageDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.ImportErrorData;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人员管理实现类
 * @author zhangjuan
 */
@Service("peopleService")
@Order(3)
public class PeopleServiceImpl implements PeopleService, CacheService {
    private static final Logger log = LogManager.getLogger(PeopleServiceImpl.class);
    private static final RedisKeyEnum UNBIND_KEY = RedisKeyEnum.ORG_UNBIND_PEOPLE;
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final int IMPORT_EXCEL_CELL = 7;
    private static final int INTERCOM_IMPORT_EXCEL_CELL = 11;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PeopleDao peopleDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private MessageConfig messageConfig;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private SkillManageDao skillDao;

    @Autowired
    private GroupService groupService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @MethodLog(name = "人员管理redis初始化", description = "人员管理redis初始化")
    @Override
    public void initCache() {
        log.info("开始进行人员管理的的redis初始化.");
        //获取人员的ID，按人员插入时间升序排序
        List<String> sortList = peopleDao.getSortList();

        //清除原有的redis缓存
        clearRedisCache();
        log.info("结束人员管理的的redis缓存的清理.");
        if (sortList.isEmpty()) {
            return;
        }

        //初始化人员顺序缓存
        RedisHelper.addToListTop(RedisKeyEnum.PEOPLE_SORT_LIST.of(), sortList);

        //获取所有的企业id和名称的映射关系map
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));

        //分批次进行人员相关缓存的初始化
        List<List<String>> cutSortList = cutList(sortList);
        Map<RedisKey, Map<String, String>> orgUnBindPeopleMap = new HashMap<>(256);
        for (List<String> subList : cutSortList) {
            List<PeopleDTO> peopleList = peopleDao.initCacheList(subList);

            int initialCapacity = (int) (peopleList.size() / 0.75) + 1;
            //模糊缓存的filed和value的映射map初始化
            Map<String, String> fuzzyMap = new HashMap<>(initialCapacity);
            Map<String, String> intercomFuzzyMap = new HashMap<>(initialCapacity);
            //人员信息缓存键值对的map初始化
            Map<RedisKey, Map<String, String>> peopleRedisMap = new HashMap<>(initialCapacity);
            //协议类型和人员信息的映射关系：协议类型-人员ID-人员编号
            Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>(16);
            //进行封装
            for (PeopleDTO people : peopleList) {
                people.setOrgName(orgMap.get(people.getOrgId()));
                fuzzyMap.put(buildFuzzyField(people), buildFuzzyValue(people));
                peopleRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(people.getId()), buildRedisInfo(people));
                if (Objects.equals(people.getBindType(), Vehicle.BindType.UNBIND)) {
                    Map<String, String> unBindMap = orgUnBindPeopleMap.get(UNBIND_KEY.of(people.getOrgId()));
                    if (unBindMap == null) {
                        unBindMap = new HashMap<>(16);
                    }
                    unBindMap.put(people.getId(), people.getName());
                    orgUnBindPeopleMap.put(UNBIND_KEY.of(people.getOrgId()), unBindMap);
                }

                //维护对讲对象的模糊搜索
                if (StringUtils.isNotBlank(people.getIntercomDeviceNumber())) {
                    String key = String.format("%s%s&%s&%s", FuzzySearchUtil.PEOPLE_TYPE, people.getName(),
                        people.getIntercomDeviceNumber(), people.getSimCardNumber());
                    intercomFuzzyMap.put(key, buildFuzzyValue(people));
                }

                //维护人员与协议类型的关系
                String deviceType = people.getDeviceType();
                if (StringUtils.isNotBlank(deviceType)) {
                    RedisKey redisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                    Map<String, String> monitorMap = protocolMap.getOrDefault(redisKey, new HashMap<>(initialCapacity));
                    monitorMap.put(people.getId(), people.getName());
                    protocolMap.put(redisKey, monitorMap);
                }
            }

            //维护模糊搜索
            RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
            //维护人员信息
            RedisHelper.batchAddToHash(peopleRedisMap);
            //维护对讲模糊搜索
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), intercomFuzzyMap);
            //维护人员与协议类型的关系
            RedisHelper.batchAddToHash(protocolMap);
        }
        //维护未绑定人员信息缓存
        RedisHelper.batchAddToHash(orgUnBindPeopleMap);
        log.info("结束人员管理的的redis初始化.");
    }

    private void clearRedisCache() {
        //删除人员排序缓存
        RedisHelper.delete(RedisKeyEnum.PEOPLE_SORT_LIST.of());
        //删除未绑定的人员缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORG_UNBIND_PEOPLE_PATTERN.of());
    }

    @MethodLog(name = "人员信息添加", description = "人员信息添加")
    @Override
    public boolean add(PeopleDTO peopleDTO) {
        if (isExistNumber(null, peopleDTO.getName())) {
            return false;
        }
        String identityCardPhoto = peopleDTO.getIdentityCardPhoto();
        if (StringUtils.isNotBlank(identityCardPhoto)) {
            peopleDTO.setIdentityCardPhoto(identityCardPhoto.split(fdfsWebServer.getWebServerUrl())[1]);
        }
        peopleDTO.setId(null);
        PeopleDO peopleDO = new PeopleDO(peopleDTO);
        boolean isSuccess = peopleDao.insert(peopleDO);
        if (!isSuccess) {
            return false;
        }
        addBaseInfo(peopleDTO);
        //维护顺序缓存
        String id = peopleDO.getId();
        RedisHelper.addToListTop(RedisKeyEnum.PEOPLE_SORT_LIST.of(), id);

        //维护人员信息缓存
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), buildRedisInfo(peopleDTO));

        //维护组织下未绑定人员缓存
        if (!Objects.equals(Vehicle.BindType.HAS_BIND, peopleDTO.getBindType())) {
            RedisHelper.addToHash(UNBIND_KEY.of(peopleDTO.getOrgId()), id, peopleDTO.getName());
        }

        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(peopleDTO), buildFuzzyValue(peopleDTO));

        String message = "新增人员 :" + peopleDTO.getName() + "(@" + peopleDTO.getOrgName() + ")";
        logService.addLog(getIp(), message, "3", "", peopleDTO.getName(), "");
        return true;
    }

    @MethodLog(name = "人员信息修改", description = "人员信息修改")
    @Override
    public boolean update(PeopleDTO peopleDTO) {
        String id = peopleDTO.getId();
        PeopleDTO oldPeople = peopleDao.getDetailById(id);
        if (Objects.isNull(oldPeople)) {
            return false;
        }
        peopleDTO.setBindType(oldPeople.getBindType());
        peopleDTO.setMonitorType(oldPeople.getMonitorType());

        if (Objects.equals(peopleDTO.getOrgId(), oldPeople.getOrgId())) {
            oldPeople.setOrgName(peopleDTO.getOrgName());
        } else {
            // 获取原来人员的所属企业名称
            OrganizationLdap oldOrg = organizationService.getOrganizationByUuid(oldPeople.getOrgId());
            oldPeople.setOrgName(oldOrg == null ? "" : oldOrg.getName());
        }

        if (isExistNumber(peopleDTO.getId(), peopleDTO.getName())) {
            return false;
        }
        String identityCardPhoto = peopleDTO.getIdentityCardPhoto();
        if (StringUtils.isNotBlank(identityCardPhoto)) {
            peopleDTO.setIdentityCardPhoto(identityCardPhoto.split(fdfsWebServer.getWebServerUrl())[1]);
        }

        //更新人员信息
        PeopleDO peopleDO = new PeopleDO(peopleDTO);
        boolean isSuccess = peopleDao.update(peopleDO);
        if (!isSuccess) {
            return false;
        }

        //不为null的时候做技能和驾照类别的处理，对讲模调用时可以置为空字符串
        if (Objects.nonNull(peopleDTO.getSkillIds()) || Objects.nonNull(peopleDTO.getDriverTypeIds())) {
            peopleDao.deleteBaseInfo(Collections.singletonList(id));
            addBaseInfo(peopleDTO);
        }

        //更新人员信息
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(id), updateMonitorInfo(peopleDTO));

        //更新人员组织下未绑定信息
        boolean unBind = !Objects.equals(oldPeople.getBindType(), Vehicle.BindType.HAS_BIND);
        if (unBind) {
            RedisHelper.addToHash(UNBIND_KEY.of(peopleDTO.getOrgId()), id, peopleDTO.getName());
        }

        //组织发生改变，同步删除原来组织下的未绑定关系
        if (!Objects.equals(oldPeople.getOrgId(), peopleDTO.getOrgId()) && unBind) {
            RedisHelper.hdel(UNBIND_KEY.of(oldPeople.getOrgId()), id);
        }

        RedisHelper.addToHash(FUZZY_KEY, buildFuzzyField(peopleDTO), buildFuzzyValue(peopleDTO));

        final boolean nameChanged = !Objects.equals(oldPeople.getName(), peopleDTO.getName());
        if (nameChanged) {
            // 同步协议下监控对象摘要信息缓存
            final String deviceType = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(id), "deviceType");
            if (StringUtils.isNotEmpty(deviceType)) {
                final RedisKey protocolKey = RedisKeyEnum.MONITOR_PROTOCOL.of(deviceType);
                RedisHelper.addToHash(protocolKey, id, peopleDTO.getName());
            }
        }

        String message = "修改人员 :" + oldPeople.getName() + "(@" + oldPeople.getOrgName() + ")";
        if (nameChanged) {
            message += " 为 : " + peopleDTO.getName() + "(@" + peopleDTO.getOrgName() + ")";
        }
        logService.addLog(getIp(), message, "3", "", peopleDTO.getName(), "");
        return true;
    }

    @MethodLog(name = "人员编号修改", description = "人员编号修改")
    @Override
    public boolean update(String id, String name) {
        PeopleDO peopleDO = new PeopleDO();
        peopleDO.setId(id);
        peopleDO.setPeopleNumber(name);
        peopleDO.setUpdateDataTime(new Date());
        peopleDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return peopleDao.updatePartField(peopleDO);
    }

    @Override
    public List<String> getUserOwnIds(String keyword, List<String> orgIds) {
        //获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();

        //获取用户权限下的企业，以及未绑定的人员ID
        Set<String> unBindIdSet = getUnbindIds(orgIds);

        //用户权限下所有的人员ID
        Set<String> userOwnSet = new HashSet<>();
        userOwnSet.addAll(bindIdSet);
        userOwnSet.addAll(unBindIdSet);

        //进行关键字模糊搜索匹配，并帅选出用户拥有权限下的人员
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, userOwnSet, MonitorTypeEnum.PEOPLE);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.PEOPLE_SORT_LIST);
    }

    private Set<String> getUnbindIds(List<String> orgIds) {
        List<String> orgIdList;
        if (orgIds == null) {
            //获取用户权限的组织ID
            orgIdList =
                userService.getCurrentUseOrgList().stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        } else {
            orgIdList = orgIds;
        }

        if (orgIdList == null || orgIdList.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> unbindIds = new HashSet<>();
        for (String orgId : orgIdList) {
            Set<String> tempSet = RedisHelper.hkeys(UNBIND_KEY.of(orgId));
            if (CollectionUtils.isNotEmpty(tempSet)) {
                unbindIds.addAll(tempSet);
            }
        }
        return unbindIds;
    }

    @Override
    public List<String> getUserOwnBindIds(String keyword) {
        //获取用户权限下所有绑定的监控对象ID
        Set<String> bindIdSet = userService.getCurrentUserMonitorIds();
        //进行关键字模糊搜索匹配，并帅选出用户拥有权限的人员ID
        Set<String> fuzzyVehicleIds = fuzzyKeyword(keyword, bindIdSet, MonitorTypeEnum.PEOPLE);
        //进行排序和过滤
        return sortList(fuzzyVehicleIds, RedisKeyEnum.PEOPLE_SORT_LIST);
    }

    @Override
    public PeopleDTO getById(String id) {
        PeopleDTO people = peopleDao.getDetailById(id);
        if (Objects.isNull(people)) {
            return null;
        }
        Map<String, String> groupMap = null;
        if (StringUtils.isNotBlank(people.getGroupId())) {
            groupMap = getGroupMap();
        }
        completePeople(people, groupMap);

        // 补全企业名称
        OrganizationLdap oldOrg = organizationService.getOrganizationByUuid(people.getOrgId());
        people.setOrgName(oldOrg == null ? "" : oldOrg.getName());
        return people;
    }

    @Override
    public PeopleDTO getByName(String monitorName) {
        PeopleDTO people = peopleDao.getDetailByNumber(monitorName);
        if (Objects.isNull(people)) {
            return null;
        }
        completePeople(people, null);
        return people;
    }

    @MethodLog(name = "人员删除", description = "人员信息删除")
    @Override
    public boolean delete(String id) {
        PeopleDTO people = getById(id);
        if (Objects.isNull(people) || Objects.equals(people.getBindType(), Vehicle.BindType.HAS_BIND)) {
            return false;
        }
        peopleDao.deleteBaseInfo(Collections.singletonList(id));
        peopleDao.delete(Collections.singletonList(id));

        //删除人员排序缓存
        RedisHelper.delListItem(RedisKeyEnum.PEOPLE_SORT_LIST.of(), id);

        //删除人员信息缓存
        RedisHelper.delete(RedisKeyEnum.MONITOR_INFO.of(id));

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, buildFuzzyField(people));

        //删除企业下未绑定人员缓存
        RedisHelper.hdel(UNBIND_KEY.of(people.getOrgId()), id);

        //删除人员图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), id);

        String msg = "删除人员 : " + people.getName() + "( @" + people.getOrgName() + ")";
        logService.addLog(getIp(), msg, "3", "", people.getName(), "");
        return true;
    }

    @MethodLog(name = "人员批量删除", description = "人员信息批量删除")
    @Override
    public JSONObject batchDel(Collection<String> ids) {
        JSONObject re = new JSONObject();
        List<PeopleDTO> peoples = getByIds(ids);
        if (peoples.isEmpty()) {
            return re;
        }

        //封装人员管理相关的缓存，过滤掉已经绑定的监控对象，只能删除未绑定的监控对象
        List<RedisKey> monitorInfoRedisKey = new ArrayList<>();
        List<String> fuzzyFieldList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgUnbindMap = new HashMap<>(16);
        List<String> unbindList = new ArrayList<>();
        StringBuilder msg = new StringBuilder();

        List<String> notBindIds = new ArrayList<>();
        List<String> notBindNames = new ArrayList<>();

        for (PeopleDTO people : peoples) {
            if (Objects.equals(Vehicle.BindType.HAS_BIND, people.getBloodTypeName())) {
                notBindIds.add(people.getId());
                notBindNames.add(people.getName());
                continue;
            }
            String id = people.getId();
            unbindList.add(id);
            monitorInfoRedisKey.add(RedisKeyEnum.MONITOR_INFO.of(id));
            fuzzyFieldList.add(buildFuzzyField(people));
            Collection<String> peopleIds = orgUnbindMap.get(UNBIND_KEY.of(people.getOrgId()));
            if (null == peopleIds) {
                peopleIds = new ArrayList<>();
            }
            peopleIds.add(id);
            orgUnbindMap.put(UNBIND_KEY.of(people.getOrgId()), peopleIds);
            msg.append("删除人员:").append(people.getName()).append("(@").append(people.getOrgName()).append(")<br/>");
        }

        if (unbindList.isEmpty()) {
            return re;
        }
        //删除数据库里的人员信息
        peopleDao.delete(unbindList);
        peopleDao.deleteBaseInfo(unbindList);

        //删除人员排序缓存
        RedisHelper.delListItem(RedisKeyEnum.PEOPLE_SORT_LIST.of(), unbindList);

        //删除监控对象信息缓存
        RedisHelper.delete(monitorInfoRedisKey);

        //删除模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzyFieldList);

        //删除组织下未绑定的人员
        RedisHelper.hdel(orgUnbindMap);

        //删除人员图标缓存
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), unbindList);

        logService.addLog(getIp(), msg.toString(), "3", "batch", "批量删除人员");

        if (notBindIds.isEmpty()) {
            re.put("boundBrands", "");
            re.put("boundBrandIds", "");
            re.put("infoMsg", "");
            return re;
        }
        re.put("boundBrands", String.join(",", notBindIds));
        re.put("boundBrandIds", String.join(",", notBindNames));
        re.put("infoMsg", messageConfig.getVehicleBrandBound());
        return re;
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList() {
        return getUbBindSelectList(RedisHelper.getList(RedisKeyEnum.PEOPLE_SORT_LIST.of()));
    }

    @Override
    public List<Map<String, Object>> getUbBindSelectList(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return getUbBindSelectList();
        }

        //获取模糊搜索的到的车辆ID
        Set<String> fuzzyIds =
            FuzzySearchUtil.scanByMonitor(MonitorTypeEnum.PEOPLE.getType(), keyword, Vehicle.BindType.UNBIND);
        if (CollectionUtils.isEmpty(fuzzyIds)) {
            return new ArrayList<>();
        }

        //把模糊搜索的结果进行排序
        List<String> ids = RedisHelper.getList(RedisKeyEnum.PEOPLE_SORT_LIST.of());
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

        //获取未绑定的人员列表
        List<RedisKey> redisKeys = orgIds.stream().map(UNBIND_KEY::of).collect(Collectors.toList());
        Map<String, String> unBindMap = RedisHelper.hgetAll(redisKeys);
        if (unBindMap.isEmpty()) {
            return new ArrayList<>();
        }

        //对未绑定的人员列表进行排序
        List<Map<String, Object>> unBindPeopleList = new ArrayList<>();
        for (String id : sortList) {
            String value = unBindMap.get(id);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            if (value.startsWith("扫")) {
                continue;
            }
            //限制下拉框返回数量
            if (Objects.equals(unBindPeopleList.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER)) {
                break;
            }
            unBindPeopleList.add(ImmutableMap.of("id", id, "brand", value));
        }
        return unBindPeopleList;
    }

    private String getIp() {
        return new GetIpAddr().getIpAddr(request);
    }

    @Override
    public boolean isExistNumber(String id, String number) {
        PeopleDO peopleDO = peopleDao.getByNumber(number);
        return Objects.nonNull(peopleDO) && !Objects.equals(peopleDO.getId(), id);
    }

    @Override
    public boolean isBind(String number) {
        PeopleDO peopleDO = peopleDao.getByNumber(number);
        if (Objects.isNull(peopleDO)) {
            return false;
        }
        PeopleDTO peopleDTO = peopleDao.getDetailById(peopleDO.getId());
        return Objects.equals(peopleDTO.getBindType(), Vehicle.BindType.HAS_BIND);
    }

    @Override
    public boolean isExistBind(Collection<String> ids) {
        List<PeopleDTO> peoples = peopleDao.getDetailByIds(ids);
        Optional<PeopleDTO> optional =
            peoples.stream().filter(o -> Objects.equals(o.getBindType(), Vehicle.BindType.HAS_BIND)).findFirst();
        return optional.isPresent();
    }

    @Override
    public List<PeopleDTO> getByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<PeopleDTO> peoples = peopleDao.getDetailByIds(ids);
        Set<String> groupIds = new HashSet<>();
        peoples.forEach(people -> {
            if (StringUtils.isNotBlank(people.getGroupId())) {
                groupIds.addAll(Arrays.asList(people.getGroupId().split(",")));
            }
        });
        Map<String, String> groupMap =
            groupService.getGroupsById(groupIds).stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        for (PeopleDTO people : peoples) {
            completePeople(people, groupMap);
            people.setOrgName(orgMap.get(people.getOrgId()));
        }
        return peoples;
    }

    @Override
    public Page<PeopleDTO> getPeopleList(PeopleQuery query) {

        //筛选企业
        if (StringUtils.isNotEmpty(query.getOrgId())) {
            return getListByOrg(query.getOrgId(), query);
        }

        //筛选分组
        if (StringUtils.isNotEmpty(query.getGroupId())) {
            return getListByGroup(Collections.singletonList(query.getGroupId()), query);
        }

        return getListByKeyWord(query);
    }

    @Override
    public Page<PeopleDTO> getListByKeyWord(PeopleQuery query) {
        //获取用户权限的人员Id
        List<String> ids = getUserOwnIds(query.getSimpleQueryParam(), null);
        return sortPeopleList(ids, query);
    }

    @Override
    public Page<PeopleDTO> getListByOrg(String orgId, PeopleQuery query) {
        //获取组织下未绑定的监控对象
        Set<String> unBindIdSet = getUnbindIds(Collections.singletonList(orgId));

        //获取组织下的分组及分组下的监控对象
        Set<String> bindSet = groupMonitorService.getMonitorIdsByOrgId(Collections.singletonList(orgId));

        Set<String> allSet = new HashSet<>();
        allSet.addAll(unBindIdSet);
        allSet.addAll(bindSet);

        //进行关键字模糊搜索匹配，并筛选出用户拥有权限的人员ID
        Set<String> fuzzyIds = fuzzyKeyword(query.getSimpleQueryParam(), allSet, MonitorTypeEnum.PEOPLE);

        //进行排序和过滤
        List<String> sortIds = sortList(fuzzyIds, RedisKeyEnum.PEOPLE_SORT_LIST);

        return sortPeopleList(sortIds, query);
    }

    @Override
    public Page<PeopleDTO> getListByGroup(List<String> groupIds, PeopleQuery query) {
        // 获取分组下的监控对象
        Set<String> idSet = groupMonitorService.getMonitorIdsByGroupId(groupIds);

        //进行关键字模糊搜索匹配，并筛选出用户拥有权限的人员ID
        Set<String> fuzzyIds = fuzzyKeyword(query.getSimpleQueryParam(), idSet, MonitorTypeEnum.PEOPLE);
        //进行排序和过滤
        List<String> sortIds = sortList(fuzzyIds, RedisKeyEnum.PEOPLE_SORT_LIST);
        return sortPeopleList(sortIds, query);
    }

    @MethodLog(name = "模板下载", description = "人员导入模板下载")
    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        headList.add("编号");
        headList.add("性别");
        headList.add("姓名");
        headList.add("身份证号");
        headList.add("电话");
        headList.add("邮箱");
        headList.add("备注");

        //获取模板的必填字段
        List<String> requiredList = new ArrayList<>();
        requiredList.add("编号");

        //获取样例数据
        List<Object> exportList = new ArrayList<>();
        exportList.add("zhangsan");
        exportList.add("男");
        exportList.add("张三");
        exportList.add("500101199106157521");
        exportList.add("13600011233");
        exportList.add("501587058@qq.com");
        exportList.add("人员信息");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);
        String[] sex = { "男", "女" };
        selectMap.put("性别", sex);

        //写入文件
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @MethodLog(name = "模板下载", description = "对讲模块人员导入模板下载")
    @Override
    public boolean generateIntercomTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        headList.add("监控对象");
        headList.add("职位");
        headList.add("技能");
        headList.add("驾照");
        headList.add("资格证");
        headList.add("血型");
        headList.add("身份证");
        headList.add("民族");
        headList.add("性别");
        headList.add("联系电话");
        headList.add("备注");
        // 必填字段
        List<String> requiredList = new ArrayList<>();
        requiredList.add("监控对象");
        requiredList.add("职位");

        Map<String, String[]> selectMap = getSelectMap();

        // 默认设置一条数据
        List<Object> exportList = new ArrayList<>();
        exportList.add("Susan");
        exportList.add(getArrFirstValue(selectMap.get("职位"), ""));
        exportList.add(getArrFirstValue(selectMap.get("技能"), ""));
        exportList.add(getArrFirstValue(selectMap.get("驾照"), ""));
        exportList.add(getArrFirstValue(selectMap.get("资格证"), ""));
        exportList.add(getArrFirstValue(selectMap.get("血型"), ""));
        exportList.add("500223199006110617");
        exportList.add(getArrFirstValue(selectMap.get("民族"), ""));
        exportList.add("男");
        exportList.add("13600011233");
        exportList.add("人员信息");

        //写入文件
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }


    @MethodLog(name = "导出", description = "人员信息导出")
    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        ExportExcelUtil.setResponseHead(response, "人员列表");
        RedisKey exportKey = RedisKeyEnum.USER_PEOPLE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> peopleIds = RedisHelper.getList(exportKey);
        List<PeopleDTO> exportList = peopleDao.getDetailByIds(peopleIds);
        List<PeopleExportDTO> peopleExportList = new ArrayList<>();
        if (!exportList.isEmpty()) {
            for (PeopleDTO people : exportList) {
                PeopleExportDTO peopleExport = new PeopleExportDTO(people);
                peopleExportList.add(peopleExport);
            }
        }
        ExportExcel export = new ExportExcel(null, PeopleExportDTO.class, 1);
        export.setDataList(peopleExportList);
        // 输出导文件
        OutputStream out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
        return true;
    }

    @MethodLog(name = "导出", description = "对讲模块人员信息导出")
    @Override
    public boolean exportIntercomPeople(HttpServletResponse response) throws Exception {
        ExportExcelUtil.setResponseHead(response, "人员列表");
        //获取用户权限下的人员id
        List<String> peopleIds = getUserOwnIds(null, null);
        List<IntercomPeopleExportDTO> peopleExportList = new ArrayList<>();

        if (!peopleIds.isEmpty()) {
            //获取用户权限下的分组信息
            Map<String, String> groupMap = getGroupMap();
            //批量进行获取数据
            List<List<String>> cutList = cutList(peopleIds);
            for (List<String> subList : cutList) {
                Map<String, IntercomPeopleExportDTO> exportMap = new HashMap<>((int) (subList.size() / 0.75) + 1);
                List<PeopleDTO> peoples = peopleDao.getDetailByIds(subList);
                for (PeopleDTO people : peoples) {
                    completePeople(people, groupMap);
                    IntercomPeopleExportDTO peopleExport = new IntercomPeopleExportDTO(people);
                    exportMap.put(people.getId(), peopleExport);
                }
                //按sort顺序进行排序
                for (String id : subList) {
                    peopleExportList.add(exportMap.get(id));
                }
            }
        }

        //数据写入文件
        ExportExcel export = new ExportExcel(null, IntercomPeopleExportDTO.class, 1);
        export.setDataList(peopleExportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }

    @MethodLog(name = "批量导入", description = "人员信息批量导入-主干")
    @ImportLock(ImportModule.PEOPLE)
    @Override
    public JsonResultBean importExcel(MultipartFile file) throws Exception {
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        if (importExcel.getRow(0).getLastCellNum() != IMPORT_EXCEL_CELL) {
            return new JsonResultBean(false, "人员信息导入模板不正确！");
        }
        List<PeopleImportDTO> importList = importExcel.getDataListNew(PeopleImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(false, "导入数据不能为空");
        }

        //导入实体转换成PeopleDTO实体
        List<PeopleDTO> peopleList = convertPeopleDTO(importList);

        // 进行导入
        JsonResultBean resultBean = importData(importList, peopleList);
        if (!resultBean.isSuccess()) {
            return resultBean;
        }
        addImportLog(peopleList);
        return new JsonResultBean(true, String.format("导入结果： 成功导入%d条数据<br/>", peopleList.size()));
    }

    @MethodLog(name = "批量导入", description = "人员信息批量导入-对讲模块")
    @ImportLock(ImportModule.PEOPLE)
    @Override
    public JsonResultBean importIntercomExcel(MultipartFile file) throws Exception {
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        if (importExcel.getRow(0).getLastCellNum() != INTERCOM_IMPORT_EXCEL_CELL) {
            return new JsonResultBean(false, "人员信息导入模板不正确！");
        }
        List<IntercomPeopleImportDTO> importList = importExcel.getDataListNew(IntercomPeopleImportDTO.class);
        if (CollectionUtils.isEmpty(importList)) {
            return new JsonResultBean(false, "导入数据不能为空");
        }

        List<PeopleDTO> peopleList = convertToPeopleDTO(importList);
        // 进行导入
        JsonResultBean resultBean = importData(importList, peopleList);
        if (!resultBean.isSuccess()) {
            return resultBean;
        }
        addImportLog(peopleList);
        return new JsonResultBean(true, String.format("导入结果： 成功导入%d条数据<br/>", peopleList.size()));
    }

    @Override
    public boolean addBatch(List<PeopleDO> peopleList) {
        return peopleDao.addByBatch(peopleList);
    }

    @Override
    public boolean updateIncumbency(String id, Integer isIncumbency) {
        PeopleDO peopleDO = new PeopleDO();
        peopleDO.setId(id);
        peopleDO.setIsIncumbency(isIncumbency);
        peopleDO.setUpdateDataTime(new Date());
        peopleDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return peopleDao.updatePartField(peopleDO);
    }

    @Override
    public boolean updateIncumbency(Collection<String> ids, Integer isIncumbency) {
        PeopleDO peopleDO = new PeopleDO();
        peopleDO.setIsIncumbency(isIncumbency);
        peopleDO.setUpdateDataTime(new Date());
        peopleDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return peopleDao.updateByBatch(ids, peopleDO);
    }

    @Override
    public void addOrUpdateRedis(List<PeopleDTO> peopleList, Set<String> updateIds) {
        if (CollectionUtils.isEmpty(peopleList)) {
            return;
        }

        // 新增的人员ID
        List<String> addPeopleIds = new ArrayList<>();

        // 未绑定转换成绑定关系时，删除组织下未绑定的监控对象
        Map<RedisKey, Collection<String>> delOrgUnbindMap = new HashMap<>(16);

        //新增未绑定的人员时，添加组织未绑定缓存关系
        Map<RedisKey, Map<String, String>> addOrgUnbindMap = new HashMap<>(16);

        //未绑定人员进行绑定时,需要先删除人员原有的模糊搜索
        List<String> deleteFuzzy = new ArrayList<>();

        //新增的模糊搜索键值对
        int initialCapacity = (int) (peopleList.size() / 0.75) + 1;
        Map<String, String> addFuzzyMap = new HashMap<>(initialCapacity);

        //人员信息缓存键值对的map初始化
        Map<RedisKey, Map<String, String>> peopleRedisMap = new HashMap<>(initialCapacity);

        for (PeopleDTO people : peopleList) {
            String id = people.getId();
            //判断人员是否属于新增
            boolean isAdd = CollectionUtils.isEmpty(updateIds) || !updateIds.contains(id);
            boolean isBind = Objects.equals(people.getBindType(), Vehicle.BindType.HAS_BIND);
            if (isAdd) {
                addPeopleIds.add(id);
            }
            RedisKey unBindKey = UNBIND_KEY.of(people.getOrgId());
            if (!isBind) {
                Map<String, String> unBindMap = addOrgUnbindMap.getOrDefault(unBindKey, new HashMap<>(initialCapacity));
                unBindMap.put(id, people.getName());
                addOrgUnbindMap.put(unBindKey, unBindMap);
            }
            if (!isAdd && isBind) {
                deleteFuzzy.add(FuzzySearchUtil.PEOPLE_TYPE + people.getName());

                Collection<String> bindIds = delOrgUnbindMap.get(unBindKey);
                if (bindIds == null) {
                    bindIds = new ArrayList<>();
                }
                bindIds.add(id);
                delOrgUnbindMap.put(unBindKey, bindIds);
            }

            addFuzzyMap.put(buildFuzzyField(people), buildFuzzyValue(people));
            peopleRedisMap.put(RedisKeyEnum.MONITOR_INFO.of(id), buildRedisInfo(people));
        }
        RedisHelper.hdel(FUZZY_KEY, deleteFuzzy);
        RedisHelper.hdel(delOrgUnbindMap);
        RedisHelper.addToListTop(RedisKeyEnum.PEOPLE_SORT_LIST.of(), addPeopleIds);
        RedisHelper.batchAddToHash(addOrgUnbindMap);
        RedisHelper.batchAddToHash(peopleRedisMap);
        RedisHelper.addToHash(FUZZY_KEY, addFuzzyMap);
    }

    @Override
    public PeopleDTO getDefaultInfo(ConfigDTO bindDTO) {
        PeopleDTO people = new PeopleDTO();
        BeanUtils.copyProperties(bindDTO, people);
        if (StringUtils.isBlank(bindDTO.getOrgId())) {
            OrganizationLdap currentUserOrg = userService.getCurUserOrgAdminFirstOrg();
            people.setOrgId(currentUserOrg.getUuid());
            people.setOrgName(currentUserOrg.getName());
        }
        if (StringUtils.isBlank(people.getGender())) {
            people.setGender(GenderEnum.MALE.getCode());
        }
        people.setJobId("default");
        people.setIsIncumbency(1);
        if (StringUtils.isNotBlank(bindDTO.getIntercomDeviceNumber())) {
            people.setIsIncumbency(2);
        }
        return people;
    }

    @Override
    public MonitorInfo getF3Data(String id) {
        PeopleDO peopleDO = peopleDao.getById(id);
        if (Objects.isNull(peopleDO)) {
            return null;
        }
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setMonitorType(Integer.valueOf(MonitorTypeEnum.PEOPLE.getType()));
        monitorInfo.setMonitorId(peopleDO.getId());
        monitorInfo.setMonitorName(peopleDO.getPeopleNumber());
        monitorInfo.setIdentity(peopleDO.getIdentity());
        monitorInfo.setGroupId(peopleDO.getOrgId());
        OrganizationLdap org = organizationService.getOrganizationByUuid(peopleDO.getOrgId());
        if (Objects.nonNull(org)) {
            monitorInfo.setGroupName(org.getName());
        }
        return monitorInfo;
    }

    @Override
    public List<MonitorBaseDTO> getByNames(Collection<String> monitorNames) {
        return peopleDao.getByNumbers(monitorNames);
    }

    @Override
    public List<String> getScanByName(String afterName) {
        return peopleDao.getScanByNumber(afterName);
    }

    @Override
    public boolean updateIcon(Collection<String> ids, String iconId, String iconName) {
        peopleDao.updateIcon(ids, iconId);
        updateIconCache(ids, iconName);
        return true;
    }

    @Override
    public boolean deleteIcon(Collection<String> ids) {
        peopleDao.updateIcon(ids, "");
        RedisHelper.hdel(RedisKeyEnum.MONITOR_ICON.of(), ids);
        return true;
    }

    @Override
    public void initIconCache() {
        List<PeopleDTO> peopleList = peopleDao.getIconList();
        if (CollectionUtils.isEmpty(peopleList)) {
            return;
        }
        Map<String, String> iconMap = new HashMap<>(CommonUtil.ofMapCapacity(peopleList.size()));
        for (PeopleDTO peopleDTO : peopleList) {
            iconMap.put(peopleDTO.getId(), peopleDTO.getIconName());
        }
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_ICON.of(), iconMap);
    }

    private void addImportLog(List<PeopleDTO> peopleList) {
        StringBuilder message = new StringBuilder();
        for (PeopleDTO people : peopleList) {
            message.append(String.format("导入人员 : %s  ( @%s ) <br/>", people.getName(), people.getOrgName()));
        }
        logService.addLog(getIp(), message.toString(), "3", "batch", "导入人员信息");
    }

    private <T extends ImportErrorData> JsonResultBean importData(List<T> importList, List<PeopleDTO> peopleList) {
        PeopleImportHandler handler = new PeopleImportHandler(peopleList, peopleDao, this);
        JsonResultBean jsonResultBean;
        try (ImportCache ignored = new ImportCache(ImportModule.PEOPLE, SystemHelper.getCurrentUsername(), handler)) {
            jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                for (int i = 0; i < peopleList.size(); i++) {
                    importList.get(i).setErrorMsg(peopleList.get(i).getErrorMsg());
                }
                ImportErrorUtil.putDataToRedis(importList, ImportModule.PEOPLE);
                return jsonResultBean;
            }
        }
        return jsonResultBean;
    }

    private List<PeopleDTO> convertPeopleDTO(List<PeopleImportDTO> importList) {
        List<PeopleDTO> peopleList = new ArrayList<>();
        OrganizationLdap org = userService.getCurUserOrgAdminFirstOrg();
        for (PeopleImportDTO people : importList) {
            PeopleDTO peopleDTO = new PeopleDTO();
            BeanUtils.copyProperties(people, peopleDTO);
            peopleDTO.setName(people.getPeopleNumber());
            peopleDTO.setAlias(people.getName());
            peopleDTO.setBindType(Vehicle.BindType.UNBIND);
            peopleDTO.setMonitorType(MonitorTypeEnum.PEOPLE.getType());
            peopleDTO.setOrgName(org.getName());
            peopleDTO.setOrgId(org.getUuid());
            peopleList.add(peopleDTO);
        }
        return peopleList;
    }

    private List<PeopleDTO> convertToPeopleDTO(List<IntercomPeopleImportDTO> importList) {
        List<PeopleDTO> peopleList = new ArrayList<>();
        Map<String, String> nationMap = cacheManger.getDictValueIdMap(DictionaryType.NATION);
        Map<String, String> driverTypeMap = cacheManger.getDictValueIdMap(DictionaryType.DRIVER_LICENSE_CATEGORY);
        Map<String, String> qualificationMap = cacheManger.getDictValueIdMap(DictionaryType.CERTIFICATION_CATEGORY);
        Map<String, String> bloodTypeMap = cacheManger.getDictValueIdMap(DictionaryType.BLOOD_TYPE);
        Map<String, String> jobMap = AssembleUtil.collectionToMap(jobDao.getAllJob(), JobDO::getJobName, JobDO::getId);
        Map<String, String> skillMap =
            AssembleUtil.collectionToMap(skillDao.getAllSkill(), SkillDO::getName, SkillDO::getId);
        OrganizationLdap org = userService.getCurUserOrgAdminFirstOrg();
        for (IntercomPeopleImportDTO people : importList) {
            PeopleDTO peopleDTO = new PeopleDTO();
            BeanUtils.copyProperties(people, peopleDTO);
            peopleDTO.setName(people.getPeopleNumber());
            peopleDTO.setBindType(Vehicle.BindType.UNBIND);
            peopleDTO.setMonitorType(MonitorTypeEnum.PEOPLE.getType());
            peopleDTO.setOrgId(org.getUuid());
            peopleDTO.setOrgName(org.getName());
            String jobId = checkParam(people.getJobName(), jobMap, "职位类别【%s】不存在", peopleDTO);
            peopleDTO.setJobId(jobId);
            if (StringUtils.isNotEmpty(people.getSkillNames())) {
                String skillIds = checkParam(people.getSkillNames(), skillMap, "技能【%s】不存在", peopleDTO);
                peopleDTO.setSkillIds(skillIds);
            }

            if (StringUtils.isNotEmpty(people.getDriverTypeNames())) {
                String driverTypeIds = checkParam(people.getDriverTypeNames(), driverTypeMap, "驾照类别【%s】不存在", peopleDTO);
                peopleDTO.setDriverTypeIds(driverTypeIds);
            }

            if (StringUtils.isNotEmpty(people.getQualification())) {
                //封装资格证书
                String qualificationId =
                    checkParam(people.getQualification(), qualificationMap, "资格证等级【%s】不存在", peopleDTO);
                peopleDTO.setQualificationId(qualificationId);
            }

            if (StringUtils.isNotEmpty(people.getBloodType())) {
                //封装血型
                String bloodType = people.getBloodType();
                String bloodTypeId = checkParam(bloodType, bloodTypeMap, "血型【%s】不存在", peopleDTO);
                peopleDTO.setBloodTypeId(bloodTypeId);
            }

            if (StringUtils.isNotEmpty(people.getNation())) {
                //封装民族
                String nation = people.getNation();
                String nationId = checkParam(nation, nationMap, "民族【%s】不存在", peopleDTO);
                peopleDTO.setNationId(nationId);
            }

            peopleList.add(peopleDTO);
        }
        return peopleList;
    }

    /**
     * 类型参数校验
     * @param param      名称参数字
     * @param valueIdMap value与ID的映射关系
     * @param msgPattern 错误信息匹配字符串
     * @param peopleDTO  人员信息
     * @return 类型参数的ID
     */
    private String checkParam(String param, Map<String, String> valueIdMap, String msgPattern, PeopleDTO peopleDTO) {
        if (StringUtils.isNotBlank(param)) {
            return null;
        }

        //单个类型的参数返回参数ID
        if (!param.contains(",")) {
            String paramId = valueIdMap.get(param);
            if (StringUtils.isBlank(paramId)) {
                peopleDTO.setErrorMsg(String.format(msgPattern, param));
            }
            return paramId;
        }

        //以逗号隔开的参数类型的id值获取
        String[] paramArr = param.split(",");
        List<String> ids = new ArrayList<>();
        for (String item : paramArr) {
            if (!valueIdMap.containsKey(item)) {
                peopleDTO.setErrorMsg(String.format(msgPattern, item));
                break;
            }
            ids.add(valueIdMap.get(item));
        }
        return StringUtils.join(ids, ",");
    }

    private Map<String, String[]> getSelectMap() {
        Map<String, String[]> selectMap = new HashMap<>(16);
        String[] sex = { "男", "女" };
        selectMap.put("性别", sex);

        String[] jobNames = jobDao.getAllJob().stream().map(JobDO::getJobName).toArray(String[]::new);
        selectMap.put("职位", jobNames);

        String[] skillNames = skillDao.getAllSkill().stream().map(SkillDO::getName).toArray(String[]::new);
        selectMap.put("技能", skillNames);

        List<DictionaryDO> driverLicenseType = cacheManger.getDictionaryList(DictionaryType.DRIVER_LICENSE_CATEGORY);
        selectMap.put("驾照", driverLicenseType.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        List<DictionaryDO> certificationCategory = cacheManger.getDictionaryList(DictionaryType.CERTIFICATION_CATEGORY);
        selectMap.put("资格证", certificationCategory.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        List<DictionaryDO> bloodType = cacheManger.getDictionaryList(DictionaryType.BLOOD_TYPE);
        selectMap.put("血型", bloodType.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        List<DictionaryDO> nation = cacheManger.getDictionaryList(DictionaryType.NATION);
        selectMap.put("民族", nation.stream().map(DictionaryDO::getValue).toArray(String[]::new));
        return selectMap;
    }


    private Page<PeopleDTO> sortPeopleList(List<String> ids, BaseQueryBean query) {
        RedisKey exportKey = RedisKeyEnum.USER_PEOPLE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        if (CollectionUtils.isEmpty(ids)) {
            return new Page<>();
        }
        //获取所有的组织ID和name的映射关系
        Map<String, String> orgMap = organizationService.getAllOrganization().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        //获取用户权限的分组信息
        Map<String, String> groupMap = getGroupMap();

        //进行分页
        Long start = query.getStart();
        Long end = Math.min(start + query.getLength(), ids.size());
        if (start >= end) {
            return RedisQueryUtil.getListToPage(new ArrayList<>(), query, ids.size());
        }
        // 满足条件的所有人员ID，存入redis用于导出
        RedisHelper.addToList(exportKey, ids);

        List<String> subList = ids.subList(Integer.valueOf(start + ""), Integer.valueOf(end + ""));
        //从数据库中获取人员信息，查出的结果是未排序的
        List<PeopleDTO> peopleList = peopleDao.getDetailByIds(subList);
        Map<String, PeopleDTO> peopleMap = new HashMap<>(16);
        for (PeopleDTO people : peopleList) {
            completePeople(people, groupMap);
            people.setOrgName(orgMap.get(people.getOrgId()));
            peopleMap.put(people.getId(), people);
        }

        List<PeopleDTO> result = new ArrayList<>();
        for (String id : subList) {
            result.add(peopleMap.get(id));
        }

        return RedisQueryUtil.getListToPage(result, query, ids.size());
    }

    private Map<String, String> getGroupMap() {
        List<GroupDTO> groupList = userService.getCurrentUserGroupList();
        return groupList != null ? groupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName)) :
            new HashMap<>(16);
    }

    /**
     * 添加人员的基础信息--1：技能，2：驾照类别
     * @param people 人员信息
     */
    private void addBaseInfo(PeopleDTO people) {
        List<PeopleBasicDO> peopleBasicList = new ArrayList<>();
        if (StringUtils.isNotBlank(people.getSkillIds())) {
            String[] skillIds = people.getSkillIds().split(",");
            for (String skillId : skillIds) {
                PeopleBasicDO peopleBasicDO = new PeopleBasicDO(people.getId(), skillId, 1);
                peopleBasicList.add(peopleBasicDO);
            }
        }

        if (StringUtils.isNotBlank(people.getDriverTypeIds())) {
            String[] driverTypeIds = people.getDriverTypeIds().split(",");
            for (String driverTypeId : driverTypeIds) {
                PeopleBasicDO peopleBasicDO = new PeopleBasicDO(people.getId(), driverTypeId, 2);
                peopleBasicList.add(peopleBasicDO);
            }
        }

        if (peopleBasicList.isEmpty()) {
            return;
        }
        peopleDao.addBaseInfo(peopleBasicList);
    }

    /**
     * 构建监控对象缓存信息
     * @param peopleDTO 人员信息
     * @return 缓存信息
     */
    private Map<String, String> buildRedisInfo(PeopleDTO peopleDTO) {
        BindDTO bindDTO = new BindDTO();
        peopleDTO.setMonitorType(MonitorTypeEnum.PEOPLE.getType());
        BeanUtils.copyProperties(peopleDTO, bindDTO);
        return MapUtil.objToMap(bindDTO);
    }

    private Map<String, String> updateMonitorInfo(PeopleDTO peopleDTO) {
        Map<String, String> map = new HashMap<>(16);
        String alias = peopleDTO.getAlias();
        map.put("alias", alias == null ? "" : alias);
        map.put("orgId", peopleDTO.getOrgId());
        map.put("orgName", peopleDTO.getOrgName());
        map.put("name", peopleDTO.getName());
        return map;
    }

    private void completePeople(PeopleDTO people, Map<String, String> groupMap) {
        if (StringUtils.isNotBlank(people.getNationId())) {
            people.setNationName(cacheManger.getDictionaryValue(people.getNationId()));
        }

        if (StringUtils.isNotBlank(people.getBloodTypeId())) {
            people.setBloodTypeName(cacheManger.getDictionaryValue(people.getBloodTypeId()));
        }
        if (StringUtils.isNotBlank(people.getQualificationId())) {
            people.setQualificationName(cacheManger.getDictionaryValue(people.getQualificationId()));
        }
        if (StringUtils.isNotBlank(people.getIdentityCardPhoto())) {
            String webServerUrl;
            if (configHelper.isSslEnabled()) {
                webServerUrl = "/";
            } else {
                webServerUrl = fdfsWebServer.getWebServerUrl();
            }
            people.setIdentityCardPhoto(webServerUrl + people.getIdentityCardPhoto());
        }
        //补全分组信息
        if (StringUtils.isNotBlank(people.getGroupId())) {
            Map<String, String> group = filterGroup(people.getGroupId(), groupMap);
            people.setGroupName(group.get("groupName"));
            people.setGroupId(group.get("groupId"));
        }
    }

    /**
     * 根据身份证号查询人员
     * @param identity
     * @return
     */
    @Override
    public PeopleDTO getPeopleByIdentity(String identity) {
        return peopleDao.getPeopleByIdentity(identity);
    }

    /**
     * 判断是否有相同的身份证号的人员
     * @param id
     * @param identity
     * @return
     */
    @Override
    public boolean isExistIdentity(String id, String identity) {
        PeopleDTO peopleDTO = peopleDao.getPeopleByIdentity(identity);
        return Objects.nonNull(peopleDTO) && !Objects.equals(peopleDTO.getId(), id);
    }

    @Override
    public PeopleDTO getByDeviceNum(String deviceNum) {
        PeopleDTO peopleDTO = peopleDao.getByDeviceNum(deviceNum);
        if (Objects.nonNull(peopleDTO) && StringUtils.isNotBlank(peopleDTO.getOrgId())) {
            RedisKey redisKey = RedisKeyEnum.ORGANIZATION_INFO.of(peopleDTO.getOrgId());
            String orgName = RedisHelper.hget(redisKey, "name");
            peopleDTO.setOrgName(orgName);
        }
        return peopleDTO;
    }
}
