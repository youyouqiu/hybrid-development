package com.zw.app.domain.alarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;


@Data
public class AppAlarmQuery {
    /**
     * 报警类型
     */
    private String alarmType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页数量
     */
    private int pageSize;

    /**
     * 数据行号
     */
    private int lineNumber;

    /**
     * 模糊搜索参数
     */
    private String fuzzyParam;

    /**
     * 报警类型
     */
    private List<Integer> alarmCode;

    private Long alarmStartTime;

    private Long alarmEndTime;

    /**
     * 多个监控对象id
     */
    private List<byte[]> monitorIds;

    /**
     * 单个监控对象id
     */
    private byte[] vehicleId;

    private String queryParam;

    //  移动端唯一标识
    private String uniquenessFlag;
}
