package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.reportManagement.FenceReport;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.reportManagement.FenceReportService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FenceReportServiceImpl implements FenceReportService {

    @Autowired
    private VehicleService vehicleService;

    private HashMap<String, List<AlarmHandle>> inOutArea; // 进出区域数据

    private List<FenceReport> fenceReportList;

    @Override
    public List<FenceReport> getFenceReport(List<String> vehicleIds, Long startTime, Long endTime)
        throws Exception {
        List<AlarmHandle> alarmHandles = this.listFenceReport(vehicleIds, startTime, endTime);
        fenceReportList = new ArrayList<>();
        inOutArea = new HashMap<>(alarmHandles.size());
        for (AlarmHandle alarmHandle : alarmHandles) {
            final String plateNumber = alarmHandle.getPlateNumber();
            final String fenceName = alarmHandle.getFenceName();
            final String fenceType = alarmHandle.getFenceType();
            if (plateNumber != null && fenceName != null && fenceType != null) {
                String mapKey = plateNumber + "_" + fenceType + "_" + fenceName;
                inOutArea.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(alarmHandle);
            }
        }
        statisticsFence(endTime);
        return fenceReportList;

    }

    private List<AlarmHandle> listFenceReport(List<String> vehicleIds, Long startTime, Long endTime) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_FENCE_REPORT, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    private void statisticsFence(long endTime)
        throws Exception {
        List<AlarmHandle> outAreaData;
        List<AlarmHandle> inAreaData;
        for (Map.Entry<String, List<AlarmHandle>> entry1 : inOutArea.entrySet()) {
            List<AlarmHandle> areaData = entry1.getValue();// Long time = 0L;
            String plateNumber = "";
            String assignmentName = "";
            String fenceType = "";
            String fenceName = "";
            long time = 0L;
            outAreaData = new ArrayList<>();
            inAreaData = new ArrayList<>();
            if (areaData.size() >= 1) {
                for (int i = 0; i <= areaData.size() - 1; i++) {
                    AlarmHandle alarmHandle = areaData.get(i);
                    if (i == 0) { // 第一条数据
                        plateNumber = alarmHandle.getPlateNumber();
                        assignmentName = alarmHandle.getAssignmentName();// 所属分组
                        fenceType = alarmHandle.getFenceType();
                        fenceName = alarmHandle.getFenceName();
                    }
                    int alarmType = alarmHandle.getAlarmType();
                    if (alarmType == 7211 || alarmType == 7311) {
                        inAreaData.add(alarmHandle);
                    }
                    if (alarmType == 7212 || alarmType == 7312) {
                        outAreaData.add(alarmHandle);
                    }
                    if (i + 1 <= areaData.size() - 1) {
                        AlarmHandle nextAlarmHandle = areaData.get(i + 1);
                        int nextAlarmType = nextAlarmHandle.getAlarmType();
                        // 当前是进区域,下一条数据是出区域(只考虑1对1的情况)
                        if (alarmType == 7211 && nextAlarmType == 7212 || alarmType == 7311 && nextAlarmType == 7312) {
                            time += nextAlarmHandle.getAlarmEndTime() - alarmHandle.getAlarmEndTime();
                        }
                    }
                }
                if (inAreaData.size() > 0 && outAreaData.size() == 0) { // 有进区域数据,没有出区域数据,就用今天的日期减去最后一条进区域数据
                    Date nowTime = new Date(); // 当前系统时间
                    long resultTime = 0;
                    if (nowTime.getTime() < endTime) { // 当前系统时间小于结束时间,就取当前系统时间进行计算
                        resultTime = nowTime.getTime();
                    } else {
                        resultTime = endTime;
                    }
                    time = resultTime - inAreaData.get(inAreaData.size() - 1).getAlarmEndTime();
                }

                String plateColorVal = vehicleService.findColorByBrand(plateNumber);
                String plateColor = PlateColor.getNameOrBlankByCode(plateColorVal);
                FenceReport fenceReport = new FenceReport();
                fenceReport.setPlateNumber(plateNumber);// 监控对象
                fenceReport.setAssignmentName(assignmentName);// 所属分组
                fenceReport.setPlateColor(plateColor);// 车牌颜色
                fenceReport.setFenceType(fenceType);// 围栏类型
                fenceReport.setFenceName(fenceName);// 围栏名称
                fenceReport.setEnterFenceTime(inAreaData.size());// 进围栏次数
                fenceReport.setOutFenceTime(outAreaData.size());// 出围栏次数
                fenceReport.setTimeTotal(DateUtil.formatTime(time));// 围栏内累计时长
                fenceReportList.add(fenceReport);
            }
        }
    }

    /**
     * 导出excel文件
     */
    @Override
    public boolean exports(String title, int type, HttpServletResponse response, List<FenceReport> fenceReports)
        throws Exception {
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, fenceReports, FenceReport.class, null, response.getOutputStream()));
    }

}
