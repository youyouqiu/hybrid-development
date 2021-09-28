package com.zw.talkback.controller.basicinfo;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.domain.basicinfo.form.AttendanceForm;
import com.zw.talkback.domain.basicinfo.form.SchedulingRelationMonitor;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;
import com.zw.talkback.service.baseinfo.AttendanceReportService;
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

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 出勤报表
 * create by denghuabing 2019.5.13
 */
@Controller
@RequestMapping("/talkback/reportManagement/scheduledAttendanceReport")
public class AttendanceReportController {

    private Logger logger = LogManager.getLogger(AttendanceReportController.class);
    private static final String LIST_PAGE = "talkback/reportManagement/scheduledAttendanceReport";

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AttendanceReportService attendanceReportService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 排班下拉
     * @return
     */
    @RequestMapping(value = "/getScheduledList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getScheduledList() {
        try {
            List<SchedulingInfo> scheduledList = attendanceReportService.getScheduledList();
            return new JsonResultBean(scheduledList);
        } catch (Exception e) {
            logger.error("出勤报表获取排班信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 根据排班id查排班所属监控对象
     * @param
     * @return
     */
    @RequestMapping(value = "/findMonitoringObject", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findPeopleByScheduled(String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                List<SchedulingRelationMonitor> monitorInfoList =
                    attendanceReportService.getSchedulingRelationMonitorInfoList(id);
                return new JsonResultBean(monitorInfoList);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("查询排班下监控对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }


    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String monitorIds, String startTime, String endTime, String id) {
        try {
            if (StringUtils.isNotEmpty(monitorIds) && StringUtils.isNotEmpty(startTime) && StringUtils
                .isNotEmpty(endTime)) {
                List<AttendanceForm> list = attendanceReportService.getList(monitorIds, startTime, endTime, id);
                String userId = SystemHelper.getCurrentUser().getId().toString();
                final RedisKey redisKey = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
                RedisHelper.delete(redisKey);
                RedisHelper.addObjectToList(redisKey, list, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
                return new JsonResultBean(list);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("出勤报表查询数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    @RequestMapping(value = "/getSummary", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getSummary(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return attendanceReportService.getSummary(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("出勤报表获取汇总信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAll(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return attendanceReportService.getAll(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("出勤报表获取所有信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/getDetail", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getDetail(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return attendanceReportService.getDetail(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("出勤报表获取明细信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/exportSummary", method = RequestMethod.GET)
    public void exportSummary(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "出勤报表汇总");
            attendanceReportService.exportSummary(response);
        } catch (Exception e) {
            logger.error("导出出勤报表汇总信息异常", e);
        }
    }

    @RequestMapping(value = "/exportAll", method = RequestMethod.GET)
    public void exportAll(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "出勤报表所有");
            attendanceReportService.exportAll(response);
        } catch (Exception e) {
            logger.error("导出出勤报表所有信息异常", e);
        }
    }

    @RequestMapping(value = "/exportDetail_{id}", method = RequestMethod.GET)
    public void exportDetail(HttpServletResponse response, @PathVariable("id") String id) {
        try {
            ExportExcelUtil.setResponseHead(response, "出勤报表明细");
            attendanceReportService.exportDetail(response, id);
        } catch (Exception e) {
            logger.error("导出出勤报表明细信息异常", e);
        }
    }

    /**
     * 图表需求全部数据
     * @param query
     * @return
     */
    @RequestMapping(value = "/getAllSummary", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAllSummary(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return attendanceReportService.getAllSummary(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("出勤报表获取汇总信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

}
