package com.zw.platform.controller.reportmanagement;

import com.cb.platform.domain.query.VehInformationStaticsQuery;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.VehicleInformationStatisticsService;
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
@RequestMapping("/m/reportManagement/vehicleInformationStatistics")
public class VehicleInformationStatisticsController {
    @Autowired
    private VehicleInformationStatisticsService visService;
    @Autowired
    private OfflineExportService exportService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/getOrgGraph", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOrgGraph(VehInformationStaticsQuery baseQueryBean) {
        return ControllerTemplate.getPassResultBean(() -> visService.getOrgGraph(baseQueryBean), "查询企业月定位信息统计图形数据异常");
    }

    @RequestMapping(value = "/getOrgList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrgList(VehInformationStaticsQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> visService.getOrgList(query.convertOrgNameToOrgIds(userService)), "查询企业月定位信息统计列表异常");
    }

    @RequestMapping(value = "/getOrgDetailGraph", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOrgDetailGraph(VehInformationStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getPassResultBean(() -> visService.getOrgDetailGraph(baseQueryBean), "查询企业下监控对象月定位信息统计图形数据异常");
    }

    @RequestMapping(value = "/getOrgDetailList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrgDetailList(VehInformationStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getPassPageBean(() -> visService.getOrgDetailList(baseQueryBean), "查询企业下监控对象每日定位信息统计列表异常");
    }

    @RequestMapping(value = "/exportOrgListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgListData(VehInformationStaticsQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.convertOrgNameToOrgIds(userService).getOrgListOffLineExport(),
                "导出车辆信息统计汇总异常");
    }

    @RequestMapping(value = "/exportOrgDetailData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgDetailData(VehInformationStaticsQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOrgDetailOffLineExport(), "导出车辆信息统计明细异常");
    }
}
