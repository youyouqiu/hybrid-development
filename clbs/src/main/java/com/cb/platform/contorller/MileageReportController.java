package com.cb.platform.contorller;

import com.cb.platform.service.VehicleMileMonthService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhangsq
 * @date 2018/5/10 11:04
 */
@RestController("cbMileageReportController")
@RequestMapping("/cb/cbReportManagement/mileageReport")
public class MileageReportController {

    private static final Logger logger = LoggerFactory.getLogger(MileageReportController.class);
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private VehicleMileMonthService vehicleMileMonthService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView enterprise() {
        return new ModelAndView("modules/cbReportManagement/mileageReport");
    }

    /**
     * 车辆日里程统计表
     */
    @RequestMapping(value = "days", method = RequestMethod.POST)
    public JsonResultBean vehicleDays(String vehicleIds, String month, String simpleQueryParam) {
        try {
            if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(month)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
            }
            return new JsonResultBean(
                vehicleMileMonthService.getVehicleMileMonths(vehicleIds, month, simpleQueryParam));
        } catch (Exception e) {
            logger.error("获取车辆日里程统计表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出车辆日里程报表
     */
    @RequestMapping(value = "/export/month", method = RequestMethod.POST)
    public JsonResultBean exportVehicleMonth(String vehicleIds, String month, String simpleQueryParam) {
        return vehicleMileMonthService.exportVehicleMonth(vehicleIds, month, simpleQueryParam);
    }

    @RequestMapping(value = "detailList", method = RequestMethod.POST)
    public JsonResultBean detailList(String vehicleIds, String startTime, String endTime, String simpleQueryParam) {
        try {
            if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
            }
            return new JsonResultBean(
                vehicleMileMonthService.getDetailList(vehicleIds, startTime, endTime, simpleQueryParam));
        } catch (Exception e) {
            logger.error("获取车辆日里程统计表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/export/detail", method = RequestMethod.POST)
    public JsonResultBean detail(String vehicleIds, String startTime, String endTime, String simpleQueryParam) {
        return vehicleMileMonthService.exportVehicleMileDetail(vehicleIds, startTime, endTime, simpleQueryParam);
    }

}
