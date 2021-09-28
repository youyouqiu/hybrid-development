package com.zw.platform.controller.workhourmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.service.workhourmgt.VibrationSensorBindService;
import com.zw.platform.service.workhourmgt.WorkingHoursService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 *
 */
@Controller
@RequestMapping("/v/workhourmgt/vbStatistic")
public class WorkHourController {
    private static Logger log = LogManager.getLogger(WorkHourController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/workhour/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private WorkingHoursService workingHoursService;

    @Autowired
    private VibrationSensorBindService vibrationSensorBindService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<VibrationSensorBind> vehicleList = vibrationSensorBindService.findReferenceVehicle();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("分页查询界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @Auth
    @RequestMapping(value = {"/getWorkHours"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getWorkHours(String type, String band, String startTime, String endTime) {
        try {
            JSONObject message = workingHoursService.getWorkHours(type, band, startTime, endTime);
            return new JsonResultBean(JsonResultBean.SUCCESS, message.toJSONString());
        } catch (Exception e) {
            log.error("获取工时信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @Auth
    @RequestMapping(value = {"/getThresholds"}, method = RequestMethod.POST)
    @ResponseBody
    public VibrationSensorBind getThresholds(String vehicleId) {
        try {
            return workingHoursService.getThresholds(vehicleId);
        } catch (Exception e) {
            log.error("获取工时信息异常", e);
            return null;
        }
    }

}
