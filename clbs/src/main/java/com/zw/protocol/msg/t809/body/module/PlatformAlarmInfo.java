package com.zw.protocol.msg.t809.body.module;

import lombok.Data;

import java.io.Serializable;

/**
 * 功能描述: 平台督办应答实体
 * @author zhengjc
 * @date 2019/7/23
 * @time 19:47
 */
@Data
public class PlatformAlarmInfo implements Serializable {
    private String monitorId;

    private String brand;

    /**
     * 子业务类型
     */
    private String objectType;

    /**
     * 上级平台ip
     */
    private String serverIp;

    /**
     * 接入码
     */
    private Integer msgGNSSCenterId;

    /**
     * 处理结果
     */
    private Integer alarmHandle;

    /**
     * 报警时间
     */
    private Long warnTime;

    /**
     * 上级平台消息处理表记录id
     */
    private String alarmMsgId;

    /**
     * 报警编号
     */
    private Integer alarmType;

    /**
     * 请求消息报文序列号
     */
    private Integer msgSn;

    /**
     * 源子业务类型标识
     */
    private Integer sourceDataType;

    /**
     * 源报文序列号
     */
    private Integer sourceMsgSn;

    /**
     * 报警开始时间
     */
    private Long alarmStartTime;

    /**
     * 车牌颜色
     */
    private Integer vehicleColor;

    /**
     * 转发平台id
     */
    private String plateFormId;

    /**
     * 川冀标主动安全的报警事件id
     */
    private String eventId;
}
