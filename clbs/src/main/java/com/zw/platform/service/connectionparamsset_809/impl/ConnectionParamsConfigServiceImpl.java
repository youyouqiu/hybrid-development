package com.zw.platform.service.connectionparamsset_809.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.basic.domain.GroupMonitorBindDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeRedisInfo;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfigQuery;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardVehicleInfo;
import com.zw.platform.domain.connectionparamsset_809.T809PlatFormSubscribe;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.event.SimNumberUpdateEvent;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsConfigService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConnectionParamsConfigServiceImpl implements ConnectionParamsConfigService {

    private static final Logger logger = LogManager.getLogger(ConnectionParamsConfigServiceImpl.class);
    /**
     * 河南油补协议
     */
    private static final int OIL_SUBSIDY_PROTOCOL_TYPE = 1603;

    @Autowired
    ConnectionParamsConfigDao connectionParamsConfigDao;

    @Autowired
    ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    ServerParamList serverParamList;

    @Autowired
    UserService userService;

    @Autowired
    private GroupMonitorDao groupMonitorDao;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private WebClientHandleCom webClientHandleCom;

    @Autowired
    private DeviceNewDao deviceDao;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @Value("${t809.upload.vehicleInfo.url}")
    private String t809UploadVehicleInfoUrl;

    @Value("${ji.t809.switch}")
    private boolean t809switch;

    @Autowired
    private BusinessScopeService businessScopeService;

    @Override
    public Page<T809ForwardConfig> findConfig(T809ForwardConfigQuery query) throws Exception {
        // 获取用户权限下分组id
        Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername()));
        //存入查询实体
        if (groupIds.size() > 0) {
            query.setAssignList(groupIds);
        }
        final Page<T809ForwardConfig> t809ForwardConfigs =
                PageHelperUtil.doSelect(query, () -> connectionParamsConfigDao.findConfig(query));

        final List<RedisKey> vehicleIds = t809ForwardConfigs.stream()
                .map(T809ForwardConfig::getVehicleId)
                .map(RedisKeyEnum.MONITOR_INFO::of)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(vehicleIds)) {
            final Map<String, String> idOrgNameMap = RedisHelper.batchGetHashMap(vehicleIds, "id", "orgName");
            t809ForwardConfigs.forEach(o -> o.setOrgName(idOrgNameMap.get(o.getVehicleId())));
        }
        return t809ForwardConfigs;
    }

    @Override
    public boolean addConfig(String platFormId, String vehicleIds, String ipAddress, String platFormName,
        String protocolType) throws Exception {
        if (StringUtils.isEmpty(platFormId) || StringUtils.isEmpty(vehicleIds)) {
            return false;
        }
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        //查询该平台下所有绑定的车辆id
        Set<String> bindVehicles = connectionParamsConfigDao.findVehiclesOfPlatform(platFormId);
        //没有绑定的车辆id
        final Set<String> unbindVehicle = new HashSet<>();
        if (bindVehicles == null || bindVehicles.size() == 0) { //该平台下没有任何绑定车辆
            unbindVehicle.addAll(vehicleIdList);
        } else {
            //过滤已绑定的车辆
            for (String id : vehicleIdList) {
                if (!bindVehicles.contains(id)) {
                    unbindVehicle.add(id);
                }
            }
        }
        if (unbindVehicle.size() <= 0) { //若车都被绑定则不做下面处理
            return true;
        }
        Set<String> orgIds = Sets.newHashSet();
        Map<String, BindDTO> deviceAndConfigInfoMap = new HashMap<>(16);
        Map<String, String> vehicleIdAndTerminalManufacturerAndTypeMap = new HashMap<>(16);
        Map<String, Set<String>> vehicleIdAndProfessionalSetMap = new HashMap<>(16);
        Set<String> allProfessionalSet = new HashSet<>();
        Map<String, BindDTO> vehicleInfos = VehicleUtil.batchGetBindInfosByRedis(unbindVehicle);
        StringBuilder sb = new StringBuilder();
        List<T809ForwardConfig> tfcs = new ArrayList<>();
        List<String> sids = Collections.singletonList(platFormId);
        for (String vid : unbindVehicle) {
            BindDTO bindDTO = vehicleInfos.get(vid);
            deviceAndConfigInfoMap.put(bindDTO.getDeviceId(), bindDTO);
            orgIds.add(bindDTO.getOrgId());
            String carLicense = bindDTO.getName();
            String deviceType = bindDTO.getDeviceType();
            String configId = bindDTO.getConfigId();
            T809ForwardConfig t809ForwardConfig = new T809ForwardConfig();
            t809ForwardConfig.setConfigId(configId);
            t809ForwardConfig.setPlantFormId(platFormId);
            t809ForwardConfig.setProtocolType(protocolType);
            tfcs.add(t809ForwardConfig);
            sb.append("监控对象：").append(carLicense).append(" 新增809上级平台（").append(platFormName).append("）转发 </br>");
            T809PlatFormSubscribe subscribe = new T809PlatFormSubscribe();
            subscribe.setIdentification(bindDTO.getSimCardNumber());
            subscribe.setSettingIds(sids);
            subscribe.setProtocolType(deviceType);
            vehicleIdAndTerminalManufacturerAndTypeMap
                .put(vid, bindDTO.getTerminalManufacturer() + "," + bindDTO.getTerminalType());
            String professionalIds = bindDTO.getProfessionalIds();
            if (StringUtils.isNotBlank(professionalIds)) {
                Set<String> professionalIdSet = Arrays.stream(professionalIds.split(",")).collect(Collectors.toSet());
                allProfessionalSet.addAll(professionalIdSet);
                vehicleIdAndProfessionalSetMap.put(vid, professionalIdSet);
            }
            sendMsg(subscribe, ConstantUtil.T809_FORWARD_DEVICE_ADD);
        }
        dataSync(platFormId, vehicleInfos.values(), orgIds, deviceAndConfigInfoMap,
            vehicleIdAndTerminalManufacturerAndTypeMap, vehicleIdAndProfessionalSetMap, allProfessionalSet);
        connectionParamsConfigDao.addConfig(tfcs);
        logSearchService.addLog(ipAddress, sb.toString(), "3", "batch", "新增809监控上级平台转发");
        //调用冀标报警上报接口地址
        if (t809switch && ProtocolTypeUtil.T809_JI_PROTOCOL_809_2013.equals(protocolType)) {
            new Thread(() -> t809UploadVehicleInfo(vehicleInfos.values())).start();
        }
        return true;
    }

    private void dataSync(String platFormId, Collection<BindDTO> unbindVehicle, Set<String> orgIds,
        Map<String, BindDTO> deviceAndConfigInfoMap, Map<String, String> vehicleIdAndTerminalManufacturerAndTypeMap,
        Map<String, Set<String>> vehicleIdAndProfessionalSetMap, Set<String> allProfessionalSet) {
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platFormId);
        Integer protocolType = plantParam.getProtocolType();
        Set<String> monitorIds = unbindVehicle.stream().map(MonitorBaseDTO::getId).collect(Collectors.toSet());
        List<JSONObject> vehicleJsonObjectList = getVehicleJsonObject(monitorIds);
        // 中位协议
        if (Objects.equals(String.valueOf(protocolType), ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019)) {
            // 中位-企业静态信息同步(0x1608)
            webClientHandleCom.send1608ByNewBindingByZwProtocol(orgIds, plantParam);
            for (JSONObject value : vehicleJsonObjectList) {
                // 中位-车辆静态信息同步消息(0x1609)
                webClientHandleCom.send1609ByZwProtocol(value, plantParam);
            }
            //四川协议
        } else if (Objects.equals(String.valueOf(protocolType), ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013)) {
            // 四川-企业静态信息同步(0x1605)
            webClientHandleCom.send1605ByNewBindingBySiChuanProtocol(orgIds, plantParam);

            List<Map<String, String>> professionalList =
                RedisHelper.batchGetHashMap(RedisKeyEnum.PROFESSIONAL_INFO.ofs(allProfessionalSet));
            Map<String, Map<String, String>> professionalMap = new HashMap<>(16);
            if (CollectionUtils.isNotEmpty(professionalList)) {
                professionalMap =
                    professionalList.stream().collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
            }
            Map<String, TerminalTypeInfo> terminalTypeInfoMap = deviceDao.getAllTerminalType().stream().collect(
                Collectors
                    .toMap(obj -> obj.getTerminalManufacturer() + "," + obj.getTerminalType(), Function.identity()));

            for (JSONObject jsonObject : vehicleJsonObjectList) {
                String vid = jsonObject.getString("id");
                List<Map<String, String>> professionalJsonObjList =
                    vehicleIdAndProfessionalSetMap.getOrDefault(vid, new HashSet<>()).stream().map(professionalMap::get)
                        .collect(Collectors.toList());
                // 四川-主动上报道路运输从业人员静态信息(0x1607)
                webClientHandleCom
                    .send1607ByNewBindingBySiChuanProtocol(jsonObject, professionalJsonObjList, plantParam);
                TerminalTypeRedisInfo terminalTypeRedisInfo = TerminalTypeRedisInfo.assembleTerminalTypeRedisInfo(
                    terminalTypeInfoMap.get(vehicleIdAndTerminalManufacturerAndTypeMap.get(vid)));
                // 四川-主动上报道路运输车辆静态信息(0x1606)
                webClientHandleCom.send1606BySiChuanProtocol(jsonObject, terminalTypeRedisInfo, plantParam);
            }
        } else if (Objects.equals(String.valueOf(protocolType), ProtocolTypeUtil.T809_HEIPROTOCOL_809_2019)) {
            //黑龙江标准
            webClientHandleCom.send1210ByNewBindingByHljProtocol(deviceAndConfigInfoMap, plantParam);
            return;
        }
        // 所有协议-主动上报终端安装信息消息(0x1240)
        webClientHandleCom.send1240ByNewBindingByZwProtocol(deviceAndConfigInfoMap, plantParam);
    }

    private List<JSONObject> getVehicleJsonObject(Set<String> monitorIds) {
        List<VehicleDO> list = vehicleService.getVehicleListByIds(monitorIds);
        List<BusinessScopeDTO> businessScopeList = businessScopeService.getBusinessScopeByIds(monitorIds);
        Map<String, List<BusinessScopeDTO>> businessScopeMap =
            businessScopeList.stream().collect(Collectors.groupingBy(BusinessScopeDTO::getId));
        List<JSONObject> vehicleJsonObject = new ArrayList<>();
        TypeCacheManger typeCacheManger = TypeCacheManger.getInstance();
        Date date;
        JSONObject jsonObject;
        VehicleTypeDTO vehicleType;
        VehiclePurposeDTO vehiclePurpose;
        for (VehicleDO vehicleDO : list) {
            jsonObject = new JSONObject();
            jsonObject.put("orgId", vehicleDO.getOrgId());
            jsonObject.put("brand", vehicleDO.getBrand());
            jsonObject.put("plateColor", vehicleDO.getPlateColor());
            jsonObject.put("cityId", vehicleDO.getCityId());
            jsonObject.put("id", vehicleDO.getId());
            jsonObject.put("chassisNumber", vehicleDO.getChassisNumber());
            jsonObject.put("roadTransportNumber", vehicleDO.getRoadTransportNumber());
            date = vehicleDO.getRoadTransportValidityStart();
            if (date != null) {
                jsonObject.put("roadTransportValidityStartStr", DateUtil.formatDate(date, "yyyy-MM-dd"));
            }
            date = vehicleDO.getRoadTransportValidity();
            if (date != null) {
                jsonObject.put("roadTransportValidityStr", DateUtil.formatDate(date, "yyyy-MM-dd"));
            }
            Integer numberLoad = vehicleDO.getNumberLoad();
            String loadingQuality = vehicleDO.getLoadingQuality();
            if (Objects.nonNull(vehicleDO.getVehicleType())) {
                vehicleType = typeCacheManger.getVehicleType(vehicleDO.getVehicleType());
                if (vehicleType != null) {
                    if (numberLoad != null && ("轿车".equals(vehicleType.getType()) || vehicleType.getType()
                        .contains("客车"))) {
                        jsonObject.put("seatTon", numberLoad);
                    }
                    if (loadingQuality != null && vehicleType.getType().contains("货车")) {
                        jsonObject.put("seatTon", loadingQuality);
                    }
                    jsonObject.put("codeNum", vehicleType.getCodeNum());
                }
            }
            if (Objects.nonNull(vehicleDO.getVehiclePurpose())) {
                vehiclePurpose = typeCacheManger.getVehiclePurpose(vehicleDO.getVehiclePurpose());
                if (vehiclePurpose != null) {
                    jsonObject.put("purposeCodeNum", vehiclePurpose.getCodeNum());
                }
            }
            jsonObject.put("engineNumber", vehicleDO.getEngineNumber());
            jsonObject.put("vehicleOwner", vehicleDO.getVehicleOwner());
            jsonObject.put("vehicleOwnerPhone", vehicleDO.getVehicleOwnerPhone());
            date = vehicleDO.getVehiclePlatformInstallDate();
            if (date != null) {
                jsonObject.put("vehiclePlatformInstallDateStr",
                    DateUtil.formatDate(date, "yyyy-MM-dd"));
            }
            List<BusinessScopeDTO> scopeList = businessScopeMap.get(vehicleDO.getId());
            if (CollectionUtils.isNotEmpty(scopeList)) {
                List<String> codes =
                    scopeList.stream().map(BusinessScopeDTO::getBusinessScopeCode).collect(Collectors.toList());
                jsonObject.put("scopeBusinessCodes", StringUtils.join(codes, ","));
            }
            vehicleJsonObject.add(jsonObject);
        }
        return vehicleJsonObject;
    }

    private void t809UploadVehicleInfo(Collection<BindDTO> bindInfo) {

        List<T809ForwardVehicleInfo> t809ForwardVehicleInfoList = Lists.newLinkedList();
        T809ForwardVehicleInfo t809ForwardVehicleInfo;
        JSONObject jsonObject;
        List<String> deviceIds = bindInfo.stream().map(BindDTO::getDeviceId).collect(Collectors.toList());
        List<String> vehicleIds = bindInfo.stream().map(BindDTO::getId).collect(Collectors.toList());
        Map<String, VehicleDO> vehicleMap = vehicleService.getVehicleListByIds(vehicleIds).stream()
            .collect(Collectors.toMap(VehicleDO::getId, Function.identity()));
        Map<String, DeviceListDO> deviceMap = deviceDao.getDeviceList(deviceIds).stream()
            .collect(Collectors.toMap(DeviceListDO::getId, Function.identity()));
        for (BindDTO configList : bindInfo) {
            t809ForwardVehicleInfo = new T809ForwardVehicleInfo();
            t809ForwardVehicleInfo.setAppId(appID);
            t809ForwardVehicleInfo.setAppSecret(appSecret);
            t809ForwardVehicleInfo.setTimestam(System.currentTimeMillis() / 1000);

            jsonObject = new JSONObject();
            jsonObject.put("car_no", configList.getName());
            jsonObject.put("car_color", configList.getPlateColor());
            //sim卡
            jsonObject.put("sim", configList.getSimCardNumber());
            //终端编号
            jsonObject.put("device_id", configList.getDeviceNumber());
            //是否是一体机
            jsonObject.put("is_integrated", true);
            VehicleDO vehicleDO = vehicleMap.get(configList.getId());
            if (vehicleDO != null) {
                jsonObject.put("vin", vehicleDO.getChassisNumber() == null ? "''" : vehicleDO.getChassisNumber());
            }
            DeviceListDO deviceListDO = deviceMap.get(configList.getDeviceId());
            if (deviceListDO != null) {
                Date installTime = deviceListDO.getInstallTime();
                //制造商名称
                String manuFacturer = deviceListDO.getManuFacturer();
                //制造商ID
                String manufacturerId = deviceListDO.getManufacturerId();
                //拼接安装时间
                if (installTime != null) {
                    LocalTime now = LocalTime.now();
                    if (now.getHour() < 6) {
                        now = now.plusHours(6);
                    }
                    jsonObject.put("install_time",
                        DateUtil.formatDate(installTime, DateUtil.DATE_Y_M_D_FORMAT) + " " + now.withNano(0)
                            .toString());
                } else {
                    jsonObject.put("install_time", new Date().toString());
                }
                if (!StringUtils.isEmpty(manuFacturer)) {
                    jsonObject.put("maker_name", manuFacturer);
                } else {
                    jsonObject.put("maker_name", "71234");
                }
                if (!StringUtils.isEmpty(manufacturerId)) {
                    jsonObject.put("maker_id", manufacturerId);
                } else {
                    jsonObject.put("maker_id", "71234");
                }
                jsonObject.put("device_type",
                    deviceListDO.getDeviceModelNumber() == null ? "''" : deviceListDO.getDeviceModelNumber());
            }
            t809ForwardVehicleInfo.setData(jsonObject);
            t809ForwardVehicleInfoList.add(t809ForwardVehicleInfo);
        }
        if (t809ForwardVehicleInfoList.size() > 0) {
            //调用接口发送
            String jsonStr;
            for (T809ForwardVehicleInfo info : t809ForwardVehicleInfoList) {
                jsonStr = JSONObject.toJSONString(info);
                logger.info("冀标上报的车辆信息为：{}", jsonStr);
                JSONObject ret = HttpClientUtil.doHttPost(t809UploadVehicleInfoUrl, jsonStr);
                logger.info("冀标上级平台返回的结果为：{}", ret != null ? ret.toJSONString() : "null");
            }
        }
    }

    @Override
    public boolean deleteConfig(List<String> configIds, String ipAddress) throws Exception {
        //查询即将要删除的转发绑定关系信息
        StringBuilder sb = new StringBuilder();
        List<String> newConfigIds = new ArrayList<>();
        for (String tid : configIds) {
            //根据configid查询车辆相关信息
            Map<String, Object> map = connectionParamsSetDao.getPlatformInfoByConfigId(tid);
            if (map == null) {
                continue;
            }
            Object brand = map.get("brand");
            Object platformName = map.get("platformName");
            Object protocolType = map.get("protocolType");
            //河南油补协议的绑定关系不能进行删除
            if (brand == null || platformName == null || Objects.equals(OIL_SUBSIDY_PROTOCOL_TYPE, protocolType)) {
                continue;
            }
            newConfigIds.add(tid);
            sb.append("监控对象：").append(brand).append(" 解除809上级平台（").append(platformName).append("）转发 </br>");
        }

        if (newConfigIds.isEmpty()) {
            return false;
        }
        List<T809PlatFormSubscribe> view = connectionParamsConfigDao.findConfigByConfigUuid(newConfigIds);
        //删除绑定关系
        boolean flag = connectionParamsConfigDao.deleteConfig(newConfigIds);
        for (T809PlatFormSubscribe t : view) {
            sendMsg(t, ConstantUtil.T809_FORWARD_DEVICE_DELETE);
        }
        logSearchService.addLog(ipAddress, sb.toString(), "3", "batch", "解除809监控上级平台转发");
        return flag;
    }

    @Override
    public JSONObject getT809ForwardTree() {
        JSONObject result = new JSONObject();
        JSONArray resultTree = new JSONArray();
        // 可以转发的监控对象集合
        Set<String> monitorIdList = new HashSet<>();
        //从缓存中获取用户权限下分组信息
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        if (CollectionUtils.isEmpty(currentUserGroupList)) {
            result.put("tree", resultTree);
            result.put("size", 0);
            return result;
        }
        Set<String> userGroupIds = currentUserGroupList.stream().map(GroupDTO::getId).collect(Collectors.toSet());
        List<GroupMonitorBindDO> groupMonitorBindInfoList =
            groupMonitorDao.getGroupMonitorBindInfoListByIds(userGroupIds, MonitorTypeEnum.VEHICLE.getType());
        Map<String, Set<String>> assignmentCountMap = groupMonitorBindInfoList.stream().collect(Collectors
            .groupingBy(GroupMonitorBindDO::getGroupId,
                Collectors.mapping(GroupMonitorBindDO::getMoId, Collectors.toSet())));
        for (GroupDTO data : currentUserGroupList) {
            Set<String> monitorIds = assignmentCountMap.getOrDefault(data.getId(), new HashSet<>());
            data.setMonitorCount(monitorIds.size());
            monitorIdList.addAll(monitorIds);
        }
        List<OrganizationLdap> useOrgList = userService.getCurrentUseOrgList();
        Map<String, OrganizationLdap> orgMap =
            useOrgList.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, Function.identity()));
        /* 判断监控对象数量是否大于5000，并按照相应逻辑处理 */
        if (monitorIdList.size() > PublicVariable.MONITOR_COUNT) {
            // 只返回企业、分组信息
            putGroupTree(currentUserGroupList, orgMap, resultTree, true);
        } else { //返回企业、分组、车辆信息
            //组装分组树结构
            putGroupTree(currentUserGroupList, orgMap, resultTree, false);
            //获取监控对象信息
            if (CollectionUtils.isNotEmpty(groupMonitorBindInfoList)) {
                groupMonitorBindInfoList
                    .stream()
                    .sorted(Comparator.comparing(GroupMonitorBindDO::getMoName))
                    .forEach(obj -> putMonitorTree(resultTree, obj));
            }
        }
        //组装组织树结构
        resultTree.addAll(JsonUtil.getOrgTree(useOrgList, null));
        result.put("tree", resultTree);
        result.put("size", monitorIdList.size());
        return result;
    }

    /**
     * 组装监控对象结构
     */
    private void putMonitorTree(JSONArray result, GroupMonitorBindDO groupMonitorBindDO) {
        JSONObject monitorInfo = new JSONObject();
        monitorInfo.put("pId", groupMonitorBindDO.getGroupId());
        monitorInfo.put("id", groupMonitorBindDO.getMoId());
        String monitorType = groupMonitorBindDO.getMonitorType();
        if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
            monitorInfo.put("iconSkin", "vehicleSkin");
            monitorInfo.put("type", "vehicle");
        } else if (Objects.equals(monitorType, MonitorTypeEnum.PEOPLE.getType())) {
            monitorInfo.put("iconSkin", "peopleSkin");
            monitorInfo.put("type", "people");
        } else if (Objects.equals(monitorType, MonitorTypeEnum.THING.getType())) {
            monitorInfo.put("iconSkin", "thingSkin");
            monitorInfo.put("type", "thing");
        }
        monitorInfo.put("name", groupMonitorBindDO.getMoName());
        result.add(monitorInfo);
    }

    /**
     * 组装分组结构
     */
    public void putGroupTree(List<GroupDTO> groups, Map<String, OrganizationLdap> orgMap, JSONArray result,
        boolean isBigData) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        for (GroupDTO group : groups) {
            JSONObject assignmentObj = new JSONObject();
            // 信息配置分组是否能勾选
            assignmentObj.put("count", group.getMonitorCount());
            assignmentObj.put("id", group.getId());
            OrganizationLdap org = orgMap.get(group.getOrgId());
            // 没有分组的所属企业权限不展示
            if (org == null) {
                continue;
            }
            assignmentObj.put("pId", org.getId().toString());
            assignmentObj.put("name", group.getName());
            assignmentObj.put("type", "assignment");
            assignmentObj.put("iconSkin", "assignmentSkin");
            assignmentObj.put("pName", group.getOrgName());
            // 前端要求注释这个
            // assignmentObj.put("nocheck", true);
            if (isBigData) {
                // 监控对象数量大于5000  有子节点
                assignmentObj.put("isParent", true);
            }
            result.add(assignmentObj);
        }
    }

    @Override
    public List<String> findConfigIdByVConfigIds(List<String> vcids) {
        return connectionParamsConfigDao.findConfigIdByVConfigIds(vcids);
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent unBindEvent) {
        if (Objects.equals("update", unBindEvent.getOperation())) {
            return;
        }
        try {
            List<String> configIds =
                unBindEvent.getUnbindList().stream().map(BindDTO::getConfigId).collect(Collectors.toList());
            // 若有809转发绑定关系则删除
            List<String> config809List = findConfigIdByVConfigIds(configIds);
            if (CollectionUtils.isNotEmpty(config809List)) {
                deleteConfigForUnBind(config809List, unBindEvent);
            }
        } catch (Exception e) {
            logger.error("信息配置解绑时809转发绑定关系删除失败", e);
        }

    }

    private boolean deleteConfigForUnBind(List<String> configIds, ConfigUnBindEvent unBindEvent) {
        //查询即将要删除的转发绑定关系信息
        StringBuilder sb = new StringBuilder();
        List<String> newConfigIds = new ArrayList<>();
        for (String tid : configIds) {
            //根据configid查询车辆相关信息
            Map<String, Object> map = connectionParamsSetDao.getPlatformInfoByConfigId(tid);
            if (map == null) {
                continue;
            }
            Object brand = map.get("brand");
            Object platformName = map.get("platformName");
            Object protocolType = map.get("protocolType");
            //河南油补协议的绑定关系不能进行删除
            if (brand == null || platformName == null || Objects.equals(OIL_SUBSIDY_PROTOCOL_TYPE, protocolType)) {
                continue;
            }
            newConfigIds.add(tid);
            sb.append("监控对象：").append(brand).append(" 解除809上级平台（").append(platformName).append("）转发 </br>");
        }

        if (newConfigIds.isEmpty()) {
            return false;
        }
        List<T809PlatFormSubscribe> view = connectionParamsConfigDao.findConfigByConfigUuid(newConfigIds);
        //删除绑定关系
        boolean flag = connectionParamsConfigDao.deleteConfig(newConfigIds);
        for (T809PlatFormSubscribe t : view) {
            sendMsg(t, ConstantUtil.T809_FORWARD_DEVICE_DELETE);
        }
        logSearchService.addLogByUserNameAndOrgId(unBindEvent.getIpAddress(), sb.toString(), "3", "batch",
                "解除809监控上级平台转发", unBindEvent.getUserName(), unBindEvent.getOrgId());
        return flag;
    }

    private void sendMsg(T809PlatFormSubscribe subscribe, int msgId) {
        T809Message t809Message = MsgUtil.getT809Message(msgId, null, null, subscribe);
        Message msg = MsgUtil.getMsg(msgId, t809Message);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);
    }

    @EventListener
    public void onSimNumberUpdate(SimNumberUpdateEvent event) {
        String oldSim = event.getOldSim();
        String newSim = event.getNewSim();
        if (Objects.equals(oldSim, newSim)) {
            return;
        }
        List<T809PlatFormSubscribe> subscribes = connectionParamsConfigDao.findConfigBySimNumber(oldSim);
        for (T809PlatFormSubscribe info : subscribes) {
            //删除老的转发
            info.setIdentification(oldSim);
            sendMsg(info, ConstantUtil.T809_FORWARD_DEVICE_DELETE);

            //添加新的转发
            info.setIdentification(newSim);
            sendMsg(info, ConstantUtil.T809_FORWARD_DEVICE_ADD);
        }
    }

}
