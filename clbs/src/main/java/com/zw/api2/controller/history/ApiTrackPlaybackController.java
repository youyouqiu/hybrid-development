package com.zw.api2.controller.history;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.api.config.ResponseUntil;
import com.zw.api2.swaggerEntity.SwaggerTrackPlaybackQuery;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.PositionInfo;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.history.MapQueryParam;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.monitoring.impl.HistoryServiceImpl;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.personalized.IcoService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ZipUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("api/v/monitoring")
@Api(tags = { "轨迹回放_dev" }, description = "轨迹回放相关api接口")
public class ApiTrackPlaybackController {
    private static final Logger log = LogManager.getLogger(ApiTrackPlaybackController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    VehicleService vs;

    @Autowired
    HistoryServiceImpl historyService;

    @Autowired
    IcoService icoService;

    @Autowired
    private PositionalService positionalService;

    /**
     * 轨迹数据
     */
    @ApiOperation(value = "轨迹数据页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Auth
    @RequestMapping(value = { "/trackPlayback" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean index(String vid, String pid) {
        JSONObject data = new JSONObject();
        data.put("vid", vid);
        data.put("pid", pid);
        return new JsonResultBean(data);
    }

    @ApiOperation(value = "根据条件查询历史轨迹数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type",
            value = "终端类型 0：808 2011扩展，1： 808 2013，2：移为，3：天禾，6：KKS，5：BDTD-SM，8：BSJ-A5，9：ASO，10：F3超长待机",
            required = true, paramType = "query", dataType = "string") })
    @SuppressWarnings("null")
    @RequestMapping(value = { "/getHistoryData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstData(String vehicleId, String startTime, String endTime, String type,
        HttpServletRequest request) {
        try {
            JsonResultBean jrb = new JsonResultBean();
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            switch (type) {
                case "0": // 808 2011扩展
                case "1": // 808 2013
                case "2": // 移为
                case "3": // 天禾
                case "6": // KKS
                case "8": // BSJ-A5
                case "9": // ASO
                case "10": // F3超长待机
                    jrb = historyService.getHistoryVehicle(vehicleId, startTime, endTime, null);
                    historyService.addlog(vehicleId, ip);
                    break;
                case "5": // BDTD-SM
                    jrb = historyService.getHistoryPeople(vehicleId, startTime, endTime, null);
                    historyService.addlog(vehicleId, ip);
                    break;
                default:
                    break;
            }
            return jrb;
        } catch (Exception e) {
            log.error("轨迹回放异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询指定日期的历史轨迹
     */
    @ApiOperation(value = "根据条件查询历史轨迹数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "nowMonth", value = "当前月   格式： yyyy-MM-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "nextMonth", value = "下个月    格式： yyyy-MM-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "终端类型 0：808 2011扩展，1： 808 2013，2：移为，3：天禾，6：KKS，8：BSJ-A5",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "offline", value = "查询实时/离线数据 0：实时数据，1： 离线数据", required = true, paramType = "query",
            dataType = "Integer") })
    @RequestMapping(value = { "/getActiveDate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getActiveDate(String vehicleId, String nowMonth, String nextMonth, String type, int offline) {
        try {
            return historyService.changeHistoryActiveDate(vehicleId, nowMonth, nextMonth, type, offline, false);
        } catch (Exception e) {
            log.error("查询指定日期的历史轨迹异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 报警参数
     * @author wangjianyu
     */
    @ApiOperation(value = "根据时间查询报警数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间(秒数如：1548311492)", required = true, paramType = "query",
            dataType = "Long"),
        @ApiImplicitParam(name = "endTime", value = "结束时间(秒数如：1548311492)", required = true, paramType = "query",
            dataType = "Long") })
    @RequestMapping(value = { "/getAlarmData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmData(String vehicleId, Long startTime, Long endTime) {
        try {
            return new JsonResultBean(historyService.getAlarmData(vehicleId, startTime, endTime, false, false));
        } catch (Exception e) {
            log.error("报警参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 进出区域详情
     * @author wangjianyu
     */
    @ApiOperation(value = "进出区域详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getQueryDetails" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getQueryDetails(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerTrackPlaybackQuery swaggerForm,
        String vehicleId) {
        try {
            MapQueryParam param = new MapQueryParam();
            BeanUtils.copyProperties(swaggerForm, param);
            param.setVehicleId(vehicleId);
            List<Positional> positionals =
                historyService.getQueryDetails(param.getVehicleId(), param.getStartTime(), param.getEndTime());
            boolean flog = true;
            boolean derail = true;
            List<List<String>> arrayList = new ArrayList<>();
            List<String> list = new ArrayList<>();
            if (positionals.isEmpty()) {
                list.add("");
                arrayList.add(list);
                return new JsonResultBean(arrayList);
            }
            double bottomRightLat = Double.parseDouble(param.getRightFloorLatitude());
            double bottomRightLong = Double.parseDouble(param.getRightFloorLongitude());
            double topLeftLat = Double.parseDouble(param.getLeftTopLatitude());
            double topLeftLong = Double.parseDouble(param.getLeftTopLongitude());
            for (Positional i : positionals) {
                double latitude = Double.parseDouble(i.getLatitude());
                double longitude = Double.parseDouble(i.getLongtitude());
                if (latitude <= bottomRightLat && latitude >= topLeftLat && longitude >= topLeftLong
                    && longitude <= bottomRightLong) {
                    if (flog) { // 当天第一条数据就在区域中,或者已经在区域中了，抛弃掉
                        if (derail) {
                            derail = false;// 关闭第一个点的开关
                            long time = i.getVtime();// 获取进入区域的时间
                            list.add(String.valueOf(time));
                        }
                    } else { // 进区域了
                        long time = i.getVtime();// 获取进入区域的时间
                        list.add(String.valueOf(time));
                        flog = true;
                    }
                } else {
                    if (flog) { // 出区域了
                        if (!derail) { // 判断是否是第一个点
                            long time = i.getVtime();// 获取出区域时间
                            list.add(String.valueOf(time));
                            arrayList.add(list);
                            list = new ArrayList<>();
                        } else {
                            derail = false;
                        }
                        flog = false;
                    }
                }
            }
            return new JsonResultBean(arrayList);
        } catch (Exception e) {
            log.error("获取进出区域详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 单个逆地址编码
     * @author wangjianyu
     */
    @ApiOperation(value = "单个逆地址编码", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/address" }, method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public String address(@ApiParam(value = "实时位置的经纬度，0代表经度，1代表纬度，3保留位默认''，4查询的监控对象类型（people人，vehicle车）",
        required = true) @RequestParam String[] addressReverse) {
        if (addressReverse != null) {
            try {
                // todo 确认经度和纬度是否弄反了，现在swagger文档和实际传参不一致
                PositionInfo info = AddressUtil.inverseAddress(addressReverse[1], addressReverse[0]);
                return info.getFormattedAddress();
            } catch (Exception e) {
                log.error("获取单个逆地址编码异常", e);
                return null;
            }
        }
        return null;
    }

    /**
     * 导出轨迹
     * @author wangjianyu
     */
    @ApiIgnore
    @RequestMapping(value = "/exportTrackPlaybackGet", method = RequestMethod.GET)
    public void export(HttpServletResponse response, String tableType) {
        try {
            String filename = "监控对象轨迹";
            if ("1".equals(tableType)) {
                filename = "车辆行驶轨迹";
            } else if ("2".equals(tableType)) {
                filename = "人员行驶轨迹";
            } else if ("3".equals(tableType)) {
                filename = "车辆停止数据";
            } else if ("4".equals(tableType)) {
                filename = "人员停止数据";
            } else if ("5".equals(tableType)) {
                filename = "报警数据";
            }
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            // realTimeServiceImpl.exportTrackPlaybackGet(null, 1, response, tableType);
        } catch (Exception e) {
            log.error("导出监控对象轨迹异常", e);
        }
    }

    @ApiIgnore
    @RequestMapping(value = "/exportTrackPlayback", method = RequestMethod.POST)
    @ResponseBody
    public String exportTrackPlayback(String trackPlayBackValue, String tableType) {
        String params;
        boolean flog;
        try {
            // 将输入流中的请求实体转换为byte数组,进行gzip解压
            byte[] bytes = trackPlayBackValue.getBytes(StandardCharsets.ISO_8859_1);
            // 对 bytes 数组进行解压
            params = ZipUtil.uncompress(bytes, "utf-8");
            if (params != null && params.trim().length() > 0) {
                // 因为前台对参数进行了 url 编码,在此进行解码
                params = URLDecoder.decode(params, "utf-8");
                List<String> result = Arrays.asList(params.split("_"));
                // flog = realTimeServiceImpl.exportTrackPlayback(result, tableType);
                // if (flog) {
                //     return tableType;
                // }
            }
        } catch (Exception e) {
            log.error("导出历史轨迹数据异常" + e);
        }
        return "";
    }

    @ApiOperation(value = "根据条件查询历史轨迹数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "监控对象类型(vehicle:车; people:人)", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getHistoryDataForBDTD" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstDataForBDTD(String vehicleId, String startTime, String endTime, String type,
        HttpServletRequest req, HttpServletResponse response) {
        try {
            if (vehicleId == null || "".equals(vehicleId) || startTime == null || "".equals(startTime)
                || endTime == null || "".equals(endTime) || type == null || "".equals(type)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 根据车辆id查询车辆全部历史轨迹
            String ip = new GetIpAddr().getIpAddr(req);// 获得访问ip
            JsonResultBean jrb = historyService.getHistoryPeople(vehicleId, startTime, endTime, null);
            historyService.addlog(vehicleId, ip);
            return jrb;
        } catch (Exception e) {
            log.error("查询历史轨迹数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    @ApiOperation(value = "查询当月里程数据(北斗天地)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "nowMonth", value = "当前月   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "nextMonth", value = "下月间   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "监控对象类型(vehicle:车; people:人)", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getActiveDateForBDTD" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getActiveDateForBDTD(String vehicleId, String nowMonth, String nextMonth, String type,
        HttpServletResponse response) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            JSONObject msg = new JSONObject();
            JSONArray dates = new JSONArray();
            JSONArray dailyMiles = new JSONArray();
            nowMonth = nowMonth + " 00:00:00";
            nextMonth = nextMonth + " 00:00:00";
            // 根据车辆 id查询指定月份返回每日里程
            List<Map<String, String>> activeDate = null;
            if (type.equals("vehicle")) {
                activeDate = historyService.getDailyMileByDate(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                    Converter.convertToUnixTimeStamp(nextMonth));
                for (Map<String, String> date : activeDate) {
                    dates.add(date.get("ATIME"));
                    dailyMiles.add(date.get("MILE"));
                }
                msg.put("date", dates);
                msg.put("dailyMile", dailyMiles);
            }
            if (activeDate != null && !activeDate.isEmpty()) {
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("查询当月里程数据(北斗天地)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}

