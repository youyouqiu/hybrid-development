package com.zw.lkyw.controller.trackback;

import com.alibaba.fastjson.JSONObject;
import com.zw.lkyw.service.trackback.TrackBackService;
import com.zw.platform.domain.oil.HistoryMileAndSpeed;
import com.zw.platform.domain.oil.HistoryStopAndTravel;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.history.MapQueryParam;
import com.zw.platform.domain.vas.history.TimeZoneQueryParam;
import com.zw.platform.domain.vas.history.TrackPlayBackChartDataQuery;
import com.zw.platform.service.monitoring.HistoryService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/lkyw/trackBack")
@Log4j2
public class TrackBackController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private TrackBackService trackBackService;

    @Autowired
    HistoryService historyService;

    /**
     * 查询今天第一条数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param type
     * @param request
     * @param sensorFlag
     * @return
     */
    @RequestMapping(value = { "/getHistoryData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstData(String vehicleId, String startTime, String endTime, String type,
        HttpServletRequest request, Integer sensorFlag, Integer reissue) {

        if (checkParameter(vehicleId, startTime, endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        JsonResultBean jrb = new JsonResultBean();
        String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
        //做数据处理
        startTime = handleParameter(startTime);
        endTime = handleParameter(endTime);
        try {
            switch (type) {
                case "0": // 808 2011扩展
                case "1": // 808 2013
                case "2": // 移为
                case "3": // 天禾
                case "6": // KKS
                case "8": // BSJ-A5
                case "9": // ASO
                case "10": // F3超长待机
                case "11": //808-2019
                case "12": //交通部JT/T808-2013（川标）
                case "13": //交通部JT/T808-2013（冀标）
                    jrb = trackBackService.getHistoryVehicle(vehicleId, startTime, endTime, sensorFlag, ip, reissue);
                    break;
                case "5": // BDTD-SM
                    jrb = trackBackService.getHistoryPeople(vehicleId, startTime, endTime, sensorFlag, ip, reissue);
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

    private String handleParameter(String parameter) {
        return parameter.replaceAll("-", "").replaceAll(":", "").replaceAll("\\s+", "");
    }

    /**
     * 查询指定日期的历史轨迹
     * @param vehicleId
     * @param nowMonth
     * @param nextMonth
     * @param bigDataFlag （离线查询标识 0：实时 1：离线）
     * @return
     */
    @RequestMapping(value = { "/getActiveDate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getActiveDate(String vehicleId, String nowMonth, String nextMonth, String type,
        Integer bigDataFlag) {
        try {
            if (checkParameter(vehicleId, nowMonth, nextMonth)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.changeHistoryActiveDate(vehicleId, nowMonth, nextMonth, type, bigDataFlag, false);
        } catch (Exception e) {
            log.error("查询指定日期的历史轨迹异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 时间段多区域查车
     * @param queryParam queryParam
     * @return JsonResultBean
     */
    @RequestMapping(value = "/findHistoryByTimeAndAddress", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findHistoryByTimeAndAddress(TimeZoneQueryParam queryParam) {
        try {
            Long startTimeOne = queryParam.getStartTimeOne();
            Long endTimeOne = queryParam.getEndTimeOne();
            Long startTimeTwo = queryParam.getStartTimeTwo();
            Long endTimeTwo = queryParam.getEndTimeTwo();
            if (StringUtils.isEmpty(queryParam.getMonitorIds())
                || startTimeOne == null || endTimeOne == null
                || (startTimeTwo != null && endTimeTwo == null)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            String stimeOne = handleParameter(DateUtil.getLongToDateStr(startTimeOne * 1000, null));
            String etimeOne = handleParameter(DateUtil.getLongToDateStr(endTimeOne * 1000, null));
            String stimeTwo = "";
            if (startTimeTwo != null) {
                stimeTwo = handleParameter(DateUtil.getLongToDateStr(startTimeTwo * 1000, null));
            }
            String etimeTwo = "";
            if (endTimeTwo != null) {
                etimeTwo = handleParameter(DateUtil.getLongToDateStr(endTimeTwo * 1000, null));
            }
            return trackBackService.findHistoryByTimeAndAddress(queryParam.getAreaListStr(),
                queryParam.getMonitorIds(), stimeOne, etimeOne, stimeTwo, etimeTwo);
        } catch (Exception e) {
            log.error("定时定区域查车异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 定时定区域导出
     * @param areaListStr
     * @param groupName
     */
    @RequestMapping(value = "/timeZoneExport", method = RequestMethod.POST)
    @ResponseBody
    public void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName,
        Integer reissue) {
        try {
            trackBackService.exportTimeZoneTrackPlay(response, areaListStr, groupName, reissue);
        } catch (Exception e) {
            log.error("定时定区域导出异常", e);
        }
    }

    /**
     * 进出区域详情
     * @param
     * @return
     * @throws @Title:
     * @author wangjianyu
     */
    @RequestMapping(value = { "/getQueryDetails" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getQueryDetails(MapQueryParam param) {
        long endTime = param.getEndTime();
        long startTime = param.getStartTime();
        String vehicleId = param.getVehicleId();
        if (StringUtils.isEmpty(vehicleId) || startTime == 0 || endTime == 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        try {
            String stime = handleParameter(DateUtil.getLongToDateStr(startTime * 1000, null));
            String etime = handleParameter(DateUtil.getLongToDateStr(endTime * 1000, null));
            List<Positional> positionals =
                trackBackService.getQueryDetails(vehicleId, stime, etime, param.getReissue());
            boolean flog = true;
            boolean derail = true;
            List<List<String>> arrayList = new ArrayList<>();
            List<String> list = new ArrayList<>();
            if (positionals.size() != 0) {
                for (Positional i : positionals) {
                    String latitude = i.getLatitude();
                    String longitude = i.getLongitude();
                    if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
                        continue;
                    }
                    if (Double.valueOf(latitude) <= Double.valueOf(param.getRightFloorLatitude())
                        && Double.valueOf(latitude) >= Double.valueOf(param.getLeftTopLatitude())
                        && Double.valueOf(longitude) >= Double.valueOf(param.getLeftTopLongitude())
                        && Double.valueOf(longitude) <= Double.valueOf(param.getRightFloorLongitude())) {
                        // 当天第一条数据就在区域中,或者已经在区域中了，抛弃掉
                        if (flog) {
                            if (derail) {
                                // 关闭第一个点的开关
                                derail = false;
                                // 获取进入区域的时间
                                long time = i.getTime();
                                list.add("已在区域内");
                            }
                            // 进区域了
                        } else {
                            // 获取进入区域的时间
                            long time = i.getTime();
                            // JSONObject obj = new JSONObject();
                            list.add(String.valueOf(time));
                            flog = true;
                        }
                    } else {
                        // 还没有进入区域,抛弃掉
                        if (!flog) {
                            // 出区域了
                        } else {
                            // 判断是否是第一个点
                            if (!derail) {
                                // 获取出区域时间
                                long time = i.getTime();
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
            }
            if (list.size() == 1) {
                list.add("");
                arrayList.add(list);
            }
            return new JsonResultBean(arrayList);
        } catch (Exception e) {
            log.error("获取进出区域详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


    /**
     * 轨迹回放查询历史数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param request
     * @param sensorFlag
     * @return
     */
    @RequestMapping(value = { "/getMonitorHistoryData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorHistoryData(String vehicleId, String startTime, String endTime,
        HttpServletRequest request, Integer sensorFlag, Integer reissue) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            String ip = new GetIpAddr().getIpAddr(request);
            return trackBackService.getMonitorHistoryData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, ip, reissue);
        } catch (Exception e) {
            log.error("轨迹回放查询历史数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    private boolean checkParameter(String vehicleId, String startTime, String endTime) {
        if (StringUtils.isEmpty(vehicleId) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return true;
        }
        return false;
    }

    /**
     * 查询油耗图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorNo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getOilConsumptionChartData", method = RequestMethod.POST)
    public JsonResultBean getOilConsumptionChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo, Integer reissue) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getOilConsumptionChartData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, sensorNo, reissue);
        } catch (Exception e) {
            log.error("轨迹回放查询油耗图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取obd数据
     * @param monitorId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getMonitorObdDate", method = RequestMethod.POST)
    public JsonResultBean getMonitorObdData(String monitorId, String startTime, String endTime, Integer sensorFlag) {
        try {
            if (checkParameter(monitorId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getMonitorObdData(monitorId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag);
        } catch (Exception e) {
            log.error("轨迹回放查询OBD数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询温度图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorNo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getTemperatureChartData", method = RequestMethod.POST)
    public JsonResultBean getTemperatureChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getTemperatureChartData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, sensorNo);
        } catch (Exception e) {
            log.error("轨迹回放查询温度图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询湿度图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorNo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getHumidityChartData", method = RequestMethod.POST)
    public JsonResultBean getHumidityChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getHumidityChartData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, sensorNo);
        } catch (Exception e) {
            log.error("轨迹回放查询湿度图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询工时图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorNo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getWorkHourChartData", method = RequestMethod.POST)
    public JsonResultBean getWorkHourChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getWorkHourChartData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, sensorNo);
        } catch (Exception e) {
            log.error("轨迹回放查询工时图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放图表-里程速度
     */
    @RequestMapping(value = "/getMileAndSpeed", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getMileSpeedData(TrackPlayBackChartDataQuery query) {

        try {
            if (checkParameter(query)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            List<HistoryMileAndSpeed> result = trackBackService.getMileSpeedData(query);
            JSONObject msg = new JSONObject();
            msg.put("mileAndSpeed", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("轨迹回放-获取里程速度图表曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放图表-行驶/停止数据
     */
    @RequestMapping(value = "/getTravelAndStop", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getTravelAndStopData(TrackPlayBackChartDataQuery query) {
        if (checkParameter(query)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        try {
            List<HistoryStopAndTravel> result = trackBackService.getTravelAndStopData(query);
            JSONObject msg = new JSONObject();
            msg.put("travelAndStop", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("轨迹回放-获取行驶/停止图标曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    private boolean checkParameter(TrackPlayBackChartDataQuery query) {
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (checkParameter(query.getMonitorId(), startTime, endTime)) {
            return true;
        }
        query.setStartTime(handleParameter(startTime));
        query.setEndTime(handleParameter(endTime));
        return false;
    }

    /**
     * 轨迹回放图表-油量数据
     */
    @RequestMapping(value = "/getOilMass", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getOilMassData(TrackPlayBackChartDataQuery query) {
        if (checkParameter(query)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        try {
            return trackBackService.getOilMassData(query);
        } catch (Exception e) {
            log.error("轨迹回放-获取油量数据图表曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取监控对象外设轮询列表
     */
    @RequestMapping(value = "/getPollingList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getSensorPollingList(String monitorId) {
        try {
            return new JsonResultBean(trackBackService.getSensorPollingListByMonitorId(monitorId));
        } catch (Exception e) {
            log.error("轨迹回放-获取监控对象外设轮询列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取正反转数据
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getPositiveInversionDate", method = RequestMethod.POST)
    public JsonResultBean getPositiveInversionDate(String monitorId, String startTime, String endTime,
        Integer sensorFlag) {
        try {
            if (checkParameter(monitorId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getPositiveInversionDate(monitorId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag);
        } catch (Exception e) {
            log.error("轨迹回放查询开关图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放导出
     *
     * @param response response
     * @param query query
     */
    @RequestMapping(value = "/exportTrackPlay", method = RequestMethod.GET)
    public void exportTrackPlay(HttpServletResponse response, PositionalQuery query) {
        try {
            if (Objects.nonNull(query)) {
                trackBackService.exportTrackPlay(response, query);
            }
        } catch (Exception e) {
            log.error("轨迹回放导出异常", e);
        }
    }

    /**
     * 轨迹回放图表-I/O数据
     */
    @RequestMapping(value = "/getSwitchData", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getSwitchData(TrackPlayBackChartDataQuery query) {
        try {
            if (checkParameter(query)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getSwitchData(query);
        } catch (Exception e) {
            log.error("轨迹回放查询开关图表(I/O)数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放图表-载重数据
     */
    @RequestMapping(value = "/getLoadWeight", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLoadWeight(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getLoadWeightDate(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, sensorNo);
        } catch (Exception e) {
            log.error("轨迹回放-获取载重数据图表曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放图表-胎压数据
     */
    @RequestMapping(value = "/getTirePressureData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer tireNum) {
        try {
            if (checkParameter(vehicleId, startTime, endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
            }
            return trackBackService.getTirePressureData(vehicleId,
                handleParameter(startTime), handleParameter(endTime), sensorFlag, tireNum);
        } catch (Exception e) {

            log.error("轨迹回放-获取胎压数据图表曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 两客一危 轨迹回放报警查询
     * @param vehicleId 监控对象id
     * @param startTime 开始时间(时间戳 秒)
     * @param endTime   结束时间(时间戳 秒)
     */
    @RequestMapping(value = { "/getAlarmData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmData(String vehicleId, Long startTime, Long endTime) {
        try {
            return historyService.getAlarmData(vehicleId, startTime, endTime, true, true);
        } catch (Exception e) {
            log.error("查询两客一危轨迹回放报警异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
