package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.FuelConsumptionStatistics;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.oilmgt.OilStatisticalService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// import static com.zw.platform.util.CalculateUtil.construct;

/**
 * 油耗统计
 * Created by Tdz on 2016/9/18.
 */
@Controller
@RequestMapping("/swagger/v/oilstatiscal")
@Api(tags = { "油耗统计" }, description = "油耗统计相关api接口")
public class SwaggerOilStatisticalController {
    private static Logger log = LogManager.getLogger(SwaggerOilStatisticalController.class);

    @Autowired
    private OilStatisticalService oilStatisticalService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //    @Autowired
    //    private Config config;
    @Auth
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
            // 数据校验
            if (vehicleService.findVehicleById(band) == null) { // 车辆是否存在
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            // 时间校验
            try {
                Date date1 = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date date2 = DateUtils.parseDate(endTime, DATE_FORMAT);
                // 开始时间必须小于结束时间
                if (date1.after(date2)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "开始时间必须小于结束时间！");
                }
                // 最多查七天
                if (RegexUtils.differentDaysByMillisecond(date1, date2) > 7) {
                    return new JsonResultBean(JsonResultBean.FAULT, "最多只能查询七天的数据！");
                }
            } catch (ParseException e) {
                return new JsonResultBean(JsonResultBean.FAULT, "开始时间或者结束时间的时间格式错误！");
            }

            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = null;
            oilInfo = oilStatisticalService.getOilInfo(band, startTime, endTime);
            msg.put("oilInfo", oilInfo);
            msg.put("infoDtails", getInfoDtails(oilInfo));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询油耗统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    public JSONObject getInfoDtails(List<Positional> oilInfo) {
        JSONObject obj = new JSONObject();
        List<FuelConsumptionStatistics> list = new ArrayList<FuelConsumptionStatistics>();
        boolean flag = false;// 判断行驶状态开始标识
        FuelConsumptionStatistics mileage = null;
        double mile = 0;
        int acc = 0;
        int speed = 0;
        double ctime = 0;
        String accOpen = "";
        String accClose = "";
        Positional temp = null;
        for (int i = 0, len = oilInfo.size(); i < len; i++) {
            temp = oilInfo.get(i);
            mile = Double.parseDouble(temp.getGpsMile()) / 10;
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            speed = Integer.parseInt(temp.getSpeed());
            String date = null;
            date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
            if (flag) { // 表示前一次数据开始记录行驶，用于判断行驶状态是否满足2次，如不满足则不记录
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
                //                mileage.setStartOil(Double.parseDouble(temp.getTransientOilwearOne()));
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
                    //mileage.setMileageCount(Double.parseDouble(
                    //                df.format(mileage.getEndMileage() - mileage.getStartMileage())));
                    mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                    mileage.setEndPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                    mileage.setEndOil(Double.parseDouble(temp.getTotalOilwearOne()));
                    mileage.setFuelConsumption((mileage.getEndOil() - mileage.getStartOil()) / 100);
                    if (mileage.getFuelConsumption() != 0 && mileage.getSteerMileage() != 0) {
                        mileage.setPerHundredKilimeters(
                            (mileage.getFuelConsumption() / mileage.getSteerMileage()) * 100);
                    }
                    //如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                    if (i == oilInfo.size() - 1) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                } else { // 行驶结束，写入list
                    // 如果只有开始时间，则舍弃这条数据
                    if (mileage.getEndTime() != null) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                    mileage = null;
                }

            }
        }
        obj.put("totalT", ctime);
        obj.put("infoDtail", list);
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
            // 校验组织是否存在
            try {
                if (userService.getOrgByUuid(groupid) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
                }
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, "组织不存在！");
            }
            JSONObject msg = new JSONObject();
            List<FuelVehicle> vehicles = null;
            vehicles = oilStatisticalService.getVehiceInfo(groupid);
            msg.put("vehicleInfo", vehicles);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询该组织下绑定车辆的详细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "获取用户权限的车辆树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "树结构类型   single : 单选树结构；  multiple:多选树结构", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/vehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleTree(String type) {
        try {
            // 校验传入参数
            if (!"single".equals(type) && !"multiple".equals(type)) {
                return new JsonResultBean(JsonResultBean.FAULT, "车辆树结构类型值错误！");
            }
            JSONArray result = vehicleService.vehicleTruckTree(type, true);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取用户权限的车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
