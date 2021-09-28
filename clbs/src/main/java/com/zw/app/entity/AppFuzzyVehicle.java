package com.zw.app.entity;

import lombok.Data;

/***
 @Author lijie
 @Date 2020/4/8 9:36
 @Description app模糊查询车辆接口
 @version 1.0
 **/
@Data
public class AppFuzzyVehicle extends BaseEntity {

    //app搜索监控对象的搜索条件
    private String fuzzyParam;

    //需要显示的车的id
    private String vehicleIds;
}


