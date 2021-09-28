package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.OilMassMile;
import com.zw.platform.dto.reportManagement.OilAmountAndSpillQuery;
import com.zw.platform.service.reportManagement.impl.OilMassMileReportServerImpl;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 油量里程报表controller
 */
@Controller
@RequestMapping("/m/reportManagement/oilMassMileReport")
public class OilMassMileReportController {
    private final Logger log  = LogManager.getLogger(OilMassMileReportController.class);

    private static final String LIST_PAGE = "modules/reportManagement/fuelMileageReport";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private OilMassMileReportServerImpl reportServerImpl;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 查询油量里程报表列表数据
     */
    @RequestMapping(value = "/getListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilMassMileData(String vehicleId, String startDate, String endDate) {
        try {
            List<OilMassMile> result = reportServerImpl.getOilMassMileData(vehicleId, startDate, endDate);
            JSONObject msg = new JSONObject();
            msg.put("oilMassMileData", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("油量里程报表列表数据查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询油量里程报表加漏油详情数据
     */
    @RequestMapping(value = "/getDetailData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilMassMileDetailData(OilAmountAndSpillQuery query) {
        try {
            return reportServerImpl.getAmountOrSpillData(query);
        } catch (Exception e) {
            log.error("油量里程报表加漏油数据查询异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出油量里程报表列表数据
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportOilMassMileData(HttpServletResponse response, String fuzzyParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "油量里程报表");
            reportServerImpl.exportOilMassMileData(response, fuzzyParam);
        } catch (Exception e) {
            log.error("导出油量里程报表异常", e);
        }
    }
}
