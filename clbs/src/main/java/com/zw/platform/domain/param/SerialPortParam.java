package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * RS232串口参数;RS485串口参数;CAN总线参数;
 * @author penghj
 * @version 1.0
 * @date 2019/1/17 17:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SerialPortParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = -6385363054782786937L;
    private String vid;
    /**
     * 串口序号
     */
    private Integer serialPortNumber;
    /**
     * 波特率; 1:2400; 2:4800; 3:9600; 4:19200; 5:38400; 6:57600; 7:115200; -1:不修改
     */
    private Integer baudRate;
    /**
     * 数据位; 5、6、7、8; -1:不修改
     */
    private Integer dataBits;
    /**
     * 停止位; 1、2; -1:不修改
     */
    private Integer stopBit;
    /**
     * 校验位; 1:奇校验; 2:偶校验; 3:无校验; -1:不修改
     */
    private Integer parityBit;
    /**
     * 流控;  1:无流控; 2:硬件流控; 3:软件流控; -1:不修改
     */
    private Integer flowControl;
    /**
     * 数据接收超时(毫秒)
     */
    private Integer dataAcceptanceTimeoutTime;
    /**
     * 串口类型; 1:RS232串口参数; 2:RS485串口参数; 3:CAN总线参数
     */
    private Integer type;
}
