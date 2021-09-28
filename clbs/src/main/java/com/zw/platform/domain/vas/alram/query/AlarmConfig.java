package com.zw.platform.domain.vas.alram.query;

import lombok.Data;

/**
 * 报警配置
 *
 * @author  Tdz
 * @create 2017-09-25 15:32
 **/
@Data
public class AlarmConfig {

    private String vehicleId;

    /**
     * 报警推送（0、无 1、局部 2、全局）
     */
    private Integer alarmPush;

    private String pos;
}
