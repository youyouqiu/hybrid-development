package com.zw.platform.domain.connectionparamsset_809;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/7/23 15:15
 @Description 809上报处理结果参数实体
 @version 1.0
 **/
@Data
public class AlarmHandleParam {

    /**
     * 报警类型
     */
    private Integer alarmType;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 报警开始时间
     */
    private Long alarmStartTime;
    /**
     * 报警处理方式
     */
    private String handleType;
    /**
     * 督办id(督办外的其他地方调用传null)
     */
    private Integer handleId;
    /**
     * 主动安全事件id
     */
    private String eventId;

    /**
     * 主动安全的风险id
     */
    private String riskIds;

    /**
     * 判断是否是联动策略自动处理
     */
    private Integer isAutoDeal = 0;

    /**
     * 处理人，主要用户主动安全处理报警之后，后续的事件复用该字段信息
     */
    private String operator;

    public static AlarmHandleParam getInstance(Integer alarmType, String monitorId, Long alarmStartTime,
        String handleType, Integer handleId, String eventId, String riskIds) {
        AlarmHandleParam alarmHandleParam = new AlarmHandleParam();
        alarmHandleParam.alarmType = alarmType;
        alarmHandleParam.monitorId = monitorId;
        alarmHandleParam.alarmStartTime = alarmStartTime;
        alarmHandleParam.handleType = handleType;
        alarmHandleParam.handleId = handleId;
        alarmHandleParam.eventId = eventId;
        alarmHandleParam.riskIds = riskIds;
        return alarmHandleParam;
    }

    /**
     * 获取主动安全的处理方式
     * @param monitorId
     * @param handleType
     * @param riskIds
     * @return
     */
    public static AlarmHandleParam getInstance(String monitorId, String handleType, String riskIds) {
        AlarmHandleParam alarmHandleParam = new AlarmHandleParam();
        alarmHandleParam.monitorId = monitorId;
        alarmHandleParam.handleType = handleType;
        alarmHandleParam.riskIds = riskIds;
        return alarmHandleParam;
    }
}
