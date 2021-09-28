package com.zw.platform.controller.oilmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.oilmgt.OilStatisticalService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
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
 * 油耗统计 Created by Tdz on 2016/9/18.
 */
@Controller
@RequestMapping("/v/oilmgt")
public class OilStatisticalController {
    private static Logger log = LogManager.getLogger(OilStatisticalController.class);

    private static final String LIST_PAGE = "vas/oilmgt/oilstatiscal/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private OilStatisticalService oilStatisticalService;

    @Autowired
    private FluxSensorBindService fluxSensorBindService;

    @Autowired
    private VehicleService vehicleService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询参考车辆
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("查询参考车辆异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/getOilInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilInfo(String band, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = oilStatisticalService.getOilInfo(band, startTime, endTime);
            String result = JSON.toJSONString(oilInfo);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("oilInfo", result);
            msg.put("infoDtails", oilStatisticalService.getInfoDtails(oilInfo, band));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询油耗信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/getVehiceInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehiceInfo(String groupid) {
        try {
            JSONObject msg = new JSONObject();
            List<FuelVehicle> vehicles = null;
            vehicles = oilStatisticalService.getVehiceInfo(groupid);
            msg.put("vehicleInfo", vehicles);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/vehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTree(String type) {
        try {
            JSONArray result = vehicleService.vehicleTruckTree(type, true);
            return result.toJSONString();
        } catch (Exception e) {
            log.error("获取车辆树异常", e);
            return null;
        }
    }

    @RequestMapping(value = "/getSensorMessage", method = RequestMethod.POST)
    @ResponseBody
    public String getSensorMessage(final String band) {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(band));
        // ??? 神仙写法
        if (flogKey) {
            return "true";
        }
        return "";
    }

    /**
     * 导出油耗报表
     * @param response
     */
    @RequestMapping(value = "/exportDataList", method = RequestMethod.GET)
    public void exportFuelConsumptionReport(HttpServletResponse response) {
        try {
            oilStatisticalService.exportFuelConsumptionReport(response);
        } catch (Exception e) {
            log.error("导出油耗报表异常", e);
        }
    }

}
