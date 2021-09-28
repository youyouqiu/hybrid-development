package com.zw.platform.controller.reportmanagement;


import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.LogSearch;
import com.zw.platform.domain.reportManagement.VideoLog;
import com.zw.platform.domain.reportManagement.query.LogSearchQuery;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.time.YearMonth;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;


/**
 * 日志查询Controller
 * @author wangying
 *
 */
@Controller
@RequestMapping("/m/reportManagement/logSearch")
public class LogSearchController {
    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    UserService userService;

    private static Logger log = LogManager.getLogger(LogSearchController.class);

    private static final String LIST_PAGE = "modules/reportManagement/logSearch";
    private static final String VIDEO_LIST_PAGE = "modules/reportManagement/videoLog";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage()
        throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final LogSearchQuery query) {
        try {
            if (query.getStartTime() == null) {
                log.error("查询zw_log必须指定startTime！");
                return new PageGridBean(false);
            }
            if (query.getEndTime() == null) {
                query.setEndTime(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_SHORT));
            }
            Page<LogSearch> result = (Page<LogSearch>) logSearchService.findLog(query, true);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findLog）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 实时监控日志查询
     * @param eventDate
     * @param webType 1:实时监控，2：实时视频
     * @return
     */
    @RequestMapping(value = {"/findLog"}, method = RequestMethod.POST)
    @ResponseBody
    public List<LogSearch> findLogByMonitoring(String eventDate, Integer webType) {
        try {
            return logSearchService.findLogByModule(eventDate, webType);
        } catch (Exception e) {
            log.error("实时监控日志查询异常", e);
            return null;
        }
    }

    @RequestMapping(value = {"/export"}, method = RequestMethod.POST)
    @ResponseBody
    public boolean export(LogSearchQuery query) {
        try {
            if (query.getStartTime() == null) {
                log.error("查询zw_log必须指定startTime！");
                return false;
            }
            if (query.getEndTime() == null) {
                query.setEndTime(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_SHORT));
            }
            List<LogSearch> list = logSearchService.findLog(query, false);
            String userId = userService.getCurrentUserInfo().getId().toString();
            RedisKey redisKey = HistoryRedisKeyEnum.EXPORT_LOG_FIND_INFORMATION.of(userId);
            // 再次查询前删除 key
            RedisHelper.delete(redisKey);
            // 获取组装数据存入redis管道
            RedisHelper.addToList(redisKey, list);
            return true;
        } catch (Exception e) {
            log.error("实时监控日志查询异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "日志查询报表");
            logSearchService.export(null, 1, res);
        } catch (Exception e) {
            log.error("日志查询导出数据异常(get)", e);
        }
    }

    @RequestMapping(value = {"/videoLog"}, method = RequestMethod.GET)
    public String videoListPage()
        throws BusinessException {
        return VIDEO_LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = {"/videoLog"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getVideoLog(final LogSearchQuery query) {
        try {
            if (query.getStartTime() == null) {
                log.error("查询zw_log必须指定startTime！");
                return new PageGridBean(false);
            }
            if (query.getEndTime() == null) {
                query.setEndTime(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_SHORT));
            }
            Page<VideoLog> result = (Page<VideoLog>) logSearchService.findVideoLog(query, true);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findLog）异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = {"/videoExport"}, method = RequestMethod.POST)
    @ResponseBody
    public boolean videoExport(LogSearchQuery query) {
        try {
            if (query.getStartTime() == null) {
                log.error("查询zw_log必须指定startTime！");
                return false;
            }
            if (query.getEndTime() == null) {
                query.setEndTime(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_SHORT));
            }
            List<VideoLog> list = logSearchService.findVideoLog(query, false);
            String userId = userService.getCurrentUserInfo().getId().toString();
            RedisKey redisKey = HistoryRedisKeyEnum.EXPORT_VIDEO_LOG_INFORMATION.of(userId);
            // 再次查询前删除 key
            RedisHelper.delete(redisKey);
            // 获取组装数据存入redis管道
            RedisHelper.addToList(redisKey, list);
            return true;
        } catch (Exception e) {
            log.error("实时监控日志查询异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/videoExport", method = RequestMethod.GET)
    public void videoExport(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "音视频日志查询报表");
            logSearchService.videoExport(null, 1, res);
        } catch (Exception e) {
            log.error("日志查询导出数据异常(get)", e);
        }
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = {"data-migration/zw_log"}, method = RequestMethod.POST)
    @ResponseBody
    public String getListPage(final String month) {
        try {
            final YearMonth yearMonth;
            try {
                yearMonth = YearMonth.parse(month, new DateTimeFormatterBuilder()
                        .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                        .toFormatter());
            } catch (Exception e) {
                return "请更正格式为yyyyMM";
            }
            return logSearchService.migrateLog(yearMonth);
        } catch (Exception e) {
            log.error("迁移数据出错", e);
            return e.getMessage();
        }
    }

    
}
