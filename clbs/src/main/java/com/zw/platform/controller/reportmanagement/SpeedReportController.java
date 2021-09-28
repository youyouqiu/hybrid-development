package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.SpeedReport;
import com.zw.platform.service.reportManagement.SpeedReportService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisUtil;
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

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/m/reportManagement/speedReport")
public class SpeedReportController {

    private static final String LIST_PAGE = "modules/reportManagement/speedReport";

    private static Logger log = LogManager.getLogger(SpeedReportController.class);

    @Autowired
    SpeedReportService speedReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询车辆超速报警报表信息(用于统计)
     * @param vehicleList
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     * @throws BusinessException
     * @throws ParseException
     */
    @RequestMapping(value = "/getSpeedAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSpeedAlarmList(String vehicleList, String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleList) && StringUtils.isNotBlank(vehicleId) && StringUtils
                .isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                List<SpeedReport> speedReports =
                    speedReportService.getSpeedAlarm(vehicleList, vehicleId, startTime, endTime);
                return new JsonResultBean(speedReports);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("超速报表页面查询超速信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询车辆超速报警数据(大数据月表)
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getSpeedReportBigData", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean getSpeedReportBigData(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<SpeedReport> speedReports =
                    speedReportService.getSpeedReportBigData(vehicleId, startTime, endTime);
                return new JsonResultBean(speedReports);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("超速报表页面查询超速信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(存数据)
     * @param vehicleList(全部车牌号)
     * @param vehicleId(全部车辆id)
     * @param startTime(开始时间)
     * @param endTime(结束时间)
     * @throws ParseException
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public boolean export1(String vehicleList, String vehicleId, String startTime, String endTime, int exportType) {
        try {
            List<SpeedReport> speedReports;
            switch (exportType) {
                case 1:
                    speedReports = speedReportService.getSpeedAlarm(vehicleList, vehicleId, startTime, endTime);
                    break;
                case 2:
                    speedReports = speedReportService.getSpeedReportBigData(vehicleId, startTime, endTime);
                    break;
                default:
                    speedReports = new ArrayList<>();
                    break;
            }
            RedisUtil.storeExportDataToRedis("exportSpeedReport", speedReports);
            return true;
        } catch (Exception e) {
            log.error("超速报表页面导出超速报表(post)异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     * @throws UnsupportedEncodingException
     */
    @Deprecated
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "超速报警报表");
            speedReportService.export(null, 1, res, RedisUtil.getExportDataFromRedis("exportSpeedReport"));
        } catch (Exception e) {
            log.error("超速报表页面导出超速报表(get)异常", e);
        }
    }

    /**
     * 获得超速报表列表
     */
    @RequestMapping(value = "/getSpeedingReportList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSpeedingReportList(String monitorIds, String startTime, String endTime) {
        try {
            if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
            }
            return speedReportService.getSpeedingReportList(monitorIds, startTime, endTime);
        } catch (Exception e) {
            log.error("获得超速报表列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出超速报表列表
     */
    @RequestMapping(value = "/exportSpeedingReportList", method = RequestMethod.POST)
    public void exportSpeedingReportList(HttpServletResponse response, String monitorIds, String startTime,
        String endTime) {
        try {
            speedReportService.exportSpeedingReportList(response, monitorIds, startTime, endTime);
        } catch (Exception e) {
            log.error("导出超速报表列表异常", e);
        }
    }
}
