package com.zw.ws.entity;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/7 17:56
 */
@Data
public class OutputControlSettingDO {
    /**
     * 监控对象id
     */
    private String vehicleId;
    /**
     * 外设id 144:自带IO; 145:外部控制器91; 146:外部控制器92
     */
    private Integer peripheralId;
    /**
     * 控制时长
     */
    private Integer controlTime;

    /**
     * 输出口
     */
    private Integer outletSet;

    /**
     * 控制类型 1:IO控制; 2:断油电;
     */
    private Integer controlSubtype;

    /**
     * 控制状态 0:断开; 1:闭合;
     */
    private Integer controlStatus;

    /**
     * 模拟量输出比例 websocket下发8500没有模拟量控制设置功能
     * 此字段展示无用
     */
    private Float analogOutputRatio;
}
