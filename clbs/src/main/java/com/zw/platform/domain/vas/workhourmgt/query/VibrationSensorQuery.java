package com.zw.platform.domain.vas.workhourmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
@Data
@EqualsAndHashCode(callSuper = false)
public class VibrationSensorQuery extends BaseQueryBean {
	/**
     * 振动传感器
     */
    private String id;

    /**
     * 传感器编号
     */
    private String sensorType;

    /**
     * 波特率
     */
    private String baudRate;

    /**
     * 奇偶校验
     */
    private String parity;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;

    /**
     * 滤波系数
     */
    private Integer filterFactor;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    private String remark;
}
