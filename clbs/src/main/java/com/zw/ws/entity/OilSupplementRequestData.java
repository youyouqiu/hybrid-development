package com.zw.ws.entity;

import com.zw.protocol.msg.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/25 16:52
 */
@Data
@NoArgsConstructor
public class OilSupplementRequestData implements Serializable {
    private static final long serialVersionUID = 4024913058942581091L;
    /**
     * 成功,即刻补发
     */
    public static final Integer REQUEST_RESULT_SUCCESS = 0x00;
    /**
     * 成功,重新请求
     */
    public static final Integer REQUEST_RESULT_SUCCESS_RE_REQUEST = 0x01;
    /**
     * 失败
     */
    public static final Integer REQUEST_RESULT_FAIL = 0x02;
    /**
     * 0x03~0xFF：其他原因
     */
    public static final Integer REQUEST_RESULT_FAIL_OTHER = 0x03;

    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 车辆编码
     */
    private  String vehicleCode;
    /**
     * 监控对象名称
     */
    private String brand;
    /**
     * 车牌颜色
     */
    private Integer plateColor;
    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;
    /**
     * sessionId
     */
    private String sessionId;
    /**
     * 发送次数
     */
    private Integer sendNumber;
    /**
     * 发送的消息
     */
    private Message message;

    public void addSendNumber() {
        this.sendNumber += 1;
    }
}
