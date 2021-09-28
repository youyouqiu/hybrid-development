package com.zw.platform.domain.basicinfo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/7/25.
 */
@Data
public class RodSensor implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 油杆传感器信息
     */
    private String id;

    /**
     * 传感器编号
     */
    private String sensorNumber;

    /**
     * 设备厂商
     */
    private String manuFacturer;

    /**
     * 启停状态
     */
    private Short isStart;

    /**
     * 出厂时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date factoryDate;

    /**
     * 量程
     */
    private String measuringRange;
    
    /**
     * 上盲区-add by liubq 2016-11-16
     */
    private String upperBlindZone = "";
    
    /**
     * 下盲区-add by liubq 2016-11-16
     */
    private String lowerBlindArea = "";

    /**
     * 标定数量
     */
    private String calibrationNumber;

    /**
     * 系数K
     */
    private String factorK;

    /**
     * 系数B
     */
    private String factorB;

    /**
     * 补偿使能
     */
    private Short compensationCanMake;

    /**
     * 波特率
     */
    private String baudRate;

    /**
     * 奇偶效验
     */
    private Short oddEvenCheck;

    /**
     * 上传间隔
     */
    private Short uploadInterval;

    /**
     * 描述
     */
    private String description;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    private String sensorLength;
    
    private String filteringFactor;
    
    private String remark;
}
