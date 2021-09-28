package com.cb.platform.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 里程统计信息
 * @author hujun
 * @date 2019/2/21 16:53
 */
@Data
public class MileageStatisticInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] monitorIdHBase; //HBase中的监控对象id
    //传感器里程
    private Double mileage;
    private Integer sensorFlag; //是否绑定里程传感器 0：没有 1：有
    private Long day; //当天0点时间（秒）
    //终端里程
    private Double gpsMile;

}
