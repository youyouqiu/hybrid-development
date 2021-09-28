package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.sx.platform.domain.sxReport.TiredAlarmReport;
import com.sx.platform.service.sxReportManagement.TiredAlarmReportService;
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
public class TiredAlarmReportServiceImpl implements TiredAlarmReportService {

    @Autowired
    UserService userService;

    @Override
    public JsonResultBean getListFromPaas(String vehicleList, String startTime, String endTime) throws Exception {
        List<TiredAlarmReport> result = new ArrayList<>();
        JSONObject obj = new JSONObject();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", vehicleList);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_FATIGUE_DRIVING_DETAIL_URL, param);
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
                TiredAlarmReport report = new TiredAlarmReport();
                String monitorId = object.getString("monitorId");
                report.setBrnad(object.getString("monitorName"));
                report.setAlarmStartLocation(object.getString("alarmStartAddress"));
                report.setProfessionalName(object.getString("professionalsName"));
                report.setAssignmentName(object.getString("assignmentName"));
                Long time = object.getLong("alarmStartTime");
                report.setAlarmStartTimeStr(DateUtil.getLongToDateStr(time, null));
                VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
                report.setGroupName(vehicleDTO.getOrgName());
                report.setColor(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                report.setVehicleType(vehicleDTO.getVehicleTypeName());
                result.add(report);
            }
        }
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.SX_TIRED_ALARM_REPORT_INFORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, result);
        obj.put("list", result);
        return new JsonResultBean(obj);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res)
        throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<TiredAlarmReport> tiredAlarmReport =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_TIRED_ALARM_REPORT_INFORMATION.of(userId),
                        TiredAlarmReport.class);
        // 逆地址编码
        final Set<String> lngLats = new HashSet<>(CommonUtil.ofMapCapacity(tiredAlarmReport.size() * 2));
        for (TiredAlarmReport tiredAlarm : tiredAlarmReport) {
            lngLats.add(tiredAlarm.getAlarmStartLocation());
        }
        final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
        for (TiredAlarmReport tiredAlarm : tiredAlarmReport) {
            tiredAlarm.setAlarmStartLocation(addressMap.get(tiredAlarm.getAlarmStartLocation()));
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, tiredAlarmReport, TiredAlarmReport.class, null, res.getOutputStream()));
    }
}
