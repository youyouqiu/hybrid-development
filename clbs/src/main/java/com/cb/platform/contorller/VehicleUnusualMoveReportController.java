package com.cb.platform.contorller;

import com.cb.platform.dto.report.sichuan.VehicleAbnormalDrivingReportQuery;
import com.cb.platform.service.VehicleUnusualMoveReportService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * 车辆异动报表
 * @author Administrator
 */
@Controller
@RequestMapping("/cb/cbReportManagement/vehicleUnusualMove")
public class VehicleUnusualMoveReportController {
    private static final Logger logger = LogManager.getLogger(VehicleUnusualMoveReportController.class);

    @Autowired
    private VehicleUnusualMoveReportService vehicleUnusualMoveReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "/modules/cbReportManagement/vehicleUnusualMove";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 车辆异动统计 - 车辆异动道路运输企业统计报表 - 列表
     */
    @RequestMapping(value = { "/companyTransport/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query) {
        try {
            String message = validateParameter(query.getOrgIds(), query.getStartTime(), query.getEndTime());
            if (null != message) {
                return new JsonResultBean(false, message);
            }
            return vehicleUnusualMoveReportService.getVehicleAbnormalDrivingOrgReport(query);
        } catch (Exception e) {
            logger.error("车辆异动道路运输企业统计报表数据查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆异动统计 - 车辆异动道路运输企业统计报表 - 离线导出
     */
    @RequestMapping(value = { "/companyTransport/export" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query) {
        String message = validateParameter(query.getOrgIds(), query.getStartTime(), query.getEndTime());
        if (null != message) {
            return new JsonResultBean(false, message);
        }
        return vehicleUnusualMoveReportService.exportVehicleAbnormalDrivingOrgReport(query);
    }

    /**
     * 车辆异动统计 - 车辆异常行驶统计表 - 列表
     */
    @RequestMapping(value = { "/drive/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query) {
        try {
            String message = validateParameter(query.getMonitorIds(), query.getStartTime(), query.getEndTime());
            if (null != message) {
                return new JsonResultBean(false, message);
            }
            return vehicleUnusualMoveReportService.getVehicleAbnormalDrivingVehicleReport(query);
        } catch (Exception e) {
            logger.error("车辆异常行驶统计报表查询失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆异动统计 - 车辆异常行驶统计表 - 离线导出
     */
    @RequestMapping(value = { "/drive/export" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query) {
        try {
            String message = validateParameter(query.getMonitorIds(), query.getStartTime(), query.getEndTime());
            if (null != message) {
                return new JsonResultBean(false, message);
            }
            return vehicleUnusualMoveReportService.exportVehicleAbnormalDrivingVehicleReport(query);
        } catch (Exception e) {
            logger.error("车辆异常行驶统计报表导出失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆异动统计 - 车辆异常行驶明细 - 列表
     */
    @RequestMapping(value = { "/detail/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query) {
        try {
            String message = validateParameter(query.getMonitorIds(), query.getStartTime(), query.getEndTime());
            if (null != message) {
                return new PageGridBean(JsonResultBean.FAULT, message);
            }
            if (query.getStart() == null || query.getLength() == null) {
                return new PageGridBean(JsonResultBean.FAULT, "参数传递错误!");
            }
            return vehicleUnusualMoveReportService.getVehicleAbnormalDrivingVehicleDetailReport(query);
        } catch (Exception e) {
            logger.error("车辆异常行驶明细查询失败", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆异动统计 - 车辆异常行驶明细 - 离线导出
     */
    @RequestMapping(value = { "/detail/export" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query) {
        String message = validateParameter(query.getMonitorIds(), query.getStartTime(), query.getEndTime());
        if (null != message) {
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
        if (query.getStart() == null || query.getLength() == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误!");
        }
        return vehicleUnusualMoveReportService.exportVehicleAbnormalDrivingVehicleDetailReport(query);
    }

    /**
     * 校验参数
     */
    private String validateParameter(String groupIds, String startTime, String endTime) {

        if (StringUtils.isEmpty(groupIds) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return "参数传递错误!";
        }
        // 检验时间 31天
        Date startDate = DateUtil.getStringToDate(startTime, null);
        Date endDate = DateUtil.getStringToDate(endTime, null);
        if (startDate == null || endDate == null) {
            return "参数传递错误!";
        }
        Long startT = startDate.getTime() / 1000;
        Long endT = endDate.getTime() / 1000;
        long thirtyOneDays = 31 * 24 * 60 * 60L;
        if (endT - startT > thirtyOneDays) {
            return "时间范围超过31天!";
        }
        return null;
    }
}
