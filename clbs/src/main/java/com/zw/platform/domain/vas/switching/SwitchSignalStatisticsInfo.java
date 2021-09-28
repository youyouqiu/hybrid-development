package com.zw.platform.domain.vas.switching;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/9/6 16:35
 */
@Data
public class SwitchSignalStatisticsInfo implements Serializable {

    private static final long serialVersionUID = -1536540840617247735L;

    /**
     * 监控对象id
     */
    private String vehicleId;
    /**
     * 监控对象名称
     */
    private String brand;

    /**
     * gps时间
     */
    private long vtime = 0L;
    private String vtimeStr;

    /**
     * 经度
     */
    private String longtitude; 

    /**
     * 纬度
     */
    private String latitude; 

    /**
     * 速度
     */
    private String speed = "0"; 

    /**里程
     * 
     */
    private String gpsMile = "0"; 

    /**
     * IO口 1、2、3、4
     */
    private Integer ioOne;

    private Integer ioTwo;

    private Integer ioThree;

    private Integer ioFour;

    /**
     * 采集板1
     */
    private String ioObjOne;
    /**
     * 采集板2
     */
    private String ioObjTwo;


}
