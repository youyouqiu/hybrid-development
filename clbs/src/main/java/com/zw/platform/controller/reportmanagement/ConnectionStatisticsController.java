package com.zw.platform.controller.reportmanagement;

import com.cb.platform.domain.query.ConnectionStaticsQuery;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.ConnectionStatisticsService;
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
@RequestMapping("/m/reportManagement/connectionStatistics")
public class ConnectionStatisticsController {
    @Autowired
    private ConnectionStatisticsService connectionStatisticsService;
    @Autowired
    private OfflineExportService exportService;

    @RequestMapping(value = "/platformList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean platformList(ConnectionStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getPassPageBean(() -> connectionStatisticsService.platformList(baseQueryBean), "查询与政府监管平台连接情况异常");
    }

    @RequestMapping(value = "/platformDetailList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean platformDetailList(ConnectionStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getPassPageBean(() -> connectionStatisticsService.platformDetailList(baseQueryBean), "查询与政府监管平台连接情况详情异常");
    }

    @RequestMapping(value = "/monitorList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean monitorList(ConnectionStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getResultBean(() -> connectionStatisticsService.monitorList(baseQueryBean), "查询与车载终端连接情况异常");
    }

    @RequestMapping(value = "/monitorDetailList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean monitorDetailList(ConnectionStaticsQuery baseQueryBean) {
        return ControllerTemplate
            .getResultBean(() -> connectionStatisticsService.monitorDetailList(baseQueryBean), "查询与车载终端连接情况详情异常");
    }

    @RequestMapping(value = "/exportPlatformList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportPlatformList(ConnectionStaticsQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOffLineExportPlatform(), "导出与政府监管平台连接情况异常");
    }

    @RequestMapping(value = "/exportPlatformDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportPlatformDetail(ConnectionStaticsQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.getOffLineExportPlatformDetail(), "导出与政府监管平台连接情况详情异常");
    }

    @RequestMapping(value = "/exportMonitorList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportMonitorList(ConnectionStaticsQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOffLineExportMonitor(), "导出与车载终端连接情况异常");
    }

    @RequestMapping(value = "/exportMonitorDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportMonitorDetail(ConnectionStaticsQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, query.getOffLineExportMonitorDetail(), "导出与车载终端连接情况详情异常");
    }

}
