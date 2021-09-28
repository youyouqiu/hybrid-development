package com.cb.platform.contorller;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.GroupSpotCheckVehicleNumberCont;
import com.cb.platform.service.SpotCheckReportService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 车辆抽查统计
 * @author zhouzongbo on 2019/2/20 9:39
 */
@Controller
@RequestMapping("/cb/cbReportManagement/spotCheckReport")
public class SpotCheckReportController {
    private static Logger log = LogManager.getLogger(SpotCheckReportController.class);

    private static final String LIST_PAGE = "modules/cbReportManagement/spotCheckReportList";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private SpotCheckReportService spotCheckReportService;

    /**
     * 获取抽查报表页面
     * @return ModelAndView
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        try {
            return new ModelAndView(LIST_PAGE);
        } catch (Exception e) {
            log.error("获取车辆抽查统计页面", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询车辆抽查明细信息
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = { "/getVehicleSpotCheckDetailList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleSpotCheckDetailList(String vehicleIds, String startTime, String endTime) {
        try {
            return spotCheckReportService.getVehicleSpotCheckDetailList(vehicleIds, startTime, endTime);
        } catch (Exception e) {
            log.error("查询车辆抽查明细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出车辆抽查明细信息
     * @param response
     * @param simpleQueryParam 监控对象名称
     */
    @RequestMapping(value = "/exportVehicleSpotCheckDetail", method = RequestMethod.GET)
    public void exportVehicleSpotCheckDetail(HttpServletResponse response, String simpleQueryParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆抽查明细表");
            spotCheckReportService.exportVehicleSpotCheckDetail(response, simpleQueryParam);
        } catch (Exception e) {
            log.error("导出车辆抽查明细表异常", e);
        }
    }

    /**
     * 用户抽查车辆数量及百分比统计报表
     * @param userIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = { "/getUserSpotCheckNumberAndPercentageList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUserSpotCheckNumberAndPercentageList(String userIds, String startTime, String endTime) {
        try {
            return spotCheckReportService.getUserSpotCheckNumberAndPercentageList(userIds, startTime, endTime);
        } catch (Exception e) {
            log.error("查询用户抽查车辆数量及百分比统计报表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出用户抽查车辆数量及百分比统计报表
     * @param response
     * @param simpleQueryParam 用户名称
     */
    @RequestMapping(value = "/exportUserSpotCheckNumberAndPercentage", method = RequestMethod.GET)
    public void exportUserSpotCheckNumberAndPercentage(HttpServletResponse response, String simpleQueryParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "用户抽查车辆数量及百分比统计报表");
            spotCheckReportService.exportUserSpotCheckNumberAndPercentage(response, simpleQueryParam);
        } catch (Exception e) {
            log.error("导出用户抽查车辆数量及百分比统计报表异常", e);
        }
    }

    /**
     * 车辆抽查数量统计表
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = { "/getVehicleSpotCheckNumberCountList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleSpotCheckNumberCountList(String vehicleIds, String startTime, String endTime) {
        try {
            return spotCheckReportService.getVehicleSpotCheckNumberCountList(vehicleIds, startTime, endTime);
        } catch (Exception e) {
            log.error("查询车辆抽查数量统计表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出车辆抽查数量统计表
     * @param response
     * @param simpleQueryParam 监控对象名称
     */
    @RequestMapping(value = "/exportVehicleSpotCheckNumberCountList", method = RequestMethod.GET)
    public void exportVehicleSpotCheckNumberCountList(HttpServletResponse response, String simpleQueryParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆抽查数量统计表");
            spotCheckReportService.exportVehicleSpotCheckNumberCountList(response, simpleQueryParam);
        } catch (Exception e) {
            log.error("导出车辆抽查数量统计表异常", e);
        }
    }

    /**
     * 道路运输企业抽查车辆数量统计表
     */
    @RequestMapping(value = "/groupSpotCheck", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupSpotCheckDataByGroupId(String groupId, String startTime, String endTime) {
        try {
            List<GroupSpotCheckVehicleNumberCont> result =
                spotCheckReportService.getGroupSportCheckVehicleData(groupId, startTime, endTime);
            JSONObject msg = new JSONObject();
            msg.put("groupData", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询道路运输企业抽查车辆数据统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出道路运输企业抽查车辆数量统计表数据
     */
    @RequestMapping(value = "/exportGroupData", method = RequestMethod.GET)
    public void exportGroupSpotCheckVehicleNumberData(HttpServletResponse response, String fuzzyParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "道路运输企业抽查车辆数量统计表");
            spotCheckReportService.exportGroupSpotCheckVehicleNumberData(response, fuzzyParam);
        } catch (Exception e) {
            log.error("导出车辆抽查明细表异常", e);
        }
    }
}
