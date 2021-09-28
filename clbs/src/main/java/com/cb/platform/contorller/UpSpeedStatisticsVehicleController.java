package com.cb.platform.contorller;

import com.cb.platform.domain.speedingStatistics.quey.UpSpeedVehicleQuery;
import com.cb.platform.service.speedingStatistics.UpSpeedStatisticsVehicleService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description: 超速统计 车辆纬度
 * @Author zhangqiang
 * @Date 2020/5/20 15:25
 */
@Controller
@RequestMapping("/cb/cbReportManagement/speedingStatistics/vehicle")
public class UpSpeedStatisticsVehicleController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private UpSpeedStatisticsVehicleService upSpeedStatisticsVehicleService;

    @Autowired
    private OfflineExportService exportService;

    /**
     * 超速列表数据
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/speedingStatisticsList", method = RequestMethod.POST)
    public PageGridBean speedingStatisticsList(UpSpeedVehicleQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> upSpeedStatisticsVehicleService.speedingStatisticsList(query), "查询超速车辆统计报警列表异常");
    }

    /**
     * 超速明细图形统计
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upSpeedGraphicalInfo", method = RequestMethod.POST)
    public JsonResultBean upSpeedGraphicalInfo(UpSpeedVehicleQuery query) {
        try {
            return upSpeedStatisticsVehicleService.upSpeedGraphicalInfo(query);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 超速明细排名信息
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/rankInfo", method = RequestMethod.POST)
    public JsonResultBean rank(UpSpeedVehicleQuery query) {
        try {
            return upSpeedStatisticsVehicleService.rankInfo(query);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 超速明细列表数据
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upSpeedInfoList", method = RequestMethod.POST)
    public PageGridBean upSpeedInfoList(UpSpeedVehicleQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> upSpeedStatisticsVehicleService.upSpeedInfoList(query), "查询超速车辆统计报警列表明细异常");
    }

    /**
     * 导出车辆超速报警统计报表
     * @param query
     * @return
     */
    @RequestMapping(value = "/exportVehListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgListData(UpSpeedVehicleQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, upSpeedStatisticsVehicleService.exportVehListData(query), "导出车辆超速统计报表异常");
    }

    /**
     * 导出车辆超速明细报表
     * @param query
     * @return
     */
    @RequestMapping(value = "/exportVehSpeedDetailsData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportVehSpeedDetailsData(UpSpeedVehicleQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, upSpeedStatisticsVehicleService.exportVehSpeedDetailsData(query),
                "导出车辆超速明细报表异常");
    }

}
