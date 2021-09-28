package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.sx.platform.domain.sxReport.SxSpeedAlarmReport;
import com.sx.platform.service.sxReportManagement.SxSpeedAlarmReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class SxSpeedAlarmReportServiceImpl implements SxSpeedAlarmReportService {

    @Autowired
    UserService userService;

    @Override
    public JsonResultBean getListFromPaas(String vehicleList, String speedType, String startTime, String endTime)
        throws Exception {
        List<SxSpeedAlarmReport> result = new ArrayList<>();
        JSONObject obj = new JSONObject();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", vehicleList);
        param.put("speedType", speedType);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_SPEED_ALARM_DETAIL_URL, param);
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
        Object data = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
        }
        if (Objects.isNull(data)) {
            obj.put("list", result);
            return new JsonResultBean(obj);
        }
        JSONArray jsonArray = JSONObject.parseArray(data.toString());
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            List<String> monitorIds = Arrays.asList(vehicleList.split(","));
            Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                SxSpeedAlarmReport report = new SxSpeedAlarmReport();
                String monitorId = object.getString("monitorId");
                report.setBrnad(object.getString("monitorName"));
                report.setAlarmStartLocation(object.getString("alarmStartAddress"));
                report.setAlarmEndLocation(object.getString("alarmEndAddress"));
                report.setProfessionalName(object.getString("professionalsName"));
                report.setAssignmentName(object.getString("assignmentName"));
                report.setSpeed(object.getString("speed"));
                report.setSpeedType(object.getInteger("speedType"));
                String durationTimeStr = null;
                Long durationTimeLong = object.getLong("durationTime");
                if (durationTimeLong != null && durationTimeLong > 0) {
                    durationTimeStr = DateUtil.formatTime(durationTimeLong * 1000);
                    if (durationTimeLong > 60000) {
                        durationTimeStr = durationTimeStr.substring(0, durationTimeStr.indexOf("分") + 1);
                    }
                }
                report.setDurationTime(durationTimeStr);
                report.setSpeedTypeStr(getSpeedType(object.getInteger("speedType")));
                Long stime = object.getLong("alarmStartTime");
                Long etime = object.getLong("alarmEndTime");
                report.setAlarmStartTime(stime);
                report.setAlarmEndTime(etime);
                report.setAlarmStartTimeStr(DateUtil.getLongToDateStr(stime, null));
                report.setAlarmEndTimeStr(DateUtil.getLongToDateStr(etime, null));
                VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
                report.setGroupName(vehicleDTO.getOrgName());
                report.setColor(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                report.setVehicleType(vehicleDTO.getVehicleTypeName());
                result.add(report);
            }
        }
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.SX_SPEED_ALARM_REPORT_INFORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, result);
        obj.put("list", result);
        return new JsonResultBean(obj);
    }

    private String getSpeedType(Integer type) {
        String str = "常规限速";
        switch (type) {
            case 0:
                str = "常规限速";
                break;
            case 1:
                str = "夜间限速";
                break;
            case 2:
                str = "道路级别限速";
                break;
            case 3:
                str = "自定义区域限速";
                break;
            default:
                break;
        }
        return str;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res)
        throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<SxSpeedAlarmReport> sxSpeedAlarmReport =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_SPEED_ALARM_REPORT_INFORMATION.of(userId),
                        SxSpeedAlarmReport.class);
        final Set<String> lngLats = new HashSet<>(CommonUtil.ofMapCapacity(sxSpeedAlarmReport.size() * 2));
        for (SxSpeedAlarmReport speedAlarmReport : sxSpeedAlarmReport) {
            lngLats.add(speedAlarmReport.getAlarmStartLocation());
            lngLats.add(speedAlarmReport.getAlarmEndLocation());
        }
        final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
        for (SxSpeedAlarmReport speedAlarmReport : sxSpeedAlarmReport) {
            speedAlarmReport.setAlarmStartLocation(addressMap.get(speedAlarmReport.getAlarmStartLocation()));
            speedAlarmReport.setAlarmEndLocation(addressMap.get(speedAlarmReport.getAlarmEndLocation()));
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, sxSpeedAlarmReport, SxSpeedAlarmReport.class, null,
                res.getOutputStream()));
    }

}
