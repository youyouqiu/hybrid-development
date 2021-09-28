package com.zw.lkyw.controller.videoCarousel;

import com.github.pagehelper.Page;
import com.zw.lkyw.domain.VideoCarouselReportQuery;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReport;
import com.zw.lkyw.domain.videoCarouselReport.VideoInspectionDetail;
import com.zw.lkyw.service.videoCarousel.VideoCarouselReportService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 视频轮播报表
 * @author 万兴 on 2019/12/27
 */
@RequestMapping("/lkyw/report/videoCarouselReport")
@Controller
public class VideoCarouselReportController {
    private Logger log = LogManager.getLogger(VideoCarouselReportController.class);

    private static final String LIST_PAGE = "vas/lkyw/videoCarouselReport/list";

    @Autowired
    private VideoCarouselReportService videoCarouselReportService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     *
     * @param query query
     * @return PageGridBean
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(VideoCarouselReportQuery query) {
        try {
            if (checkParameters(query)) {
                return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
            }
            setParameterMapOne(query);
            Page<VideoCarouselReport> result = videoCarouselReportService.getListPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询终端信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 分页查询
     *
     * @param query query
     * @return PageGridBean
     */
    @RequestMapping(value = {"/detail"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean detail(VideoCarouselReportQuery query) {
        try {
            if (checkParameters(query)) {
                return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
            }
            setParameterMapTwo(query);
            Page<VideoInspectionDetail> result = videoCarouselReportService.detail(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询终端信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出接口
     * @param response
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        try {
            if (checkParameters(query)) {
                return new JsonResultBean(PageGridBean.FAULT, "参数传递错误");
            }
            String message = videoCarouselReportService.export(response, query);
            if (message == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, message);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "导出视频巡检明细出错");
        }

    }

    /**
     * 导出明细接口
     * @param response
     */
    @RequestMapping(value = "/exportDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportDetail(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        try {
            if (checkParameters(query)) {
                return new JsonResultBean(PageGridBean.FAULT, "参数传递错误");
            }
            String message = videoCarouselReportService.exportDetail(response, query);
            if (message == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, message);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "导出视频巡检明细出错");
        }
    }

    /**
     * 批量导出接口
     * @param response
     */
    @RequestMapping(value = "/batchExport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchExport(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        try {
            if (checkParameters(query)) {
                return new JsonResultBean(PageGridBean.FAULT, "参数传递错误");
            }
            setParameterMapTwo(query);
            String message = videoCarouselReportService.batchExport(response, query);
            if (message == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, message);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "批量导出视频巡检明细出错");
        }
    }

    private boolean checkParameters(VideoCarouselReportQuery query) {
        if (CollectionUtils.isEmpty(query.getMonitorIds()) || StringUtils.isBlank(query.getStartTime()) || StringUtils
            .isBlank(query.getEndTime())) {
            return true;
        }
        return false;
    }

    private void setParameterMapOne(VideoCarouselReportQuery query) {
        Map<String, String> queryParam = query.getQueryParam();
        queryParam.put("monitorIds", String.join(",", query.getMonitorIds()));
        queryParam.put("startTime", query.getStartTime()
            .replaceAll("\\s+", "").replaceAll("-", "").replaceAll(":", ""));
        queryParam.put("endTime", query.getEndTime().replaceAll("\\s+", "")
            .replaceAll("-", "").replaceAll(":", ""));
        if (StringUtils.isNotBlank(query.getStatus())) {
            queryParam.put("status", query.getStatus());
        }
    }

    private void setParameterMapTwo(VideoCarouselReportQuery query) {
        Map<String, String> queryParam = query.getQueryParam();
        queryParam.put("monitorIds", String.join(",", query.getMonitorIds()));
        queryParam.put("startTime", query.getStartTime()
            .replaceAll("\\s+", "").replaceAll("-", "").replaceAll(":", "") + "000000");
        queryParam.put("endTime", query.getEndTime().replaceAll("\\s+", "")
            .replaceAll("-", "").replaceAll(":", "") + "235959");
    }
}
