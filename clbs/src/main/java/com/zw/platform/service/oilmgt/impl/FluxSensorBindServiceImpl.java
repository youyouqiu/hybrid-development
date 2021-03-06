package com.zw.platform.service.oilmgt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorBindQuery;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.FluxSensorBindDao;
import com.zw.platform.repository.vas.FluxSensorDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.monitoring.RealTimeCommandService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.WsOilConsumeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Title: ?????????????????????ServiceImp</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * 2016???9???19?????????9:31:52
 *
 * @version 1.0
 * @author wangying
 */
@Service
public class FluxSensorBindServiceImpl implements FluxSensorBindService {
    private static final Logger log = LogManager.getLogger(FluxSensorBindServiceImpl.class);

    @Autowired
    private FluxSensorBindDao fluxSensorBindDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private RealTimeCommandService commandService;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private FluxSensorDao fluxSensorDao;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private WsOilConsumeService wsOilConsumeService;

    @Value("${up.error.terminal.off.line}")
    private String upErrorTerminalOffLine;

    @Value("${vehicle.bound.oilwear}")
    private String vehicleBoundOilwear;

    @Value("${bound.seccess}")
    private String boundSeccess;

    @Value("${bound.fail}")
    private String boundFail;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public List<FuelVehicle> findFluxSensorBind(FluxSensorBindQuery query) throws Exception {

        Page<FuelVehicle> page = new Page<>();
        try {
            RedisSensorQuery redisQuery =
                new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                    query.getProtocol());

            List<String> vehicleList =
                redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_OIL_CONSUMER_MONITOR);
            if (vehicleList == null) {
                throw new RedisException(">=======redis ???????????????===========<");
            }
            int listSize = vehicleList.size();
            int curPage = query.getPage().intValue();// ?????????
            int pageSize = query.getLimit().intValue(); // ????????????
            int lst = (curPage - 1) * pageSize;// ??????????????????
            int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// ????????????

            List<String> vehicles = new ArrayList<>();

            for (int i = 0; i < vehicleList.size(); i++) {
                if (i >= lst && i < ps) {
                    vehicles.add(vehicleList.get(i));
                }
            }
            List<FuelVehicle> list = Lists.newArrayList();
            if (vehicles.size() > 0) {
                list = fluxSensorBindDao.findVehicleSensorRedis(vehicles);
            }
            if (list != null && list.size() > 0) {
                final Set<String> vehicleIds = list.stream().map(FuelVehicle::getVId).collect(Collectors.toSet());
                Map<String, VehicleDTO> vehicleInfoMap = MonitorUtils.getVehicleMap(vehicleIds);
                for (FuelVehicle fuelVehicle : list) {
                    //????????????????????????
                    final VehicleDTO vehicleInfo = vehicleInfoMap.get(fuelVehicle.getVId());
                    if (vehicleInfo != null) {
                        fuelVehicle.setGroups(vehicleInfo.getOrgName());
                        if (0 == fuelVehicle.getMonitorType()) { //???
                            fuelVehicle.setVehicleType(vehicleInfo.getVehicleTypeName());
                        } else if (2 == fuelVehicle.getMonitorType()) { //??????
                            //???????????????????????????
                            fuelVehicle.setVehicleType("????????????");
                        }
                    }
                }
                // ????????????????????????????????????,????????????
                VehicleUtil.sort(list, vehicles);
                page = RedisQueryUtil.getListToPage(list, query, listSize);
            }
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // ??????redis ???????????? ?????????????????????
                String userId = userService.getCurrentUserUuid();
                // ?????????????????????????????????????????????
                List<String> orgList = userService.getCurrentUserOrgIds();
                if (StringUtils.isNotBlank(userId) && orgList != null && orgList.size() > 0) {
                    page = PageHelperUtil
                        .doSelect(query, () -> fluxSensorBindDao.findVehicleSensor(query, userId, orgList));
                }
            } else {
                log.error("????????????--->????????????????????????????????????", e);
            }

        }
        setTransmissionStatus(page);
        return page;
    }

    /**
     * @author wangying
     */
    public void setTransmissionStatus(Page<FuelVehicle> result) throws Exception {
        if (null != result && result.size() > 0) {
            for (FuelVehicle parameter : result) {
                // ????????????
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), "2"); // 2:??????
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        Directive param1 = paramlist1.get(0);
                        parameter.setParamId(param1.getId());
                        parameter.setTransmissionParamId("");
                        parameter.setStatus(param1.getStatus());
                    }

                }
            }
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public FuelVehicle findFluxSensorByVid(String vehicleId) {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }

        List<FuelVehicle> fuelVehicles = new ArrayList<>();
        FluxSensorBindQuery query = new FluxSensorBindQuery();
        query.setVehicleId(vehicleId);

        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        String userUuidById = userService.getCurrentUserUuid();
        if (StringUtils.isNotBlank(userUuidById) && orgList != null && orgList.size() > 0) {
            fuelVehicles = fluxSensorBindDao.findVehicleSensor(query, userUuidById, orgList);
        }

        if (fuelVehicles.size() == 0) {
            return null;
        }

        FuelVehicle fuelVehicle = fuelVehicles.get(0);

        // ????????????
        if (StringUtils.isNotBlank(fuelVehicle.getVehicleId()) && StringUtils.isNotBlank(fuelVehicle.getId())) {
            List<Directive> paramlist1 =
                parameterDao.findParameterByType(fuelVehicle.getVehicleId(), fuelVehicle.getId(), "2"); // 2:??????
            Directive param1;
            if (paramlist1 != null && paramlist1.size() > 0) {
                param1 = paramlist1.get(0);
                fuelVehicle.setParamId(param1.getId());
                fuelVehicle.setTransmissionParamId("");
                fuelVehicle.setStatus(param1.getStatus());
            }
        }
        return fuelVehicle;
    }

    /**
     * ????????????????????????????????????????????????
     */
    @Override
    public List<FuelVehicle> findReferenceVehicle() throws Exception {
        List<FuelVehicle> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotBlank(userId) && orgList != null && orgList.size() > 0) {
            
            List<String> reportDeviceTypes = Arrays.asList(ProtocolEnum.REPORT_DEVICE_TYPE);
            list = fluxSensorBindDao.findFuelVehicle(userId, orgList, reportDeviceTypes);

        }
        return list;
    }

    /**
     * ?????????????????????????????????????????????
     */
    @Override
    public JsonResultBean addFluxSensorBind(FluxSensorBindForm form, String ipAddress) {
        // ????????????id ??????????????????????????????????????????
        if (StringUtils.isNotBlank(findFuelVehicleByVid(form.getVehicleId()).getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundOilwear);
        }
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
        form.setCreateDataTime(new Date()); // ????????????
        boolean result = fluxSensorBindDao.addFluxSensorBind(form);
        if (result) {
            // ??????????????????????????????
            addVehicleSensorCache(form);
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
            Objects.requireNonNull(vehicleInfo);
            String brand = vehicleInfo.getName();
            String plateColor = "";
            if (vehicleInfo.getPlateColor() != null) {
                plateColor = vehicleInfo.getPlateColor().toString();
            }
            String groupName = vehicleInfo.getOrgName();
            String msg = "???????????????" + brand + "( @" + groupName + ") ??????????????????";
            logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            return new JsonResultBean(JsonResultBean.SUCCESS, boundSeccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT, boundFail);
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void addVehicleSensorCache(FluxSensorBindForm form) {
        FluxSensor fluxSensor = fluxSensorDao.findById(form.getOilWearId());
        RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST.of(), form.getVehicleId(),
            fluxSensor.getOilWearNumber());
    }

    @Override
    public FuelVehicle findFuelVehicleById(String id) throws Exception {
        if (id != null && !"".equals(id)) {
            FuelVehicle fuel = fluxSensorBindDao.findFuelVehicleById(id);
            if (fuel != null) {
                // ??????????????????
                String fuelType = fuel.getFuelSelect();
                if ("??????".equals(fuelType)) {
                    fuel.setFuelSelect("01");
                } else if ("??????".equals(fuelType)) {
                    fuel.setFuelSelect("02");
                } else if ("?????????".equals(fuelType)) {
                    fuel.setFuelSelect("03");
                } else {
                    fuel.setFuelSelect("01");
                }

                // ?????????
                fuel.setBaudRateStr(BaudRateUtil.getBaudRateVal(Integer.valueOf(fuel.getBaudRate())));
                // ????????????
                fuel.setInertiaCompEnStr(CompEnUtil.getCompEnVal(fuel.getInertiaCompEn()));
                // ????????????
                fuel.setFilterFactorStr(FilterFactorUtil.getFilterFactorVal(fuel.getFilterFactor()));
                // ??????
                fuel.setParityStr(ParityCheckUtil.getParityCheckVal(Integer.valueOf(fuel.getParity())));
                // ??????????????????
                fuel.setAutoUploadTimeStr(UploadTimeUtil.getUploadTimeVal(Integer.parseInt(fuel.getAutoUploadTime())));
            }
            return fuel;
        }
        return null;
    }

    @Override
    public JsonResultBean updateFluxSensorBind(FluxSensorBindForm form, String ipAddress) throws Exception {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
        form.setUpdateDataTime(new Date()); // ????????????
        // ???????????????????????????
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId(), "2");
        boolean result = fluxSensorBindDao.updateFluxSensorBind(form);
        if (result) {
            // ??????????????????????????????
            addVehicleSensorCache(form);
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor =
                    Objects.isNull(vehicleInfo.getPlateColor()) ? "" : String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "???????????????" + brand + "( @" + groupName + ") ??????????????????";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteFluxSensorBind(String bindId, String ipAddress) throws Exception {
        String[] item = bindId.split(",");
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        if (item.length > 0) {
            for (String id : item) {
                FuelVehicle fuelVehicle = findFuelVehicleById(id);
                boolean result = fluxSensorBindDao.deleteFluxSensorBind(id);
                if (result && fuelVehicle != null) {
                    // ???????????????????????????
                    RedisHelper.hdel(RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST.of(), fuelVehicle.getVehicleId());
                    BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(fuelVehicle.getVehicleId());
                    Objects.requireNonNull(vehicleInfo);
                    brand = fuelVehicle.getBrand(); // ?????????
                    plateColor =
                        Objects.isNull(vehicleInfo.getPlateColor()) ? "" : String.valueOf(vehicleInfo.getPlateColor());
                    String number = fuelVehicle.getOilWearNumber(); // ?????????????????????
                    // ????????????
                    String groupName = vehicleInfo.getOrgName();
                    message.append("???????????? : ").append(brand).append(" ( @").append(groupName).append(" ) ?????? ")
                        .append(number).append(" (???????????????) <br/>");
                }

            }
        }
        if (!"".equals(message.toString())) {
            if (item.length != 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????????????????");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public FuelVehicle findFuelVehicleByVid(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return fluxSensorBindDao.findFuelVehicleByVid(vehicleId);
        }
        return null;
    }

    @Override
    public JsonResultBean sendFuel(String sendParam, String ipAddress) throws Exception {
        List<JSONObject> paramList = JSON.parseArray(sendParam, JSONObject.class);
        StringBuilder errorMsg = new StringBuilder("????????????<br/>");
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        if (paramList != null && paramList.size() > 0) {
            for (JSONObject obj : paramList) {
                String parameterName = "";
                String vehicleId = "";
                String paramId = "";
                if (obj.get("fluxVehicleId") != null && !"".equals(obj.get("fluxVehicleId"))) {
                    parameterName = obj.get("fluxVehicleId").toString();
                }
                if (obj.get("vehicleId") != null && !"".equals(obj.get("vehicleId"))) {
                    vehicleId = obj.get("vehicleId").toString();
                }
                if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
                    paramId = obj.get("paramId").toString();
                }
                if (parameterName != null && !"".equals(parameterName) && vehicleId != null && !"".equals(vehicleId)) {
                    // ???????????????????????????????????????
                    String paramType = "2"; // ??????
                    FuelVehicle fuelVehicle = findFuelVehicleById(parameterName); // ???????????????????????????
                    if (fuelVehicle == null) {
                        errorMsg.append("?????????????????????<br/>");
                        continue;
                    }
                    // ????????????
                    sendOilSensor(paramId, vehicleId, paramType, parameterName, fuelVehicle);
                    BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
                    Objects.requireNonNull(vehicleInfo);
                    brand = vehicleInfo.getName();
                    if (vehicleInfo.getPlateColor() != null) {
                        plateColor = vehicleInfo.getPlateColor().toString();
                    }
                    String groupName = vehicleInfo.getOrgName();
                    message.append("???????????? : ").append(brand).append(" ( @").append(groupName).append(" ) ??????????????????</br>");
                }
            }
            if (!"".equals(message.toString())) {
                if (paramList.size() != 1) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????");
                } else {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "", brand, plateColor);
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT, errorMsg.toString());
    }

    public String sendOilSensor(String paramId, String vehicleId, String paramType, String parameterName,
        FuelVehicle fuelVehicle) throws Exception {
        // ???????????????????????????
        paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, "2");
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        Objects.requireNonNull(vehicle);
        String deviceNumber = vehicle.getDeviceNumber();
        vehicle.setId(vehicleId);
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // ??????????????????
            // ????????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicle.getId(), paramType, parameterName);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicle.getId());
            f3SendStatusProcessService.updateSendParam(sendParam, 1);

            wsOilConsumeService.sendOilBenchmarkCommand(vehicle, fuelVehicle, msgSN);
        } else { // ???????????????
            int status = 5; // ???????????????
            msgSN = 0;
            // ??????????????????
            updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
        return msgSN.toString();
    }

    /**
     * ???????????????????????????
     * @param paramId       ????????????id
     * @param vehicleId     ??????id
     * @param paramType     ??????????????????
     * @param msgSN         ?????????
     * @param parameterName ??????id
     * @author wangying
     */
    public void updateParameterStatus(String paramId, int msgSN, int status, String vehicleId, String paramType,
        String parameterName) throws Exception {
        if (StringUtils.isNotBlank(paramId)) {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(paramId);
            // ???????????? ??????????????????
            parameterDao.updateMsgSNAndNameById(paramIds, msgSN, status, parameterName, 1); // 1 : ???????????????
        } else {
            DirectiveForm form = new DirectiveForm();
            form.setDownTime(new Date());
            form.setMonitorObjectId(vehicleId);
            form.setParameterName(parameterName);
            form.setStatus(status);
            form.setParameterType(paramType);
            form.setSwiftNumber(msgSN);
            form.setReplyCode(1);
            // ????????????
            parameterDao.addDirective(form);
        }
    }

    /**
     * ????????????
     * @author lifudong
     */
    @Override
    public JsonResultBean updateWirelessUpdate(WirelessUpdateParam wirelessParam, String vehicleId, Integer commandType,
        String ipAddress) throws Exception {
        List<MonitorCommandBindForm> bindFormList = new ArrayList<>();
        WirelessUpdateParam wirelessParamCopy = new WirelessUpdateParam();
        BeanUtils.copyProperties(wirelessParam, wirelessParamCopy, "id");
        commandService.deleteParamSetting(vehicleId, commandType);
        commandService.addWirelessUpdateParam(wirelessParamCopy);
        MonitorCommandBindForm bindForm = new MonitorCommandBindForm();
        Field field = wirelessParamCopy.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        bindForm.setVid(vehicleId);
        bindForm.setParamId((String) field.get(wirelessParamCopy));
        bindForm.setCommandType(commandType);
        bindForm.setCreateDataTime(new Date());
        bindForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        bindFormList.add(bindForm);
        commandService.addCommandBind(bindFormList);

        DeviceCommand wiDeviceCommand = new DeviceCommand();
        wiDeviceCommand.setCw(1);
        String param = wirelessParam.sendParamString();
        wiDeviceCommand.setParam(param);

        BindDTO monitorMap = VehicleUtil.getBindInfoByRedis(vehicleId);
        Objects.requireNonNull(monitorMap);
        String deviceId = monitorMap.getDeviceId();
        String deviceNumber = monitorMap.getDeviceNumber();
        String simcardNumber = monitorMap.getSimCardNumber();
        String deviceType = monitorMap.getDeviceType();

        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        String paramId = sendHelper.getLastSendParamID(vehicleId, bindForm.getId(), commandType.toString());
        if (msgSN != null) {
            paramId = sendHelper
                .updateParameterStatus(paramId, msgSN, 4, vehicleId, commandType.toString(), bindForm.getId());
            // ??????
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 4);

            // ??????????????????
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);

            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);

            sendTxtService.deviceCommand(simcardNumber, wiDeviceCommand, msgSN, deviceId, deviceType, null);
        } else {
            sendHelper.updateParameterStatus(paramId, 0, 5, vehicleId, commandType.toString(), bindForm.getId());
        }
        if (String.valueOf(msgSN) == null) {
            return new JsonResultBean(JsonResultBean.FAULT, upErrorTerminalOffLine);
        }

        String brand = monitorMap.getName();
        String plateColor = monitorMap.getPlateColor().toString();
        String groupName = monitorMap.getOrgName();
        String logMsg = "???????????????" + brand + "(@" + groupName + ") ??????????????????????????????????????????:" + msgSN;
        logSearchService.addLog(ipAddress, logMsg, "3", "??????????????????", brand, plateColor);

        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSN));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }

    @Override
    public List<FuelVehicle> findReferenceVehicleByProtocols(List<Integer> protocols) {
        List<FuelVehicle> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotBlank(userId) && orgList != null && orgList.size() > 0) {
            
            list =
                fluxSensorBindDao.findFuelVehicleByProtocols(userId, orgList, protocols);

        }
        return list;
    }
}
