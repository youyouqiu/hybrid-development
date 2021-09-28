package com.zw.platform.domain.vas.oilmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>Title: 流量传感器实体</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年9月19日上午9:13:36
 * @version 1.0
 */
@Data
public class FluxSensor implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
     * 油耗传感器
     */
    private String id;

    /**
     * 传感器编号
     */
    private String oilWearNumber;

    /**
     * 外设ID
     */
    private Integer deviceNumber;

    /**
     * 参数长度
     */
    private String parameterLength;

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

    /**
     * 量程
     */
    private Integer ranges;

    /**
     * 燃料选择
     */
    private Integer fuelSelect;

    /**
     * 测量方案
     */
    private Integer meteringSchemes;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    private String remark;
    
    private String vehicleId;

}
