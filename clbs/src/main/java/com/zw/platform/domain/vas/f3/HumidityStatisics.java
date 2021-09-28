package com.zw.platform.domain.vas.f3;

import lombok.Data;

/**
 * 湿度统计实体
 * Created by Administrator on 2017/7/17.
 */
@Data
public class HumidityStatisics {
    //车辆id
    private String monitorId;
    //车牌号
    private String monitorName;

    private double gpsMile;//行驶里程

    private double speed;//速度

    private String address;//位置

    private long vTime;//位置数据更新时间

    private String longtitude;//经度

    private String latitude;//纬度

    private String positionCoordinates;//位置坐标(纬度,经度)

    private Double wetnessValueOne;//一号湿度传感器温度值

    private Double wetnessValueTwo;//二号湿度传感器温度值

    private Double wetnessValueThree;//三号湿度传感器温度值

    private Double wetnessValueFour;//四号湿度传感器温度值

    private Integer wetnessHighLowOne; //一号湿度传感器高低湿度报警

    private Integer wetnessHighLowTwo; //二号湿度传感器高低湿度报警

    private Integer wetnessHighLowThree; //三号湿度传感器高低湿度报警

    private Integer wetnessHighLowFour; //四号湿度传感器高低湿度报警

}
