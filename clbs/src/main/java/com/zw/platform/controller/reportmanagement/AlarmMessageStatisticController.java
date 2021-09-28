package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.AlarmMessageInfo;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.dto.reportManagement.AlarmMessageDto;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.reportManagement.AlarmMessageStatisticService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 报警信息统计
 * @author zhouzongbo on 2019/5/18 9:17
 */
@Controller
@RequestMapping("/m/reportManagement/alarmMessageStatistic")
public class AlarmMessageStatisticController {

    private static Logger log = LogManager.getLogger(AlarmMessageStatisticController.class);

    private static final String LIST_PAGE = "modules/reportManagement/alarmMessageStatistic";

    @Autowired
    private AlarmMessageStatisticService alarmMessageStatisticService;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        // 查询报警类型
        List<AlarmType> alarms = alarmSearchService.getAlarmType();
        modelAndView.addObject("alarmType", JSON.toJSONString(alarms));
        return modelAndView;
    }

    /**
     * 查询车辆报警信息(用于统计)
     * @param vehicleIds 监控对象ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param alarmTypes 报警类型
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findAlarmMessageList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAlarmMessageList(String vehicleIds, String startTime, String endTime, String alarmTypes) {
        try {
            List<AlarmMessageInfo> alarmMessageList = new ArrayList<>();
            if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime) && StringUtils.isNotBlank(alarmTypes)) {
                alarmMessageList =
                    alarmMessageStatisticService.getAlarmMessageList(vehicleIds, startTime, endTime, alarmTypes);
            }
            return new JsonResultBean(alarmMessageList);
        } catch (Exception e) {
            log.error("报警信息统计查询数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询车辆报警信息(用于统计)-调用paas-cloud接口
     * @param vehicleIds 监控对象ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param alarmTypes 报警类型
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findAlarmMessageListFromPaas", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAlarmMessageListFromPaas(String vehicleIds, String startTime, String endTime,
        String alarmTypes) {
        try {
            List<AlarmMessageDto> alarmMessageList = new ArrayList<>();
            if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime) && StringUtils.isNotBlank(alarmTypes)) {
                alarmMessageList =
                    alarmMessageStatisticService.getAlarmMessageListNew(vehicleIds, startTime, endTime, alarmTypes);
            }
            return new JsonResultBean(alarmMessageList);
        } catch (Exception e) {
            log.error("报警信息统计查询数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询单个报警类型详情数据
     * @param vehicleId 监控对象ID
     * @param alarmType 报警类型名称
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findAlarmDetailMessageList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAlarmDetailMessageList(String vehicleId, String alarmType) {
        try {
            if (StringUtils.isNotEmpty(vehicleId) && StringUtils.isNotEmpty(alarmType)) {
                return new JsonResultBean(alarmMessageStatisticService.getAlarmDetailMessageList(vehicleId, alarmType));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "查询条件不能为空!");
        } catch (Exception e) {
            log.error("查询单个报警类型详情数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询单个报警类型详情数据-同调用paas-cloud接口查询相配
     * @param vehicleId 监控对象ID
     * @param alarmType 报警类型名称
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findAlarmDetailMessageListNew", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findAlarmDetailMessageListNew(String vehicleId, String alarmType, String startTime,
        String endTime) {
        try {
            if (StringUtils.isNotEmpty(vehicleId) && StringUtils.isNotEmpty(alarmType)) {
                return new JsonResultBean(alarmMessageStatisticService
                    .getAlarmDetailMessageListNew(vehicleId, alarmType, startTime, endTime));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "查询条件不能为空!");
        } catch (Exception e) {
            log.error("查询单个报警类型详情数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据用户名导出报警信息统计列表数据
     * @param response
     * @return
     */
    @RequestMapping(value = "/getExportAlarmMessageList", method = RequestMethod.GET)
    @ResponseBody
    public void getExportAlarmMessageList(HttpServletResponse response, String fuzzyQuery) {
        try {
            ExportExcelUtil.setResponseHead(response, "报警信息统计");
            alarmMessageStatisticService.getExportAlarmMessageList(response, fuzzyQuery);
        } catch (Exception e) {
            log.error("导出数据异常", e);
        }

    }

}
