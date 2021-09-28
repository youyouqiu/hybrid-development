package com.cb.platform.domain;


import lombok.Data;


@Data
public class EnterpriseDispatch {

    // 车牌号
    private String brand;

    // 车牌颜色
    private String color;

    // 车牌颜色
    private String vehicleType;

    private String groupName;

    private String [] dateTime;

    private String  times;

    private String  month;

    private int count;

}
