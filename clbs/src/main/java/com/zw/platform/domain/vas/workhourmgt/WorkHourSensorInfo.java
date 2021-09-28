package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工时传感器实体
 * @author denghuabing
 * @date 2018.5.29
 * @version 1.0
 */
@Data
public class WorkHourSensorInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 传感器id
     */
    private String id;

    /**
     * 传感器型号
     */
    private String sensorNumber;

    /**
     * 检测方式(1:电压比较式;2:油耗流量计式)
     */
    private Integer detectionMode;

    /**
     * 波特率
     */
    private Integer baudRate;

    /**
     * 奇偶校验（1：奇校验；2：偶校验；3：无校验）
     */
    private Integer oddEvenCheck;

    /**
     * 补偿使能（1:使能,2:禁用）
     */
    private Integer compensate;

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    private Integer filterFactor;

    /**
     * 自动上传时间
     */
    private Integer autoTime;

    /**
     * 1温度传感器  2湿度传感器  3正反转传感器 4工时传感器
     */
    private Integer sensorType;

    /**
     * 备注
     */
    private String remark;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}
