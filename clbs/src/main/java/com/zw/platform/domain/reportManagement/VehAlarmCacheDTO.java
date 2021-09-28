package com.zw.platform.domain.reportManagement;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:车辆报警处理以及下发短信缓存信息
 * @Date: create in 2020/11/16 11:25
 */
@Data
public class VehAlarmCacheDTO {
    /**
     * 报警总数
     */
    private Long total;

    /**
     * 局部推送的报警数量
     */
    private Long part;

    /**
     * 全局推送的报警数量
     */
    private Long global;

    /**
     * 已经处理的报警总数
     */
    private Long processed;

    /**
     * 下发短信，处理的报警数量
     */
    private Long sendMsm;
}
