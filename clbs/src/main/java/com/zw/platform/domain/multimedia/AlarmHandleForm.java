package com.zw.platform.domain.multimedia;

import com.zw.adas.domain.riskManagement.AdasInstantEnum;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

@Data
public class AlarmHandleForm {

    //    private String vehicleId;
    private byte[] vehicleId;

    private String plateNumber;

    private int alarmType;

    private long startTime;

    private long handleTime;

    private String personName;

    private String personId;

    private String handleType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 报警类型描述
     */
    private String description;

    /**
     * 809新增处理人的所属企业名称
     */
    private String personGroupName;

    /**
     * 主干4.1.1新增报表处理短信内容
     */
    private String dealOfMsg;

    /**
     * 主动安全督办新增有效应答时间（毫秒值）
     */
    private Long responseTime;

    /**
     * 督办结果
     */
    private Integer overseeResult;

    public static AlarmHandleForm getDealForm(AdasRiskEsBean reb, String user, String dealer, Long handleTime) {
        AlarmHandleForm alarmHandleForm = new AlarmHandleForm();
        alarmHandleForm.vehicleId = UuidUtils.getBytesFromStr(reb.getVehicleId());
        alarmHandleForm.setAlarmType(AdasInstantEnum.ADAS_ALARM_TYPE);
        alarmHandleForm.setStartTime(reb.getWarningTime().getTime());
        //处理时间需要除以1000
        alarmHandleForm.setHandleTime(handleTime / 1000);
        alarmHandleForm.setPersonName(dealer);
        alarmHandleForm.setPersonId(user);
        //有效应答时间，在进行adas报警处理需要更新该字段
        alarmHandleForm.setResponseTime(handleTime);
        Long overseeDeadTime = reb.getOverseeDeadTime();
        setOverseeResult(handleTime, alarmHandleForm, overseeDeadTime);

        return alarmHandleForm;
    }

    private static void setOverseeResult(Long handleTime, AlarmHandleForm alarmHandleForm, Long overseeDeadTime) {
        if (overseeDeadTime == null) {
            return;
        }
        if (overseeDeadTime >= handleTime) {
            alarmHandleForm.overseeResult = AdasInstantEnum.OVERSEE_RESULT_REPLY;
        } else {
            alarmHandleForm.overseeResult = AdasInstantEnum.OVERSEE_RESULT_OUT_DATE;
        }
    }


}
