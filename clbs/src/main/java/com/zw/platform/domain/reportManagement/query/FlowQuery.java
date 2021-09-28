package com.zw.platform.domain.reportManagement.query;


import com.zw.platform.util.common.BaseQueryBean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@EqualsAndHashCode(callSuper = false)
public class FlowQuery extends BaseQueryBean {
    @Getter
    @Setter
    private String vehicleIds;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String endTime;

    @Getter
    @Setter
    private String simcardNumber;

    @Getter
    @Setter
    private String plateNumber;

}
