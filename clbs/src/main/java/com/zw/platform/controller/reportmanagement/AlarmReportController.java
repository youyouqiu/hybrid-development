package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.AlarmInformation;
import com.zw.platform.service.reportManagement.AlarmReportService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fanlu
 * @date 2017/3/30
 */
@Controller
@RequestMapping("/m/reportManagement/alarmReport")
public class AlarmReportController {
    private static final Logger log = LogManager.getLogger(AlarmReportController.class);

    private static final String LIST_PAGE = "modules/reportManagement/alarmReport";

    @Autowired
    private AlarmReportService alarmReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询车辆报警信息(用于统计)
     * @param vehicleId(全部车id) startTime(开始时间) endTime(结束时间)
     * @return JsonResultBean
     */
    @RequestMapping(value = "/information", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean information(String vehicleId, String startTime, String endTime) {
        try {
            List<AlarmInformation> alarmInformationList = new ArrayList<>();
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                alarmInformationList = alarmReportService.getAlarmInformation(vehicleId, startTime, endTime);
            }
            return new JsonResultBean(alarmInformationList);
        } catch (Exception e) {
            log.error("报警信息统计查询车辆报警信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(存数据)
     * @param vehicleId (全部车辆id)
     * @param startTime (开始时间)
     * @param endTime   (结束时间)
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean exportQueryList(String vehicleId, String startTime, String endTime, String simpleQueryParam,
        int exportType) {
        try {
            return alarmReportService.exportQueryList(vehicleId, startTime, endTime, simpleQueryParam, exportType);
        } catch (Exception e) {
            log.error("报警信息统计页面导出报警信息统计列表异常(post)", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse httpResponse) {
        try {
            alarmReportService.export(httpResponse);
        } catch (Exception e) {
            log.error("报警信息统计页面导出报警信息统计列表(get)异常", e);
        }
    }

    @RequestMapping(value = "/getMonitorAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorAlarmData(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<AlarmInformation> queryResult = alarmReportService.getAlarmData(vehicleId, startTime, endTime);
                return new JsonResultBean(queryResult);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "页面参数错误");
            }
        } catch (Exception e) {
            log.error("查询报警信息报表(大数据月表)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
