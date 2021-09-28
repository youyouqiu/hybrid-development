package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>Title: 振动传感器与车的绑定表实体</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年9月19日上午9:13:36
 * @version 1.0
 */
@Data
public class VibrationSensorBind implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
     * 震动传感器与车辆关联
     */
    private String id;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 震动传感器ID
     */
    private String shockSensorId;

    /**
     * 每秒采集个数
     */
    private Integer collectNumber;

    /**
     * 上传个数
     */
    private Integer uploadNumber;

    /**
     * 自动上传时间
     */
    private String uploadTime;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 停机频率阈值
     */
    private String outageFrequencyThreshold;

    /**
     * 怠速频率阈值
     */
    private String idleFrequencyThreshold;

    /**
     * 持续停机时间阈值
     */
    private String continueOutageTimeThreshold;

    /**
     * 持续怠速时间阈值
     */
    private String continueIdleTimeThreshold;

    /**
     * 报警频率阈值
     */
    private String alarmFrequencyThreshold;

    /**
     * 工作频率阈值
     */
    private String workFrequencyThreshold;

    /**
     * 持续报警时间阈值
     */
    private String continueAlarmTimeThreshold;

    /**
     * 持续工作时间阈值
     */
    private String continueWorkTimeThreshold;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private Integer monitorType;
    /**
     *  传感器型号
     */
    private String sensorType;

    /**
     * 传感器厂商
     */
    private String manufacturers;
	
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

    private Integer status; // 下发状态
    
    private String brand; // 车牌号
    
    private String groups; // 组织
    
    private String vehicleType; // 车辆类型
    
    /**
     * 车辆id
     */
    private String vId;
    
    /**
     * 下发参数id
     */
    private String paramId;
    
    private String transmissionParamId; // 通讯参数下发id
    
}
