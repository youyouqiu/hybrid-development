package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/1 15:14
 */
@Data
public class SerialPortSettingItem implements Serializable {
    private static final long serialVersionUID = 1606242778310119003L;

    /**
     * 外设ID 0xF901(RS232串口参数)   F902(RS485串口参数)  F903(CAN总线参数)
     */
    private Integer id;
    /**
     * 消息长度
     */
    private Integer len;
    /**
     * 串口个数
     */
    private Integer number;
    /**
     * 特殊参数设置实体
     */
    private List<SerialPort> serialPort;
}
