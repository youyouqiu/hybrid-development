package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.Ridership;
import com.zw.platform.domain.reportManagement.query.RidershipQuery;
import com.zw.platform.service.reportManagement.RidershipService;
import com.zw.platform.util.common.JsonResultBean;
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
import java.util.Arrays;
import java.util.List;

/**
 * 获取乘客流量报表
 *
 * @author zhangsq
 * @date 2018/3/22 16:09
 */
@Controller
@RequestMapping("/m/reportManagement/passengerFlowReport")
public class PassengerFlowReportController {

    private static final String LIST_PAGE = "modules/reportManagement/passengerFlowReport";


    private static Logger log = LogManager.getLogger(FlowReportController.class);

    @Autowired
    private RidershipService ridershipService;
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getPassengerFlowReports", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFlowReports(final RidershipQuery query) {
        try {
            if (StringUtils.isNotBlank(query.getVehicleIdStr())) {
                List<String> vehicleIds = Arrays.asList(query.getVehicleIdStr().split(","));
                query.setVehicleIds(vehicleIds);
            }
            List<Ridership> result = ridershipService.findByVehicleIdAndDate(query);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取乘客流量报表！", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(生成excel文件)
     *
     * @param res
     */
    @RequestMapping(value = "/export")
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "乘客流量报表");
            ridershipService.export(null, 1, res);
        } catch (Exception e) {
            log.error("乘客流量报表页面导出数据异常(get)", e);
        }
    }

}
