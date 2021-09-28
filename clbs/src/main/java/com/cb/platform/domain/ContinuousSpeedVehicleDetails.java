package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 持续超速车辆明细报表实体
 * @author hujun
 * @Date 创建时间：2018年4月27日 上午10:33:03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContinuousSpeedVehicleDetails implements Serializable {
    private static final long serialVersionUID = -7378517889418818407L;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 所属道路运输企业
     */
    private String groupName;

    /**
     * 报警开始时间
     */
    private String alarmStartTime;

    /**
     * 报警结束时间
     */
    private String alarmEndTime;

    /**
     * 最高速度
     */
    private Double maxSpeed;

    /**
     * 持续时间
     */
    private Integer speedTime;

    /**
     * 报警开始位置
     */
    private String alarmStartLocation;

    /**
     * 报警结束位置
     */
    private String alarmEndLocation;
}
