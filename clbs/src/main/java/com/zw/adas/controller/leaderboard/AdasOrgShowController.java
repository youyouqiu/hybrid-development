package com.zw.adas.controller.leaderboard;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.service.leaderboard.AdasOrgShowService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;


/**
 * 领导看板（企业）
 */
@Controller
@RequestMapping("/adas/lbOrg/show")
public class AdasOrgShowController {

    private static final Logger log = LogManager.getLogger(AdasOrgShowController.class);

    private static final String LIST_PAGE = "modules/reportManagement/show/lbOrgList";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    @Autowired
    private AdasOrgShowService adasOrgShowService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        modelAndView.addObject("vehicleIds", JSONObject.toJSONString(userPrivilegeUtil.getCurrentUserVehicles()));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping("/getEventRanking")
    public JsonResultBean getEventRanking(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getEventRanking(groupId, isToday));
        } catch (Exception e) {
            log.error("获取企业风险事件信息失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业风险事件信息失败");
        }

    }

    /**
     * 获取实时风控预警数
     *
     * @return
     */
    @RequestMapping(value = "/getNowRisk", method = RequestMethod.POST)
    @ResponseBody
    public int getNowRisk() {
        try {
            return adasOrgShowService.getNowRiskNum();
        } catch (Exception e) {
            log.error("获取实时风控预警数失败");
            return -1;
        }
    }

    /**
     * 获取昨日风控预警数
     *
     * @return
     */
    @RequestMapping(value = "/getYesterdayRisk", method = RequestMethod.POST)
    @ResponseBody
    public int getYesterdayRisk() {
        try {
            return adasOrgShowService.getYesterdayRiskNum();
        } catch (Exception e) {
            log.error("获取昨日风控预警数失败");
            return -1;
        }
    }

    /**
     * 获取当日此时风险事件数环比增长
     * lijie
     *
     * @return
     */
    @RequestMapping(value = "/getRingRatioRiskEvent", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRingRatioRiskEvent() {
        try {
            return adasOrgShowService.getRingRatioRiskEvent();
        } catch (Exception e) {
            log.error("获取当日此时风险事件数环比增长率失败");
            return new JsonResultBean(JsonResultBean.FAULT, "获取当日此时风险事件数环比增长率失败");
        }

    }

    /**
     * 获取当前权限下车辆在线数
     * lijie
     *
     * @return
     */
    @RequestMapping(value = "/getVehicleOnlie", method = RequestMethod.POST)
    @ResponseBody
    public int getVehicleOnlie() {
        try {
            return adasOrgShowService.getVehicleOnlie();
        } catch (Exception e) {
            log.error("获取车辆在线数失败");
            return -1;
        }
    }

    /**
     * 获取车辆今日此时上线率和昨日整日的上线率
     * lijie
     *
     * @return
     */
    @RequestMapping(value = "/getLineRate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLineRate1() {
        try {
            return adasOrgShowService.getLineRate();
        } catch (Exception e) {
            log.error("获取当前上线率异常");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取运营类别
     * lijie
     *
     * @return
     */
    @RequestMapping(value = "/getOperationCategory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOperationCategory() {
        try {
            List<Map<String, String>> list = adasOrgShowService.getOperCag();
            return new JsonResultBean(list);
        } catch (Exception e) {
            log.error("获取运营类型占比失败");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ResponseBody
    @RequestMapping("/getVehOnlineTrend")
    public JsonResultBean getVehOnlineTrend(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getVehOnlineTrend(groupId, isToday));
        } catch (Exception e) {
            log.error("获取企业车辆上线趋势图失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业车辆上线趋势图失败");
        }
    }

    @ResponseBody
    @RequestMapping("/getEventTrend")
    public JsonResultBean getEventTrend(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getEventTrend(groupId, isToday));
        } catch (Exception e) {
            log.error("获取企业报警趋势图失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业报警趋势图失败");
        }
    }

    @RequestMapping("/getCustomerServiceTrend")
    @ResponseBody
    public JsonResultBean getCustomerServiceTrend(boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getCustomerServiceTrend(isToday));
        } catch (Exception e) {
            log.error("获取风控人员在线趋势图失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取风控人员在线趋势图失败");
        }
    }

    @ResponseBody
    @RequestMapping("/getRiskTypeTrend")
    public JsonResultBean getRiskTypeTrend(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getRiskTypeTrend(groupId, isToday));
        } catch (Exception e) {
            log.error("获取企业风险预警趋势图失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业风险预警趋势图失败");
        }
    }

    @ResponseBody
    @RequestMapping("/getRiskProportion")
    public JsonResultBean getRiskProportion(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getRiskProportion(groupId, isToday));
        } catch (Exception e) {
            log.error("获取企业风险占比信息失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业风险占比信息失败");
        }
    }

    @ResponseBody
    @RequestMapping("/getRisksDealInfo")
    public JsonResultBean getRisksDealInfo(String groupId, boolean isToday) {
        try {
            return new JsonResultBean(adasOrgShowService.getRiskDealInfo(groupId, isToday));
        } catch (Exception e) {
            log.error("获取风险处置情况占比失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取风险处置情况占比失败");
        }
    }

}
