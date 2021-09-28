package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/1 14:58
 */
@Data
public class SerialPort implements Serializable {
    private static final long serialVersionUID = -3291858279107456562L;

    /**
     * 序号
     */
    private Integer sum;
    /**
     * 波特率
     */
    private Integer baudRate;
    /**
     * 数据位
     */
    private Integer dataPosition;
    /**
     * 停止位
     */
    private Integer stopPosition;
    /**
     * 校验位
     */
    private Integer checkPosition;
    /**
     * 流控
     */
    private Integer flowControl;
    /**
     * 接收超时
     */
    private Integer receiveTimeOut;
}
