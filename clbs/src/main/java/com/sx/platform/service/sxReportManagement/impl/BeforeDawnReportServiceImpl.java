package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.sx.platform.domain.sxReport.BeforeDawnReport;
import com.sx.platform.service.sxReportManagement.BeforeDawnReportService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class BeforeDawnReportServiceImpl implements BeforeDawnReportService {

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    @Autowired
    UserService userService;

    @Override
    public JsonResultBean getListFromPaas(String band, String startTime, String endTime) throws Exception {
        List<BeforeDawnReport> result = new ArrayList<>();
        JSONObject obj = new JSONObject();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", band);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_BEFORE_DAWN_URL, param);
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
            List<String> monitorIds = Arrays.asList(band.split(","));
            Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                BeforeDawnReport report = new BeforeDawnReport();
                String monitorId = object.getString("monitorId");
                report.setBrnad(object.getString("monitorName"));
                report.setAssignmentName(object.getString("assignmentName"));
                report.setAlarmStartLocation(object.getString("alarmStartAddress"));
                report.setAlarmEndLocation(object.getString("alarmEndAddress"));
                Long durationTime = object.getLong("durationTime");
                report.setDurationTime(DateUtil.formatTime(durationTime * 1000));
                Long alarmStartTime = object.getLong("alarmStartTime");
                report.setAlarmStartTime(alarmStartTime);
                report.setAlarmStartTimeStr(DateUtil.getLongToDateStr(alarmStartTime, null));
                Long alarmEndTime = object.getLong("alarmEndTime");
                report.setAlarmEndTime(alarmEndTime);
                report.setAlarmEndTimeStr(DateUtil.getLongToDateStr(alarmEndTime, null));
                VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
                report.setGroupName(vehicleDTO.getOrgName());
                report.setColor(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                report.setVehicleType(vehicleDTO.getVehicleTypeName());
                result.add(report);
            }
        }
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.SX_BEFORE_DAWN_REPORT_INFORMATION.of(userId);
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
        List<BeforeDawnReport> beforeDawnReport =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_BEFORE_DAWN_REPORT_INFORMATION.of(userId),
                        BeforeDawnReport.class);
        // 逆地址编码
        final Set<String> lngLats = new HashSet<>(CommonUtil.ofMapCapacity(beforeDawnReport.size() * 2));
        for (BeforeDawnReport beforeDawn : beforeDawnReport) {
            lngLats.add(beforeDawn.getAlarmStartLocation());
            lngLats.add(beforeDawn.getAlarmEndLocation());
        }
        final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
        for (BeforeDawnReport beforeDawn : beforeDawnReport) {
            beforeDawn.setAlarmStartLocation(addressMap.get(beforeDawn.getAlarmStartLocation()));
            beforeDawn.setAlarmEndLocation(addressMap.get(beforeDawn.getAlarmEndLocation()));
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, beforeDawnReport, BeforeDawnReport.class, null, res.getOutputStream()));
    }

    /**
     * 判断该开始时间，结束时间是否在凌晨2-5点之间
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public boolean beforeDawnFlag(long startTime, long endTime) throws Exception {
        String twoClock = "";// 凌晨2点
        String fiveClock = "";// 凌晨五点
        if ((endTime - startTime) > 86400000) {
            return true;
        }
        String stime = sdf.format(startTime);
        String etime = sdf.format(endTime);

        if (stime.equals(etime)) {
            twoClock = stime + " 02:00:00";
            fiveClock = etime + " 05:00:00";
        } else {
            twoClock = etime + " 02:00:00";
            fiveClock = etime + " 05:00:00";
        }
        long two = simpleDateFormat.parse(twoClock).getTime();
        long five = simpleDateFormat.parse(fiveClock).getTime();
        if (((startTime > two) && endTime < five) || ((endTime >= two) && endTime <= five) || ((startTime >= two)
            && startTime <= five) || ((startTime < two) && (endTime > five))) {
            return true;
        }
        return false;

    }

}
