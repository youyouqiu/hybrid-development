package com.zw.platform.domain.vas.carbonmgt;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by 王健宇 on 2017/2/21.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MobileSourceManage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String brand;//车牌号
    private String groupName;//车组
    private String vehicleType;//车辆类型
    private String fuelType;//燃油类型
    private String vtime;// gps时间
    private String sumGpsMile;//累计总里程
    private int stageMileage;//阶段里程
    private String sumTotalOilwearOne;//累计总油耗
    private int stageTotalOilwearOne;//阶段里程
    private String airConditionStatus;//空调状态
    private double airConditioningDuration;//空调时长

}
