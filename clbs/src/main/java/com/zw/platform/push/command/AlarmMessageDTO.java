package com.zw.platform.push.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 联动策略 - 报警实体
 * @author create by zhouzongbo on 2020/9/27.
 */
@Data
public class AlarmMessageDTO implements Serializable {

    private static final long serialVersionUID = -5162436895749631667L;

    /**
     * 实时视频
     */
    public static final int REALTIME_VIDEO_MSG_TYPE = 1;
    /**
     * 资源列表
     */
    public static final int RESOURCE_LIST_MSG_TYPE = 2;

    /**
     * 报警处理结果
     */
    public static final int ALARM_HANDLE_RESULT_MSG_TYPE = 3;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警开始时间
     */
    private Long startAlarmTime;

    /**
     *  1 实时视频 2资源列表 3报警结果处理
     */
    private Integer msgType;
}
