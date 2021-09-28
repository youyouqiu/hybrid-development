package com.zw.platform.domain.oil;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/10/14.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkHours implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; //主键
    private String vehicleId; //车辆ID
    private String assignmentId;//车队ID
    private String team;//车队名称
    private String brand;//车牌号
    private String longtitude;//经度
    private String latitude;//纬度
    private String rate;//频率
    private String duration;//持续时间
    private String status;//发动机状态
    private String vtime;//时间
    private String position;//位置中文信息
}
