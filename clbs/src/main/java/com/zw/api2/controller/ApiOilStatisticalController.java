package com.zw.api2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.oilmgt.OilStatisticalService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
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

// import static com.zw.platform.util.CalculateUtil.construct;

/**
 * 油耗统计 Created by Tdz on 2016/9/18.
 */
@Controller
@RequestMapping("/api/v/oilstatiscal")
@Api(tags = { "油耗统计dev" }, description = "油耗统计相关api接口")
public class ApiOilStatisticalController {
    private static Logger log = LogManager.getLogger(ApiOilStatisticalController.class);

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

    @ApiOperation(value = "查询参考车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询参考车辆
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("查询参考车辆异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    @ApiOperation(value = "查询油耗统计数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "band", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间  yyyy-MM-dd HH:mm:ss(最多查询七天的数据)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间  yyyy-MM-dd HH:mm:ss", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getOilInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilInfo(String band, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = null;
            oilInfo = oilStatisticalService.getOilInfo(band, startTime, endTime);
            String result = JSON.toJSONString(oilInfo);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("oilInfo", result);
            msg.put("infoDtails", getInfoDtails(oilInfo, band));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询油耗信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    public JSONObject getInfoDtails(List<Positional> oilInfo, String band) {
        JSONObject obj = new JSONObject();
        /*    List<FuelConsumptionStatistics> list = new ArrayList<FuelConsumptionStatistics>();
        boolean flag = false;// 判断行驶状态开始标识
        boolean flag1 = false;// 打火标识
        FuelConsumptionStatistics mileage = null;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        double mile = 0;
        int acc = 0;
        double speed = 0;
        double ctime = 0;
        String accOpen = "";
        String accClose = "";
        Positional temp = null;

        String key = "sensorMessage" + band;
        boolean flogKey = RedisHelper.isContainsKey(key, 2);

        for (int i = 0, len = oilInfo.size(); i < len; i++) {
            temp = oilInfo.get(i);
            if (flogKey) {
                if (temp.getMileageTotal() != null) {
                    mile = temp.getMileageTotal();
                }
                if (temp.getMileageSpeed() != null) {
                    speed = temp.getMileageSpeed();
                }
            } else {
                mile = Double.parseDouble(temp.getGpsMile());
                speed = Double.parseDouble(temp.getSpeed());
            }
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            String date = null;
            date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
            // 表示前一次数据开始记录行驶，用于判断行驶状态是否满足2次，如不满足则不记录
            if (flag) {
                if (speed == 0 || acc == 0) {
                    mileage = null;
                }
                flag = false;
            }
            if (acc == 1 && speed != 0 && mileage == null) {
                flag = true;
                mileage = new FuelConsumptionStatistics();
                mileage.setStartTime(date);
                mileage.setStartMileage(mile);
                // mileage.setStartOil(Double.parseDouble(temp.getTransientOilwearOne()));
                mileage.setStartPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                mileage.setStartOil(Double.parseDouble(temp.getTotalOilwearOne()));

            }
            if (acc == 1 && accOpen.equals("")) {
                accOpen = date;
            }
            if (acc == 0) {
                if (accClose.equals("") && !accOpen.equals("")) {
                    accClose = date;
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            } else if (accClose.equals("") && !accOpen.equals("")) {
                if (i == oilInfo.size() - 1) {
                    accClose =
                        Converter.timeStamp2Date(String.valueOf(oilInfo.get(oilInfo.size() - 1).getVtime()), null);
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            }
            if (mileage != null && !flag) {
                // 行驶过程，每次更新行驶末尾状态
                if (acc == 1 && speed != 0) {
                    mileage.setEndTime(date);
                    mileage.setSteerTime(
                        String.valueOf(CalculateUtil.toDateTimeS(mileage.getEndTime(), mileage.getStartTime())));
                    mileage.setEndMileage(mile);
                    mileage.setSteerMileage((mileage.getEndMileage() - mileage.getStartMileage()));
                    // mileage.setMileageCount(Double.parseDouble(df.format(mileage.getEndMileage()
                    // - mileage.getStartMileage())));
                    mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                    mileage.setEndPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                    mileage.setEndOil(Double.parseDouble(temp.getTotalOilwearOne()));
                    mileage.setFuelConsumption((mileage.getEndOil() - mileage.getStartOil()));
                    if (mileage.getFuelConsumption() != 0 && mileage.getSteerMileage() != 0) {
                        mileage.setPerHundredKilimeters(
                            (mileage.getFuelConsumption() / mileage.getSteerMileage()) * 100);
                    }
                    // 如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                    if (i == oilInfo.size() - 1) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                } else {
                    // 行驶结束，写入list
                    // 如果只有开始时间，则舍弃这条数据
                    if (mileage != null && mileage.getEndTime() != null) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                    mileage = null;
                }

            }
        }
        obj.put("totalT", ctime);
        obj.put("infoDtail", list);*/
        return obj;
    }

    @ApiOperation(value = "根据组织id查询该组织下绑定车辆的详细信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "groupid", value = "组织id", required = true, paramType = "query", dataType = "string")
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

    @ApiOperation(value = "获取用户权限的车辆树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "树结构类型   single : 单选树结构；  multiple:多选树结构", required = true,
        paramType = "query", dataType = "string")
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

    @ApiOperation(value = "获取传感器信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "band", value = "车牌号", required = true, paramType = "query", dataType = "string") })
    @RequestMapping(value = "/getSensorMessage", method = RequestMethod.POST)
    @ResponseBody
    public String getSensorMessage(final String band) {
        String key = "sensorMessage" + band;
        /* boolean flogKey = RedisHelper.isContainsKey(key, 2);
        if (flogKey) {
            return "true";
        }*/
        return "";
    }

}
