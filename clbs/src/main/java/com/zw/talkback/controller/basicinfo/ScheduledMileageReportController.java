package com.zw.talkback.controller.basicinfo;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.domain.basicinfo.form.AttendanceForm;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;
import com.zw.talkback.service.baseinfo.AttendanceReportService;
import com.zw.talkback.service.baseinfo.ScheduledMileageReportService;
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

@Controller
@RequestMapping("/talkback/reportManagement/scheduledMileageReport")
public class ScheduledMileageReportController {

    private Logger logger = LogManager.getLogger(AttendanceReportController.class);
    private static final String LIST_PAGE = "talkback/reportManagement/scheduledMileageReport";

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AttendanceReportService attendanceReportService;

    @Autowired
    private ScheduledMileageReportService scheduledMileageReportService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String monitorIds, String startTime, String endTime, String id) {
        try {
            if (StringUtils.isNotEmpty(monitorIds) && StringUtils.isNotEmpty(startTime) && StringUtils
                .isNotEmpty(endTime)) {
                List<AttendanceForm> list = attendanceReportService.getList(monitorIds, startTime, endTime, id);
                String userId = SystemHelper.getCurrentUser().getId().toString();
                final RedisKey redisKey = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
                RedisHelper.delete(redisKey);
                RedisHelper.addObjectToList(redisKey, list, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("里程报表查询数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    @RequestMapping(value = "/getSummary", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getSummary(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return scheduledMileageReportService.getSummary(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("里程报表获取汇总信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAll(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return scheduledMileageReportService.getAll(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("里程报表获取所有信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/getDetail", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getDetail(AttendanceReportQuery query) {
        try {
            if (query != null) {
                return scheduledMileageReportService.getDetail(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("里程报表获取明细信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/exportSummary", method = RequestMethod.GET)
    public void exportSummary(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "里程报表汇总");
            scheduledMileageReportService.exportSummary(response);
        } catch (Exception e) {
            logger.error("导出里程报表汇总信息异常", e);
        }
    }

    @RequestMapping(value = "/exportAll", method = RequestMethod.GET)
    public void exportAll(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "里程报表所有");
            scheduledMileageReportService.exportAll(response);
        } catch (Exception e) {
            logger.error("导出里程报表所有信息异常", e);
        }
    }

    @RequestMapping(value = "/exportDetail_{id}", method = RequestMethod.GET)
    public void exportDetail(HttpServletResponse response, @PathVariable("id") String id) {
        try {
            ExportExcelUtil.setResponseHead(response, "里程报表明细");
            scheduledMileageReportService.exportDetail(response, id);
        } catch (Exception e) {
            logger.error("导出里程报表明细信息异常", e);
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
                return scheduledMileageReportService.getAllSummary(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("里程报表获取汇总信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

}
