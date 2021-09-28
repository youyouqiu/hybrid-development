package com.zw.platform.service.mileageSensor.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.sensorparam.MileageParam;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.MileageSensorConfigDao;
import com.zw.platform.repository.vas.MileageSensorDao;
import com.zw.platform.service.mileageSensor.MileageSensorConfigService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.MonitorTypeUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:20
 */
@Service
public class MileageSensorConfigServiceImpl implements MileageSensorConfigService {
    private static Logger logger = LogManager.getLogger(MileageSensorConfigServiceImpl.class);

    @Autowired
    private MileageSensorConfigDao mileageSensorConfigDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private SendHelper sendHelper;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private MileageSensorDao mileageSensorDao;

    @Autowired
    private LogSearchService logSearchService;
    @Autowired
    private MonitorTypeUtil monitorTypeUtil;

    @Value("${poll.param.null}")
    private String pollParamNull;

    @Value("${set.success}")
    private String setSuccess;

    @Autowired
    private OrganizationService organizationService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    /**
     * 新增里程传感器设置
     * @param mileageSensorConfig
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean addMileageSensorConfig(MileageSensorConfig mileageSensorConfig, String ipAddress)
        throws Exception {
        mileageSensorConfig.setCreateDataTime(new Date());
        mileageSensorConfig.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = mileageSensorConfigDao.addMileageSensorConfig(mileageSensorConfig);
        if (flag) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(mileageSensorConfig.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "监控对象 : " + brand + " ( @" + groupName + " ) 新增里程传感器设置";
                logSearchService.addLog(ipAddress, msg, "3", "里程传感器配置管理", brand, plateColor);
                // 维护车和传感器的缓存
                String sensorId = mileageSensorConfig.getMileageSensorId();
                MileageSensor mileageSensor = mileageSensorDao.findById(sensorId);
                if (mileageSensor != null) {
                    RedisHelper
                        .addToHash(RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST.of(), mileageSensorConfig.getVehicleId(),
                            mileageSensor.getSensorType());
                }
                return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 修改里程监测设置
     * @param mileageSensorConfig
     * @param isClearSendStatus   true 清除原来下发状态 false 不清除原来的下发状态
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean updateMileageSensorConfig(MileageSensorConfig mileageSensorConfig, boolean isClearSendStatus,
        String ipAddress) throws Exception {
        mileageSensorConfig.setUpdateDataTime(new Date());
        mileageSensorConfig.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        if (isClearSendStatus) {
            // 清除原来的下发状态
            sendHelper.deleteByVehicleIdParameterName(mileageSensorConfig.getVehicleId(), mileageSensorConfig.getId(),
                "0x8103-0xF353");
        }
        boolean flag = mileageSensorConfigDao.updateMileageSensorConfig(mileageSensorConfig);
        if (flag) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(mileageSensorConfig.getVehicleId());
            RedisHelper.addToHash(RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST.of(), mileageSensorConfig.getVehicleId(),
                mileageSensorConfig.getSensorType());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "监控对象 : " + brand + " ( @" + groupName + " ) 修改里程传感器设置";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
                return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 查询绑定了轮速传感器的监控对象
     * @return
     * @throws Exception
     */
    @Override
    public List<MileageSensorConfig> findVehicleSensorSet() throws Exception {
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        
        List<String> reportDeviceTypes = Arrays.asList(ProtocolEnum.REPORT_DEVICE_TYPE);
        List<MileageSensorConfig> list = this.mileageSensorConfigDao
            .findVehicleSensorSet(userService.getCurrentUserUuid(), orgList, reportDeviceTypes);
        getMileageSensorConfig(list);
        return list;
    }

    private void getMileageSensorConfig(List<MileageSensorConfig> list) {
        if (list != null && list.size() > 0) {
            Set<String> vids = list.stream().map(MileageSensorConfig::getVehicleId).collect(Collectors.toSet());
            Map<String, Map<String, String>> map =
                RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vids)).stream()
                    .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
            for (MileageSensorConfig mileageSensorConfig : list) {
                Map<String, String> vehicleInfo = map.get(mileageSensorConfig.getVehicleId());
                mileageSensorConfig.setGroupName(vehicleInfo.get("orgName") == null ? "" : vehicleInfo.get("orgName"));
                mileageSensorConfig.setGroups(vehicleInfo.get("orgId") == null ? "" : vehicleInfo.get("orgId"));
                if ("0".equals(mileageSensorConfig.getMonitorType())) {
                    mileageSensorConfig.setVehicleType(vehicleInfo.get("vehicleType") == null ? "" :
                        cacheManger.getVehicleType(vehicleInfo.get("vehicleType")).getType());
                } else if ("2".equals(mileageSensorConfig.getMonitorType())) {
                    mileageSensorConfig.setVehicleType("其他物品");
                }
            }
        }
    }

    /**
     * 解除里程传感器与监控对象的绑定
     * @param vehicleIds
     * @param ipAddress
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteBatchMileageSensorConfig(List<String> vehicleIds, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        boolean flag = mileageSensorConfigDao.deleteBatchMileageSensorConfig(vehicleIds);
        if (flag) {
            Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
            if (MapUtils.isNotEmpty(configLists)) {
                for (Map.Entry<String, BindDTO> map : configLists.entrySet()) {
                    BindDTO bindDTO = map.getValue();
                    if (Objects.nonNull(bindDTO)) {
                        message.append("监控对象 : ").append(bindDTO.getName()).append(" ( @")
                            .append(bindDTO.getOrgName()).append(" ) 解绑里程传感器设置 <br/>");
                    }
                }
            }
            // 维护车和传感器的缓存
            RedisHelper.hdel(RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST.of(), vehicleIds);

            if (vehicleIds.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleIds.get(0));
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量解除里程传感器设置");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) throws Exception {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        deleteBatchMileageSensorConfig(monitorIds, event.getIpAddress());
    }

    @Override
    public Boolean updateNominalStatus(MileageSensorConfig mileageSensorConfig) throws Exception {
        mileageSensorConfig.setUpdateDataTime(new Date());
        mileageSensorConfig.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        return this.mileageSensorConfigDao.updateMileageSensorConfig(mileageSensorConfig);
    }

    @Override
    public MileageSensorConfig findByVehicleId(String vehicleId) throws Exception {
        MileageSensorConfig mileageSensorConfig = this.mileageSensorConfigDao.findByVehicleId(vehicleId);
        if (mileageSensorConfig != null) {
            //获取信息缓存
            Map<String, String> map =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(mileageSensorConfig.getVehicleId()));
            mileageSensorConfig.setGroupName(map.get("orgName"));
            mileageSensorConfig.setGroups(map.get("orgId"));
            //车辆
            if ("0".equals(map.get("monitorType"))) {
                mileageSensorConfig.setVehicleType(
                    map.get("vehicleType") == null ? "" : cacheManger.getVehicleType(map.get("vehicleType")).getType());
            } else if ("2".equals(map.get("monitorType"))) { //物品
                //根据物品和id获取物品类型
                mileageSensorConfig.setVehicleType("其他物品");
            }
        }
        return mileageSensorConfig;
    }

    @Override
    public MileageSensorConfig findByVehicleId(String vehicleId, boolean isAllow) throws Exception {
        MileageSensorConfig mc = this.mileageSensorConfigDao.findByVehicleId(vehicleId);
        if (isAllow) {
            List<Directive> paramlist =
                parameterDao.findParameterByType(mc.getVehicleId(), mc.getId(), "0x8103-0xF353"); // 6:报警
            Directive param1 = null;
            if (paramlist != null && !paramlist.isEmpty()) {
                param1 = paramlist.get(0);
            }
            if (param1 != null) {
                mc.setSend8103ParamId(param1.getId());
                mc.setSendStatus(param1.getStatus());
            }
        }
        return mc;
    }

    @Override
    public Page<MileageSensorConfig> findByQuery(MileageSensorConfigQuery query) throws Exception {

        Page<MileageSensorConfig> list = new Page<>();
        try {
            RedisSensorQuery redisQuery =
                new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                    query.getProtocol());

            List<String> vehicleList =
                redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_MILEAGE_MONITOR);
            if (vehicleList == null) {
                throw new RedisException(">=======redis 缓存出错了===========<");
            }
            int listSize = vehicleList.size();
            int curPage = query.getPage().intValue();// 当前页
            int pageSize = query.getLimit().intValue(); // 每页条数
            int lst = (curPage - 1) * pageSize;// 遍历开始条数
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数

            List<String> necessaryList = new ArrayList<String>();

            for (int i = 0; i < vehicleList.size(); i++) {
                if (i >= lst && i < ps) {
                    necessaryList.add(vehicleList.get(i));
                }
            }
            if (necessaryList.size() > 0) {
                List<MileageSensorConfig> milList = mileageSensorConfigDao.findByQueryRedis(necessaryList);
                if (milList != null && milList.size() > 0) {
                    Set<String> vehicleIds =
                        milList.stream().map(MileageSensorConfig::getVehicleId).collect(Collectors.toSet());
                    Map<String, Map<String, String>> vehicleMap =
                        RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIds)).stream()
                            .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
                    for (MileageSensorConfig mileageSensorConfig : milList) {
                        //获取信息缓存
                        Map<String, String> vmap = vehicleMap.get(mileageSensorConfig.getVehicleId());
                        mileageSensorConfig.setGroupName(vmap.get("orgName"));
                        mileageSensorConfig.setGroups(vmap.get("orgId"));
                        //车辆
                        if ("0".equals(vmap.get("monitorType"))) {
                            mileageSensorConfig.setVehicleType(vmap.get("vehicleType") == null ? "" :
                                cacheManger.getVehicleType(vmap.get("vehicleType")).getType());
                        } else if ("2".equals(vmap.get("monitorType"))) { //物品
                            //根据物品和id获取物品类型
                            mileageSensorConfig.setVehicleType("其他物品");
                        }
                    }
                    // 按照车辆配置绑定关系进行排序
                    VehicleUtil.sort(milList, necessaryList);
                }
                list = RedisQueryUtil.getListToPage(milList, query, listSize);
            }
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // 获取当前用户所属组织及下级组织
                List<String> orgList = userService.getCurrentUserOrgIds();
                if (orgList != null && orgList.size() > 0) {
                    String userUuidById = userService.getCurrentUserUuid();

                    list = PageHelperUtil
                        .doSelect(query, () -> mileageSensorConfigDao.findByQuery(query, userUuidById, orgList));
                }
            } else {
                logger.error("应用管理--->里程监测设置分页查询失败", e);
            }

        }
        setPollinglISet(list, false);
        return list;
    }

    @Override
    public JSONObject sendParam(Map<String, Object> map) throws Exception {
        String vehicleId = (String) map.get("vehicleId");
        MileageSensorConfig sc = this.findByVehicleId(vehicleId);
        if (sc == null) {
            return null;
        }
        Integer rollingRadius = sc.getRollingRadius();
        if (map.containsKey("rollingRadius")) {
            rollingRadius = (Integer) map.get("rollingRadius");
        }
        MileageParam mileageParam = new MileageParam();
        mileageParam.setAutomaticUploadTime(sc.getUploadTime());
        mileageParam.setCompensationFactorB(sc.getOutputB());
        mileageParam.setCompensationFactorK(sc.getOutputK());
        if (sc.getSpeedRatio() == null) {
            sc.setSpeedRatio(10.0);
        }
        Double speedRatio = sc.getSpeedRatio() * 100;
        mileageParam.setSpeedRatio(speedRatio.intValue());
        mileageParam.setMileageMeasurementScheme(sc.getMeasuringScheme());
        mileageParam.setRollingRadius(sc.getTireRollingRadius().intValue());
        mileageParam.setRollingRadiusCompensationFactor(rollingRadius);
        mileageParam.setCompensatingEnable(sc.getCompEn());
        // 下发8103
        String msgIN8103 = sendSensor8103(vehicleId, sc.getId(), mileageParam, "BD-0x8103-0xF353", null);
        JSONObject json = new JSONObject();
        json.put("msgIN8103", msgIN8103);
        return json;
    }

    /**
     * 下发里程监测设置
     * @param paramList
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean sendParam(ArrayList<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        Set<String> vehicleIds =
            paramList.stream().map(o -> o.getString("vehicleId")).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, Map<String, String>> vehicleMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIds)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        List<MileageSensorConfig> mileageSensorConfigs = mileageSensorConfigDao.findByVehicleIdBatch(vehicleIds);
        if (CollectionUtils.isEmpty(mileageSensorConfigs)) {
            return new JsonResultBean(JsonResultBean.FAULT, pollParamNull);
        }
        Map<String, MileageSensorConfig> sensorConfigMap = mileageSensorConfigs.stream()
            .collect(Collectors.toMap(MileageSensorConfig::getVehicleId, Function.identity()));
        for (JSONObject object : paramList) {
            if (object.get("vehicleId") != null) {
                vehicleId = object.get("vehicleId").toString();
                // 判断当前车辆是否绑定参数设置
                MileageSensorConfig sc = sensorConfigMap.get(vehicleId);
                if (sc == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, pollParamNull);
                }
                MileageParam mileageParam = new MileageParam();
                mileageParam.setAutomaticUploadTime(sc.getUploadTime());
                mileageParam.setCompensationFactorB(sc.getOutputB());
                mileageParam.setCompensationFactorK(sc.getOutputK());
                if (sc.getSpeedRatio() == null) {
                    sc.setSpeedRatio(10.0);
                }
                Double speedRatio = sc.getSpeedRatio() * 100;
                mileageParam.setSpeedRatio(speedRatio.intValue());
                mileageParam.setMileageMeasurementScheme(sc.getMeasuringScheme());
                Double rollingRadius = sc.getTireRollingRadius();
                mileageParam.setRollingRadius(rollingRadius.intValue());
                mileageParam.setRollingRadiusCompensationFactor(sc.getRollingRadius());
                mileageParam.setCompensatingEnable(sc.getCompEn());
                // 下发8103
                String msgIN8103 = sendSensor8103(vehicleId, sc.getId(), mileageParam, "0x8103-0xF353", 3);
                Map vehicleInfo = vehicleMap.get(vehicleId);
                if (vehicleInfo != null) {
                    Object brand = vehicleInfo.get("name");
                    Object groupId = vehicleInfo.get("orgName");
                    if (brand != null && groupId != null) {
                        message.append("监控对象 : ").append(brand).append(" ( @").append(groupId.toString())
                            .append(" ) 下发里程监测设置");
                    }
                }
            }
        }
        if (!"".equals(message.toString())) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量下发里程监测设置");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 下发8103
     * @param vehicleId     车id
     * @param parameterName 报警设置id
     * @param flag          下发的类型 3 代表里程检测
     * @Description: 报警参数设置下发
     */
    private String sendSensor8103(String vehicleId, String parameterName, MileageParam mileageParam, String paramType,
        Integer flag)

        throws Exception {
        List<ParamItem> params = new ArrayList<>();
        ParamItem item = new ParamItem();
        item.setParamId(0xF353);
        item.setParamLength(56);
        item.setParamValue(mileageParam);
        params.add(item);
        String msgSN = this.sendTxtService
            .setF3SetParamByVehicleAndPeopleAndThing(vehicleId, parameterName, params, paramType, true, flag);
        return String.valueOf(msgSN);
    }

    /**
     * 设置实体数据
     * @param list
     * @param isAll
     */
    private void setPollinglISet(Page<MileageSensorConfig> list, boolean isAll) throws Exception {
        if (list != null && list.size() > 0) {
            for (MileageSensorConfig sc : list) {
                StringBuilder groupName = new StringBuilder();
                List<String> groupIds = Arrays.asList(sc.getGroups().split(";"));
                Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(new HashSet<>(groupIds));
                for (String groupId : groupIds) {
                    OrganizationLdap organization = orgMap.get(groupId);
                    if (organization != null) {
                        groupName.append(organization.getName()).append(",");
                    }
                }

                sc.setGroupName(StrUtil.getFinalStr(groupName));
                if (sc.getMileageSensorId() == null || "".equals(sc.getMileageSensorId())) {
                    continue;
                }
                String pollid = sc.getId();
                if (pollid == null || "".equals(pollid)) {
                    continue;
                }
                sc.setCompEnStr(CompEnUtil.getCompEnVal(sc.getCompEn()));
                // 6:报警
                List<Directive> paramlist =
                    parameterDao.findParameterByType(sc.getVehicleId(), pollid, "0x8103-0xF353");
                Directive param1 = null;
                if (paramlist != null && !paramlist.isEmpty()) {
                    param1 = paramlist.get(0);
                }
                if (param1 != null) {
                    sc.setSend8103ParamId(param1.getId());
                    sc.setSendStatus(param1.getStatus());
                }
            }
        }
    }

    @Override
    public List<MileageSensorConfig> findVehicleSensorSetByProtocols(List<Integer> protocols) {
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        
        List<MileageSensorConfig> list = this.mileageSensorConfigDao
            .findVehicleSensorSetByProtocols(userService.getCurrentUserUuid(), orgList, protocols);
        getMileageSensorConfig(list);
        return list;
    }
}
