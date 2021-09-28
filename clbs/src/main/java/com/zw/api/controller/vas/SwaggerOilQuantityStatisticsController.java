package com.zw.api.controller.vas;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilmassmgt.OilQuantityStatisticsService;
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
import java.util.Date;
import java.util.List;


/**
 * 油量统计Controller <p>Title: OilQuantityStatisticsController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月24日下午4:25:07
 * @version 1.0
 */
@Controller
@RequestMapping("/swagger/v/oilquantitystatistics")
@Api(tags = {"油量统计"}, description = "油量统计相关api接口")
public class SwaggerOilQuantityStatisticsController {

    @Autowired
    private OilQuantityStatisticsService quantityStatisticsService;

    @Autowired
    private VehicleService vehicleService;

    private static Logger logger = LogManager.getLogger(SwaggerOilQuantityStatisticsController.class);
    
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Auth

    @ApiOperation(value = "查询油量统计数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "band", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间  yyyy-MM-dd HH:mm:ss(最多查询七天的数据)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间 yyyy-MM-dd HH:mm:ss", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "signal", value = "功能id标识位", required = true, paramType = "query",
            dataType = "string")})
    @RequestMapping(value = "/getOilInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilInfo(String band, String startTime, String endTime, Integer[] signal) {
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

            // String band, String startTime, String endTime
            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = null;
            oilInfo = quantityStatisticsService.getOilMassInfo(band, startTime, endTime);
            msg.put("oilInfo", oilInfo);
            msg.put("infoDtails", quantityStatisticsService.getInfoDtails(oilInfo, band, signal));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询油量统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}
