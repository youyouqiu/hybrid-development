package com.zw.platform.controller.statistic;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.ObdTripDataQuery;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * OBD行程报表
 * @author zhouzongbo on 2019/2/25 10:07
 */
@Controller
@RequestMapping("/v/statistic/obdTripReport/")
public class ObdTripReportController {

    private static Logger log = LogManager.getLogger(ObdTripReportController.class);

    private static final String LIST_PAGE = "vas/statistic/obdTripReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Resource
    private PositionalService positionalService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        return new ModelAndView(LIST_PAGE);
    }

    /**
     * 查询obd行程统计数据
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = { "/getObdTripDataList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getObdTripDataList(String vehicleIds, String startTime, String endTime) {
        try {
            return positionalService.getObdTripDataList(vehicleIds, startTime, endTime);
        } catch (Exception e) {
            log.error("查询obd行程统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 查询obd行程统计分页数据
     */
    @ResponseBody
    @RequestMapping(value = "/getObdTripDataPageList", method = RequestMethod.POST)
    public PageGridBean getObdTripDataPageList(final ObdTripDataQuery query) {
        try {
            return positionalService.getTotalDataFormInfo(query);
        } catch (Exception e) {
            log.error("查询obd行程统计分页数据", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出obd行程统计
     * @param response
     * @param simpleQueryParam 监控对象名称
     */
    @RequestMapping(value = "/exportObdTripDataList", method = RequestMethod.GET)
    public void exportObdTripDataList(HttpServletResponse response, String simpleQueryParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "OBD行程报表");
            positionalService.exportObdTripDataList(response, simpleQueryParam);
        } catch (Exception e) {
            log.error("导出obd行程统计", e);
        }
    }
}
