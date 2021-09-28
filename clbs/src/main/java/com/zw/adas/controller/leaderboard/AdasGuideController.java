package com.zw.adas.controller.leaderboard;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.service.leaderboard.AdasGuideService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.platform.util.spring.InitData;
import com.zw.ws.entity.vehicle.VehiclePositionalInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 领导看板（引导页）
 */
@Controller
@RequestMapping("/adas/lb/guide")
public class AdasGuideController {
    private static final Logger log = LogManager.getLogger(AdasGuideController.class);

    private static final String LIST_PAGE = "modules/reportManagement/guide/list";
    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;
    @Autowired
    AdasGuideService adasGuideService;

    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        modelAndView.addObject("vehicleIds", JSONObject.toJSONString(userPrivilegeUtil.getCurrentUserVehicles()));
        return modelAndView;
    }

    /**
     * 跳转页面前判断权限
     *
     * @param moduleName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/isPermissions"}, method = RequestMethod.POST)
    public boolean isPermissions(String moduleName) {
        Integer num = adasGuideService.isPermission(moduleName);
        if (num != null && num > 0) {
            return true;
        }
        return false;
    }

    @ResponseBody
    @RequestMapping("/getVehiclePositional")
    public JsonResultBean getVehiclePositional() {
        try {
            final List<VehiclePositionalInfo> vehiclePosInfos = userPrivilegeUtil.getCurrentUserVehicles().stream()
                    .map(InitData.vehiclePositionalInfo::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return new JsonResultBean(vehiclePosInfos);
        } catch (Exception e) {
            log.error("获取车辆位置信息失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取车辆位置信息失败");
        }
    }

    /**
     * 引导页企业排行榜
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/getRankOfGroup"}, method = RequestMethod.POST)
    public JsonResultBean getGroupRank() {
        try {
            return new JsonResultBean(adasGuideService.getGroupRank());
        } catch (Exception e) {
            log.error("获取企业报警排行榜失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取企业报警排行榜失败");
        }
    }

    /**
     * 引导页监控对象排行榜
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/getRankOfVehicle"}, method = RequestMethod.POST)
    public JsonResultBean getVehicleRank() {
        try {
            return new JsonResultBean(adasGuideService.getVehicleRank());
        } catch (Exception e) {
            log.error("获取监控对象报警排行榜失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取监控对象报警排行榜失败");
        }
    }

    /**
     * 引导页驾驶员报警排行榜
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/getRankOfDriver"}, method = RequestMethod.POST)
    public JsonResultBean getDriverRank(int limitSize) {
        try {
            return new JsonResultBean(adasGuideService.getDriverRank(limitSize));
        } catch (Exception e) {
            log.error("获取监控对象报警排行榜失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取监控对象报警排行榜失败");
        }
    }

    /**
     * 热力图
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"/showHotMap"}, method = RequestMethod.POST)
    public JsonResultBean getHotMapData(int type) {
        try {
            return new JsonResultBean(adasGuideService.getHotMapData(type));
        } catch (Exception e) {
            log.error("获取区域风险热力图失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取区域风险热力图失败");
        }
    }

}
