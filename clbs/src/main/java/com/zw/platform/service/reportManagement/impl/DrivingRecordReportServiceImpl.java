package com.zw.platform.service.reportManagement.impl;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.customenum.RecordCollectionEnum;
import com.zw.platform.domain.reportManagement.DrivingRecordInfo;
import com.zw.platform.domain.reportManagement.query.DrivingRecordInfoQuery;
import com.zw.platform.repository.modules.DrivingRecordReportDao;
import com.zw.platform.service.reportManagement.DrivingRecordReportService;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrivingRecordReportServiceImpl implements DrivingRecordReportService {

    private static final String SYMBOL_COMMASY = ",";

    private static final int MINUTE_OF_30_MILLISECOND = 1800000;

    @Autowired
    private DrivingRecordReportDao drivingRecordReportDao;

    @Override
    public void addDrivingRecordInfo(String monitorId, String cw, int msgAck) throws Exception {
        if (StringUtils.isBlank(monitorId)) {
            return;
        }
        if (StringUtils.isBlank(cw)) {
            return;
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(monitorId);
        if (bindDTO == null) {
            return;
        }
        String monitorName = bindDTO.getName();
        String groupName = bindDTO.getOrgName();
        DrivingRecordInfo driving = new DrivingRecordInfo();
        driving.setMonitorId(monitorId);
        driving.setMonitorName(monitorName);
        driving.setGroupName(groupName);
        driving.setCollectionCommand(cw); // 命令字
        driving.setCollectionCommandDescribe(RecordCollectionEnum.getSignContentBy(cw)); // 命令字描述
        driving.setMsgSNAck(msgAck); // 流水号
        String userName = SystemHelper.getCurrentUsername();
        driving.setCreateDataUsername(userName); // 创建人
        driving.setCreateDataTime(new Date());
        driving.setUpdateDataUsername(userName);
        drivingRecordReportDao.addDrivingRecordInfo(driving);
    }

    @Override
    public void updateDrivingRecordInfoByMonitorId(DrivingRecordInfoQuery query) throws Exception {
        if (query == null) {
            return;
        }
        // 根据监控对象id,流水号,采集命令查询半小时内的下发记录
        // 当前时间
        Date nowTime = new Date();
        // 半个小时前的时间
        Date beforeTime = new Date(nowTime.getTime() - MINUTE_OF_30_MILLISECOND);
        query.setMaxDate(nowTime);
        query.setMinDate(beforeTime);
        List<DrivingRecordInfo> info = drivingRecordReportDao.getDrivingRecordByMsgSNAck(query);
        if (info != null && info.size() > 0) {
            // 对比出离当前时间最近的记录
            DrivingRecordInfo result = info.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(DrivingRecordInfo::getCreateDataTime).reversed()))
                .collect(Collectors.toList()).get(0);
            result.setUpdateDataTime(new Date());
            result.setMessage(query.getMessage());
            drivingRecordReportDao.updateDrivingRecordInfo(result);
        }
    }

    @Override
    public List<DrivingRecordInfo> getDrivingRecordCollection(String monitorId, String queryStartTime,
        String queryEndTime) throws Exception {
        List<DrivingRecordInfo> result = new ArrayList<>();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(queryStartTime) || StringUtils
            .isBlank(queryEndTime)) {
            return result;
        }
        RedisKey key = HistoryRedisKeyEnum.DRIVING_RECORD_LIST.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(key)) {
            RedisHelper.delete(key);
        }
        List<String> monitorIds = Arrays.asList(monitorId.split(SYMBOL_COMMASY));
        result = drivingRecordReportDao.getDrivingRecordDataByMonitionId(monitorIds, queryStartTime, queryEndTime);
        if (CollectionUtils.isNotEmpty(result)) {
            RedisHelper.addToList(key, result);
            RedisHelper.expireKey(key, 60 * 60);
        }
        return result;
    }

    private String getDateStrByDate(Date date) throws Exception {
        String dataStr = "";
        if (date == null) {
            return dataStr;
        }
        dataStr = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
        return dataStr;
    }

    @Override
    public void exportDrivingRecord(HttpServletResponse response, String fuzzyParam) throws Exception {
        ExportExcel export = new ExportExcel(null, DrivingRecordInfo.class, 1, null);
        RedisKey key = HistoryRedisKeyEnum.DRIVING_RECORD_LIST.of(SystemHelper.getCurrentUsername());
        List<DrivingRecordInfo> allExportList = RedisHelper.getList(key, DrivingRecordInfo.class);
        List<DrivingRecordInfo> exportResult = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            if (StringUtils.isNotBlank(fuzzyParam)) {
                fuzzyParam = fuzzyParam.toUpperCase();
            }
            for (DrivingRecordInfo report : allExportList) {
                String brand = report.getMonitorName();
                if (StringUtils.isBlank(brand)) {
                    continue;
                }
                if (StringUtils.isNotBlank(fuzzyParam) && !brand.toUpperCase().contains(fuzzyParam)) {
                    continue;
                }
                String startDateStr = getDateStrByDate(report.getCreateDataTime());
                String endDateStr = getDateStrByDate(report.getUpdateDataTime());
                report.setCreateDataTimeStr(startDateStr);
                report.setUpdateDataTimeStr(endDateStr);
                exportResult.add(report);
            }
        }
        export.setDataList(exportResult);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }
}