package com.zw.app.domain.monitor;

import lombok.Data;

import java.util.List;


/**
 * 监控对象开关历史数据
 */

@Data
public class SwitchSignalInfo {
    /**
     * 时间
     */
    private Long time;

    /**
     * 开关状态
     */
    private List<Integer> statuses;
}
