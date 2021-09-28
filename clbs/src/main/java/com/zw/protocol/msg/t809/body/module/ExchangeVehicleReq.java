package com.zw.protocol.msg.t809.body.module;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 车辆交换信息时间
 * Created by LiaoYuecai on 2017/2/13.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExchangeVehicleReq extends MainModule {
    private static final long serialVersionUID = -4254875468229247318L;
    private Long startTime;
    private Long endTime;

    public ExchangeVehicleReq(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
