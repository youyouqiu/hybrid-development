package com.zw.adas.domain.driverScore.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/15 11:19
 @Description 弹出框司机报警信息详情列表
 @version 1.0
 **/
@Data
public class AdasDriverScoreEventShow {
    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 报警事件
     */
    private String event;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 报警时间
     */
    private String eventTime;


    /**
     * 车辆速度
     */
    private Double speed;

    /**
     * 报警位置
     */
    private String address;

    private int number;
}
