package com.zw.api2.controller.calibration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerPageQuery;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionlList;
import com.zw.platform.domain.oil.PositionlQuery;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilQuantityStatisticsService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.service.sensor.SensorPollingService;
import com.zw.platform.service.switching.SwitchingSignalService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * 油量统计Controller <p>Title: OilQuantityStatisticsController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:25:07
 */
@Controller
@RequestMapping("api/v/oilmassmgt/oilquantitystatistics")
@Api(tags = {"油量统计_dev"}, description = "油量统计相关api接口")
public class ApiOilQuantityStatisticsController {

    @Autowired
    private OilQuantityStatisticsService quantityStatisticsService;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private SwitchingSignalService signalService;

    @Autowired
    private SensorPollingService sensorPollingService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static Logger logger = LogManager.getLogger(ApiOilQuantityStatisticsController.class);

    @ApiOperation(value = "分页查询", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean listPage() {
        try {
            JSONObject data = new JSONObject();
            // 查询参考车辆
            List<OilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceBrand();
            data.put("vehicleList", JSON.toJSONString(vehicleList));
            return new JsonResultBean(data);
        } catch (Exception e) {
            logger.error("分页查询异常", e);
            return new JsonResultBean();
        }
    }

    @ApiOperation(value = "获得油量信息数据", authorizations = {
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
    public JsonResultBean getOilInfo(String band, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = quantityStatisticsService.getOilMassInfo(band, startTime, endTime);
            Integer[] signal = signalService.findAirStatus(band);
            String ioStatus = sensorPollingService.findStatus(band);
            // 根据车辆id获取与其绑定的油箱的理论容积
            List<FuelTankForm> fuelList = fuelTankManageService.getFuelTankDetailByVehicleId(band);
            String box1Volume = "";
            String box2Volume = "";
            if (null != fuelList && !fuelList.isEmpty()) {
                for (FuelTankForm f : fuelList) {
                    if ("1".equals(Converter.toBlank(f.getTanktyp()))) { // 油箱1
                        box1Volume = Converter.toBlank(f.getTheoryVolume());
                    } else if ("2".equals(Converter.toBlank(f.getTanktyp()))) {
                        box2Volume = Converter.toBlank(f.getTheoryVolume());
                    }
                }
            }
            String result = JSON.toJSONString(oilInfo);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("box1", box1Volume);
            msg.put("box2", box2Volume);
            msg.put("oilInfo", result);
            msg.put("signal", signal);
            msg.put("ioStatus", ioStatus);
            msg.put("infoDtails", quantityStatisticsService.getInfoDtails(oilInfo, band, signal));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("获得油量信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "油量报表分页查询", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/getOilPagInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilPagiInfo(@Validated({ValidGroupUpdate.class})
        @ModelAttribute("query") SwaggerPageQuery query) {
        final PositionlQuery query1 = new PositionlQuery();
        BeanUtils.copyProperties(query, query1);
        Page<PositionlList> result;
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "");
        result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query1);
        return new PageGridBean(query1, result, true);
    }

    @ApiOperation(value = "加油报表分页查询", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/getOilAmountPagiInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilAmountPagiInfo(@Validated({ValidGroupUpdate.class})
        @ModelAttribute("query") SwaggerPageQuery query) {
        final PositionlQuery query1 = new PositionlQuery();
        BeanUtils.copyProperties(query, query1);
        Page<PositionlList> result;
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-a");
        result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query1);
        return new PageGridBean(query1, result, true);
    }

    @ApiOperation(value = "漏油报表分页查询", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/getOilSpillPagiInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilSpillPagiInfo(@Validated({ValidGroupUpdate.class})
        @ModelAttribute("query") SwaggerPageQuery query) {
        final PositionlQuery query1 = new PositionlQuery();
        BeanUtils.copyProperties(query, query1);
        Page<PositionlList> result;
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-s");
        result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query1);
        return new PageGridBean(query1, result, true);
    }

    @ApiOperation(value = "判断是否绑定传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "band", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/getSensorMessage", method = RequestMethod.POST)
    @ResponseBody
    public String getSensorMessage(final String band) {
        final RedisKey key = HistoryRedisKeyEnum.SENSOR_MESSAGE.of(band);
        boolean flogKey = RedisHelper.isContainsKey(key);
        if (flogKey) {
            return "true";
        }
        return "";
    }

}
