package com.zw.platform.controller.monitoring;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.HistoryMileAndSpeed;
import com.zw.platform.domain.oil.HistoryStopAndTravel;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.history.MapQueryParam;
import com.zw.platform.domain.vas.history.TimeZoneQueryParam;
import com.zw.platform.domain.vas.history.TrackPlayBackChartDataQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.monitoring.HistoryService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.personalized.IcoService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/v/monitoring")
public class TrackPlaybackController {
    private static final String INDEX_PAGE_NEW = "vas/monitoring/trackPlaybackNew";
    private static final String INDEX_PAGE_LKYW = "vas/monitoring/trackPlaybackLkyw";

    /**
     * 保存历史轨迹为围栏新增页面
     */
    private static final String ADD_FENCE_PAGE = "vas/monitoring/addToFence";

    private static Logger log = LogManager.getLogger(TrackPlaybackController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 轨迹数据
     */
    @Auth
    @RequestMapping(value = { "/trackPlayback" }, method = RequestMethod.GET)
    public ModelAndView indexNew(String vid, String pid) {
        ModelAndView mv = new ModelAndView(INDEX_PAGE_NEW);
        mv.addObject("vid", vid);
        mv.addObject("pid", pid);
        return mv;
    }

    @Auth
    @RequestMapping(value = { "/trackPlaybackLkyw" }, method = RequestMethod.GET)
    public ModelAndView indexOld(String vid, String pid) {
        ModelAndView mv = new ModelAndView(INDEX_PAGE_LKYW);
        mv.addObject("vid", vid);
        mv.addObject("pid", pid);
        return mv;
    }

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    VehicleService vs;

    @Autowired
    HistoryService historyService;

    @Autowired
    IcoService icoService;

    @Autowired
    private PositionalService positionalService;

    String[] lineArrs;

    @SuppressWarnings("null")
    @RequestMapping(value = { "/getHistoryData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstData(String vehicleId, String startTime, String endTime, String type,
        HttpServletRequest request, Integer sensorFlag) {
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
                case "11": //808-2019
                case "12": //交通部JT/T808-2013（川标）
                case "13": //交通部JT/T808-2013（冀标）
                    jrb = historyService.getHistoryVehicle(vehicleId, startTime, endTime, sensorFlag);
                    historyService.addlog(vehicleId, ip);
                    break;
                case "5": // BDTD-SM
                    jrb = historyService.getHistoryPeople(vehicleId, startTime, endTime, sensorFlag);
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
            return historyService.changeHistoryActiveDate(vehicleId, nowMonth, nextMonth, type, bigDataFlag, false);
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
            if (Objects.nonNull(queryParam)) {
                return historyService.findHistoryByTimeAndAddress(queryParam);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "查询条件不能为空!");
            }
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
    public void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName) {
        try {
            historyService.exportTimeZoneTrackPlay(response, areaListStr, groupName);
        } catch (Exception e) {
            log.error("定时定区域导出异常", e);
        }
    }

    /**
     * 报警参数
     * @param
     * @return
     * @throws @Title:
     * @author wangjianyu
     */
    @RequestMapping(value = { "/getAlarmData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmData(String vehicleId, Long startTime, Long endTime) {
        try {
            return historyService.getAlarmData(vehicleId, startTime, endTime, true, false);
        } catch (Exception e) {
            log.error("报警参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
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
        try {
            List<Positional> positionals =
                    historyService.getQueryDetails(param.getVehicleId(), param.getStartTime(), param.getEndTime());
            boolean flog = true;
            boolean derail = true;
            List<List<String>> arrayList = new ArrayList<>();
            List<String> list = new ArrayList<>();
            if (positionals.size() != 0) {
                for (Positional i : positionals) {
                    String latitude = i.getLatitude();
                    String longtitude = i.getLongtitude();
                    if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longtitude)) {
                        continue;
                    }
                    if (Double.valueOf(latitude) <= Double.valueOf(param.getRightFloorLatitude())
                            && Double.valueOf(latitude) >= Double.valueOf(param.getLeftTopLatitude())
                            && Double.valueOf(longtitude) >= Double.valueOf(param.getLeftTopLongitude())
                            && Double.valueOf(longtitude) <= Double.valueOf(param.getRightFloorLongitude())) {
                        // 当天第一条数据就在区域中,或者已经在区域中了，抛弃掉
                        if (flog) {
                            if (derail) {
                                // 关闭第一个点的开关
                                derail = false;
                                // 获取进入区域的时间
                                long time = i.getVtime();
                                list.add("已在区域内");
                            }
                            // 进区域了
                        } else {
                            // 获取进入区域的时间
                            long time = i.getVtime();
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
                                long time = i.getVtime();
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
     * 轨迹生成电子围栏新增页面
     * @param param
     * @return ModelAndView
     * @throws @Title: toAddPage
     * @author Liubangquan
     */
    @RequestMapping(value = { "/addToFence_{param}" }, method = RequestMethod.GET)
    public ModelAndView toAddPage(@PathVariable final String param) {
        ModelAndView mav = new ModelAndView(ADD_FENCE_PAGE);
        mav.addObject("result", null);
        return mav;
    }

    /**
     * 导出kml文件
     * @param
     * @return
     * @throws @Title:
     * @throws IOException
     * @author wangjianyu
     */
    @RequestMapping(value = { "/exportKML" }, method = RequestMethod.GET)
    public void exportKML(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "导出谷歌轨迹";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".kml");
            response.setContentType("application/xml;charset=UTF-8");
            realTime.exportKML(lineArrs, response);
        } catch (Exception e) {
            log.error("导出kml文件异常", e);
        }
    }

    /**
     * 进出区域详情
     * @param
     * @return
     * @throws @Title:
     * @author wangjianyu
     */
    @RequestMapping(value = { "/exportKMLLineArr" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportKMLLineArr(String[] lineArr) {
        lineArrs = lineArr;
        return new JsonResultBean();
    }

    /**
     * 单个逆地址编码
     * @param
     * @return
     * @throws @Title:
     * @author wangjianyu
     */
    @RequestMapping(value = { "/address" }, method = RequestMethod.POST)
    @ResponseBody
    public String address(String[] addressReverse) {

        if (addressReverse == null) {
            return "未定位";
        }
        try {
            if (addressReverse[1] != null && addressReverse[0] != null && !addressReverse[1].equals("0.0")
                && !addressReverse[1].equals("0") && !addressReverse[1].equals("") && !addressReverse[0].equals("0")
                && !addressReverse[0].equals("0.0") && !addressReverse[0].equals("") && addressReverse[1].length() >= 7
                && addressReverse[0].length() >= 6) {
                String longitude = addressReverse[1].substring(0, 7);
                String latitude = addressReverse[0].substring(0, 6);
                return positionalService.getAddress(longitude, latitude);
            }
        } catch (Exception e) {
            log.error("获取单个逆地址编码异常", e);
        }
        return "未定位";
    }


    /**#############################################################*/

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
            String ip = new GetIpAddr().getIpAddr(request);
            historyService.addlog(vehicleId, ip);
            return historyService.getMonitorHistoryData(vehicleId, startTime, endTime, sensorFlag, reissue);
        } catch (Exception e) {
            log.error("轨迹回放查询历史数据异常", e);
            String message = e instanceof BusinessException ? ((BusinessException) e).getDetailMsg() : sysErrorMsg;
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
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
        Integer sensorFlag, Integer sensorNo) {
        try {
            return historyService.getOilConsumptionChartData(vehicleId, startTime, endTime, sensorFlag, sensorNo);
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
    public JsonResultBean getMonitorObdDate(String monitorId, String startTime, String endTime, Integer sensorFlag) {
        try {
            return historyService.getMonitorObdDate(monitorId, startTime, endTime, sensorFlag);
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
            return historyService.getTemperatureChartData(vehicleId, startTime, endTime, sensorFlag, sensorNo);
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
            return historyService.getHumidityChartData(vehicleId, startTime, endTime, sensorFlag, sensorNo);
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
            return historyService.getWorkHourChartData(vehicleId, startTime, endTime, sensorFlag, sensorNo);
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
            List<HistoryMileAndSpeed> result = historyService.getMileSpeedData(query);
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
        try {
            List<HistoryStopAndTravel> result = historyService.getTravelAndStopData(query);
            JSONObject msg = new JSONObject();
            msg.put("travelAndStop", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("轨迹回放-获取行驶/停止图标曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放图表-油量数据
     */
    @RequestMapping(value = "/getOilMass", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getOilMassData(TrackPlayBackChartDataQuery query) {
        try {
            return historyService.getOilMassData(query);
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
            return new JsonResultBean(historyService.getSensorPollingListByMonitorId(monitorId));
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
            return historyService.getPositiveInversionDate(monitorId, startTime, endTime, sensorFlag);
        } catch (Exception e) {
            log.error("轨迹回放查询开关图表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 轨迹回放导出
     * @param response response
     * @param query    query
     */
    @RequestMapping(value = "/exportTrackPlay", method = RequestMethod.GET)
    public void exportTrackPlay(HttpServletResponse response, PositionalQuery query) {
        try {
            if (Objects.nonNull(query)) {
                historyService.exportTrackPlay(response, query);
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
            return historyService.getSwitchData(query);
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
            return historyService.getLoadWeightDate(vehicleId, startTime, endTime, sensorFlag, sensorNo);
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
            return historyService.getTirePressureData(vehicleId, startTime, endTime, sensorFlag, tireNum);
        } catch (Exception e) {

            log.error("轨迹回放-获取胎压数据图表曲线数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
