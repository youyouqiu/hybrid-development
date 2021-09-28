package com.zw.platform.service.workhourmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.VibrationSensorBindDao;
import com.zw.platform.repository.vas.VibrationSensorDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.workhourmgt.VibrationSensorBindService;
import com.zw.platform.util.MonitorTypeUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.SensorService;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Title: 振动传感器绑定ServiceImp</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:31:52
 */
@Service
public class VibrationSensorBindServiceImpl implements VibrationSensorBindService {
    private static Logger log = LogManager.getLogger(VibrationSensorBindServiceImpl.class);

    @Autowired
    private VibrationSensorBindDao vibrationSensorBindDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private VibrationSensorDao vibrationSensorDao;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${edit.success}")
    private String editSuccess;

    @Value("${edit.fail}")
    private String editFail;

    @Value("${bound.seccess}")
    private String boundSuccess;

    @Value("${bound.fail}")
    private String boundFail;

    @Autowired
    private MonitorTypeUtil monitorTypeUtil;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public Page<VibrationSensorBind> findWorkHourSensorBind(VibrationSensorBindQuery query) {

        Page<VibrationSensorBind> page = new Page<>();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", query.getGroupId());
            map.put("assignmentId", query.getAssignmentId());
            map.put("query", query.getSimpleQueryParam());

            List<String> vehicleList = redisVehicleService
                .getUserVehicles(map, RedisKeys.SensorType.SENSOR_SHOCK_MONITOR, Integer.valueOf(query.getProtocol()));
            if (vehicleList == null) {
                throw new RedisException(">=======redis 缓存出错了===========<");
            }
            int listSize = vehicleList.size();
            int curPage = query.getPage().intValue();// 当前页
            int pageSize = query.getLimit().intValue(); // 每页条数
            int lst = (curPage - 1) * pageSize;// 遍历开始条数
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数

            List<String> vehicles = new ArrayList<String>();

            for (int i = 0; i < vehicleList.size(); i++) {
                if (i >= lst && i < ps) {
                    vehicles.add(vehicleList.get(i));
                }
            }
            List<VibrationSensorBind> list = vibrationSensorBindDao.findVehicleSensorRedis(vehicles);
            if (list != null && list.size() > 0) {
                for (VibrationSensorBind vibrationSensorBind : list) {
                    //车辆
                    final Map<String, String> infoMap = RedisHelper.getHashMap(
                            RedisKeyEnum.MONITOR_INFO.of(vibrationSensorBind.getVId(), "orgName", "vehicleType"));
                    if (infoMap != null) {
                        vibrationSensorBind.setGroups(ObjectUtils.defaultIfNull(infoMap.get("orgName"), ""));
                        if (vibrationSensorBind.getMonitorType() == 0) {
                            vibrationSensorBind.setVehicleType(
                                    ObjectUtils.defaultIfNull(infoMap.get("vehicleType"), ""));
                        } else if (vibrationSensorBind.getMonitorType() == 2) {
                            vibrationSensorBind.setVehicleType(
                                monitorTypeUtil.findByMonitorTypeAndId("2", vibrationSensorBind.getVId()));
                        }
                    }
                }
            }
            // 按照绑定关系进行排序
            VehicleUtil.sort(list, vehicles);
            page = RedisQueryUtil.getListToPage(list, query, listSize);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // 如果缓存失败，从数据库中获取
                String userId = SystemHelper.getCurrentUser().getId().toString();
                // 获取当前用户所属组织及下级组织
                List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
                if (userId != null && !"".equals(userId) && userOrgListId != null && userOrgListId.size() > 0) {
                    String userUuidById = userService.getUserUuidById(userId);
                    page = PageHelperUtil.doSelect(query, () -> vibrationSensorBindDao
                        .findVehicleSensor(query, userUuidById, userOrgListId));
                }
            }
            log.error("应用管理--->工时车辆管理分页查询失败", e);
        } finally {
            PageHelper.clearPage();
        }

        // 处理result，将groupId对应的groupName给result相应的值赋上
        setGroupNameByGroupId(page);
        return page;
    }

    /**
         * @param result
     * @return void
     * @throws @author wangying
     * @Title: setGroupNameByGroupId
     */
    public void setGroupNameByGroupId(List<VibrationSensorBind> result) {
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization();
        if (null != result && result.size() > 0) {
            for (VibrationSensorBind parameter : result) {
                String groupId = parameter.getGroups();
                if (StringUtils.isNotBlank(groupId)) {
                    String groupName = "";
                    for (OrganizationLdap orgLdap : orgLdapList) {
                        if (Converter.toBlank(orgLdap.getUuid()).equals(groupId)) {
                            groupName = Converter.toBlank(orgLdap.getName());
                        }
                    }
                    parameter.setGroups(groupName);
                }
                // 下发状态
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), "3"); // 3:工时
                    Directive param1 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }

                    if (param1 != null) {
                        parameter.setParamId(param1.getId());
                        parameter.setTransmissionParamId("");
                        parameter.setStatus(param1.getStatus());
                    }
                }
            }
        }
    }

    @Override
    public List<VibrationSensorBind> findReferenceVehicle() throws Exception {
        List<VibrationSensorBind> list = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getOrgUuidsByUser(userId);
        if (userId != null && !"".equals(userId) && orgList != null && orgList.size() > 0) {
            
            list = vibrationSensorBindDao.findWorkHourVehicle(userService.getUserUuidById(userId), orgList);
        }
        // // 判断是否是管理员
        // if (userService.isManagerRole()){ // 管理员
        // if (userId != null && userId != "" && orgList != null && orgList.size() > 0) {
        // list = vibrationSensorBindDao.findWorkHourVehicleForAdmin(userId, orgList);
        // }
        // }else{ // 普通用户
        // if (userId != null && userId != "") {
        // list = vibrationSensorBindDao.findWorkHourVehicleForUser(userId);
        // }
        // }
        return list;
    }

    @Override
    public JsonResultBean addWorkHourSensorBind(VibrationSensorBindForm form, String ipAddress) throws Exception {
        if (form != null) {
            form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // 创建者
            form.setCreateDataTime(new Date()); // 创建时间
            boolean result = vibrationSensorBindDao.addWorkHourSensorBind(form);
            if (result) {
                // 维护缓存
                updateVehicleSensorCache(form);
                // 从缓存中获取监控对象信息
                final Map<String, String> infoMap = RedisHelper.getHashMap(
                        RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId(), "name", "plateColor", "orgName"));
                if (infoMap != null) {
                    String brand = infoMap.get("name");
                    String plateColor = infoMap.get("plateColor");
                    String orgName = infoMap.get("orgName");
                    // 消息
                    String message = "监控对象：" + brand + " ( @" + orgName + " ) 设置车辆工时参数";
                    // 记录设置工时参数日志
                    logSearchService.addLog(ipAddress, message, "3", "", brand, plateColor);
                }
                return new JsonResultBean(JsonResultBean.SUCCESS, boundSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT, boundFail);
    }

    @Override
    public VibrationSensorBind findWorkHourVehicleById(String id) {
        if (id != null && !"".equals(id)) {
            return vibrationSensorBindDao.findWorkHourVehicleById(id);
        }
        return null;
    }

    @Override
    public JsonResultBean updateWorkHourSensorBind(VibrationSensorBindForm form, String ipAddress) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // 设置修改人为当前用户
        form.setUpdateDataTime(new Date()); // 设置数据修改时间
        boolean flag = vibrationSensorBindDao.updateWorkHourSensorBind(form);
        if (flag) {
            final Map<String, String> infoMap = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId(), "name", "plateColor", "orgName"));
            if (infoMap != null) {
                String brand = infoMap.get("name");
                String plateColor = infoMap.get("plateColor");
                String orgName = infoMap.get("orgName");
                // 消息
                String message = "监控对象：" + brand + " ( @" + orgName + " ) 修改车辆工时设置参数";
                // 记录设置工时参数日志
                logSearchService.addLog(ipAddress, message, "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, editSuccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT, editFail);
    }

    private void updateVehicleSensorCache(VibrationSensorBindForm form) {
        VibrationSensorForm vibrationSensorForm = vibrationSensorDao.findVibrationSensorById(form.getShockSensorId());
        RedisHelper.addToHash(
                RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST.of(), form.getVehicleId(), vibrationSensorForm.getSensorType());
    }

    @Override
    public JsonResultBean deleteWorkHourSensorBindById(String id, String ipAddress) throws Exception {
        List<String> ids = Arrays.asList(id.split(","));
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (String bandId : ids) {
            VibrationSensorBind vibrationSensorBind = vibrationSensorBindDao.findWorkHourVehicleById(bandId);
            boolean result = vibrationSensorBindDao.deleteWorkHourSensorBindById(bandId);
            if (result && vibrationSensorBind != null) {
                // 维护缓存
                vehicleId = vibrationSensorBind.getVehicleId();
                RedisHelper.hdel(RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST.of(), vibrationSensorBind.getVehicleId());
                // 车牌号
                String brand = vibrationSensorBind.getBrand();
                // 传感器型号
                String sensorType = vibrationSensorBind.getSensorType();
                // 消息
                message.append("监控对象 : ").append(brand).append(" 解除绑定振动传感器 ( ").append(sensorType).append(" )")
                    .append(" <br/>");
            }
        }
        if (!message.toString().isEmpty()) {
            if (ids.size() == 1) {
                // 记录解除绑定震动传感器日志
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量解除振动传感器绑定");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        vibrationSensorBindDao.deleteBatchWorkHourSensorBindByVid(monitorIds);
    }

    @Override
    public void sendWorkHour(ArrayList<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            // 参数类型
            String paramType = "3"; // 工时
            String parameterName = "";
            vehicleId = "";
            String paramId = "";
            String transmissionParamId = "";
            if (obj.get("fluxVehicleId") != null) {
                parameterName = obj.get("fluxVehicleId").toString();
            }
            if (obj.get("vehicleId") != null) {
                vehicleId = obj.get("vehicleId").toString();
            }
            if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
                paramId = obj.get("paramId").toString();
            }
            if (obj.get("transmissionParamId") != null && !"".equals(obj.get("transmissionParamId"))) { // 油箱设置
                transmissionParamId = obj.get("transmissionParamId").toString();
            }
            if (!parameterName.isEmpty() && !vehicleId.isEmpty()) {
                // 根据id查询振动传感器
                VibrationSensorBind vibrationSensor = findWorkHourVehicleById(parameterName);
                if (vibrationSensor != null) {
                    // 工时下发
                    sendVibrationSensor(paramId, vehicleId, paramType, parameterName, vibrationSensor);
                    String brand = vibrationSensor.getBrand(); // 车牌号
                    message.append("监控对象 : ").append(brand).append(" 下发工时车辆设置参数").append(" <br/>");
                }

            }
        }
        if (!message.toString().isEmpty()) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量下发工时车辆设置参数");
            }
        }
    }

    public void sendVibrationSensor(String paramId, String vehicleId, String paramType, String parameterName,
        VibrationSensorBind vibrationSensor) throws Exception {
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> infoMap =
                RedisHelper.getHashMap(key, "deviceNumber", "deviceType", "simCardNumber", "deviceId");
        String deviceNumber = infoMap.get("deviceNumber");
        VehicleInfo vehicle = new VehicleInfo();
        ConvertUtils.register(vehicle, Date.class);
        vehicle.setDeviceType(infoMap.get("deviceType"));
        vehicle.setSimcardNumber(infoMap.get("simCardNumber"));
        vehicle.setDeviceId(infoMap.get("deviceId"));
        vehicle.setId(vehicleId);
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            // 下发参数
            sensorService.sendSensorParam(1, vehicle, vibrationSensor, msgSN);
            int status = 4; // 已下发
            // 油箱绑定下发
            updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        } else { // 设备未注册
            int status = 5; // 设备为注册
            msgSN = 0;
            // 油箱绑定下发
            updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
    }

    /**
         * @param paramId       参数下发id
     * @param vehicleId     车辆id
     * @param paramType     参数下发类型
     * @param msgSN         流水号
     * @param parameterName 绑定id
     * @return
     * @throws @author wangying
     * @Title: updateParameterStatus 修改参数下发表数据
     */
    public void updateParameterStatus(String paramId, int msgSN, int status, String vehicleId, String paramType,
        String parameterName) throws Exception {
        // int status = 4; // 已下发
        if (paramId != null && paramId != "") {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(paramId);
            // 重新下发 ，修改流水号
            parameterDao.updateMsgSNAndNameById(paramIds, msgSN, status, parameterName, 1); // 1 : 下发未回应
        } else {
            // 新增下发参数数据
            DirectiveForm form = generateDirective(vehicleId, status, paramType, msgSN, parameterName, 1);
            if (form != null) {
                // 批量新增
                parameterDao.addDirective(form);
            }
        }
    }

    /**
         * @param parameterType
     * @param vehicleId
     * @param status
     * @param msgSN
     * @return void
     * @throws @author wangying
     * @Title: 生成参数下发数据
     */
    public DirectiveForm generateDirective(String vehicleId, int status, String parameterType, int msgSN,
        String parameterName, int replyCode) throws Exception {
        DirectiveForm form = new DirectiveForm();
        form.setDownTime(new Date());
        form.setMonitorObjectId(vehicleId);
        form.setParameterName(parameterName);
        form.setStatus(status);
        form.setParameterType(parameterType);
        form.setSwiftNumber(msgSN);
        form.setReplyCode(replyCode);
        return form;
    }

    @Override
    public VibrationSensorBind findWorkHourVehicleByVid(String vehicleId) throws Exception {
        if (vehicleId != null && !"".equals(vehicleId)) {
            return vibrationSensorBindDao.findWorkHourVehicleByVid(vehicleId);
        }
        return null;
    }

}
