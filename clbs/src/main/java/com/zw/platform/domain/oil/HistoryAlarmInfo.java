package com.zw.platform.domain.oil;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/10/12.
 */
@Data
public class HistoryAlarmInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private String id;

    private String vtime;// gps时间

    private String alarm;

    private String longtitude;

    private String latitude;

    private String description;

    private String handle_time;

    private String handle_type;

    private String status;

    private String person_name;
}
