package com.zw.adas.controller.monitorScore;

import com.zw.adas.domain.monitorScore.MonitorAlarmInfo;
import com.zw.adas.domain.monitorScore.MonitorScore;
import com.zw.adas.domain.monitorScore.MonitorScoreEventInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreQuery;
import com.zw.adas.service.monitorScore.MonitorScoreService;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Controller
@RequestMapping("/adas/v/monitoring/score")
public class MonitorScoreController {
    private Logger log = LogManager.getLogger(MonitorScoreController.class);

    @Autowired
    private MonitorScoreService monitorScoreService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean list(String groupId, int time) {
        try {
            if (StringUtils.isNotEmpty(groupId)) {
                return new JsonResultBean(monitorScoreService.list(groupId, time));
            }
        } catch (Exception e) {
            log.error("查询监控对象评分列表异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @RequestMapping(value = {"/sortByAverageTravelTime"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sortByAverageTravelTime(String groupId, int time, String parameter, boolean isDownSort) {
        try {
            if (StringUtils.isNotEmpty(groupId)) {
                return new JsonResultBean(monitorScoreService.sort(groupId, time, parameter, isDownSort));
            }
        } catch (Exception e) {
            log.error("查询监控对象评分列表异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @RequestMapping(value = {"/scoreInfo"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean scoreInfo(String vehicleId, int time) {
        try {
            return new JsonResultBean(monitorScoreService.scoreInfo(vehicleId, time));
        } catch (Exception e) {
            log.error("查询监控对象评分详情列表异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @RequestMapping(value = {"/monitorAlarmInfo"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean monitorAlarmInfo(MonitorScoreQuery query) {
        try {
            return new JsonResultBean(monitorScoreService.monitorAlarmInfo(query));
        } catch (Exception e) {
            log.error("查询监控对象报警详情列表异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    /**
     * 导出单个监控对象评分详情(生成excel文件)
     *
     * @param res res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res, String vehicleId, int time) {
        try {
            MonitorScoreInfo monitorScoreInfo = monitorScoreService.scoreInfo(vehicleId, time);
            if (monitorScoreInfo != null) {
                monitorScoreInfo.setScoreStr(Math.round(monitorScoreInfo.getScore()) + "");
                monitorScoreService.setAlarmRatioStr(monitorScoreInfo);
                monitorScoreService.setScoreRingRatioStr(monitorScoreInfo);
                monitorScoreService.setHundredsAlarmRingRatioStr(monitorScoreInfo);
                List<MonitorAlarmInfo> monitorAlarmInfoList = monitorScoreService.monitorAlarmInfoList(vehicleId, time);
                List<MonitorScoreEventInfo> eventInfoList = monitorScoreService.eventTypeList(vehicleId, time);
                Map<String, Object> data = new HashMap<>();
                data.put("monitorAlarmInfoList", monitorAlarmInfoList);
                data.put("monitorScoreInfo", monitorScoreInfo);
                data.put("eventInfoList", eventInfoList);
                if (monitorScoreInfo.getVehiclePhotoPath() != null) {
                    data.put("img", fastDFSClient.downloadFile(monitorScoreInfo.getVehiclePhotoPath()));
                }
                String name = monitorScoreInfo.getBrand() != null ? monitorScoreInfo.getBrand() : "未知";
                String fileName = "监控对象评分明细报表" + name;
                templateExportExcel.templateExportExcel("/file/cargoReport/monitorScore.xls", res, data, fileName);
            }
        } catch (Exception e) {
            log.error("导出单个监控对象评分详情异常", e);
        }
    }

    /**
     * 批量导出监控对象评分详情(生成excel文件)
     *
     * @param response res
     */
    @RequestMapping(value = "/batchExport", method = RequestMethod.POST)
    public void batchExport(HttpServletResponse response, String vehicleIds, int time) {
        try {
            Set<String> vehicleIdSet = new HashSet<>(Arrays.asList(vehicleIds.split(",")));
            List<MonitorScoreInfo> monitorScoreInfoList = monitorScoreService.scoreInfoList(vehicleIdSet, time);
            Map<String, List<MonitorAlarmInfo>> alarmInfoMap =
                monitorScoreService.monitorAlarmInfoMap(vehicleIdSet, time);
            Map<String, List<MonitorScoreEventInfo>> eventInfoMap =
                monitorScoreService.eventTypeMap(vehicleIdSet, time);
            List<Map<String, Object>> prarm = new ArrayList<>();
            String name = "未知";
            int index = 1;
            for (MonitorScoreInfo monitorScoreInfo : monitorScoreInfoList) {
                Map<String, Object> data = new HashMap<>();
                data.put("monitorAlarmInfoList", alarmInfoMap.get(monitorScoreInfo.getVehicleId()) != null
                    ? alarmInfoMap.get(monitorScoreInfo.getVehicleId()) : new ArrayList());
                data.put("monitorScoreInfo", monitorScoreInfo);
                data.put("eventInfoList", eventInfoMap.get(monitorScoreInfo.getVehicleId()) != null
                    ? eventInfoMap.get(monitorScoreInfo.getVehicleId()) : new ArrayList<>());
                if (monitorScoreInfo.getVehiclePhotoPath() != null) {
                    data.put("img", fastDFSClient.downloadFile(monitorScoreInfo.getVehiclePhotoPath()));
                }
                if (monitorScoreInfo.getBrand() != null) {
                    data.put("templateSingleFileName", "监控对象评分明细报表" + monitorScoreInfo.getBrand());
                } else {
                    data.put("templateSingleFileName", "监控对象评分明细报表" + name + index);
                    index += 1;
                }
                prarm.add(data);
            }
            templateExportExcel
                .templateExportExcels("/file/cargoReport/monitorScore.xls", response, prarm, "监控对象评分明细报表");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("批量导出监控对象评分详情异常", e);
        }
    }

    /**
     * 导出监控对象评分统计列表(生成excel文件)
     *
     * @param res res
     */
    @RequestMapping(value = "/exportMonitorScoreList", method = RequestMethod.GET)
    public void exportMonitorScoreList(HttpServletResponse res, String groupId, int time) {
        try {
            List<MonitorScore> monitorScoreList = monitorScoreService.exportList(groupId, time);
            List<String> timeList = monitorScoreService.conversionTime(time, false);
            Map<String, Object> data = new HashMap<>();
            data.put("monitorScoreList", monitorScoreList);
            data.put("startTime", timeList.get(0));
            data.put("endTime", timeList.get(1));
            String fileName = "监控对象评分明细报表";
            templateExportExcel.templateExportExcel("/file/cargoReport/monitorScoreList.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出监控对象评分统计列表异常", e);
        }
    }

}
