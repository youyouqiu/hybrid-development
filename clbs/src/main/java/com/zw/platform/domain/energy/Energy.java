package com.zw.platform.domain.energy;


import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年11月14日 上午9:33:56
* 类说明:
*/
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class Energy extends BaseFormBean implements Serializable {

    private String vehicleId;//车辆id

    private Double travelMile;//总行驶里程

    private Double travelTotal;//总行驶油耗量

    private Double travelBase;//行驶油耗基准(可输入）

    private Double travelBaseList;//行驶能耗基准(基准计算的)

    private Double avgSpeed;//平均速度

    private Double travelTotalCap;//行驶CO2总排放量

    private Double travelBaseCap;//行驶CO2排放基准

    private Integer idleTime;//总怠速时长(s)

    private Double idleTotal;//总怠速油耗量

    private Double idleBase;//怠速油耗基准(可输入）

    private Double idleBaseList;//怠速油耗基准(基准计算的)

    private Double idleMile;//慢速里程

    private Double idleTotalCap;//怠速CO2总排放量

    private Double idleBaseCap;//怠速CO2排放基准

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date installTime;//节油产品安装日期

    private Integer idleThreshold;//怠速阈值
    
}
