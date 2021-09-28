package com.zw.platform.service.oilVehicleSetting.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.param.F3SensorParamQuery;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.ShapeUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.PersonLoadParam;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilVehicleSettingForm;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.RodSensorDao;
import com.zw.platform.repository.vas.BasicManagementDao;
import com.zw.platform.repository.vas.FuelTankManageDao;
import com.zw.platform.repository.vas.LoadAdDao;
import com.zw.platform.repository.vas.LoadSensorDao;
import com.zw.platform.repository.vas.LoadVehicleSettingDao;
import com.zw.platform.repository.vas.OilVehicleSettingDao;
import com.zw.platform.repository.vas.WorkHourSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.loadmgt.LoadVehicleSettingService;
import com.zw.platform.service.loadmgt.impl.LoadVehicleSettingServiceImpl;
import com.zw.platform.service.monitoring.RealTimeCommandService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.oilmgt.impl.FluxSensorBindServiceImpl;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.service.workhourmgt.WorkHourSettingService;
import com.zw.platform.service.workhourmgt.impl.WorkHourSettingServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.WsOilSensorCommandService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月09日 16:01
 */
@Service
public class F3OilVehicleSettingServiceImpl implements F3OilVehicleSettingService, IpAddressService {
    private static final Logger log = LogManager.getLogger(F3OilVehicleSettingServiceImpl.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
    private static final Integer MAP_UPGRADE_TYPE = 9;
    private static final Integer PERIPHERAL_DESIGN_TYPE = 8;
    private static final Integer RESET_SETTING = 10;
    @Value("${terminal.off.line}")
    private String terminalOffLine;
    @Value("${up.error.terminal.off.line}")
    private String upErrorTerminalOffLine;
    @Autowired
    private RealTimeCommandService commandService;
    @Autowired
    private FuelTankManageDao fuelTankManageDao;
    @Autowired
    private FuelTankManageService fuelTankManageService;
    @Autowired
    private SendTxtService sendTxtService;
    @Autowired
    private BasicManagementDao basicManagementDao;
    @Autowired
    private SendHelper sendHelper;
    @Autowired
    private WsOilSensorCommandService wsOilSensorCommandService;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private ParameterDao parameterDao;
    @Autowired
    private RodSensorDao rodSensorDao;
    @Autowired
    private FluxSensorBindService fluxSensorBindService;
    @Autowired
    private OilVehicleSettingDao oilVehicleSettingDao;
    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;
    @Autowired
    private FluxSensorBindServiceImpl fluxSensorBindServiceImpl;
    @Autowired
    private LogSearchService logSearchService;
    @Autowired
    private WorkHourSettingService workHourSettingService;
    @Autowired
    private WorkHourSettingServiceImpl workHourSettingServiceImpl;
    @Autowired
    private WorkHourSettingDao workHourSettingDao;
    @Autowired
    private LoadVehicleSettingService loadVehicleSettingService;
    @Autowired
    private LoadVehicleSettingServiceImpl loadVehicleSettingServiceImpl;
    @Autowired
    private LoadSensorDao loadSensorDao;
    @Autowired
    private LoadVehicleSettingDao loadVehicleSettingDao;
    @Autowired
    private LoadAdDao loadAdDao;

    @Override
    public JsonResultBean sendF3SensorParam(String vid, String sensorId, String commandType) throws Exception {
        if (StringUtils.isBlank(vid) || StringUtils.isBlank(sensorId) || StringUtils.isBlank(commandType)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vid);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象不存在");
        }
        String username = SystemHelper.getCurrentUsername();
        String deviceId = bindDTO.getDeviceId();
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceType = bindDTO.getDeviceType();
        Integer msgSno = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramType = "0x8106-" + "0xF9" + sensorId.replaceAll("0x", "");
        String paramId = this.getLastSendParamId(vid, vid + paramType, paramType);
        // 设备已经注册
        if (msgSno != null) {
            UserCache.put(Converter.toBlank(msgSno), username);

            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSno, 4, vid, paramType, vid + paramType);

            List<F3SensorParamQuery> list = new ArrayList<>();
            F3SensorParamQuery query = new F3SensorParamQuery();
            query.setSign(Integer.parseInt(commandType.replaceAll("0x", ""), 16));
            query.setId(Integer.parseInt(sensorId.replaceAll("0x", ""), 16));
            list.add(query);

            // 订阅消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_PARAM_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            sendTxtService.queryF3SensorParam(simCardNumber, list, msgSno, deviceId, deviceType);
            // 设备未注册, 终端离线
        } else {
            msgSno = 0;
            sendHelper.updateParameterStatus(paramId, msgSno, 5, vid, paramType, vid + paramType);
            return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
        }
        String brand = bindDTO.getName();
        Integer plateColorInt = bindDTO.getPlateColor();
        String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        String groupName = bindDTO.getOrgName();
        String logMsg = "";
        switch (commandType) {
            //F4 查询类型
            case "f4":
                logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 获取F3-外设常规参数数据";
                break;
            //F5
            case "f5":
                logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 获取F3-外设通讯参数数据";
                break;
            //F6
            case "f6":
                logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 获取F3-外设传感器标定数据";
                break;
            //F8
            case "f8":
                logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 获取F3-外设传感器基本数据";
                break;
            case "f0":
                logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 获取OBD基本数据";
                break;
            default:
                break;
        }
        logSearchService.addLog(getIpAddress(), logMsg, "3", "", brand, plateColor);
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSno));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }

    @Override
    public JsonResultBean sendF3SensorPrivateParam(String vehicleId, String sensorID, String commandStr,
        String ipAddress, String sign) throws Exception {
        BindDTO monitorConfig = VehicleUtil.getBindInfoByRedis(vehicleId);

        if (monitorConfig == null) {
            return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
        }
        String deviceNumber = monitorConfig.getDeviceNumber();
        String deviceId = monitorConfig.getDeviceId();
        String simcardNumber = monitorConfig.getSimCardNumber();

        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        String paramType = "0x8106-" + "0xF9" + sensorID.replaceAll("0x", "");
        String paramId = this.getLastSendParamId(vehicleId, vehicleId + paramType, paramType);
        if (msgSN != null) { // 设备已经注册
            // 订阅回应的user
            String username = SystemHelper.getCurrentUsername();
            UserCache.put(Converter.toBlank(msgSN), username);

            // 下发参数
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, vehicleId + paramType);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 订阅推送消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);

            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);

            sendTxtService
                .getF3SensorPrivateParam(simcardNumber, Integer.parseInt(sensorID, 16), commandStr, msgSN, deviceId,
                    monitorConfig);
        } else { // 设备未注册
            msgSN = 0;
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, vehicleId + paramType);
        }
        if (String.valueOf(msgSN) != null && !String.valueOf(msgSN).equals("0")) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                // 模块标识(1：油量车辆设置 2：油耗车辆设置 3:工时车辆设置)
                String module = "";
                switch (sign) {
                    case "1":
                        module = "油量车辆设置";
                        break;
                    case "2":
                        module = "油耗车辆设置";
                        break;
                    case "3":
                        module = "工时车辆设置";
                        break;
                    default:
                        break;
                }
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + module + " 获取F3传感器私有数据";
                logSearchService.addLog(ipAddress, logMsg, "3", "", brand, plateColor);
            }
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", String.valueOf(msgSN));
            json.put("userName", username);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine); // 返回终端离线
    }

    /**
     * 修改载重常规参数设置
     * @param setting   workHourSettingInfo
     * @param dealType
     * @param ipAddress ip
     * @param mark      0:常规参数下发/参数下发; 1:基值修正下发
     * @return JsonResultBean
     * @throws Exception ex
     */
    @Override
    public JsonResultBean updateLoadSetting(LoadVehicleSettingInfo setting, String dealType, String ipAddress, int mark)
        throws Exception {
        // 补偿使能
        setting.setCompensate(CompEnUtil.getCompEn(setting.getCompensateStr()));
        // 滤波系数
        setting.setFilterFactor(FilterFactorUtil.getFilterFactor(setting.getFilterFactorStr()));
        PersonLoadParam personLoadParam = setting.getPersonLoadParam();
        dealPersonUnitAndWay(personLoadParam);
        setting.setPersonLoadParam(personLoadParam);
        LoadVehicleSettingInfo loadVehicleSettingInfo =
            loadVehicleSettingService.getSensorVehicleByBindId(setting.getId());
        // report 传感器参数
        if ("report".equals(dealType)) {
            //修改sensor_vehicle
            LoadVehicleSettingSensorForm form = new LoadVehicleSettingSensorForm();
            // 参数id
            form.setId(setting.getId());
            form.setParamId(setting.getParamId());
            form.setVehicleId(setting.getVehicleId());
            form.setPersonLoadParamJSON(JSONObject.toJSON(setting.getPersonLoadParam()).toString());
            String paramType = "F3-8103-loadSensor0";
            if ("70".equals(setting.getSensorPeripheralID())) {
                paramType = "F3-8103-loadSensor0";
            }
            if ("71".equals(setting.getSensorPeripheralID())) {
                paramType = "F3-8103-loadSensor1";
            }
            loadVehicleSettingService.updateWorkSettingBind(form, ipAddress, paramType);
            // 修改补偿使能
            LoadSensorForm loadSensorForm = new LoadSensorForm();
            loadSensorForm.setId(loadVehicleSettingInfo.getSensorId());
            loadSensorForm.setCompensate(setting.getCompensate());
            loadSensorForm.setFilterFactor(setting.getFilterFactor());
            loadSensorForm.setUpdateDataTime(new Date());
            loadSensorForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            loadSensorDao.updateWorkHourSensorSomeField(loadSensorForm);

        }
        // 参数类型F3(平台)-8103(协议类型)-F4(标识定义-常规参数)-workHour(自定义)
        String paramType = "F3-8103-loadSensor" + setting.getSensorSequence();
        // 下发 状态
        String paramId = this.getLastSendParamId(setting.getVehicleId(), setting.getParamId(), paramType);
        if (dealPersonParam(loadVehicleSettingInfo)) {
            return new JsonResultBean(JsonResultBean.FAULT, "平台设置参数数据有误");
        }
        if (Objects.isNull(loadVehicleSettingInfo)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未查询到绑定的传感器");
        }
        loadVehicleSettingInfo.setPersonLoadParam(
            JSONObject.parseObject(loadVehicleSettingInfo.getPersonLoadParamJSON(), PersonLoadParam.class));
        // 下发 载重传感器
        String msgId = loadVehicleSettingServiceImpl
            .sendLoadSensor(paramId, setting.getVehicleId(), paramType, setting.getId(), loadVehicleSettingInfo, mark);
        if (!"0".equals(msgId)) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(setting.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 修改常规参数修正,处理编号:" + msgId;
                logSearchService.addLog(ipAddress, logMsg, "3", "载重车辆设置", brand, plateColor);
            }
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgId);
            json.put("userName", username);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    /**
     * 处理单位和计重方式
     */
    private void dealPersonUnitAndWay(PersonLoadParam personLoadParam) {
        // 0:单计重 1:双计重 2:四计重
        if ("单计重".equals(personLoadParam.getLoadMeterWayStr())) {
            personLoadParam.setLoadMeterWay("0");
        }
        if ("双计重".equals(personLoadParam.getLoadMeterWayStr())) {
            personLoadParam.setLoadMeterWay("1");
        }
        if ("四计重".equals(personLoadParam.getLoadMeterWayStr())) {
            personLoadParam.setLoadMeterWay("2");
        }
        // 0:0.1kg 1:1kg 2:10kg 3:100kg
        if ("0.1kg".equals(personLoadParam.getLoadMeterUnitStr())) {
            personLoadParam.setLoadMeterUnit("0");
        }
        if ("1kg".equals(personLoadParam.getLoadMeterUnitStr())) {
            personLoadParam.setLoadMeterUnit("1");
        }
        if ("10kg".equals(personLoadParam.getLoadMeterUnitStr())) {
            personLoadParam.setLoadMeterUnit("2");
        }
        if ("100kg".equals(personLoadParam.getLoadMeterUnitStr())) {
            personLoadParam.setLoadMeterUnit("3");
        }
        personLoadParam.setLoadMeterWayStr("");
        personLoadParam.setLoadMeterUnitStr("");

    }

    private boolean dealPersonParam(LoadVehicleSettingInfo loadVehicleSettingInfo) {
        boolean flag = false;
        //        PersonLoadParam personLoadParam =
        // JSONObject.parseObject(loadVehicleSettingInfo.getPersonLoadParamJSON(), PersonLoadParam.class);
        //        if(personLoadParam.getOverLoadValue().compareTo(new BigDecimal(65535))>0){
        //            flag=true;
        //        }if(personLoadParam.getFullLoadValue().compareTo(new BigDecimal(65535))>0){
        //            flag=true;
        //        }
        //        if(personLoadParam.getLightLoadValue().compareTo(new BigDecimal(65535))>0){
        //            flag=true;
        //        }if(personLoadParam.getNoLoadValue().compareTo(new BigDecimal(65535))>0){
        //            flag=true;
        //        }
        return flag;
    }

    /**
     * 修改载重标定设置
     * @param form
     * @param setting
     * @param ip
     * @return
     */
    @Override
    public JsonResultBean updateLoadAdSetting(LoadVehicleSettingSensorForm form, LoadVehicleSettingInfo setting,
        String ip) throws Exception {
        // 如果以传感器为准,清除之前平台相应传感的下发状态,添加数据库
        if (!(form.getAdParamJson() == null || "".equals(form.getAdParamJson()) || "[]"
            .equals(form.getAdParamJson()))) {
            loadAdDao.deleteAdLoad(form.getId());
            String id = UUID.randomUUID().toString();
            String sensorId = form.getSensorId();
            String vehicleId = form.getVehicleId();
            String currentUsername = SystemHelper.getCurrentUsername();
            String adParamJson = form.getAdParamJson();
            loadAdDao.addByBatch(id, "1", form.getId(), sensorId, vehicleId, currentUsername, adParamJson);
        }
        String calibrationParamType = "F3-8103-loadCalibration" + setting.getSensorSequence();
        // 封装标定数据
        String paramId = "";
        // 标定数据下发
        LoadVehicleSettingInfo sensorVehicleByBindId = loadVehicleSettingDao.findSensorVehicleByBindId(form.getId());
        int mark = 0;
        if (0 == setting.getSensorSequence()) {
            mark = 0x70;
        }
        if (1 == setting.getSensorSequence()) {
            mark = 0x71;
        }
        // 标定下发
        String msgid = loadVehicleSettingServiceImpl
            .sendLoadCalibration(paramId, form.getVehicleId(), calibrationParamType, form.getId(),
                sensorVehicleByBindId, mark);
        if (!"0".equals(msgid)) {
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgid);
            json.put("userName", username);
            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String orgName = vehicleInfo.get("orgName");
                String logMsg = "监控对象：" + vehicleInfo.get("name") + "( @" + orgName + " 修改标定参数修正下传,处理编号:" + msgid;
                logSearchService.addLog(ip, logMsg, "3", "载重车辆设置", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    @Override
    public JsonResultBean updateWirelessUpdate(WirelessUpdateParam wirelessParam, String vehicleId, Integer commandType,
        String ipAddress, int mark) throws Exception {
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

        BindDTO reusltMap = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceId = reusltMap.getDeviceId();
        String deviceNumber = reusltMap.getDeviceNumber();
        String simcardNumber = reusltMap.getSimCardNumber();
        String deviceType = reusltMap.getDeviceType();

        WirelessUpdateParam wiParam = wirelessParam;
        DeviceCommand wiDeviceCommand = new DeviceCommand();
        String param = wiParam.sendParamString();
        String restoreType = wiParam.getRestoreType();
        //远程升级 命令字 8代表外设设计，9代表路网地图升级，10恢复出厂设置  1(为原来逻辑 暂不知其意思)
        //控制类型 0升级 1恢复设置
        if (wiParam.getControlType() == 0 && restoreType != null) {
            wiDeviceCommand.setCw(restoreType.equals("0xE7") ? MAP_UPGRADE_TYPE : PERIPHERAL_DESIGN_TYPE);
            // String prefix = "/M_Upgrade_" + restoreType.substring(restoreType.length() - 2, restoreType.length());
            // if (!wiParam.getFirmwareVersion().startsWith("/")) {
            //     prefix = prefix + "/";
            // }
            // //中位标准 拼接类型
            // wiParam.setFirmwareVersion(prefix + wiParam.getFirmwareVersion());
            if (PERIPHERAL_DESIGN_TYPE.equals(wiDeviceCommand.getCw())) {
                wiParam.setWTimeLimit(null);
            }
        } else if (wiParam.getControlType() == 1) {
            wiDeviceCommand.setCw(RESET_SETTING);
            param = wiParam.getRestoreType();
        } else {
            wiDeviceCommand.setCw(1);
        }
        wiDeviceCommand.setParam(param);
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        String paramId = this.getLastSendParamId(vehicleId, bindForm.getId(), commandType.toString());
        if (msgSN != null) {
            paramId = sendHelper
                .updateParameterStatus(paramId, msgSN, 4, vehicleId, commandType.toString(), bindForm.getId());
            if (mark != 3) {
                // 标定下发
                SendParam sendParam = new SendParam();
                sendParam.setMsgSNACK(msgSN);
                sendParam.setParamId(paramId);
                sendParam.setVehicleId(vehicleId);
                f3SendStatusProcessService.updateSendParam(sendParam, 4);
            }

            // 订阅推送消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);
            //主动安全远程升级(川冀标订阅0108)
            if (mark == 3) {
                SubscibeInfo subscibeInfo =
                    new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_UPLOAD_ACK,
                        1);
                SubscibeInfoCache.getInstance().putTable(subscibeInfo);
            }
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
        if (reusltMap != null) {
            String brand = reusltMap.getName();
            String plateColor =
                Objects.isNull(reusltMap.getPlateColor()) ? "" : String.valueOf(reusltMap.getPlateColor());
            String groupName = reusltMap.getOrgName();
            if (mark == 0) {
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 油位传感器远程升级；升级编号:" + msgSN;
                logSearchService.addLog(ipAddress, logMsg, "3", "油量车辆设置", brand, plateColor);
            } else if (mark == 1) {
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 工时传感器远程升级；升级编号:" + msgSN;
                logSearchService.addLog(ipAddress, logMsg, "3", "工时车辆设置", brand, plateColor);
            } else if (mark == 2) {
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 载重传感器远程升级；升级编号:" + msgSN;
                logSearchService.addLog(ipAddress, logMsg, "3", "载重车辆设置", brand, plateColor);
            } else if (mark == 3) {
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 主动安全远程升级；升级编号:" + msgSN;
                logSearchService.addLog(ipAddress, logMsg, "3", "主动安全参数设置", brand, plateColor);
            }

        }
        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSN));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }

    public OilVehicleSetting findOilBoxVehicleByBindId(String id) {
        if (StringUtils.isNotBlank(id)) {
            OilVehicleSetting list = oilVehicleSettingDao.findOilBoxVehicleByBindId(id);
            list.setBaudRateStr(BaudRateUtil.getBaudRateVal(Integer.valueOf(list.getBaudRate())));
            list.setCompensationCanMakeStr(CompEnUtil.getCompEnVal(list.getCompensationCanMake()));
            list.setFilteringFactorStr(FilterFactorUtil.getFilterFactorVal(Integer.valueOf(list.getFilteringFactor())));
            list.setOddEvenCheckStr(ParityCheckUtil.getParityCheckVal(list.getOddEvenCheck()));
            list.setAutomaticUploadTimeStr(
                UploadTimeUtil.getUploadTimeVal(Integer.parseInt(list.getAutomaticUploadTime())));
            list.setShapeStr(ShapeUtil.getShapeVal(Integer.parseInt(list.getShape())));
            return list;
        }
        return null;
    }

    /**
     * 修改常规参数设置
     * @param setting
     * @return
     */
    @Override
    public JsonResultBean updateRoutineSetting(OilVehicleSetting setting, String dealType, String ipAddress)
        throws Exception {
        // 自动上传时间
        setting.setAutomaticUploadTime("0" + UploadTimeUtil.getUploadTime(setting.getAutomaticUploadTimeStr()));
        // 补偿使能
        setting.setCompensationCanMake(CompEnUtil.getCompEn(setting.getCompensationCanMakeStr()));
        // 波特率系数
        setting.setFilteringFactor("0" + FilterFactorUtil.getFilterFactor(setting.getFilteringFactorStr()));
        // 油箱形状
        setting.setShape("0" + ShapeUtil.getShape(setting.getShapeStr()));
        if ("report".equals(dealType)) {
            // 修改车辆燃油 1
            VehicleDTO vehicleDTO = newVehicleDao.getDetailById(setting.getVehicleId());
            if (vehicleDTO != null && setting.getFuelOil() != null && (vehicleDTO.getFuelType() == null
                || !vehicleDTO.getFuelType().equals(setting.getFuelOil()))) {
                String fuelType = "";
                VehicleForm vehicleForm = new VehicleForm();
                vehicleForm.setId(setting.getVehicleId());
                if (setting.getFuelOil().equals("柴油")) {
                    fuelType = "0#柴油";
                } else if (setting.getFuelOil().equals("汽油")) {
                    fuelType = "93#汽油";
                } else if (setting.getFuelOil().equals("LNG")) {
                    fuelType = "LNG";
                } else if (setting.getFuelOil().equals("CNG")) {
                    fuelType = "CNG";
                }
                List<FuelType> fts = this.basicManagementDao.findFuelType(fuelType);
                if (fts == null || fts.size() == 0) {
                    String errMessage = "未设置燃料[" + fuelType + "]类型";
                    return new JsonResultBean(JsonResultBean.FAULT, errMessage);
                }
                vehicleForm.setFuelType(fts.get(0).getId());
                this.newVehicleDao.updateVehicleFuelType(vehicleForm);
            }
            // 修改通讯参数 2
            RodSensorForm form = new RodSensorForm();
            form.setId(setting.getSensorType());
            form.setFilteringFactor(setting.getFilteringFactor());
            form.setCompensationCanMake(Short.parseShort(setting.getCompensationCanMake().toString()));
            rodSensorDao.updateParamById(form);

            // 修改油箱油箱形状/参数 4
            FuelTankForm fuelTankForm = new FuelTankForm();
            fuelTankForm.setId(setting.getOilBoxId());
            fuelTankForm.setShape(setting.getShape());
            fuelTankForm.setWidth(setting.getWidth());
            fuelTankForm.setHeight(setting.getHeight());
            fuelTankForm.setBoxLength(setting.getBoxLength());
            this.fuelTankManageService.updateFuelTank(fuelTankForm, ipAddress);

            // 修改车辆设置的 上传时间、加油/漏油量阈值 7
            OilVehicleSettingForm oilVehicleSettingForm = new OilVehicleSettingForm();
            oilVehicleSettingForm.setId(setting.getId());
            oilVehicleSettingForm.setAddOilAmountThreshol(setting.getAddOilAmountThreshol());
            oilVehicleSettingForm.setAddOilTimeThreshold(setting.getAddOilTimeThreshold());
            oilVehicleSettingForm.setAutomaticUploadTime(setting.getAutomaticUploadTime());
            oilVehicleSettingForm.setSeepOilAmountThreshol(setting.getSeepOilAmountThreshol());
            oilVehicleSettingForm.setSeepOilTimeThreshold(setting.getSeepOilTimeThreshold());
            oilVehicleSettingForm.setOutputCorrectionCoefficientB(setting.getOutputCorrectionCoefficientB());
            oilVehicleSettingForm.setOutputCorrectionCoefficientK(setting.getOutputCorrectionCoefficientK());
            this.oilVehicleSettingDao.updateParamOilSetting(oilVehicleSettingForm);
        }
        // 参数类型
        String paramType = "F3-4"; // 油箱
        // 下发状态
        String paramId = this.getLastSendParamId(setting.getVehicleId(), setting.getId(), paramType);
        OilVehicleSetting oilVehicle = this.findOilBoxVehicleByBindId(setting.getId());
        sendOilVehicleList(oilVehicle); // 组装下发数据
        // 油箱下发
        String msgId = sendOilTank(paramId, setting.getVehicleId(), paramType, setting.getId(), oilVehicle);
        if (!"0".equals(msgId)) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(setting.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor =
                    Objects.isNull(vehicleInfo.getPlateColor()) ? "" : String.valueOf(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 修改常规参数修正,处理编号:" + msgId;
                logSearchService.addLog(ipAddress, logMsg, "3", "油量车辆设置", brand, plateColor);
            }
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgId);
            json.put("userName", username);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    /**
     * 修改油耗常规参数设置
     * @param setting
     * @return
     */
    @Override
    public JsonResultBean updateFuelSetting(FuelVehicle setting, String dealType, String ipAddress) throws Exception {
        if ("".equals(dealType) || setting == null || "".equals(ipAddress)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 自动上传时间
        setting.setAutoUploadTime("0" + UploadTimeUtil.getUploadTime(setting.getAutoUploadTimeStr()));
        // 补偿使能
        setting.setInertiaCompEn(CompEnUtil.getCompEn(setting.getInertiaCompEnStr()));
        // 波特率系数
        setting.setFilterFactor(FilterFactorUtil.getFilterFactor(setting.getFilterFactorStr()));
        if ("report".equals(dealType)) {
            // 修改通讯参数
            RodSensorForm form = new RodSensorForm();
            form.setId(setting.getOilWearNumber());
            form.setFilteringFactor(setting.getFilterFactor().toString());
            form.setCompensationCanMake(Short.parseShort(setting.getInertiaCompEn().toString()));
            rodSensorDao.updateParamById(form);
            // 修改车辆设置的 上传时间、加油/漏油量阈值 
            FluxSensorBindForm settingForm = new FluxSensorBindForm();
            settingForm.setId(setting.getId());
            settingForm.setAutoUploadTime(setting.getAutoUploadTime());
            settingForm.setOutputCorrectionB(setting.getOutputCorrectionB());
            settingForm.setOutputCorrectionK(setting.getOutputCorrectionK());
            settingForm.setOilWearId(setting.getOilWearId());
            settingForm.setVehicleId(setting.getVehicleId());
            this.fluxSensorBindService.updateFluxSensorBind(settingForm, ipAddress);
        }
        // 参数类型
        String paramType = "2"; // 油耗
        // 下发状态
        String paramId = this.getLastSendParamId(setting.getVehicleId(), setting.getId(), paramType);
        FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(setting.getId());
        // 油箱下发
        String msgId = fluxSensorBindServiceImpl
            .sendOilSensor(paramId, setting.getVehicleId(), paramType, setting.getId(), fuelVehicle);
        if (!"0".equals(msgId)) {
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgId);
            json.put("userName", username);
            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(
                    setting.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String orgName = vehicleInfo.get("orgName");
                String logMsg = "监控对象：" + brand + "( @" + orgName + ")" + " 修改常规参数修正,处理编号:" + msgId;
                logSearchService.addLog(ipAddress, logMsg, "3", "油耗车辆设置", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    @Override
    public JsonResultBean updateDemarcateSetting(List<OilCalibrationForm> list, OilVehicleSetting setting,
        String ipAddress) throws Exception {
        if (list != null && list.size() > 0) {
            this.fuelTankManageDao.deleteOilCalibration(setting.getId());
            for (OilCalibrationForm ocb : list) {
                this.fuelTankManageDao.addOilCalibration(ocb);
            }
        }
        String paramType = "F3-5"; // 标定下发
        String calibrationParamId = "";
        // 标定
        List<FuelTankForm> calibrationList = fuelTankManageService.getOilCalibrationByBindId(setting.getId());
        // 标定下发
        String msgid =
            sendCalibration(calibrationParamId, setting.getVehicleId(), paramType, setting.getId(), calibrationList);
        if (!"0".equals(msgid)) {
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgid);
            json.put("userName", username);

            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(setting.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String groupName = vehicleInfo.get("orgName");
                String logMsg = "监控对象：" + vehicleInfo.get("name") + "( @" + groupName + " 修改标定参数修正下传,处理编号:" + msgid;
                logSearchService.addLog(ipAddress, logMsg, "3", "油量车辆设置", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    /**
     * TODO 油箱参数下发
     * @param paramId       下发id
     * @param vehicleId     车id
     * @param paramType     下发类型
     * @param parameterName 油箱数组
     * @return
     * @throws @Title: sendOilTank
     * @author wangying
     */
    public String sendOilTank(String paramId, String vehicleId, String paramType, String parameterName,
        OilVehicleSetting oilVehicle) throws Exception {
        // 获取车辆及设备信息
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();

        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
            // 油箱绑定下发
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 下发参数
            wsOilSensorCommandService.oilRodSensorCompose(oilVehicle, msgSN, vehicle);
        } else { // 设备未注册
            int status = 5; // 设备为注册
            msgSN = 0;
            // 油箱绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
        return Converter.toBlank(msgSN.toString());
    }

    /**
     * TODO 标定透传下发
     * @param paramId         参数下发id
     * @param vehicleId       车id
     * @param paramType       下发类型
     * @param calibrationList 标定数组
     * @return String 返回msgSN 油量标定界面，匹配指定用户弹出标定是否标定成功状态
     * @throws @Title: sendCalibration
     * @author wangying
     */
    public String sendCalibration(String paramId, String vehicleId, String paramType, String parameterName,
        List<FuelTankForm> calibrationList) throws Exception {
        // 获取车辆及设备信息
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        String deviceId = vehicle.getDeviceId();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);

            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);

            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);

            wsOilSensorCommandService.markDataCompose(msgSN, vehicle, calibrationList);
        } else { // 设备未注册
            int status = 5; // 设备为注册
            msgSN = 0;
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
        return Converter.toBlank(msgSN);
    }

    /**
     * 根据车辆、下发参数编号、下发类型获取最后一次下发的编号
     * @param vehicleId 车辆编号
     * @param paramId   下发参数编号
     * @param type      下发类型获
     */
    private String getLastSendParamId(String vehicleId, String paramId, String type) {
        List<Directive> directiveList = parameterDao.findParameterByType(vehicleId, paramId, type);
        if (CollectionUtils.isNotEmpty(directiveList)) {
            return directiveList.get(0).getId();
        }
        return "";
    }

    public void sendOilVehicleList(OilVehicleSetting setting) {
        if (setting != null) {
            // 组装燃料类型
            String fuelType = setting.getFuelOil();
            if ("0#柴油".equals(fuelType) || "-10#柴油".equals(fuelType) || "-20#柴油".equals(fuelType) || "-30#柴油"
                .equals(fuelType) || "-50#柴油".equals(fuelType)) {
                setting.setFuelOil("01");
            } else if ("89#汽油".equals(fuelType) || "90#汽油".equals(fuelType) || "92#汽油".equals(fuelType) || "93#汽油"
                .equals(fuelType) || "95#汽油".equals(fuelType) || "97#汽油".equals(fuelType) || "98#汽油".equals(fuelType)) {
                setting.setFuelOil("02");
            } else if ("LNG".equals(fuelType)) {
                setting.setFuelOil("03");
            } else if ("CNG".equals(fuelType)) {
                setting.setFuelOil("04");
            } else {
                setting.setFuelOil("01");
            }

            // 加油量阈值和楼油量阈值改为0.1升单位
            String addOilAmountThreshol = setting.getAddOilAmountThreshol();
            String seepOilAmountThreshol = setting.getSeepOilAmountThreshol();
            if (StringUtils.isNotBlank(addOilAmountThreshol)) {
                setting.setAddOilAmountThreshol(DECIMAL_FORMAT.format(Converter.toDouble(addOilAmountThreshol) * 10));
            }
            if (StringUtils.isNotBlank(seepOilAmountThreshol)) {
                setting.setSeepOilAmountThreshol(DECIMAL_FORMAT.format(Converter.toDouble(seepOilAmountThreshol) * 10));
            }
        }
    }

    @Override
    public JsonResultBean updateWorkHourSetting(WorkHourSettingInfo setting, String dealType, String ipAddress,
        int mark) throws Exception {
        // 补偿使能
        setting.setCompensate(CompEnUtil.getCompEn(setting.getCompensateStr()));
        // 滤波系数
        setting.setFilterFactor(FilterFactorUtil.getFilterFactor(setting.getFilterFactorStr()));
        // report 传感器参数
        if ("report".equals(dealType)) {
            //修改sensor_vehicle
            WorkHourSettingForm form = new WorkHourSettingForm();
            // 参数id
            form.setId(setting.getId());
            form.setParamId(setting.getParamId());
            form.setLastTime(setting.getLastTime());
            form.setThresholdVoltage(setting.getThresholdVoltage());
            form.setThresholdWorkFlow(setting.getThresholdWorkFlow());
            form.setThresholdStandbyAlarm(setting.getThresholdStandbyAlarm());
            form.setVehicleId(setting.getVehicleId());
            form.setSmoothingFactor(setting.getSmoothingFactor());
            form.setBaudRateCalculateNumber(setting.getBaudRateCalculateNumber());
            form.setThreshold(setting.getThreshold());
            Integer baudRateCalculateTimeScope = setting.getBaudRateCalculateTimeScope();
            if (Objects.nonNull(baudRateCalculateTimeScope)) {
                switch (baudRateCalculateTimeScope) {
                    case 10:
                        form.setBaudRateCalculateTimeScope(1);
                        break;
                    case 15:
                        form.setBaudRateCalculateTimeScope(2);
                        break;
                    case 20:
                        form.setBaudRateCalculateTimeScope(3);
                        break;
                    case 30:
                        form.setBaudRateCalculateTimeScope(4);
                        break;
                    case 60:
                        form.setBaudRateCalculateTimeScope(5);
                        break;
                    default:
                        break;
                }
            }
            workHourSettingService.updateWorkSettingBind(form, ipAddress);
            // 修改补偿使能
            WorkHourSettingInfo sensorVehicle = workHourSettingDao.getSensorVehicleById(setting.getId());
            WorkHourSensorForm workHourSensorForm = new WorkHourSensorForm();
            workHourSensorForm.setId(sensorVehicle.getSensorId());
            workHourSensorForm.setCompensate(setting.getCompensate());
            workHourSensorForm.setFilterFactor(setting.getFilterFactor());
            workHourSensorForm.setUpdateDataTime(new Date());
            workHourSensorForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            workHourSettingDao.updateWorkHourSensorSomeField(workHourSensorForm);

        }
        // 参数类型F3(平台)-8103(协议类型)-F4(标识定义-常规参数)-workHour(自定义)
        String paramType = "F3-8103-F4-workHour" + setting.getSensorSequence();
        // 下发状态
        String paramId = this.getLastSendParamId(setting.getVehicleId(), setting.getParamId(), paramType);
        WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(setting.getId());
        if (Objects.isNull(workHourSettingInfo)) {
            return new JsonResultBean(JsonResultBean.FAULT, "未查询到绑定的传感器");
        }
        workHourSettingInfo.setTotalAwaitBaseValue(setting.getTotalAwaitBaseValue());
        workHourSettingInfo.setTotalHaltBaseValue(setting.getTotalHaltBaseValue());
        workHourSettingInfo.setTotalWorkBaseValue(setting.getTotalWorkBaseValue());
        String msgId = workHourSettingServiceImpl
            .sendWorkHourSensor(paramId, setting.getVehicleId(), paramType, setting.getId(), workHourSettingInfo, mark);
        if (!"0".equals(msgId)) {
            final Map<String, String> vehicleInfo = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(setting.getVehicleId(), "name", "plateColor", "orgName"));
            if (vehicleInfo != null) {
                String brand = vehicleInfo.get("name");
                String plateColor = vehicleInfo.get("plateColor");
                String orgName = vehicleInfo.get("orgName");
                String logMsg = "监控对象：" + brand + "( @" + orgName + ")" + " 修改常规参数修正,处理编号:" + msgId;
                logSearchService.addLog(ipAddress, logMsg, "3", "工时车辆设置", brand, plateColor);
            }
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgId);
            json.put("userName", username);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    @Override
    public JsonResultBean updateWirelessUp(WirelessUpdateParam wirelessParam, String vehicleId, Integer commandType,
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

        VehicleDTO vehicleDTO = newVehicleDao.getDetailById(vehicleId);
        String deviceId = vehicleDTO.getDeviceId();
        String deviceNumber = vehicleDTO.getDeviceNumber();
        String simcardNumber = vehicleDTO.getSimCardNumber();
        String deviceType = vehicleDTO.getDeviceType();
        WirelessUpdateParam wiParam = wirelessParam;
        DeviceCommand wiDeviceCommand = new DeviceCommand();
        wiDeviceCommand.setCw(1);
        String param = wiParam.sendParamString();
        wiDeviceCommand.setParam(param);
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        String paramId = this.getLastSendParamId(vehicleId, bindForm.getId(), commandType.toString());
        if (msgSN != null) {
            paramId = sendHelper
                .updateParameterStatus(paramId, msgSN, 4, vehicleId, commandType.toString(), bindForm.getId());
            // 标定下发
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 4);

            // 订阅推送消息
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
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO != null) {
            String brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            String groupName = bindDTO.getOrgName();
            String logMsg = "监控对象：" + brand + "( @" + groupName + ")" + " 传感器远程升级；升级编号:" + msgSN;
            logSearchService.addLog(ipAddress, logMsg, "3", "主动安全参数设置", brand, plateColor);
        }
        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSN));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
    }
}
