package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.reportManagement.AccStatisticsDetailQuery;
import com.zw.platform.dto.reportManagement.AccStatisticsQuery;
import com.zw.platform.service.reportManagement.AccStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ACC统计报表controller
 * @author tianzhangxu
 */
@Controller
@RequestMapping("/m/reportManagement/accStatistics")
public class AccStatisticsController {
    private static final Logger log = LogManager.getLogger(AccStatisticsController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "/modules/reportManagement/accStatistics/list";

    @Autowired
    private AccStatisticsService accStatisticsService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询ACC统计报表列表
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAccStatisticsInfo(AccStatisticsQuery query) {
        try {
            return accStatisticsService.getAccStatisticsInfo(query);
        } catch (Exception e) {
            log.error("ACC统计报表列表查询异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出ACC统计报表列表
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportAccStatisticsInfo(AccStatisticsQuery query) {
        return accStatisticsService.exportAccStatisticsInfo(query);
    }

    /**
     * 查询ACC统计报表详情列表
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAccStatisticsDetailInfo(AccStatisticsDetailQuery query) {
        try {
            return accStatisticsService.getAccStatisticsDetailInfo(query);
        } catch (Exception e) {
            log.error("ACC统计报表详情列表查询异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

}
