package com.cb.platform.contorller;

import com.cb.platform.domain.speedingStatistics.quey.UpSpeedGroupQuery;
import com.cb.platform.service.speedingStatistics.UpSpeedStatisticsGroupService;
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
 * @Description: 四川超速统计报表 企业纬度
 * @Author zhangqiang
 * @Date 2020/5/14 16:45
 */
@Controller
@RequestMapping("/cb/cbReportManagement/speedingStatistics/group")
public class UpSpeedStatisticsGroupController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private UpSpeedStatisticsGroupService speedingStatisticsService;

    @Autowired
    private OfflineExportService exportService;

    /**
     * 超速图表统计数据
     * @param groupId
     * @param time
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/graphical", method = RequestMethod.POST)
    public JsonResultBean graphicalStatistics(String groupId, String time, String isSingle) {
        try {
            return speedingStatisticsService.findGraphicalStatistics(groupId, time, isSingle);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 超速列表数据
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/speedingStatisticsList", method = RequestMethod.POST)
    public PageGridBean speedingStatisticsList(UpSpeedGroupQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> speedingStatisticsService.speedingStatisticsList(query), "查询超速企业列表报警列表异常");
    }

    /**
     * 超速明细图形统计
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upSpeedGraphicalInfo", method = RequestMethod.POST)
    public JsonResultBean upSpeedGraphicalInfo(String groupId, String time) {
        try {
            return speedingStatisticsService.findGraphicalStatistics(groupId, time, "0");
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
    public PageGridBean rank(UpSpeedGroupQuery query) {
        return ControllerTemplate.getPassPageBean(() -> speedingStatisticsService.rankInfo(query), "查询超速企业排名明细报异常");
    }

    /**
     * 超速明细列表数据
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upSpeedInfoList", method = RequestMethod.POST)
    public PageGridBean upSpeedInfoList(UpSpeedGroupQuery query) {
        return ControllerTemplate
            .getPassPageBean(() -> speedingStatisticsService.upSpeedInfoList(query), "查询超速企业明细报警列表异常");
    }

    @RequestMapping(value = "/exportOrgListData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgListData(UpSpeedGroupQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, speedingStatisticsService.exportOrgListData(query), "导出企业超速统计报表异常");
    }

    @RequestMapping(value = "/exportOrgSpeedDetailsData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportOrgSpeedDetailsData(UpSpeedGroupQuery query) {
        return ControllerTemplate
            .addExportOffline(exportService, speedingStatisticsService.exportOrgSpeedDetailsData(query),
                "导出企业超速明细报表异常");
    }

}
