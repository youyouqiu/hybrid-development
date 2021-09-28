package com.cb.platform.contorller;

import com.cb.platform.domain.query.FatigueDrivingQuery;
import com.cb.platform.domain.query.FatigueDrivingVehQuery;
import com.cb.platform.service.FatigueDrivingService;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 @Author zhengjc
 @Date 2020/5/15 16:54
 @Description 四川报表新增：疲劳驾驶报警统计报表
 @version 1.0
 **/
@Controller
@RequestMapping("/cb/cbReportManagement/fatigueDriving")
public class FatigueDrivingController {

    private static final String LIST_PAGE = "/modules/cbReportManagement/fatigueDriving/list";

    @Autowired
    private FatigueDrivingService fatService;
    @Autowired
    private OfflineExportService exportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/listOrg", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getData(FatigueDrivingQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(() -> fatService.getOrgDataList(baseQueryBean), "查询企业疲劳报警列表异常");
    }

    @RequestMapping(value = "/listVeh", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean listVeh(FatigueDrivingVehQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(() -> fatService.getVehDataList(baseQueryBean), "查询车辆疲劳报警列表异常");
    }

    @RequestMapping(value = "/getOrgGraphicsData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOrgGraphicsData(FatigueDrivingQuery baseQueryBean) {
        return ControllerTemplate.getPassResultBean(() -> fatService.getOrgGraphicsData(baseQueryBean), "查询企业疲劳报警图形异常");
    }

    @RequestMapping(value = "/getVehGraphicsData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehGraphicsData(FatigueDrivingVehQuery baseQueryBean) {
        return ControllerTemplate.getPassResultBean(() -> fatService.getVehGraphicsData(baseQueryBean), "查询车辆疲劳报警图形异常");
    }

    @RequestMapping(value = "/getOrgRankData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOrgRankData(FatigueDrivingQuery baseQueryBean) {
        return ControllerTemplate.getPassResultBean(() -> fatService.getOrgRankData(baseQueryBean), "查询单个企业疲劳报警排行异常");
    }

    @RequestMapping(value = "/getVehRankData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehRankData(FatigueDrivingVehQuery baseQueryBean) {
        return ControllerTemplate.getPassResultBean(() -> fatService.getVehRankData(baseQueryBean), "查询单个车辆疲劳报警排行异常");
    }

    @RequestMapping(value = "/getOrgDetailData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOrgDetailData(FatigueDrivingQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(() -> fatService.getOrgDetailData(baseQueryBean), "查询企业疲劳报警车辆详情列表异常");
    }

    @RequestMapping(value = "/getVehDetailData", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getVehDetailData(FatigueDrivingVehQuery baseQueryBean) {
        return ControllerTemplate.getPassPageBean(() -> fatService.getVehDetailData(baseQueryBean), "查询疲劳报警车辆详情列表异常");
    }

    @RequestMapping(value = "/exportOrgListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgListData(FatigueDrivingQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOrgListOffLineExport(), "导出企业疲劳驾驶列表异常");
    }

    @RequestMapping(value = "/exportOrgDetailData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgDetailData(FatigueDrivingQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOrgDetailOffLineExport(), "企业疲劳驾驶报警明细统计报表");
    }

    @RequestMapping(value = "/exportVehListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehListData(FatigueDrivingVehQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOrgListOffLineExport(), "导出车辆疲劳驾驶列表异常");
    }

    @RequestMapping(value = "/exportVehDetailData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehDetailData(FatigueDrivingVehQuery query) {
        return ControllerTemplate.addExportOffline(exportService, query.getOrgDetailOffLineExport(), "车辆疲劳驾驶报警明细统计报表");
    }

}
