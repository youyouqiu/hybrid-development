package com.zw.lkyw.domain.trackback;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class MileageDetailDO implements Serializable {

    private Long day;
    private Double gpsMile;
    private Double mileage;
    private String objectType = "其他车辆";
    private Integer sensorFlag;
}
