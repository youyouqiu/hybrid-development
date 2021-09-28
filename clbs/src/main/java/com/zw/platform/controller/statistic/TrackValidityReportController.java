package com.zw.platform.controller.statistic;

import com.zw.platform.service.statistic.TrackValidReportService;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * 轨迹有效性报表
 * @author zhouzongbo on 2019/5/31 15:37
 */
@Controller
@RequestMapping("/m/trackValidReport")
public class TrackValidityReportController {

    private static Logger logger = LogManager.getLogger(TrackValidityReportController.class);

    private static final String LIST_PAGE = "vas/statistic/trackValidReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private TrackValidReportService trackValidReportService;

    // @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView initList() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 获取连续性分析报表数据(图表和列表)
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getTrackValidList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTrackValidList(String monitorId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotEmpty(monitorId) && StringUtils.isNotEmpty(startTime) && StringUtils
                .isNotEmpty(endTime)) {
                return trackValidReportService.trackValidityReportService(monitorId, startTime, endTime);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("获取连续性分析报表数据列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取连续性分析报表数据(图表和列表)
     * @return JsonResultBean
     */
    @RequestMapping(value = "/getTrackValidListByF3Pass", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTrackValidListByF3Pass(String monitorId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotEmpty(monitorId) && StringUtils.isNotEmpty(startTime) && StringUtils
                .isNotEmpty(endTime)) {
                return trackValidReportService.getTrackValidListByF3Pass(monitorId, startTime, endTime);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("获取连续性分析报表数据列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


    @RequestMapping(value = "/exportTrackValidList", method = RequestMethod.GET)
    public void exportTrackValidList(HttpServletResponse response, String simpleQueryParam) {
        try {
            trackValidReportService.exportTrackValidList(response, simpleQueryParam);
        } catch (Exception e) {
            logger.error("导出轨迹有效性报表异常", e);
        }

    }

}
