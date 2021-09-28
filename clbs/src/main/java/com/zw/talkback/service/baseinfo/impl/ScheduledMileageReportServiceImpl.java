package com.zw.talkback.service.baseinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.talkback.domain.basicinfo.MileageExport;
import com.zw.talkback.domain.basicinfo.MileageExportAll;
import com.zw.talkback.domain.basicinfo.form.AttendanceForm;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;
import com.zw.talkback.service.baseinfo.ScheduledMileageReportService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduledMileageReportServiceImpl implements ScheduledMileageReportService {

    private DecimalFormat df = new DecimalFormat("0.0");

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public PageGridBean getSummary(AttendanceReportQuery query) throws Exception {
        Page<MileageExport> resultPage = new Page<>();
        try {
            List<MileageExport> result = new ArrayList<>();
            String userId = SystemHelper.getCurrentUser().getId().toString();
            RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            List<MileageExport> mileageObjs = new ArrayList<>();
            for (AttendanceForm form : allAttendance) {
                MileageExport mileageExport = new MileageExport();
                BeanUtils.copyProperties(form, mileageExport);
                mileageObjs.add(mileageExport);
            }
            Map<String, MileageExport> map = new HashMap<>();
            statisticsInfo(mileageObjs, map);
            for (Map.Entry entry : map.entrySet()) {
                MileageExport value = (MileageExport) entry.getValue();
                calculationMileage(value);
                result.add(value);
            }
            if (CollectionUtils.isNotEmpty(result)) {
                //每页条数
                Long pageSize = query.getLength();
                //遍历开始条数
                int fromIndex = query.getStart().intValue();
                //页码
                int pageNum = query.getPage().intValue();
                //总条数
                int totalNum = result.size();
                //遍历结束条数
                Long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
                List<MileageExport> subList =
                    result.stream().sorted(Comparator.comparing(MileageExport::getAverageMileageDouble).reversed())
                        .collect(Collectors.toList()).subList(fromIndex, toIndex.intValue());
                resultPage = RedisQueryUtil.getListToPage(subList, query, totalNum);
            }
        } finally {
            return new PageGridBean(query, resultPage, true);

        }
    }

    /**
     * 统计信息
     * @param allAttendance
     * @param map
     */
    private void statisticsInfo(List<MileageExport> allAttendance, Map<String, MileageExport> map) {
        for (MileageExport attendanceForm : allAttendance) {
            if (map.containsKey(attendanceForm.getMonitorId())) {
                MileageExport form = map.get(attendanceForm.getMonitorId());
                // 应工作天数
                Integer workDay = form.getWorkDays();
                form.setWorkDays(workDay + 1);
                // 实际工作天数
                Integer actualWorkDay = form.getActualWorkDays();
                Long nowWorkingDuration = attendanceForm.getActualWorkingDuration();
                if (nowWorkingDuration != null && nowWorkingDuration != 0) {
                    form.setDayEffectiveMileage(
                        form.getDayEffectiveMileage() + (attendanceForm.getDayEffectiveMileage() == null
                                                         ? 0.0 : attendanceForm
                                                             .getDayEffectiveMileage()));
                    // 实际工作时长不为0 则实际工作天数加1
                    actualWorkDay++;
                }
                form.setActualWorkDays(actualWorkDay);
                map.put(attendanceForm.getMonitorId(), form);
            } else {
                attendanceForm.setWorkDays(1);
                if (attendanceForm.getActualWorkingDuration() != null
                    && attendanceForm.getActualWorkingDuration() != 0) {
                    // 有工作时长
                    attendanceForm.setActualWorkDays(1);
                } else {
                    attendanceForm.setActualWorkDays(0);
                }
                attendanceForm.setDayEffectiveMileage(
                    attendanceForm.getDayEffectiveMileage() == null ? 0.0 : attendanceForm.getDayEffectiveMileage());
                map.put(attendanceForm.getMonitorId(), attendanceForm);
            }
        }
    }

    /**
     * 平均里程
     * @param attendanceForm
     */
    private void calculationMileage(MileageExport attendanceForm) {
        double averageMileage = 0.0;
        if (attendanceForm.getActualWorkDays() != null && attendanceForm.getActualWorkDays() != 0) {
            averageMileage = attendanceForm.getDayEffectiveMileage() / attendanceForm.getActualWorkDays();
        }
        String averageMileageStr = df.format(averageMileage);
        attendanceForm.setAverageMileage(averageMileageStr);
        attendanceForm.setAverageMileageDouble(averageMileage);
    }

    @Override
    public PageGridBean getAll(AttendanceReportQuery query) throws Exception {
        List<AttendanceForm> result;
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
        result = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(result)) {
            result = new ArrayList<>();
        }
        for (AttendanceForm attendanceForm : result) {
            attendanceForm.setDayStr(format.format(new Date(attendanceForm.getDay() * 1000)));
        }
        List<AttendanceForm> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            //每页条数
            Long pageSize = query.getLength();
            //遍历开始条数
            int fromIndex = query.getStart().intValue();
            //页码
            int pageNum = query.getPage().intValue();
            //总条数
            int totalNum = result.size();
            //遍历结束条数
            long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
            sorted = result.stream().sorted(Comparator.comparing(AttendanceForm::getDayEffectiveMileage).reversed())
                .collect(Collectors.toList()).subList(fromIndex, (int) toIndex);
        }
        Page<AttendanceForm> results = RedisUtil.queryPageList(sorted, query, key);
        return new PageGridBean(query, results, true);
    }

    @Override
    public PageGridBean getDetail(AttendanceReportQuery query) throws Exception {
        Page<AttendanceForm> resultPage = new Page<>();
        try {
            String peopleId = query.getMonitorId();
            List<AttendanceForm> result = new ArrayList<>();
            String userId = SystemHelper.getCurrentUser().getId().toString();
            RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            for (AttendanceForm attendanceForm : allAttendance) {
                if (attendanceForm.getMonitorId().equals(peopleId)) {
                    attendanceForm.setDayStr(format.format(new Date(attendanceForm.getDay() * 1000)));
                    result.add(attendanceForm);
                }
            }

            if (CollectionUtils.isNotEmpty(result)) {
                //每页条数
                Long pageSize = query.getLength();
                //遍历开始条数
                int fromIndex = query.getStart().intValue();
                //页码
                int pageNum = query.getPage().intValue();
                //总条数
                int totalNum = result.size();
                //遍历结束条数
                Long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
                List<AttendanceForm> subList =
                    result.stream().sorted(Comparator.comparing(AttendanceForm::getDayEffectiveMileage).reversed())
                        .collect(Collectors.toList()).subList(fromIndex, toIndex.intValue());
                resultPage = RedisQueryUtil.getListToPage(subList, query, totalNum);
            }
            return new PageGridBean(query, resultPage, true);
        } finally {
            resultPage.close();
        }
    }

    @Override
    public void exportSummary(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(null, MileageExport.class, 1, null);
        List<MileageExport> result = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
        List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(allAttendance)) {
            allAttendance = new ArrayList<>();
        }
        List<MileageExport> mileageObjs = new ArrayList<>();
        for (AttendanceForm form : allAttendance) {
            MileageExport mileageExport = new MileageExport();
            BeanUtils.copyProperties(form, mileageExport);
            mileageObjs.add(mileageExport);
        }
        Map<String, MileageExport> map = new HashMap<>();
        statisticsInfo(mileageObjs, map);
        for (Map.Entry entry : map.entrySet()) {
            MileageExport value = (MileageExport) entry.getValue();
            calculationMileage(value);
            result.add(value);
        }
        List<MileageExport> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            sorted = result.stream().sorted(Comparator.comparing(MileageExport::getAverageMileageDouble).reversed())
                .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public void exportAll(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(null, MileageExportAll.class, 1, null);
        List<AttendanceForm> result;
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
        result = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(result)) {
            result = new ArrayList<>();
        }
        List<MileageExportAll> mileageObjs = new ArrayList<>();
        for (AttendanceForm form : result) {
            MileageExportAll mileageExport = new MileageExportAll();
            BeanUtils.copyProperties(form, mileageExport);
            mileageExport.setDayStr(format.format(new Date(mileageExport.getDay() * 1000)));
            mileageObjs.add(mileageExport);
        }
        List<MileageExportAll> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mileageObjs)) {
            sorted =
                mileageObjs.stream().sorted(Comparator.comparing(MileageExportAll::getDayEffectiveMileage).reversed())
                    .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public void exportDetail(HttpServletResponse response, String id) throws Exception {
        ExportExcel export = new ExportExcel(null, MileageExportAll.class, 1, null);
        List<MileageExportAll> result = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
        List<AttendanceForm> allForm = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(allForm)) {
            allForm = new ArrayList<>();
        }
        for (AttendanceForm form : allForm) {
            MileageExportAll mileageExport = new MileageExportAll();
            BeanUtils.copyProperties(form, mileageExport);
            if (mileageExport.getMonitorId().equals(id)) {
                mileageExport.setDayStr(format.format(new Date(mileageExport.getDay() * 1000)));
                result.add(mileageExport);
            }
        }
        List<MileageExportAll> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            sorted =
                result.stream().sorted(Comparator.comparing(MileageExportAll::getDayEffectiveMileage).reversed())
                    .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public PageGridBean getAllSummary(AttendanceReportQuery query) throws Exception {
        Page<MileageExport> result = new Page<>();
        try {
            String userId = SystemHelper.getCurrentUser().getId().toString();
            RedisKey key = HistoryRedisKeyEnum.SCHEDULED_MILEAGE_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            List<MileageExport> mileageObjs = new ArrayList<>();
            for (AttendanceForm form : allAttendance) {
                MileageExport mileageExport = new MileageExport();
                BeanUtils.copyProperties(form, mileageExport);
                mileageObjs.add(mileageExport);
            }
            Map<String, MileageExport> map = new HashMap<>();
            List<MileageExport> mileages = new ArrayList<>();
            statisticsInfo(mileageObjs, map);
            for (Map.Entry entry : map.entrySet()) {
                MileageExport value = (MileageExport) entry.getValue();
                calculationMileage(value);
                mileages.add(value);
            }
            List<MileageExport> sorted = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(mileages)) {
                sorted =
                    mileages.stream().sorted(Comparator.comparing(MileageExport::getDayEffectiveMileage).reversed())
                        .collect(Collectors.toList());
            }
            result.addAll(sorted);
        } finally {
            return new PageGridBean(query, result, true);

        }
    }
}
