package com.zw.platform.dto.alarm;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 10:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AlarmPageReq extends BaseQueryBean {
    private static final long serialVersionUID = 3526772542227784715L;
    /**
     * 报警来源 -1:全部; 0:终端报警; 1:平台报警;
     */
    Integer alarmSource;
    /**
     * 报警类型 逗号分隔
     */
    String alarmTypes;
    /**
     * 处理状态 -1:全部; 0:未处理; 1:已处理;
     */
    Integer status;
    /**
     * 报警开始时间(yyyy-MM-dd HH:mm:ss)
     */
    String alarmStartTime;
    /**
     * 报警结束时间(yyyy-MM-dd HH:mm:ss)
     */
    String alarmEndTime;
    /**
     * 车辆id 逗号分隔
     */
    String vehicleIds;

    public OfflineExportInfo getAlarmRecordOffLineExport() {
        //默认名称
        String fileName = "报警查询" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportDetailParam(), OffLineExportBusinessId.ALARM_RECORD_LIST);
    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance("报警查询报表", fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;
    }

    public TreeMap<String, String> getExportDetailParam() {
        TreeMap<String, String> queryParam = new TreeMap<>();
        queryParam.put("monitorIds", vehicleIds);
        queryParam.put("alarmTypes", alarmTypes);
        queryParam.put("startTime", alarmStartTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", alarmEndTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        if (status != null && status != -1) {
            queryParam.put("status", String.valueOf(status));
        }
        if (alarmSource != null && alarmSource != -1) {
            queryParam.put("alarmSource", String.valueOf(alarmSource));
        }
        queryParam.put("flag", UUID.randomUUID().toString());
        return queryParam;
    }

}
