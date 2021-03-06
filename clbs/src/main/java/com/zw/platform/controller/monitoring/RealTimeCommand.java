package com.zw.platform.controller.monitoring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceConnectServerParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.param.form.EventSetParamForm;
import com.zw.platform.domain.param.form.InformationParamForm;
import com.zw.platform.domain.param.form.PhoneBookParamForm;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.RealTimeCommandQuery;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.MultiThreadProcessService;
import com.zw.platform.service.monitoring.RealTimeCommandService;
import com.zw.platform.service.sendTxt.F3SendTxtService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ????????????
 */
@Controller
@RequestMapping("/v/monitoring/command")
public class RealTimeCommand {
    private static Logger log = LogManager.getLogger(RealTimeCommand.class);

    private static final String LIST_PAGE = "vas/monitoring/realTimeCommand/list";

    private static final String EDIT_PAGE = "vas/monitoring/realTimeCommand/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private RealTimeCommandService commandService;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private F3SendTxtService f3SendTxtService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private MultiThreadProcessService multiThreadProcessService;

    @Autowired
    private static final String COLUMN_STR =
        "id,paramId,commandType,brand,replyCode,createDataTime,groupName,status,vehicleId,dId";

    /**
     * ??????????????????
     * @return ModelAndView
     * @author FanLu
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * ??????????????????
     * @return ModelAndView
     * @author FanLu
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(RealTimeCommandQuery query) {
        try {
            Page<Map<String, Object>> result = new Page<>();
            List<Map<String, Object>> dataMap = new ArrayList<>();
            if (StringUtils.isBlank(query.getSimpleQueryParam())) { // ??????????????????
                result = (Page<Map<String, Object>>) commandService
                    .findRealTimeCommand(query, Arrays.asList(query.getVehicleIdList().split(",")), true);
                dataMap = result.getResult();
            }
            // ???????????????????????????????????????????????????""
            String[] column = COLUMN_STR.split(",");
            for (Map<String, Object> map : dataMap) {
                for (String keyStr : column) {
                    if (!map.containsKey(keyStr)) {
                        map.put(keyStr, "");
                    }
                }
            }
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("?????????????????????findRealTimeCommand?????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @return ModelAndView
     * @author FanLu
     */
    @RequestMapping(value = { "/saveCommand" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(StationParam stationParam, CommunicationParam commParam, CameraParam cameraParam,
        DeviceParam deviceParam, GNSSParam gnssParam, PhoneParam phoneParam, PositionParam positionParam,
        WirelessUpdateParam wirelessParam, DeviceConnectServerParam connectParam, EventSetParamForm eventParam,
        InformationParamForm infoParam, PhoneBookParamForm phoneBookParam, String vid, String commandNodes,
        final BindingResult bindingResult) {
        // ????????????
        List<String> vehicles = Arrays.asList(vid.split(","));
        String[] paramTables = commandNodes.split(",");
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            try {
                for (String paramTable : paramTables) {
                    updateParamTable(vehicles, paramTable, stationParam, commParam, cameraParam, deviceParam, gnssParam,
                        phoneParam, positionParam, wirelessParam, connectParam, eventParam, infoParam, phoneBookParam,
                        false, false);
                }
            } catch (Exception e) {
                log.error("?????????????????????????????????????????????", e);
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ????????????????????????????????????
     * @return ModelAndView
     * @author FanLu
     */
    @RequestMapping(value = { "/generateDeviceControl" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean generateDeviceControl(String vid) {
        try {
            List<String> vehicles = Arrays.asList(vid.split(","));
            updateParamTable(vehicles, "13", null, null, null, null, null, null, null, null, null, null, null, null,
                true, false);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @return ModelAndView
     * @author FanLu
     */
    @RequestMapping(value = { "/generateDeviceSearch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean generateDeviceSearch(String vid) {
        try {
            List<String> vehicles = Arrays.asList(vid.split(","));
            updateParamTable(vehicles, "15", null, null, null, null, null, null, null, null, null, null, null, null,
                false, true);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("?????????????????????????????????updateParamTable?????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ???????????????????????????
     */
    private MonitorCommandBindForm initBindForm(BaseFormBean obj, int commandType, String vid)
        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        MonitorCommandBindForm bindForm = new MonitorCommandBindForm();
        Field field = obj.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        bindForm.setVid(vid);
        bindForm.setParamId((String) field.get(obj));
        bindForm.setCommandType(commandType);
        bindForm.setCreateDataTime(new Date());
        bindForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return bindForm;
    }

    /**
     * ??????????????????(????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
     */
    private void updateParamTable(List<String> vehicles, String paramTable, StationParam stationParam,
        CommunicationParam commParam, CameraParam cameraParam, DeviceParam deviceParam, GNSSParam gnssParam,
        PhoneParam phoneParam, PositionParam positionParam, WirelessUpdateParam wirelessParam,
        DeviceConnectServerParam connectParam, EventSetParamForm eventParam, InformationParamForm infoParam,
        PhoneBookParamForm phoneBookParam, boolean deviceControl, boolean deviceSearch) {
        try {
            List<MonitorCommandBindForm> bindForm = new ArrayList<>();
            for (String vehicle : vehicles) {
                switch (paramTable) {
                    case "11":// ????????????
                        CommunicationParam commParamCopy = new CommunicationParam();
                        BeanUtils.copyProperties(commParam, commParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 11);
                        commandService.addCommunicationParam(commParamCopy);
                        bindForm.add(initBindForm(commParamCopy, 11, vehicle));
                        break;
                    case "12":// ????????????
                        DeviceParam deviceParamCopy = new DeviceParam();
                        BeanUtils.copyProperties(deviceParam, deviceParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 12);
                        commandService.addDeviceParam(deviceParamCopy);
                        bindForm.add(initBindForm(deviceParamCopy, 12, vehicle));
                        break;
                    case "13":// ????????????
                        if (deviceControl) {
                            commandService.deleteParamSetting(vehicle, 13);
                            for (int k = 1; k <= 5; k++) {
                                MonitorCommandBindForm commandBindForm = new MonitorCommandBindForm();
                                commandBindForm.setVid(vehicle);
                                commandBindForm.setCommandType(13);
                                commandBindForm.setParamId(getDeviceParam(k));
                                commandBindForm.setCreateDataTime(new Date());
                                commandBindForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                                bindForm.add(commandBindForm);
                            }
                        }
                        break;
                    case "131":// ????????????
                        WirelessUpdateParam wirelessParamCopy = new WirelessUpdateParam();
                        BeanUtils.copyProperties(wirelessParam, wirelessParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 131);
                        commandService.addWirelessUpdateParam(wirelessParamCopy);
                        bindForm.add(initBindForm(wirelessParamCopy, 131, vehicle));
                        break;
                    case "132":// ?????????????????????????????????
                        DeviceConnectServerParam connectParamCopy = new DeviceConnectServerParam();
                        BeanUtils.copyProperties(connectParam, connectParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 132);
                        commandService.addDeviceConnectServerParam(connectParamCopy);
                        bindForm.add(initBindForm(connectParamCopy, 132, vehicle));
                        break;
                    case "14":// ??????????????????
                        PositionParam positionParamCopy = new PositionParam();
                        BeanUtils.copyProperties(positionParam, positionParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 14);
                        commandService.addPositionParam(positionParamCopy);
                        bindForm.add(initBindForm(positionParamCopy, 14, vehicle));
                        break;
                    case "15":// ????????????
                        if (deviceSearch) {
                            commandService.deleteParamSetting(vehicle, 15);
                            MonitorCommandBindForm deviceQueryForm = new MonitorCommandBindForm();
                            deviceQueryForm.setVid(vehicle);
                            deviceQueryForm.setCommandType(15);
                            deviceQueryForm.setParamId("????????????");
                            deviceQueryForm.setCreateDataTime(new Date());
                            deviceQueryForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                            bindForm.add(deviceQueryForm);
                        }
                        break;
                    case "16":// ????????????
                        PhoneParam phoneParamCopy = new PhoneParam();
                        BeanUtils.copyProperties(phoneParam, phoneParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 16);
                        commandService.addPhoneParam(phoneParamCopy);
                        bindForm.add(initBindForm(phoneParamCopy, 16, vehicle));
                        break;
                    case "17":// ??????????????????
                        CameraParam cameraParamCopy = new CameraParam();
                        BeanUtils.copyProperties(cameraParam, cameraParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 17);
                        commandService.addCameraParam(cameraParamCopy);
                        bindForm.add(initBindForm(cameraParamCopy, 17, vehicle));
                        break;
                    case "18":// GNSS??????
                        GNSSParam gnssParamCopy = new GNSSParam();
                        BeanUtils.copyProperties(gnssParam, gnssParamCopy, "id");
                        commandService.deleteParamSetting(vehicle, 18);
                        commandService.addGNSSParam(gnssParamCopy);
                        bindForm.add(initBindForm(gnssParamCopy, 18, vehicle));
                        break;
                    case "19":// ????????????
                        int eventLen = eventParam.getEventId().length;
                        StringBuilder eventParamIds = new StringBuilder();
                        for (int i = 0; i < eventLen; i++) {
                            EventSetParam eventSetParam = new EventSetParam();
                            eventParamIds.append(eventSetParam.getId()).append(",");
                            eventSetParam.setOperationType(eventParam.getEOperationType());
                            eventSetParam.setEventId(eventParam.getEventId()[i]);
                            if (eventParam.getEventContent().length > 0) {
                                eventSetParam.setEventContent(eventParam.getEventContent()[i]);
                            }
                            eventSetParam.setCreateDataTime(new Date());
                            eventSetParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
                            commandService.addEventSetParam(eventSetParam);
                        }
                        commandService.deleteParamSetting(vehicle, 19);
                        MonitorCommandBindForm eventBind = new MonitorCommandBindForm();
                        eventBind.setParamId(eventParamIds.substring(0, eventParamIds.length() - 1));
                        eventBind.setVid(vehicle);
                        eventBind.setCommandType(19);
                        eventBind.setCreateDataTime(new Date());
                        eventBind.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        bindForm.add(eventBind);
                        break;
                    case "20":// ???????????????
                        int phoneLen = phoneBookParam.getPhoneBookId().length;
                        StringBuilder phoneParamIds = new StringBuilder();
                        for (int i = 0; i < phoneLen; i++) {
                            PhoneBookParam phoneBookParamPO = new PhoneBookParam();
                            phoneParamIds.append(phoneBookParamPO.getId()).append(",");
                            phoneBookParamPO.setOperationType(phoneBookParam.getPOperationType());
                            phoneBookParamPO.setPhoneBookId(phoneBookParam.getPhoneBookId()[i]);
                            phoneBookParamPO.setCallType(phoneBookParam.getCallType()[i]);
                            phoneBookParamPO.setPhoneNo(phoneBookParam.getPhoneNo()[i]);
                            phoneBookParamPO.setContact(phoneBookParam.getContact()[i]);
                            phoneBookParamPO.setCreateDataTime(new Date());
                            phoneBookParamPO.setCreateDataUsername(SystemHelper.getCurrentUsername());
                            commandService.addPhoneBookParam(phoneBookParamPO);
                        }
                        commandService.deleteParamSetting(vehicle, 20);
                        MonitorCommandBindForm phoneBind = new MonitorCommandBindForm();
                        phoneBind.setParamId(phoneParamIds.substring(0, phoneParamIds.length() - 1));
                        phoneBind.setVid(vehicle);
                        phoneBind.setCommandType(20);
                        phoneBind.setCreateDataTime(new Date());
                        phoneBind.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        bindForm.add(phoneBind);
                        break;
                    case "21":// ??????????????????
                        int infoLen = infoParam.getInfoId().length;
                        StringBuilder infoParamIds = new StringBuilder();
                        for (int i = 0; i < infoLen; i++) {
                            InformationParam infoParamPO = new InformationParam();
                            infoParamIds.append(infoParamPO.getId()).append(",");
                            infoParamPO.setOperationType(infoParam.getIOperationType());
                            infoParamPO.setInfoId(infoParam.getInfoId()[i]);
                            infoParamPO.setInfoContent(infoParam.getInfoContent()[i]);
                            infoParamPO.setCreateDataTime(new Date());
                            infoParamPO.setCreateDataUsername(SystemHelper.getCurrentUsername());
                            commandService.addInformationParam(infoParamPO);
                        }
                        commandService.deleteParamSetting(vehicle, 21);
                        MonitorCommandBindForm infoBind = new MonitorCommandBindForm();
                        infoBind.setParamId(infoParamIds.substring(0, infoParamIds.length() - 1));
                        infoBind.setVid(vehicle);
                        infoBind.setCommandType(21);
                        infoBind.setCreateDataTime(new Date());
                        infoBind.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        bindForm.add(infoBind);
                        break;

                    case "22":// F3????????????????????????
                        StringBuilder locationTime = new StringBuilder();
                        for (String str : stationParam.getLocationTimes()) {
                            locationTime.append(str).append(";");
                        }
                        stationParam.setLocationTime(locationTime.toString());
                        commandService.addStationParam(stationParam);
                        commandService.deleteParamSetting(vehicle, 22);
                        MonitorCommandBindForm stationBind = new MonitorCommandBindForm();
                        stationBind.setParamId(stationBind.getId());
                        stationBind.setVid(vehicle);
                        stationBind.setCommandType(22);
                        stationBind.setCreateDataTime(new Date());
                        stationBind.setCreateDataUsername(SystemHelper.getCurrentUsername());
                        bindForm.add(stationBind);
                        break;
                    default:
                }
            }
            if (bindForm.size() > 0) {
                commandService.addCommandBind(bindForm);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    private String getDeviceParam(int paramId) {
        switch (paramId) {
            case 1:
                return "????????????";
            case 2:
                return "????????????";
            case 3:
                return "??????????????????";
            case 4:
                return "??????????????????";
            case 5:
                return "????????????????????????";
            default:
                return "";
        }
    }

    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getEditPage(@PathVariable final String id, String paramId, String commandType,
        String vehicleId) {
        try {
            ModelAndView mv = new ModelAndView(EDIT_PAGE);
            switch (commandType) {
                case "11":
                    CommunicationParam commParam = commandService.findCommunicationParam(id);
                    mv.addObject("commParam", commParam);
                    break;
                case "12":
                    DeviceParam deviceParam = commandService.findDeviceParam(id);
                    mv.addObject("deviceParam", deviceParam);
                    break;
                case "131":
                    WirelessUpdateParam wirelessUpdateParam = commandService.findWirelessUpdateParam(id);
                    mv.addObject("wirelessUpdateParam", wirelessUpdateParam);
                    break;
                case "132":
                    DeviceConnectServerParam deviceConnectServerParam = commandService.findDeviceConnectServerParam(id);
                    mv.addObject("deviceConnectServerParam", deviceConnectServerParam);
                    break;
                case "14":
                    PositionParam positionParam = commandService.findPositionParam(id);
                    mv.addObject("positionParam", positionParam);
                    break;
                case "16":
                    PhoneParam phoneParam = commandService.findPhoneParam(id);
                    mv.addObject("phoneParam", phoneParam);
                    break;
                case "17":
                    CameraParam cameraParam = commandService.findCameraParam(id);
                    mv.addObject("cameraParam", cameraParam);
                    break;
                case "18":
                    GNSSParam gnssParam = commandService.findGNSSParam(id);
                    mv.addObject("gnssParam", gnssParam);
                    break;
                case "19":
                    List<EventSetParam> eventSetParams =
                        commandService.findEventParam(Arrays.asList(paramId.split(",")));
                    mv.addObject("eventSetParam", JSON.parseArray(JSON.toJSONString(eventSetParams)).size() > 0
                        ? JSON.parseArray(JSON.toJSONString(eventSetParams)) : null);
                    break;
                case "20":
                    List<PhoneBookParam> phoneBookParams =
                        commandService.findPhoneBookParam(Arrays.asList(paramId.split(",")));
                    mv.addObject("phoneBookParam", JSON.parseArray(JSON.toJSONString(phoneBookParams)).size() > 0
                        ? JSON.parseArray(JSON.toJSONString(phoneBookParams)) : null);
                    break;
                case "21":
                    List<InformationParam> informationParams =
                        commandService.findInformationParam(Arrays.asList(paramId.split(",")));
                    mv.addObject("informationParam", JSON.parseArray(JSON.toJSONString(informationParams)).size() > 0
                        ? JSON.parseArray(JSON.toJSONString(informationParams)) : null);
                    break;
                case "22":
                    StationParam stationParam = commandService.findStationParam(id);
                    mv.addObject("stationParam", stationParam);
                    break;
                default:
                    break;
            }
            // ??????????????????????????????
            List<MonitorCommandBindForm> referVehicleList =
                commandService.findReferVehicleExcept(commandType, vehicleId);
            String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
            mv.addObject("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
            mv.addObject("commandType", commandType);
            mv.addObject("vehicleId", vehicleId);
            mv.addObject("brand", vehicleService.findVehicleById(vehicleId).getBrand());
            return mv;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = { "/edit" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(StationParam stationParam, CommunicationParam commParam, CameraParam cameraParam,
        DeviceParam deviceParam, GNSSParam gnssParam, PhoneParam phoneParam, PositionParam positionParam,
        WirelessUpdateParam wirelessParam, DeviceConnectServerParam connectParam, EventSetParamForm eventParam,
        InformationParamForm infoParam, PhoneBookParamForm phoneBookParam, String vid, String commandType,
        final BindingResult bindingResult) throws IllegalArgumentException, SecurityException {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            try {
                updateParamTable(Collections.singletonList(vid), commandType, stationParam, commParam, cameraParam,
                    deviceParam, gnssParam, phoneParam, positionParam, wirelessParam, connectParam, eventParam,
                    infoParam, phoneBookParam, false, false);
            } catch (Exception e) {
                log.error("?????????????????????updateParamTable?????????", e);
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public final JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            MonitorCommandBindForm bindForm = new MonitorCommandBindForm();
            bindForm.setId(id);
            bindForm.setFlag(0);
            commandService.deleteCommandBind(bindForm);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public final JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String[] item = items.split(",");
            commandService.deleteCommandBindByBatch(item);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id???????????????????????????
     * @return JsonResultBean
     * @author FanLu
     */
    @RequestMapping(value = { "/getCommandTypes" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCommandTypes(String vid) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("commandTypes", commandService.findRealTimeCommand(null, Arrays.asList(vid.split(",")), false));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????id,?????????????????????????????????????????????
     * @return JsonResultBean
     * @author FanLu
     */
    @RequestMapping(value = { "/getCommandParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCommandParam(String vid, String commandType, boolean isRefer, String minotor) {
        try {
            JSONObject msg = new JSONObject();
            switch (commandType) {
                case "11":
                    msg.put("communicationParam", commandService.getCommunicationParam(vid, commandType));
                    break;
                case "12":
                    msg.put("deviceParam", commandService.getDeviceParam(vid, commandType));
                    break;
                case "131":// ????????????-??????
                case "13141":// ????????????-??????-41??????
                case "13142":// ????????????-??????-42??????
                    msg.put("wirelessUpdateParam", commandService.getWirelessUpdateParam(vid, commandType));
                    break;
                case "132":
                    msg.put("deviceConnectServerParam", commandService.getDeviceConnectServerParam(vid, commandType));
                    break;
                case "14":
                    msg.put("positionParam", commandService.getPositionParam(vid, commandType));
                    break;
                case "16":
                    msg.put("phoneParam", commandService.getPhoneParam(vid, commandType));
                    break;
                case "17":
                    msg.put("cameraParam", commandService.getCameraParam(vid, commandType));
                    break;
                case "18":
                    msg.put("gnssParam", commandService.getGNSSParam(vid, commandType));
                    break;
                case "19":
                    MonitorCommandBindForm eventBind = commandService.findBind(vid, commandType);
                    if (eventBind != null) {
                        List<EventSetParam> event =
                            commandService.findEventParam(Arrays.asList(eventBind.getParamId().split(",")));
                        msg.put("event", event);
                    }
                    break;
                case "20":
                    MonitorCommandBindForm phoneBind = commandService.findBind(vid, commandType);
                    if (phoneBind != null) {
                        List<PhoneBookParam> phone =
                            commandService.findPhoneBookParam(Arrays.asList(phoneBind.getParamId().split(",")));
                        msg.put("phone", phone);
                    }
                    break;
                case "21":
                    MonitorCommandBindForm infoBind = commandService.findBind(vid, commandType);
                    if (infoBind != null) {
                        List<InformationParam> info =
                            commandService.findInformationParam(Arrays.asList(infoBind.getParamId().split(",")));
                        msg.put("info", info);
                    }
                    break;
                case "22":
                    msg.put("stationParam", commandService.getStationParam(vid, commandType));
                    break;
                default:
                    msg.put("commandTypes", "");
            }
            // ??????????????????????????????
            List<MonitorCommandBindForm> referVehicleList = commandService.findReferVehicleExcept(commandType, vid);
            String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
            msg.put("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
            if (!isRefer) {
                msg.put("vid", vehicleService.findVehicleById(vid).getBrand());
            } else {
                msg.put("vid", minotor);
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    // ????????????????????????
    @RequestMapping(value = { "/sendParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParam(String sendParam) {
        List<JSONObject> paramList = JSON.parseArray(sendParam, JSONObject.class);
        if (paramList != null && paramList.size() > 0) {
            try {
                for (JSONObject obj : paramList) {
                    sendParam(obj.getString("id"), obj.getString("paramId"), obj.getString("vehicleId"),
                        obj.getString("type"), obj.getString("dId"));
                }
            } catch (Exception e) {
                log.error("??????????????????????????????", e);
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    public void sendParam(String id, String paramId, String vehicleId, String commandType, String did) {
        try {
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (bindDTO == null) {
                return;
            }
            String deviceNumber = bindDTO.getDeviceNumber();
            String mobile = bindDTO.getSimCardNumber();
            String deviceId = bindDTO.getDeviceId();
            String deviceType = bindDTO.getDeviceType();
            Integer msgSN;
            switch (commandType) {
                case "11":// ????????????
                    CommunicationParam communicationParam = commandService.findCommunicationParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) { // ???????????????
                        sendTxtService
                            .setCommunicationParam(deviceId, mobile, communicationParam, msgSN, deviceType, null);
                        SendParam sendParam = new SendParam();
                        sendParam.setMsgSNACK(msgSN);
                        sendParam.setParamId(id);
                        sendParam.setVehicleId(vehicleId);
                        multiThreadProcessService.updateSendParam(sendParam);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "11", id);
                    } else { // ???????????????
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "11", id);
                    }
                    break;
                case "12":// ????????????
                    DeviceParam deviceParam = commandService.findDeviceParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setDeviceParam(deviceId, mobile, deviceParam, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "12", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "12", id);
                    }
                    break;
                case "131":// ????????????
                    WirelessUpdateParam wwParam = commandService.findWirelessUpdateParam(id);
                    DeviceCommand wwDeviceCommand = new DeviceCommand();
                    wwDeviceCommand.setCw(1);
                    String param = wwParam.toString();
                    wwDeviceCommand.setParam(param);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.deviceCommand(mobile, wwDeviceCommand, msgSN, deviceId, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "131", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "131", id);
                    }
                    break;
                case "132":// ?????????????????????????????????
                    DeviceConnectServerParam ccParam = commandService.findDeviceConnectServerParam(id);
                    DeviceCommand ccDeviceCommand = new DeviceCommand();
                    ccDeviceCommand.setCw(2);
                    String connectParam = ccParam.toString();
                    ccDeviceCommand.setParam(connectParam);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.deviceCommand(mobile, ccDeviceCommand, msgSN, deviceId, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "132", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "132", id);
                    }
                    break;
                case "13":// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    DeviceCommand deviceCommand = new DeviceCommand();
                    String type = "";
                    if ("????????????".equals(paramId)) {
                        deviceCommand.setCw(3);
                        type = "133";
                    } else if ("????????????".equals(paramId)) {
                        deviceCommand.setCw(4);
                        type = "134";
                    } else if ("??????????????????".equals(paramId)) {
                        deviceCommand.setCw(5);
                        type = "135";
                    } else if ("??????????????????".equals(paramId)) {
                        deviceCommand.setCw(6);
                        type = "136";
                    } else if ("????????????????????????".equals(paramId)) {
                        deviceCommand.setCw(7);
                        type = "137";
                    }
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.deviceCommand(mobile, deviceCommand, msgSN, deviceId, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, type, id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, type, id);
                    }
                    break;
                case "14":// ??????????????????
                    PositionParam positionParam = commandService.findPositionParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setPositionParam(deviceId, mobile, positionParam, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "14", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "14", id);
                    }
                    break;
                case "15":// ????????????
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.devicePropertyQuery(deviceId, mobile, msgSN, deviceType);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "15", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "15", id);
                    }
                    break;
                case "16":// ????????????
                    PhoneParam phoneParam = commandService.findPhoneParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setPhoneParam(deviceId, mobile, phoneParam, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "16", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "16", id);
                    }
                    break;
                case "17":// ??????????????????
                    CameraParam cameraParam = commandService.findCameraParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setCameraParam(deviceId, mobile, cameraParam, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "17", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "17", id);
                    }
                    break;
                case "18":// GNSS??????
                    GNSSParam gnssParam = commandService.findGNSSParam(id);
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setGNSSParam(deviceId, mobile, gnssParam, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "18", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "18", id);
                    }
                    break;
                case "19":// ????????????
                    List<EventSetParam> event = commandService.findEventParam(Arrays.asList(paramId.split(",")));
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setEvent(deviceId, mobile, event, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "19", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "19", id);
                    }
                    break;
                case "20":// ???????????????
                    List<PhoneBookParam> phone = commandService.findPhoneBookParam(Arrays.asList(paramId.split(",")));
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setPhoneBook(deviceId, mobile, phone, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "20", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "20", id);
                    }
                    break;
                case "21":// ??????????????????
                    List<InformationParam> info =
                        commandService.findInformationParam(Arrays.asList(paramId.split(",")));
                    msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                    if (msgSN != null) {
                        sendTxtService.setInformationDemand(deviceId, mobile, info, msgSN, deviceType, null);
                        sendHelper.updateParameterStatus(did, msgSN, 4, vehicleId, "21", id);
                    } else {
                        sendHelper.updateParameterStatus(did, 0, 5, vehicleId, "21", id);
                    }
                    break;
                case "22":
                    // F3??????????????????????????????

                    StationParam stationParam = commandService.findStationParam(id);
                    int paramLength = 15;
                    if (stationParam.getRequitePattern() == 1) { // ??????????????????
                        stationParam.setLocationTimeNum(0);// ?????????????????????
                        stationParam.setLocationTime("");// ??????????????????
                    } else {
                        stationParam.setLocationNumber(0);// ??????????????????
                        stationParam.setRequiteTime("000001");// ??????????????????
                        String[] times = stationParam.getLocationTime().split(";");
                        int count = 0;
                        StringBuilder timestr = new StringBuilder();
                        for (String time : times) {
                            if (StringUtil.isNullOrBlank(time)) {
                                continue;
                            }
                            timestr.append(time);
                            count++;
                        }
                        stationParam.setLocationTime(timestr.toString());// ??????????????????
                        stationParam.setLocationTimeNum(count);
                        paramLength = paramLength + (count * 3);
                    }
                    ParamItem t808Param = new ParamItem();
                    t808Param.setParamId(0x08);
                    t808Param.setParamLength(paramLength);
                    t808Param.setParamValue(stationParam);
                    List<ParamItem> params = new ArrayList<>();
                    params.add(t808Param);
                    f3SendTxtService.setF3SetParam(vehicleId, stationParam.getId(), params, "8103-F3-08", false);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    // ????????????????????????
    @RequestMapping(value = { "/getReferCommand" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getReferCommand(String commandType) {
        try {
            JSONObject msg = new JSONObject();
            // ??????????????????????????????
            List<MonitorCommandBindForm> referVehicleList = commandService.findReferVehicle(commandType);
            String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
            msg.put("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
