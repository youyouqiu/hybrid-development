package com.zw.platform.domain.reportManagement.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhangsq
 * @date 2018/3/23 10:16
 */
@EqualsAndHashCode(callSuper = false)
public class RidershipQuery {

    @Getter
    @Setter
    private String vehicleIdStr;

    @Getter
    @Setter
    private List<String> vehicleIds;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String endTime;

    @Getter
    @Setter
    private String plateNumber;
}
