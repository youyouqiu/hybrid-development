package com.zw.platform.controller.reportmanagement;

import com.cb.platform.domain.query.PointQuery;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.PointService;
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
@RequestMapping("/m/reportManagement/point")
public class PointController {
    @Autowired
    private PointService pointService;
    @Autowired
    private OfflineExportService exportService;
    @Autowired
    private FenceConfigService fenceConfigService;

    @RequestMapping(value = "/getOrgData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrgData(PointQuery baseQueryBean) {
        return ControllerTemplate
            .getPassPageBean(() -> pointService.getOrgData(baseQueryBean.initOrgPassPointIds(fenceConfigService)),
                "查询企业月途经点统计数据异常");
    }

    @RequestMapping(value = "/getMonitorData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getMonitorData(PointQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> pointService.getMonitorData(query.initMonitorPassPointIds(fenceConfigService)),
                "查询监控对象月途经点统计数据异常");
    }

    @RequestMapping(value = "/getMonitorDetailData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getMonitorDetailData(PointQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(
            () -> pointService.getMonitorDetailData(baseQueryBean.initMonitorPassPointIds(fenceConfigService)),
            "查询月途经点详情统计数据异常");
    }

    @RequestMapping(value = "/exportOrgData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgData(PointQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.initOrgPassPointIds(fenceConfigService).getOrgOffLineExport(),
                "导出车辆途经点统计(按道路运行企业统计)异常");
    }

    @RequestMapping(value = "/exportMonitorData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportMonitorData(PointQuery query) {
        return ControllerTemplate.addExportOffline(exportService,
            query.initMonitorPassPointIds(fenceConfigService).getMonitorOffLineExport(), "导出车辆途经点统计(按车辆统计)异常");
    }

    @RequestMapping(value = "/exportMonitorDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDivisionOffLineExport(PointQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.initMonitorPassPointIds(fenceConfigService).getMonitorDetailExport(),
                "导出车辆途经点统计(按行政区域统计)异常");
    }

}
