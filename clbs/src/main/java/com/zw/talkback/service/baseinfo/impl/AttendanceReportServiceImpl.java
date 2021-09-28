package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.talkback.domain.basicinfo.AttendanceExportAll;
import com.zw.talkback.domain.basicinfo.form.AttendanceForm;
import com.zw.talkback.domain.basicinfo.form.SchedulingRelationMonitor;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;
import com.zw.talkback.repository.mysql.AttendanceReportMysqlDao;
import com.zw.talkback.service.baseinfo.AttendanceReportService;
import com.zw.talkback.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceReportServiceImpl implements AttendanceReportService {

    @Autowired
    private AttendanceReportMysqlDao attendanceReportMysqlDao;

    @Autowired
    private UserService userService;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");

    private final DecimalFormat df = new DecimalFormat("0.0");

    @Override
    public List<Personnel> findPeopleByScheduled(String id) {
        return attendanceReportMysqlDao.findPeopleByScheduled(id);
    }

    /**
     * 获得排班管理监控对象id
     * @param scheduledInfoId 排班id
     * @return List<String>
     */
    @Override
    public List<SchedulingRelationMonitor> getSchedulingRelationMonitorInfoList(String scheduledInfoId) {
        return attendanceReportMysqlDao.getSchedulingRelationMonitorInfoListById(scheduledInfoId);
    }

    @Override
    public List<AttendanceForm> getList(String monitorIds, String startTime, String endTime, String id)
        throws ParseException {

        String[] ids = monitorIds.split(",");
        long startDate = format.parse(startTime).getTime() / 1000;
        long endDate = format.parse(endTime).getTime() / 1000;
        String startMonth = monthFormat.format(new Date(startDate * 1000));
        String endMonth = monthFormat.format(new Date(endDate * 1000));
        byte[] scheduledId = UuidUtils.getBytesFromUUID(UUID.fromString(id));
        List<byte[]> pids = new ArrayList<>();
        for (String pid : ids) {
            pids.add(UuidUtils.getBytesFromUUID(UUID.fromString(pid)));
        }
        if (startMonth.equals(endMonth)) {
            List<AttendanceForm> list = this.getAttendanceList(pids, startDate, endDate, startMonth, scheduledId);
            for (AttendanceForm form : list) {
                form.setMonitorId(UuidUtils.getUUIDStrFromBytes(form.getMonitorIdByte()));
                form.setScheduledInfoId(UuidUtils.getUUIDStrFromBytes(form.getScheduledInfoIdByte()));
            }
            return list;
        } else {
            // 跨月
            List<AttendanceForm> startList = this.getAttendanceList(pids, startDate, endDate, startMonth, scheduledId);
            List<AttendanceForm> endList = this.getAttendanceList(pids, startDate, endDate, endMonth, scheduledId);
            startList.addAll(endList);
            for (AttendanceForm form : startList) {
                form.setMonitorId(UuidUtils.getUUIDStrFromBytes(form.getMonitorIdByte()));
                form.setScheduledInfoId(UuidUtils.getUUIDStrFromBytes(form.getScheduledInfoIdByte()));
            }
            return startList;
        }
    }

    private List<AttendanceForm> getAttendanceList(List<byte[]> monitorIds, long startDate, long endDate, String month,
                                                   byte[] scheduledId) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("ids", JSON.toJSONString(monitorIds));
        params.put("startDate", String.valueOf(startDate));
        params.put("endDate", String.valueOf(endDate));
        params.put("month", month);
        params.put("id", JSON.toJSONString(scheduledId));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_ATTENDANCE, params);
        return PaasCloudUrlUtil.getResultListData(str, AttendanceForm.class);
    }

    @Override
    public List<SchedulingInfo> getScheduledList() {
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        if (CollectionUtils.isEmpty(orgList)) {
            return null;
        }
        List<SchedulingInfo> scheduledList = attendanceReportMysqlDao.getScheduledList(orgList);
        if (CollectionUtils.isNotEmpty(scheduledList)) {
            long newDate = System.currentTimeMillis();
            for (SchedulingInfo info : scheduledList) {
                // 开始时间过1天
                if (info.getStartDate().getTime() < (newDate - 24 * 60 * 60 * 1000)) {
                    info.setStatus(1);
                } else {
                    info.setStatus(0);
                }
            }
        }
        return scheduledList;
    }

    @Override
    public PageGridBean getSummary(AttendanceReportQuery query) throws Exception {
        Page<AttendanceForm> resultPage = new Page<>();
        try {
            List<AttendanceForm> result = new ArrayList<>();
            String userId = SystemHelper.getCurrentUser().getId().toString();
            final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            Map<String, AttendanceForm> map = new HashMap<>();
            statisticsInfo(allAttendance, map);
            for (Map.Entry entry : map.entrySet()) {
                AttendanceForm value = (AttendanceForm) entry.getValue();
                calculationAttendance(value);
                result.add(value);
            }
            List<AttendanceForm> sorted =
                result.stream().sorted(Comparator.comparing(AttendanceForm::getAttendanceDouble).reversed())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sorted)) {
                //每页条数
                Long pageSize = query.getLength();
                //遍历开始条数
                int fromIndex = query.getStart().intValue();
                //页码
                int pageNum = query.getPage().intValue();
                //总条数
                int totalNum = sorted.size();
                //遍历结束条数
                Long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
                List<AttendanceForm> subList = sorted.subList(fromIndex, toIndex.intValue());
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
    private void statisticsInfo(List<AttendanceForm> allAttendance, Map<String, AttendanceForm> map) {
        for (AttendanceForm attendanceForm : allAttendance) {
            if (map.containsKey(attendanceForm.getMonitorId())) {
                AttendanceForm form = map.get(attendanceForm.getMonitorId());
                // 应工作天数
                Integer workDay = form.getWorkDays();
                form.setWorkDays(workDay + 1);
                // 实际工作天数
                Integer actualWorkDay = form.getActualWorkDays();
                Long nowWorkingDuration = attendanceForm.getActualWorkingDuration();
                if (nowWorkingDuration != null && nowWorkingDuration != 0) {
                    Long actualWorkingDuration =
                        form.getActualWorkingDuration() == null ? 0 : form.getActualWorkingDuration();
                    form.setActualWorkingDuration(actualWorkingDuration + nowWorkingDuration);
                    // 实际工作时长不为0 则实际工作天数加1
                    actualWorkDay++;
                }
                form.setActualWorkDays(actualWorkDay);
                Long shouldWorkDuration = form.getShouldWorkDuration() == null ? 0 : form.getShouldWorkDuration();
                form.setShouldWorkDuration(shouldWorkDuration + attendanceForm.getShouldWorkDuration());
                map.put(attendanceForm.getMonitorId(), form);
            } else {
                attendanceForm.setWorkDays(1);
                //实际工作时长不为空
                if (attendanceForm.getActualWorkingDuration() != null
                    && attendanceForm.getActualWorkingDuration() != 0) {
                    // 有工作时长
                    attendanceForm.setActualWorkDays(1);
                } else {
                    //实际工作天数
                    attendanceForm.setActualWorkDays(0);
                }
                map.put(attendanceForm.getMonitorId(), attendanceForm);
            }
        }
    }

    private void calculationAttendance(AttendanceForm attendanceForm) {
        String shouldWorkDuration = DateUtil.timeConversion2(attendanceForm.getShouldWorkDuration());
        //应工作总时长
        attendanceForm.setShouldWorkDurationStr(shouldWorkDuration);
        String actualWorkingDuration = DateUtil.timeConversion2(attendanceForm.getActualWorkingDuration());
        //实际工作时长
        attendanceForm.setActualWorkingDurationStr(actualWorkingDuration);
        //出勤率
        double attendance =
            (double) DateUtil.stringToLong(actualWorkingDuration) / DateUtil.stringToLong(shouldWorkDuration);
        String attendanceStr = df.format(attendance * 100);
        attendanceForm.setAttendance(attendanceStr);
        attendanceForm.setAttendanceDouble(attendance);
    }

    @Override
    public PageGridBean getAll(AttendanceReportQuery query) throws Exception {
        Page<AttendanceForm> resultPage;
        List<AttendanceForm> result;
        String userId = SystemHelper.getCurrentUser().getId().toString();
        final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
        result = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(result)) {
            result = new ArrayList<>();
        }
        for (AttendanceForm attendanceForm : result) {
            // 计算出勤率
            calculationAttendance(attendanceForm);
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
            sorted = result.stream().sorted(Comparator.comparing(AttendanceForm::getAttendanceDouble).reversed())
                .collect(Collectors.toList()).subList(fromIndex, (int) toIndex);
        }
        resultPage = RedisUtil.queryPageList(sorted, query, key);
        return new PageGridBean(query, resultPage, true);
    }

    @Override
    public PageGridBean getDetail(AttendanceReportQuery query) throws Exception {
        Page<AttendanceForm> resultPage = new Page<>();
        try {
            String peopleId = query.getMonitorId();
            List<AttendanceForm> result = new ArrayList<>();
            String userId = SystemHelper.getCurrentUser().getId().toString();
            final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            for (AttendanceForm attendanceForm : allAttendance) {
                if (attendanceForm.getMonitorId().equals(peopleId)) {
                    // 计算出勤率
                    calculationAttendance(attendanceForm);
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
                List<AttendanceForm> sorted =
                    result.stream().sorted(Comparator.comparing(AttendanceForm::getAttendanceDouble).reversed())
                        .collect(Collectors.toList());
                Long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
                List<AttendanceForm> subList = sorted.subList(fromIndex, toIndex.intValue());
                resultPage = RedisQueryUtil.getListToPage(subList, query, totalNum);
            }
            return new PageGridBean(query, resultPage, true);
        } finally {
            resultPage.close();
        }
    }

    @Override
    public void exportSummary(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(null, AttendanceForm.class, 1);
        List<AttendanceForm> result = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
        List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(allAttendance)) {
            allAttendance = new ArrayList<>();
        }
        Map<String, AttendanceForm> map = new HashMap<>();
        statisticsInfo(allAttendance, map);
        for (Map.Entry entry : map.entrySet()) {
            AttendanceForm value = (AttendanceForm) entry.getValue();
            calculationAttendance(value);
            result.add(value);
        }
        List<AttendanceForm> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            sorted = result.stream().sorted(Comparator.comparing(AttendanceForm::getAttendanceDouble).reversed())
                .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public void exportAll(HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(null, AttendanceExportAll.class, 1);
        List<AttendanceExportAll> result = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
        List<AttendanceForm> allForm = RedisHelper.getListObj(key, 1, -1);
        if (CollectionUtils.isEmpty(allForm)) {
            result = new ArrayList<>();
        }
        for (AttendanceForm form : allForm) {
            AttendanceExportAll mileageExport = new AttendanceExportAll();
            BeanUtils.copyProperties(form, mileageExport);
            result.add(mileageExport);
        }
        for (AttendanceExportAll attendanceForm : result) {
            String shouldWorkDuration = DateUtil.timeConversion2(attendanceForm.getShouldWorkDuration());
            attendanceForm.setShouldWorkDurationStr(shouldWorkDuration);
            String actualWorkingDuration = DateUtil.timeConversion2(attendanceForm.getActualWorkingDuration());
            attendanceForm.setActualWorkingDurationStr(actualWorkingDuration);
            double attendance =
                (double) DateUtil.stringToLong(actualWorkingDuration) / DateUtil.stringToLong(shouldWorkDuration);
            String attendanceStr = df.format(attendance * 100);
            if ("0.0".equals(attendanceStr)) {
                attendanceStr = "0";
            } else if ("100.0".equals(attendanceStr)) {
                attendanceStr = "100";
            }
            attendanceForm.setAttendance(attendanceStr);
            attendanceForm.setAttendanceDouble(attendance);
            String dayStr = format.format(new Date(attendanceForm.getDay() * 1000));
            attendanceForm.setDayStr(dayStr);
        }
        List<AttendanceExportAll> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            sorted = result.stream().sorted(Comparator.comparing(AttendanceExportAll::getAttendanceDouble).reversed())
                .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public void exportDetail(HttpServletResponse response, String id) throws Exception {
        ExportExcel export = new ExportExcel(null, AttendanceExportAll.class, 1, null);
        List<AttendanceExportAll> result = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
        List<AttendanceForm> allForm = RedisHelper.getListObj(key, 1, -1);
        DecimalFormat df = new DecimalFormat("0.0");
        if (CollectionUtils.isEmpty(allForm)) {
            allForm = new ArrayList<>();
        }
        List<AttendanceExportAll> attendanceExportAlls = new ArrayList<>();
        for (AttendanceForm form : allForm) {
            AttendanceExportAll mileageExport = new AttendanceExportAll();
            BeanUtils.copyProperties(form, mileageExport);
            attendanceExportAlls.add(mileageExport);
        }
        for (AttendanceExportAll attendanceForm : attendanceExportAlls) {
            if (attendanceForm.getMonitorId().equals(id)) {
                String shouldWorkDuration = DateUtil.timeConversion2(attendanceForm.getShouldWorkDuration());
                attendanceForm.setShouldWorkDurationStr(shouldWorkDuration);
                String actualWorkingDuration = DateUtil.timeConversion2(attendanceForm.getActualWorkingDuration());
                attendanceForm.setActualWorkingDurationStr(actualWorkingDuration);
                double attendance =
                    (double) DateUtil.stringToLong(actualWorkingDuration) / DateUtil.stringToLong(shouldWorkDuration);
                String attendanceStr = df.format(attendance * 100);
                if ("0.0".equals(attendanceStr)) {
                    attendanceStr = "0";
                } else if ("100.0".equals(attendanceStr)) {
                    attendanceStr = "100";
                }
                attendanceForm.setAttendance(attendanceStr);
                attendanceForm.setAttendanceDouble(attendance);
                String dayStr = format.format(new Date(attendanceForm.getDay() * 1000));
                attendanceForm.setDayStr(dayStr);
                result.add(attendanceForm);
            }
        }
        List<AttendanceExportAll> sorted = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            sorted = result.stream().sorted(Comparator.comparing(AttendanceExportAll::getAttendanceDouble).reversed())
                .collect(Collectors.toList());
        }
        export.setDataList(sorted);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public PageGridBean getAllSummary(AttendanceReportQuery query) throws Exception {
        Page<AttendanceForm> resultPage = new Page<>();
        Page<AttendanceForm> result = new Page<>();
        try {
            String userId = SystemHelper.getCurrentUser().getId().toString();
            final RedisKey key = HistoryRedisKeyEnum.SCHEDULED_REPORT.of(userId);
            List<AttendanceForm> allAttendance = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(allAttendance)) {
                allAttendance = new ArrayList<>();
            }
            Map<String, AttendanceForm> map = new HashMap<>();
            statisticsInfo(allAttendance, map);
            for (Map.Entry entry : map.entrySet()) {
                AttendanceForm value = (AttendanceForm) entry.getValue();
                calculationAttendance(value);
                resultPage.add(value);
            }
            List<AttendanceForm> sorted =
                resultPage.stream().sorted(Comparator.comparing(AttendanceForm::getAttendanceDouble).reversed())
                    .collect(Collectors.toList());
            result.addAll(sorted);
        } finally {
            return new PageGridBean(query, result, true);

        }
    }
}
