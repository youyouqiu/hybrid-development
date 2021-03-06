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
 * ????????????controller
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
     * ????????????
     */
    public static final String BASICINFO_PAGE = "vas/workhourmgt/workhoursetting/basicInfo";

    /**
     * ????????????
     */
    private static final String GENERAL_PAGE = "vas/workhourmgt/workhoursetting/general";

    /**
     * ????????????
     */
    private static final String NEWSLETTER_PAGE = "vas/workhourmgt/workhoursetting/newsletter";

    /**
     * ????????????
     */
    private static final String PARAMETERS_PAGE = "vas/workhourmgt/workhoursetting/parameters";

    /**
     * ????????????
     */
    private static final String UPGRADE_PAGE = "vas/workhourmgt/workhoursetting/upgrade";

    /**
     * ??????????????????
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
     * ????????????????????????
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ??????
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
            log.error("????????????????????????????????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ????????????????????????
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
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }



    /**
     * ????????????
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingBindPage_{id}_{monitorType}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getWorkHourSettingPage(@PathVariable final String id, @PathVariable final Integer monitorType) {
        try {
            ModelAndView modelAndView = new ModelAndView(BIND_PAGE);
            // ????????????
            buildCommonResultData(id, modelAndView, monitorType);
            return modelAndView;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????Id?????????????????????????????????
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
            log.error("????????????Id???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ????????????
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = "/getWorkHourSettingEditPage_{vehicleId}_{type}_{monitorType}.gsp",
        method = RequestMethod.GET)
    public ModelAndView getWorkHourSettingEditPage(@PathVariable final String vehicleId,
        @PathVariable final String type, @PathVariable final Integer monitorType, HttpServletResponse response) {
        try {
            ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
            // ???????????????????????????SensorSettingController findSensorInfo();
            buildCommonResultData(vehicleId, modelAndView, monitorType);
            // ????????????
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
                out.println("layer.msg('??????????????????????????????');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ?????????????????????????????????
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
     * ??????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
     * @param id id ?????????
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????(getF3Param)
     * @param id sensorVehicleId
     * @return ModelAndView
     */
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView basicInfo(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // ??????id???????????????????????????
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????????????????id????????????
     * @param id  id
     * @param mav mav
     */
    private void getWorkHourBindData(String id, ModelAndView mav) {
        WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
        workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
        mav.addObject("result", workHourSettingInfo);
    }

    /**
     * ????????????(getF3Param)
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ???????????????????????????????????????: v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus???
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
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     * @param id id
     * @return ????????????
     */
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView newsletterPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            getWorkHourBindData(id, mav);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
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
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????id????????????
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
                // ????????????
                workHourSettingService.sendWorkHourSetting(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3???????????????
     * @param vid         vid
     * @param commandType ????????????: 0xF8; ????????????: 0xF5; ????????????: 0xF4;
     * @param sensorID    ????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2???129):
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????????????????
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
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????(????????????)
     * @param vid        vid
     * @param sensorID   ????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2: 129):
     * @param commandStr ????????????
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
            log.error("??????F3???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????????????????
     * @param setting setting
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(WorkHourSettingInfo setting) {
        try {
            // pt:??????; report:??????????????????
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, dealType, ipAddress, 0);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
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
        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            //  commandType???0x80: 128; 0x81:129;
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
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 1);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * ????????????????????????
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
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
