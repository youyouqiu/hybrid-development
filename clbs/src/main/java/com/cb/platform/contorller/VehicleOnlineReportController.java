package com.cb.platform.contorller;

import com.cb.platform.dto.VehicleOnlineRateQuery;
import com.cb.platform.service.VehicleOnlineMonthService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 车辆在线率统计报表
 * @author zhangsq
 * @date 2018/5/2 11:48
 */
@RestController
@RequestMapping("/cb/cbReportManagement/vehicleOnlineRateReport")
public class VehicleOnlineReportController {
    private static final Logger logger = LogManager.getLogger(VehicleOnlineReportController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private VehicleOnlineMonthService vehicleOnlineMonthService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView enterprise() {
        return new ModelAndView("modules/cbReportManagement/vehicleOnlineRateReport");
    }

    /**
     * 车辆在线率道路运输企业统计月报表
     */
    @RequestMapping(value = "enterpriseMonthList", method = RequestMethod.POST)
    public PageGridBean getVehicleOnlineRateOrgMonthReport(VehicleOnlineRateQuery query) {
        try {
            if (StringUtils.isBlank(query.getEnterpriseIds()) || StringUtils.isBlank(query.getMonth())
                || query.getPage() == null || query.getLimit() == null) {
                return new PageGridBean(PageGridBean.FAULT, "参数不能为空");
            }
            return vehicleOnlineMonthService.getVehicleOnlineRateOrgMonthReport(query);
        } catch (Exception e) {
            logger.error("获取车辆在线率道路运输企业统计月报表异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆在线率统计月报表
     */
    @RequestMapping(value = "vehicleMonthList", method = RequestMethod.POST)
    public PageGridBean getVehicleOnlineRateVehicleMonthReport(VehicleOnlineRateQuery query) {
        try {
            if (StringUtils.isBlank(query.getVehicleIds()) || StringUtils.isBlank(query.getMonth())
                || query.getPage() == null || query.getLimit() == null) {
                return new PageGridBean(PageGridBean.FAULT, "参数不能为空");
            }
            return vehicleOnlineMonthService.getVehicleOnlineRateVehicleMonthReport(query);
        } catch (Exception e) {
            logger.error("获取车辆在线率统计月报表异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆在线明细
     */
    @RequestMapping(value = "detailList", method = RequestMethod.POST)
    public JsonResultBean getVehicleOnlineDetails(VehicleOnlineRateQuery query) {
        try {
            if (StringUtils.isBlank(query.getVehicleIds()) || StringUtils.isBlank(query.getStartTime()) || StringUtils
                .isBlank(query.getEndTime())) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
            }
            return new JsonResultBean(vehicleOnlineMonthService.getVehicleOnlineDetails(query));
        } catch (Exception e) {
            logger.error("获取车辆在线率统计月报表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(生成excel文件)
     */
    @RequestMapping(value = "/export/{type}", method = RequestMethod.POST)
    public JsonResultBean export(VehicleOnlineRateQuery query, @PathVariable("type") Integer type) {
        switch (type) {
            case 1:
                return vehicleOnlineMonthService.exportVehicleOnlineRateOrgMonthReport(query);
            case 2:
                return vehicleOnlineMonthService.exportVehicleOnlineRateVehicleMonthReport(query);
            case 3:
                return vehicleOnlineMonthService.exportVehicleOnlineDetails(query);
            default:
                break;
        }
        return new JsonResultBean(JsonResultBean.FAULT, "导出类型错误");
    }

}
