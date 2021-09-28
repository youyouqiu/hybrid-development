package com.zw.platform.domain.reportManagement;

import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 809报警转发中间表
 */
@Data
public class T809AlarmForwardInfoMiddleQuery implements Serializable {
    /**
     * 报警时间
     */
    private Long time;

    /**
     * 监控对象id
     */
    private byte[] monitorId;

    /**
     * 监控对象id
     */
    private String monitorIdStr;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 转发平台id
     */
    private byte[] platId;

    /**
     * 转发平台id
     */
    private String platIdStr;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 流水号
     */
    private Integer msgSn;

    /**
     * 源子业务类型
     */
    private Integer msgId;

    /**
     * 报警开始时间
     */
    private Long alarmStartTime;

    /**
     * 协议类型
     */

    private Integer protocolType;

    /**
     * 主动安全风险事件id
     */
    private String riskEventId;

    /**
     * 报警类型集合
     */
    private Set<Integer> alarmTypes;

    /**
     * 报警时间的集合
     */
    private Set<Long> times;

    /**
     * 桂标809需要的报警标识
     */
    private String alarmId;

    private Long startTime;

    private Long endTime;

    /**
     * 获取809转发中间表查询对象
     */
    public static T809AlarmForwardInfoMiddleQuery getInstance(Integer alarmType, String monitorId, Long alarmStartTime,
        PlantParam param, Long startTime, Long endTime) {
        T809AlarmForwardInfoMiddleQuery queryParam = new T809AlarmForwardInfoMiddleQuery();
        queryParam.monitorId = UuidUtils.getBytesFromStr(monitorId);
        queryParam.monitorIdStr = monitorId;
        queryParam.alarmType = alarmType;
        queryParam.time = alarmStartTime;
        queryParam.platId = UuidUtils.getBytesFromStr(param.getId());
        queryParam.platIdStr = param.getId();
        queryParam.startTime = startTime;
        queryParam.endTime = endTime;
        queryParam.protocolType = param.getProtocolType();
        return queryParam;
    }
}
