package com.zw.app.domain.alarm;

import lombok.Data;

import java.io.Serializable;


@Data
public class AppAlarmInfo implements Serializable {

    /**
     * 监控对象id
     */
    private String id;


    /**
     *监控对象类型
     */
    private String type;

    /**
     * 车牌号
     */
    private String name;

    /**
     * 报警时间
     */
    private Long time;

    /**
     * 报警开始位置(具体地址)
     */
    private String address;
}
