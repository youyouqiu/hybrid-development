package com.zw.platform.controller.reportmanagement;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.leaderboard.VehicleRank;
import com.zw.platform.domain.reportManagement.query.AdasAlarmRankQuery;
import com.zw.platform.service.reportManagement.AdasAlarmRankService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/r/reportManagement/adasAlarmRank")
public class AdasAlarmRankController {

    @Autowired
    private AdasAlarmRankService adasAlarmRankService;

    private static final Logger log = LogManager.getLogger(AdasAlarmRankController.class);

    private static final String LIST_PAGE = "modules/reportManagement/adasAlarm/alarmRankList";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = {"/getRankPage"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(AdasAlarmRankQuery query, boolean isGroupRank) {
        try {
            if (isGroupRank) {
                Page<AdasGroupRank> result = adasAlarmRankService.getRankOfGroup(query);
                return new PageGridBean(query, result, true);
            } else {
                Page<VehicleRank> result = adasAlarmRankService.getRankOfVehicle(query);
                return new PageGridBean(query, result, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出企业报警排行榜
     *
     * @param groupIds
     * @param startTime
     * @param endTime
     * @param response
     */
    @RequestMapping(value = "/exportGroupRank", method = RequestMethod.POST)
    @ResponseBody
    public void exportGroupRank(String groupIds, String startTime, String endTime, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "企业报警排行榜");
            adasAlarmRankService.exportGroupRank(null, 1, response,
                adasAlarmRankService.getGroupRank(groupIds, startTime, endTime));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导出排行榜数据失败", e);
        }
    }

    /**
     * 查询企业报警排行榜
     *
     * @param groupIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = {"/getGroupRankPage"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getGroupPage(String groupIds, String startTime, String endTime) {
        try {
            if (StringUtils.isNotEmpty(groupIds) && StringUtils.isNotEmpty(startTime)
                && StringUtils.isNotEmpty(endTime)) {
                return new JsonResultBean(adasAlarmRankService.getGroupRank(groupIds, startTime, endTime));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "大王，你没有传条件给我，我查不到啊！！！");
        } catch (Exception e) {
            log.error("查询企业报警排行榜异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询企业报警排行榜异常");
        }
    }

    /**
     * 导出驾驶员排行榜
     *
     * @param driverIds
     * @param startTime
     * @param endTime
     * @param response
     */
    @RequestMapping(value = "/exportDriverRank", method = RequestMethod.POST)
    @ResponseBody
    public void exportDriverRank(String driverIds, String startTime, String endTime, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "驾驶员报警排行榜");
            adasAlarmRankService.exportDriverRank(null, 1, response,
                adasAlarmRankService.getDriverRank(driverIds, startTime, endTime));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导出驾驶员排行榜数据失败", e);
        }
    }

    /**
     * 查询驾驶员报警排行榜异常
     *
     * @param driverIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = {"/getDriverRankPage"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDriverPage(String driverIds, String startTime, String endTime) {
        try {
            if (StringUtils.isNotEmpty(driverIds) && StringUtils.isNotEmpty(startTime)
                && StringUtils.isNotEmpty(endTime)) {
                return new JsonResultBean(adasAlarmRankService.getDriverRank(driverIds, startTime, endTime));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "大王，你没有传条件给我，我查不到啊！！！");
        } catch (Exception e) {
            log.error("查询驾驶员报警排行榜异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询驾驶员报警排行榜异常");
        }
    }

    /**
     * 查询监控对象报警排行榜
     *
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = {"/getVehicleRankPage"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehiclePage(String vehicleIds, String startTime, String endTime) {
        try {
            if (StringUtils.isNotEmpty(vehicleIds) && StringUtils.isNotEmpty(startTime)
                && StringUtils.isNotEmpty(endTime)) {
                return new JsonResultBean(adasAlarmRankService.getVehicleRank(vehicleIds, startTime, endTime));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "大王，你没有传条件给我，我查不到啊！！！");
        } catch (Exception e) {
            log.error("查询监控对象报警排行榜异常");
            return new JsonResultBean(JsonResultBean.FAULT, "查询监控对象报警排行榜异常");
        }
    }

    /**
     * 导出监控对象排行榜
     *
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @param response
     */
    @RequestMapping(value = "/exportVehicleRank", method = RequestMethod.POST)
    @ResponseBody
    public void exportVehicleRank(String vehicleIds, String startTime, String endTime, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "监控对象排行榜");
            adasAlarmRankService.exportVehicleRank(null, 1, response,
                adasAlarmRankService.getVehicleRank(vehicleIds, startTime, endTime));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导出监控对象排行榜数据失败", e);
        }
    }

    /**
     * 获取插卡录入驾驶员树
     *
     * @return
     */
    @RequestMapping(value = "/driverTree", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTree(String queryParam) {
        try {
            return adasAlarmRankService.driverTree(queryParam).toJSONString();
        } catch (Exception e) {
            log.error("获取插卡录入驾驶员树信息异常", e);
            return null;
        }
    }

}
