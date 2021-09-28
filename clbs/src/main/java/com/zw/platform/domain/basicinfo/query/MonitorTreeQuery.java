package com.zw.platform.domain.basicinfo.query;

import lombok.Data;


/***
 @Author zhengjc
 @Date 2019/5/24 16:49
 @Description 实时监相关组织控树查询实体
 @version 1.0
 **/
@Data
public class MonitorTreeQuery {

    /**
     * monitor：按照监控对象查询 assignment:按照分组名称查询 vehType 按照车辆类型查询
     */
    private String queryType;

    /**
     * 1在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线 10所有车辆，离线、在线车辆
     */
    private int type;

    /**
     * 1:实时监控界面，2:实时视频界面
     */
    private int webType;

    /**
     * 搜索框参数
     */
    private String queryParam;

    /**
     * 终端的类型
     */
    private String devType;

}
