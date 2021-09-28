package com.zw.platform.controller.peripheral;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.param.RemoteUpgradeSensorBasicInfo;
import com.zw.platform.domain.param.RemoteUpgradeTask;
import com.zw.platform.domain.vas.sensorUpgrade.SenosrMonitorQuery;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.sensor.RemoteUpgradeInstance;
import com.zw.platform.service.sensor.SensorUpgradeService;
import com.zw.platform.util.RemoteUpgradeUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 传感器升级Controller
 * @author zhouzongbo on 2019/1/18 10:50
 */
@Controller
@RequestMapping("/v/sensorConfig/sensorUpgrade")
public class SensorUpgradeController {
    public static final Logger logger = LogManager.getLogger(SensorUpgradeController.class);

    @Autowired
    private SensorUpgradeService sensorUpgradeService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    private static final String LIST_PAGE = "/vas/monitoring/remoteUpgrade";

    private static final String SEND_REMOTE_PAGE = "/vas/monitoring/sendRemoteUpgradePage";

    private static final String BASICINFO_PAGE = "/vas/monitoring/basicInfo";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 由于前端周六要来开发,先放个页面在这里
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 获取传感器列表
     */
    @RequestMapping(value = "/getSensorList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getSensorList() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("sensorData", sensorUpgradeService.getSensorIdList());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("获取远程升级页面左侧传感器列表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据传感器id获取绑定传感器并轮询了的监控对象或对此型号传感器id进行升级过的监控对象
     */
    @RequestMapping(value = "/getBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getBindSensorMonitorInfo(SenosrMonitorQuery query) {
        try {
            return new PageGridBean(query, sensorUpgradeService.findMonitorByPage(query), true);
        } catch (Exception e) {
            logger.error("分页获取远程升级页面有轮询设置的监控对象失败", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 获取远程升级页面
     */
    @RequestMapping(value = "/getSendRemoteUpgradePage_{monitorIds}_{sensorId}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView sendRemoteUpgrade(@PathVariable String monitorIds, @PathVariable Integer sensorId) {
        ModelAndView modelAndView = new ModelAndView(SEND_REMOTE_PAGE);
        List<RemoteUpgradeTask> remoteUpgradeTaskList = new ArrayList<>();
        if (StringUtils.isNotEmpty(monitorIds)) {
            List<String> monitorIdList = Arrays.stream(monitorIds.split(",")).collect(Collectors.toList());
            Map<String, BindDTO> bindInfoMap =
                VehicleUtil.batchGetBindInfosByRedis(monitorIdList, Lists.newArrayList("name", "deviceId"));
            RemoteUpgradeTask remoteUpgradeTask;
            for (String moId : monitorIdList) {
                BindDTO bindDTO = bindInfoMap.get(moId);
                if (bindDTO == null) {
                    continue;
                }
                String name = bindDTO.getName();
                String deviceId = bindDTO.getDeviceId();
                remoteUpgradeTask = RemoteUpgradeInstance.getInstance().getRemoteUpgradeTask(deviceId);
                if (Objects.isNull(remoteUpgradeTask)) {
                    remoteUpgradeTask = new RemoteUpgradeTask();
                    remoteUpgradeTask.setMonitorId(moId);
                    remoteUpgradeTask.setPlateNumber(name);
                    remoteUpgradeTask.setDeviceId(deviceId);
                } else {
                    Integer peripheralId = remoteUpgradeTask.getPeripheralId();
                    if (Objects.nonNull(peripheralId) && peripheralId.intValue() != sensorId) {
                        // 同一个终端只能同时给一个外设升级. 例如: a用户正在a终端进行21温度传感器升级,
                        // b用户给a终端进行22传感器升级, 则提示b用户a终端正在升级中
                        remoteUpgradeTask = new RemoteUpgradeTask();
                        remoteUpgradeTask.setMonitorId(moId);
                        remoteUpgradeTask.setPlateNumber(name);
                        remoteUpgradeTask.setDeviceId(deviceId);
                        remoteUpgradeTask.setIsStartUpgrade(true);
                        remoteUpgradeTask.setPlatformToF3Status(RemoteUpgradeUtil.PLATFORM_STATUS_ALREADY_UPGRADE);
                    }
                }
                remoteUpgradeTaskList.add(remoteUpgradeTask);
            }
        }
        modelAndView.addObject("remoteUpgradeTaskList", JSON.toJSONString(remoteUpgradeTaskList));
        modelAndView.addObject("peripheralId", sensorId);
        return modelAndView;
    }

    /**
     * "[{"sensorMonitorId":"1","paramId":"","monitorId":"1"},{"sensorMonitorId":"2","paramId":"","monitorId":"2"}]";
     * @param monitorIds   远程升级param
     * @param peripheralId 外设ID
     * @param file         file
     */
    @RequestMapping(value = "/sendRemoteUpgrade", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendRemoteUpgrade(String monitorIds, Integer peripheralId,
        @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return new JsonResultBean(JsonResultBean.FAULT, "升级文件不能为空!");
            }
            if (StringUtils.isEmpty(monitorIds)) {
                return new JsonResultBean(JsonResultBean.FAULT, "升级对象不能为空!");
            }
            if (Objects.isNull(peripheralId)) {
                return new JsonResultBean(JsonResultBean.FAULT, "外设不能为空!");
            }
            return sensorUpgradeService.sendRemoteUpgrade(monitorIds, peripheralId, file);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 中止远程升级
     */
    @RequestMapping(value = "/getTerminationUpgrade", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateTerminationUpgrade(String deviceId) {
        try {
            if (Objects.nonNull(deviceId)) {
                return sensorUpgradeService.updateTerminationUpgrade(deviceId);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空!");
        } catch (Exception e) {
            logger.error("中止远程升级异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 读取基本信息
     */
    @RequestMapping(value = { "/basicInfo_{id}_{sensorId}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String monitorId,
        @PathVariable("sensorId") String sensorId) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据车辆id查询车与传感器的绑定
            RemoteUpgradeSensorBasicInfo fuelVehicle = sensorUpgradeService.getBasicInfo(monitorId, sensorId);
            mav.addObject("result", fuelVehicle);
            return mav;
        } catch (Exception e) {
            logger.error("基本信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid         vid
     * @param commandType 基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4;
     * @param sensorID    外设ID
     */
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            logger.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
