package com.zw.platform.controller.reportmanagement;

import com.cb.platform.domain.query.VehicleOnlineTimeQuery;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.VehicleOnlineTimeService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2021/1/8 9:57
 */
@Controller
@RequestMapping("/m/reportManagement/vehicleOnlineTime")
public class VehicleOnlineTimeController {
    @Autowired
    private VehicleOnlineTimeService vehicleOnlineTimeService;
    @Autowired
    private OfflineExportService exportService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/getOrgData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrgData(VehicleOnlineTimeQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(
            () -> vehicleOnlineTimeService.getOrgData(baseQueryBean.convertOrgNameToOrgIds(userService)),
            "查询企业月在线时长统计数据异常");
    }

    @RequestMapping(value = "/getMonitorData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getMonitorData(VehicleOnlineTimeQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> vehicleOnlineTimeService.getMonitorData(query.convertMonitorNameToMonitorIds()),
                "查询监控对象月在线时长统计数据异常");
    }

    @RequestMapping(value = "/getDivisionData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDivisionData(VehicleOnlineTimeQuery baseQueryBean) {
        return ControllerTemplate
            .getPassResultBean(() -> vehicleOnlineTimeService.getDivisionData(baseQueryBean), "查询行政区划月在线时长统计数据异常");
    }

    @RequestMapping(value = "/exportOrgData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgData(VehicleOnlineTimeQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.convertOrgNameToOrgIds(userService).getOrgOffLineExport(),
                "导出车辆在线时长统计(按道路运行企业统计)异常");
    }

    @RequestMapping(value = "/exportMonitorData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportMonitorData(VehicleOnlineTimeQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.convertMonitorNameToMonitorIds().getMonitorOffLineExport(),
                "导出车辆在线时长统计(按车辆统计)异常");
    }

    @RequestMapping(value = "/exportDivisionData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportDivisionData(VehicleOnlineTimeQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.convertOrgNameToOrgIds(userService).getDivisionOffLineExport(),
                "导出车辆在线时长统计(按行政区域统计)异常");
    }

}
