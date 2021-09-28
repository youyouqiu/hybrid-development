package com.zw.platform.controller.oilsubsidy;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.mileagereport.OilSubsidyVehicleMileMonthVO;
import com.zw.platform.service.oilsubsidy.OilSubsidyVehicleMileageReportService;
import com.zw.platform.util.common.BusinessException;
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
 * 上报里程统计
 * @author XK
 */
@Controller
@RequestMapping("/m/report/mileage/statistics")
public class ReportMileageStatisticsController {
    private static final String LIST_PAGE = "/modules/oilSubsidyManage/reportMileageStatistics/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";
    private static Logger logger = LogManager.getLogger(ReportMileageStatisticsController.class);
    @Autowired
    private OilSubsidyVehicleMileageReportService oilSubsidyVehicleMileageReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 车辆日里程统计表
     * @param vehicleId 车辆id
     * @param month     月份
     * @return json
     */
    @ResponseBody
    @RequestMapping(value = "days", method = RequestMethod.POST)
    public JsonResultBean vehicleDays(String vehicleId, String month) {
        try {
            if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(month)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数异常");
            }
            List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
            List<OilSubsidyVehicleMileMonthVO> vehicleMileMonthReport =
                oilSubsidyVehicleMileageReportService.getVehicleMileMonths(vehicleIds, month);
            return new JsonResultBean(vehicleMileMonthReport);
        } catch (Exception e) {
            logger.error("获取油补车辆里程统计表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出车上报里程统计报表
     * @param res res
     */
    @RequestMapping(value = "/export/month")
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "上报里程统计报表");
            oilSubsidyVehicleMileageReportService.exportVehicleMonth(null, 1, res);
        } catch (Exception e) {
            logger.error(" 导出车辆日里程统计报表异常", e);
        }
    }
}