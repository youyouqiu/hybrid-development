package com.zw.adas.controller.driverScore;

import com.zw.adas.domain.driverScore.show.query.AdasDriverScoreQuery;
import com.zw.adas.domain.driverStatistics.bean.AdasFaceCheckAuto;
import com.zw.adas.service.driverScore.AdasDriverScoreService;
import com.zw.adas.service.realTimeMonitoring.AdasRealTimeMonitoringService;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/***
 @Author zhengjc
 @Date 2019/10/14 19:12
 @Description 司机评分controller
 @version 1.0
 **/
@Controller
@RequestMapping("/m/reportManagement/driverScore")
public class AdasDriverScoreController {
    private static Logger log = LogManager.getLogger(AdasDriverScoreController.class);

    private static final String LIST_PAGE = "modules/reportManagement/driverScore/list";

    @Autowired
    private AdasDriverScoreService adasDriverScoreService;
    @Autowired
    private AdasRealTimeMonitoringService adasRealTimeMonitoringService;

    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getIcCardDriverInfo")
    @ResponseBody
    public JsonResultBean getIcCardDriverInfo(String groupId, long time) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.getGroupDriverGeneralScoreInfos(groupId, time), "司机评分统计信息！");
    }

    @RequestMapping(value = "/getIcCardDriverInfoList")
    @ResponseBody
    public JsonResultBean getIcCardDriverInfoList(AdasDriverScoreQuery query) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.getGroupDriverGeneralScoreInfoList(query), "司机评分统计列表信息！");
    }

    @RequestMapping(value = "/getDriverScoreProfessionalInfo")
    @ResponseBody
    public JsonResultBean getDriverScoreProfessionalInfo(AdasDriverScoreQuery query) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.getDriverScoreProfessionalInfo(query), "司机评分统计弹出框司机信息！");
    }

    @RequestMapping(value = "/getIcCardDriverEventList")
    @ResponseBody
    public JsonResultBean getIcCardDriverEvents(AdasDriverScoreQuery query) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.selectIcCardDriverEvents(query), "司机评分弹出框报警信息！");
    }

    @RequestMapping(value = "/exportIcCardDriverInfoList")
    public void exportIcCardDriverInfoList(AdasDriverScoreQuery adasDriverScoreQuery, HttpServletResponse response) {
        AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.exportIcCardDriverInfoList(adasDriverScoreQuery, response),
                "导出驾驶员评分列表异常！");
    }

    @RequestMapping(value = "/exportDriverScoreProfessionalDetail")
    @ResponseBody
    public JsonResultBean exportDriverScoreProfessionalDetail(AdasDriverScoreQuery query,
        HttpServletResponse response) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.exportDriverScoreProfessionalDetail(query, response),
                "司机统计模块获取从业人员异常");
    }

    @RequestMapping(value = "/exportDriverScoreProfessionalDetails")
    @ResponseBody
    public JsonResultBean exportDriverScoreProfessionalDetails(AdasDriverScoreQuery query,
        HttpServletResponse response) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverScoreService.exportDriverScoreProfessionalDetails(query, response),
                "司机统计模块获取从业人员异常");
    }

    @RequestMapping(value = "/test1408")
    @ResponseBody
    public JsonResultBean test1408(String vehicleId) {
        return AdasControllerTemplate
            .getResultBean(() -> adasRealTimeMonitoringService.sendFaceCheckAuto(vehicleId, new AdasFaceCheckAuto()),
                "测试1408");
    }

    @RequestMapping(value = "/testGroupRelation")
    @ResponseBody
    public JsonResultBean testGroupRelation() {
        return AdasControllerTemplate.getResultBean(() -> adasDriverScoreService.getGroupIdMap(), "企业关系维护");
    }

}
