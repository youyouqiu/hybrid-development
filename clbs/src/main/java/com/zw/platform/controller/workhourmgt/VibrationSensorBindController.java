package com.zw.platform.controller.workhourmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.workhourmgt.VibrationSensorBindService;
import com.zw.platform.service.workhourmgt.VibrationSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
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

@Controller
@RequestMapping("/v/workhourmgt/vbbind")
public class VibrationSensorBindController {
    private static Logger log = LogManager.getLogger(VibrationSensorBindController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/vibrationsensorbind/list";

    private static final String BIND_PAGE = "vas/workhourmgt/vibrationsensorbind/bind";

    private static final String EDIT_PAGE = "vas/workhourmgt/vibrationsensorbind/edit";

    private static final String DETAIL_PAGE = "vas/workhourmgt/vibrationsensorbind/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private VibrationSensorBindService vibrationSensorBindService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private VibrationSensorService vibrationSensorService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bound.shocksensor}")
    private String vehicleBoundShocksensor;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Value("${bound.seccess}")
    private String boundSuccess;

    @Value("${bound.fail}")
    private String boundFail;

    @Value("${edit.success}")
    private String editSuccess;

    @Value("${edit.fail}")
    private String editFail;

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
    public PageGridBean getListPage(final VibrationSensorBindQuery query) {
        try {
            if (query != null) {
                Page<VibrationSensorBind> result = vibrationSensorBindService.findWorkHourSensorBind(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("?????????????????????findWorkHourSensorBind?????????", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // ????????????????????????
    public ModelAndView bindPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // ?????????
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // ??????????????????
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // ?????????????????????
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            mav.addObject("vehicle", vehicle);
            mav.addObject("vibrationSensorList", JSON.toJSONString(vibrationSensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: ??????(??????)
     */
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // ????????????????????????
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // ????????????id ??????????????????????????????????????????
                if (StringUtils
                    .isNotBlank(vibrationSensorBindService.findWorkHourVehicleByVid(form.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundShocksensor);
                }
                // ?????????????????????IP??????
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                // ???????????????
                return vibrationSensorBindService.addWorkHourSensorBind(form, ip);
            }
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws @author wangying
     * @Title: ??????????????????
     */
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") final String id, HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // ?????????????????????
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            // ?????????????????????
            List<VibrationSensorForm> vibrationSensorList =
                    vibrationSensorService.findVibrationSensorByPage(null, false);
            // ????????????id??????????????????????????????

            VibrationSensorBind sensor = vibrationSensorBindService.findWorkHourVehicleById(id);
            if (sensor != null) {
                mav.addObject("vibrationSensorList", JSON.toJSONString(vibrationSensorList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("result", sensor);
                return mav;
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
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @author wangying
     * @Title: ??????
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final VibrationSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                if (vibrationSensorBindService.findWorkHourVehicleById(form.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                }
                // ??????IP??????
                String ip = new GetIpAddr().getIpAddr(request);
                // ???????????????
                return vibrationSensorBindService.updateWorkHourSensorBind(form, ip);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @param id
     * @return ModelAndView
     * @throws @author wangying
     * @Title: ??????
     */
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // ????????????id??????????????????????????????
            VibrationSensorBind vibrationSensor = vibrationSensorBindService.findWorkHourVehicleByVid(id);
            mav.addObject("result", vibrationSensor);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????id??????(????????????)
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (!id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return vibrationSensorBindService.deleteWorkHourSensorBindById(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            // ????????????---??????????????????????????????
            String items = request.getParameter("deltems");
            if (!items.isEmpty()) {
                // ??????????????????IP??????
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return vibrationSensorBindService.deleteWorkHourSensorBindById(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id????????????
     */
    @RequestMapping(value = "/sendWorkHour", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendWorkHour(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList != null && paramList.size() > 0) {
                // ??????ip??????
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                // ????????????
                vibrationSensorBindService.sendWorkHour(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
