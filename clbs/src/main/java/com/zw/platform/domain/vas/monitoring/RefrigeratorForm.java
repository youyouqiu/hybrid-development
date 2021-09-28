package com.zw.platform.domain.vas.monitoring;

import lombok.Data;


/**
 * 冷柜温湿度实体
 */
@Data
public class RefrigeratorForm {

    private String vehcileId;//车辆id

    private String plateNumber;//车牌号

    private long vTime;//位置数据更新时间

    private Double tempValueOne;//一号温度传感器温度值

    private Double tempValueTwo;//二号温度传感器温度值

    private Double tempValueThree;//三号温度传感器温度值

    private Double tempValueFour;//四号温度传感器温度值

    private Double tempValueFive;//五号温度传感器温度值

    private Double wetnessValueOne;//一号湿度传感器温度值

    private Double wetnessValueTwo;//二号湿度传感器温度值

    private Double wetnessValueThree;//三号湿度传感器温度值

    private Double wetnessValueFour;//四号湿度传感器温度值

}
