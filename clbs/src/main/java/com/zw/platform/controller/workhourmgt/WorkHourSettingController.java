package com.zw.platform.controller.workhourmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSettingQuery;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.workhourmgt.WorkHourSettingService;
import com.zw.platform.service.workhourmgt.impl.WorkHourSettingServiceImpl;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 工时设置controller
 * @author zhouzongbo on 2018/5/28 16:21
 */
@Controller
@RequestMapping("/v/workhourmgt/workhoursetting")
public class WorkHourSettingController {

    private static Logger log = LogManager.getLogger(WorkHourSettingController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/workhoursetting/list";

    private static final String BIND_PAGE = "vas/workhourmgt/workhoursetting/bind";

    private static final String EDIT_PAGE = "vas/workhourmgt/workhoursetting/edit";

    private static final String DETAIL_PAGE = "vas/workhourmgt/workhoursetting/detail";

    /**
     * 基本信息
     */
    public static final String BASICINFO_PAGE = "vas/workhourmgt/workhoursetting/basicInfo";

    /**
     * 常规参数
     */
    private static final String GENERAL_PAGE = "vas/workhourmgt/workhoursetting/general";

    /**
     * 通讯参数
     */
    private static final String NEWSLETTER_PAGE = "vas/workhourmgt/workhoursetting/newsletter";

    /**
     * 私有参数
     */
    private static final String PARAMETERS_PAGE = "vas/workhourmgt/workhoursetting/parameters";

    /**
     * 远程升级
     */
    private static final String UPGRADE_PAGE = "vas/workhourmgt/workhoursetting/upgrade";

    /**
     * 工时基值修订
     */
    private static final String WORK_HOUR_BASE_VALUE_UPDATE = "vas/workhourmgt/workhoursetting/basevalueupdate";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private WorkHourSettingService workHourSettingService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Value("${up.error}")
    private String upError;

    @Value("${up.error.workHour.type}")
    private String upErrorWorkHourType;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bind.workHour}")
    private String hasWorkHour;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    /**
     * 工时车辆设置列表
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final WorkHourSettingQuery query) {
        try {
            if (query != null) {
                Page<WorkHourSettingInfo> result = workHourSettingService.findWorkHourSettingList(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询工时设置列表异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 刷新参数下发状态
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "/refreshSendStatus" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            WorkHourSettingInfo workHourSettingInfo = workHourSettingService.findWorkHourSettingByVid(vehicleId);
            if (workHourSettingInfo != null) {
                return new JsonResultBean(workHourSettingInfo);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("工时刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }



    /**
     * 设置页面
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingBindPage_{id}_{monitorType}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getWorkHourSettingPage(@PathVariable final String id, @PathVariable final Integer monitorType) {
        try {
            ModelAndView modelAndView = new ModelAndView(BIND_PAGE);
            // 车辆信息
            buildCommonResultData(id, modelAndView, monitorType);
            return modelAndView;
        } catch (Exception e) {
            log.error("获取工时设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 设置
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addWorkHourSetting(@Validated({ ValidGroupAdd.class }) WorkHourSettingForm form,
        final BindingResult bindingResult) {
        try {
            if (Objects.nonNull(form)) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                }
                WorkHourSettingInfo info = workHourSettingService.findVehicleWorkHourSettingByVid(form.getVehicleId());
                if (Objects.nonNull(info) && StringUtils.isNotBlank(info.getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, hasWorkHour);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.addWorkHourSetting(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("设置工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据车辆Id查询车与工时的绑定信息
     * @param vehicleId vehicleId
     */
    @RequestMapping(value = "/getWorkHourBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean referenceBrandSet(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                WorkHourSettingInfo workHourSetting = workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
                return new JsonResultBean(workHourSetting);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆Id查询车与工时的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 修改页面
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingEditPage_{vehicleId}_{type}_{monitorType}.gsp",
        method = RequestMethod.GET)
    public ModelAndView getWorkHourSettingEditPage(@PathVariable final String vehicleId,
        @PathVariable final String type, @PathVariable final Integer monitorType, HttpServletResponse response) {
        try {
            ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
            // 传感器型号单独调用SensorSettingController findSensorInfo();
            buildCommonResultData(vehicleId, modelAndView, monitorType);
            // 编辑数据
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            if (Objects.nonNull(vehicleWorkHourSetting)) {
                modelAndView.addObject("vehicleWorkHourSetting", vehicleWorkHourSetting);
                modelAndView.addObject("type", type);
                return modelAndView;
            } else {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('该条数据已解除绑定！');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("获取工时修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 车辆信息和参考对象信息
     * @param id           id
     * @param modelAndView modelAndView
     * @throws Exception ex
     */
    private void buildCommonResultData(String id, ModelAndView modelAndView, Integer monitorType) {
        List<Integer> protocols = new ArrayList<>();
        JSONObject monitor = WorkHourSettingServiceImpl.getSensorJSONObject(id, protocols);
        if (monitor == null) {
            return;
        }
        List<WorkHourSettingInfo> vehicleList = workHourSettingService.findReferenceVehicleByProtocols(protocols);
        modelAndView.addObject("vehicleInfo", monitor);
        modelAndView.addObject("monitorType", monitorType);
        modelAndView.addObject("vehicleList", JSON.toJSONString(vehicleList));
    }

    /**
     * 修改
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateWorkHourSetting(@Validated({ ValidGroupUpdate.class }) WorkHourSettingForm form,
        final BindingResult bindingResult) {
        try {
            if (Objects.nonNull(form)) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                }

                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.updateWorkHourSetting(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 解绑
     * @param id id 发动机
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteWorkHourSettingBind_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSettingBind(@PathVariable final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.deleteWorkHourSettingBind(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解绑工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量解绑
     * @param ids ids
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deleteMoreWorkHourSettingBind", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreWorkHourSettingBind(final String ids) {
        try {
            if (StringUtils.isNotBlank(ids)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.deleteMoreWorkHourSettingBind(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解绑工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 详情
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingDetailPage_{vehicleId}_{type}.gsp", method = RequestMethod.GET)
    public ModelAndView getWorkHourSettingDetail(@PathVariable final String vehicleId,
        @PathVariable final String type) {
        try {
            ModelAndView modelAndView = new ModelAndView(DETAIL_PAGE);
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            vehicleWorkHourSetting.setSensorSequenceType(type);
            modelAndView.addObject("vehicleWorkHourSetting", vehicleWorkHourSetting);
            return modelAndView;
        } catch (Exception e) {
            log.error("获取工时详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 读取基本信息(getF3Param)
     * @param id sensorVehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据id查询车绑定的发动机
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据传感器车辆绑定表id查询数据
     * @param id  id
     * @param mav mav
     */
    private void getWorkHourBindData(String id, ModelAndView mav) {
        WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
        workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
        mav.addObject("result", workHourSettingInfo);
    }

    /**
     * 常规参数(getF3Param)
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
            workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
            Integer baudRateCalculateTimeScope = workHourSettingInfo.getBaudRateCalculateTimeScope();
            if (Objects.nonNull(baudRateCalculateTimeScope)) {
                switch (baudRateCalculateTimeScope) {
                    case 1:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(10);
                        break;
                    case 2:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(15);
                        break;
                    case 3:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(20);
                        break;
                    case 4:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(30);
                        break;
                    case 5:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(60);
                        break;
                    default:
                        break;
                }
            }
            mav.addObject("result", workHourSettingInfo);
            return mav;
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 远程升级
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView upgradePage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 私有参数（校验车辆是否在线: v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus）
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView parametersPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 通讯参数
     * @param id id
     * @return 通讯参数
     */
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView newsletterPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 工时基准修正
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = { "/getWorkHourBaseValue_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getWorkHourBaseValue(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(WORK_HOUR_BASE_VALUE_UPDATE);
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("工时基准修正弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 根据id下发参数
     * sendParam: [{"sensorVehicleId":"eaae4ea2-81e2-43f6-a579-1a5a3e59ceb4",
     * "paramId":"","vehicleId":"c0cd2974-b2c0-405c-ab47-282317820f59"}]
     * @param sendParam
     * @return JsonResultBean
     */
    @RequestMapping(value = "/sendWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendWorkHourSetting(String sendParam) {
        try {
            List<JSONObject> paramList = JSON.parseObject(sendParam, List.class);
            if (CollectionUtils.isNotEmpty(paramList)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 工时下发
                workHourSettingService.sendWorkHourSetting(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid         vid
     * @param commandType 基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4;
     * @param sensorID    工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129):
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器平台设置常规参数
     * @param id id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id) {
        try {
            WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", workHourSettingInfo);
            if (workHourSettingInfo != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据(下发调用)
     * @param vid        vid
     * @param sensorID   工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2: 129):
     * @param commandStr 下发内容
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService
                .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ipAddress, "3");
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 工时传感器常规参数修正下发
     * @param setting setting
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(WorkHourSettingInfo setting) {
        try {
            // pt:平台; report:以传感器为准
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, dealType, ipAddress, 0);
        } catch (Exception e) {
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 外设软件升级
     * @param wirelessParam
     * @param vehicleId
     * @param bindingResult
     * @return ModelAndView
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(WirelessUpdateParam wirelessParam, String vehicleId, final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            //  commandType：0x80: 128; 0x81:129;
            Integer commandType = 128;
            try {
                String commandTypeStr = request.getParameter("commandType");
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorWorkHourType);
                }
                commandType = Integer.parseInt(commandTypeStr);
            } catch (Exception ex) {
                return new JsonResultBean(JsonResultBean.FAULT, upErrorWorkHourType);

            }
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 1);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * 工时基值修正下发
     * @param setting setting
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateWorkHourBaseValue", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean updateWorkHourBaseValue(WorkHourSettingInfo setting) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, "", ipAddress, 1);
        } catch (Exception e) {
            log.error("工时基值修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
