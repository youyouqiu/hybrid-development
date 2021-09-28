package com.zw.platform.controller.tyrepressure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.TyrePressureParameter;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSettingQuery;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.sensor.TyrePressureSensorService;
import com.zw.platform.service.sensor.TyrePressureSettingService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 胎压监测设置
 * create by denghuabing 2019.2.24
 */
@Controller
@RequestMapping("/v/tyrepressure/setting")
public class TyrePressureSettingController {

    private Logger logger = LogManager.getLogger(TyrePressureSettingController.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private TyrePressureSettingService tyrePressureSettingService;

    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Autowired
    private TyrePressureSensorService tyrePressureSensorService;

    @Value("${sys.error.msg}")
    private String syError;

    private static final String LIST_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/list";
    private static final String BIND_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/bind";
    private static final String EDIT_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/edit";
    private static final String DETAIL_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/detail";
    private static final String BASIC_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/basic";
    private static final String GENERAL_PAGE = "vas/tirePressureManager/tirePressureManagerSetting/general";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(TyrePressureSettingQuery query) {
        try {
            Page<TyrePressureSettingForm> list = tyrePressureSettingService.getList(query);
            return new PageGridBean(list, true);
        } catch (Exception e) {
            logger.error("胎压监测设置列表查询异常", e);
            return new PageGridBean(PageGridBean.FAULT);
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
            TyrePressureSettingForm tyrePressureSettingForm = tyrePressureSettingService.refreshSendStatus(vehicleId);
            if (tyrePressureSettingForm != null) {
                return new JsonResultBean(tyrePressureSettingForm);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @RequestMapping(value = "/bind_{vehicleId}", method = RequestMethod.GET)
    public ModelAndView getBindPage(@PathVariable("vehicleId") String vehicleId) {
        try {
            if (StringUtils.isNotEmpty(vehicleId)) {
                ModelAndView modelAndView = new ModelAndView(BIND_PAGE);
                VehicleDTO vehicleDTO = MonitorUtils.getVehicle(vehicleId);
                VehicleInfo vehicle = new VehicleInfo();
                vehicle.setId(vehicleDTO.getId());
                vehicle.setBrand(vehicleDTO.getName());
                String deviceType = vehicleDTO.getDeviceType();
                List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
                List<TransdusermonitorSet> referenceList = sensorSettingsService.consultVehicle(7, protocols);
                List<TyrePressureSensorForm> allSensor = tyrePressureSensorService.findAllSensor();
                modelAndView.addObject("allSensor", JSON.toJSONString(allSensor));
                modelAndView.addObject("vehicleInfo", vehicle);
                modelAndView.addObject("referenceList", JSON.toJSONString(referenceList));
                return modelAndView;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("胎压监测设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean bindTyrePressureSetting(TyrePressureSettingForm form, HttpServletResponse response) {
        try {
            if (form != null) {
                List<TyrePressureSettingForm> list = tyrePressureSettingService.findExistByVid(form.getVehicleId());
                if (list != null && list.size() > 0) {
                    response.setContentType("text/htmlcharset=utf-8");
                    PrintWriter out = response.getWriter();
                    out.println("<script language='javascript'>");
                    out.println("$('#commonWin').modal('hide');");
                    out.println("layer.msg('该车已设置胎压监测！');");
                    out.println("myTable.refresh();");
                    out.println("</script>");
                    return null;
                }
                return tyrePressureSettingService.addTyrePressureSetting(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("绑定");
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/edit_{id}_{vehicleId}", method = RequestMethod.GET)
    public ModelAndView getEditPage(@PathVariable("id") String id, @PathVariable("vehicleId") String vehicleId,
        HttpServletResponse response) {
        try {
            if (StringUtils.isNotEmpty(id) || StringUtils.isNotEmpty(vehicleId)) {
                ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
                TyrePressureSettingForm form = tyrePressureSettingService.findTyrePressureSettingById(id);
                if (form != null) {
                    VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(vehicleId);
                    List<TransdusermonitorSet> referenceList = getReferenceListByEdit(vehicleId);
                    TyrePressureParameter tyrePressureParameter =
                        JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
                    form.setTyrePressureParameter(tyrePressureParameter);
                    List<TyrePressureSensorForm> allSensor = tyrePressureSensorService.findAllSensor();
                    modelAndView.addObject("allSensor", JSON.toJSONString(allSensor));
                    modelAndView.addObject("result", form);
                    modelAndView.addObject("vehicleInfo", vehicle);
                    modelAndView.addObject("referenceList", JSON.toJSONString(referenceList));
                    return modelAndView;
                } else {
                    unbundlingHints(response);
                    return null;
                }
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("胎压监测设置修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改时的参考对象，排除自己
     * @return
     */
    private List<TransdusermonitorSet> getReferenceListByEdit(String vehicleId) throws Exception {
        VehicleDTO configList = MonitorUtils.getVehicle(vehicleId);
        String deviceType = configList.getDeviceType();
        List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
        List<TransdusermonitorSet> referenceList = sensorSettingsService.consultVehicle(7, protocols);
        List<TransdusermonitorSet> result = new ArrayList<>();
        for (TransdusermonitorSet reference : referenceList) {
            if (!vehicleId.equals(reference.getVehicleId())) {
                result.add(reference);
            }
        }
        return result;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateTyrePressureSetting(TyrePressureSettingForm form, HttpServletResponse response) {
        try {
            if (form != null) {
                TyrePressureSettingForm tyrePressureSettingForm =
                    tyrePressureSettingService.findTyrePressureSettingById(form.getId());
                if (tyrePressureSettingForm != null) {
                    return tyrePressureSettingService.updateTyrePressureSetting(form);
                } else {
                    unbundlingHints(response);
                    return null;
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压监测设置修改失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 解绑提示
     * @param response
     */
    private void unbundlingHints(HttpServletResponse response) throws IOException {
        response.setContentType("text/htmlcharset=utf-8");
        PrintWriter out = response.getWriter();
        out.println("<script language='javascript'>");
        out.println("$('#commonWin').modal('hide');");
        out.println("layer.msg('该车已解除绑定！');");
        out.println("myTable.refresh();");
        out.println("</script>");
    }

    @RequestMapping(value = "/delete_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTyrePressureSetting(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                return tyrePressureSettingService.deleteTyrePressureSetting(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压监测设置恢复默认异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/deleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String ids) {
        try {
            if (StringUtils.isNotEmpty(ids)) {
                return tyrePressureSettingService.deleteMore(ids);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压监测设置批量删除异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/detail_{id}", method = RequestMethod.GET)
    public ModelAndView getDetailPage(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView modelAndView = new ModelAndView(DETAIL_PAGE);
                TyrePressureSettingForm form = tyrePressureSettingService.findTyrePressureSettingById(id);
                if (form != null) {
                    TyrePressureParameter tyrePressureParameter =
                        JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
                    form.setTyrePressureParameter(tyrePressureParameter);
                    VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(form.getVehicleId());
                    List<TyrePressureSensorForm> allSensor = tyrePressureSensorService.findAllSensor();
                    modelAndView.addObject("allSensor", JSON.toJSONString(allSensor));
                    modelAndView.addObject("result", form);
                    modelAndView.addObject("vehicleInfo", vehicle);
                    return modelAndView;
                } else {
                    unbundlingHints(response);
                }
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("胎压监测设置详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 基础信息
     * @param id
     * @param response
     * @return
     */
    @RequestMapping(value = "/basic_{id}", method = RequestMethod.GET)
    public ModelAndView getBasicPage(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView modelAndView = new ModelAndView(BASIC_PAGE);
                TyrePressureSettingForm form = tyrePressureSettingService.findTyrePressureSettingById(id);
                if (form != null) {
                    TyrePressureParameter tyrePressureParameter =
                        JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
                    form.setTyrePressureParameter(tyrePressureParameter);
                    VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(form.getVehicleId());
                    modelAndView.addObject("result", form);
                    modelAndView.addObject("vehicleInfo", vehicle);
                    return modelAndView;
                } else {
                    unbundlingHints(response);
                }
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("胎压监测设置基础信息页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 基础信息
     * @param id
     * @param response
     * @return
     */
    @RequestMapping(value = "/general_{id}", method = RequestMethod.GET)
    public ModelAndView getGeneralPage(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView modelAndView = new ModelAndView(GENERAL_PAGE);
                TyrePressureSettingForm form = tyrePressureSettingService.findTyrePressureSettingById(id);
                if (form != null) {
                    TyrePressureParameter tyrePressureParameter =
                        JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
                    form.setTyrePressureParameter(tyrePressureParameter);
                    VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(form.getVehicleId());
                    modelAndView.addObject("result", form);
                    modelAndView.addObject("vehicleInfo", vehicle);
                    return modelAndView;
                } else {
                    unbundlingHints(response);
                }
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("胎压监测设置基础信息页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 传感器常规参数修正下发
     * @param form
     * @return
     */
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(TyrePressureSettingForm form, String dealType) {
        try {
            if (StringUtils.isNotEmpty(dealType) && form != null) {
                return tyrePressureSettingService.updateRoutineSetting(form, dealType);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("胎压传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendSettingParam(String sendParam) {
        try {
            if (StringUtils.isNotEmpty(sendParam)) {
                List<JSONObject> list = JSON.parseObject(sendParam, ArrayList.class);
                tyrePressureSettingService.sendSettingParam(list);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压监测设置下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 根据车id获取参考对象信息
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = "/getReferenceInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getReferenceInfo(String vehicleId) {
        try {
            if (StringUtils.isNotEmpty(vehicleId)) {
                TyrePressureSettingForm form = tyrePressureSettingService.findTyrePressureSettingByVid(vehicleId);
                TyrePressureParameter tyrePressureParameter =
                    JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
                form.setTyrePressureParameter(tyrePressureParameter);
                return new JsonResultBean(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压监测设置获取参考对象信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

}
