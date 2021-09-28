package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@EqualsAndHashCode(callSuper = false)
public class VehOnlineStatusQuery extends BaseQueryBean {
    @Getter
    @Setter
    private String vehicleIds;

    @Getter
    @Setter
    private Long startTime;

    @Getter
    @Setter
    private Long endTime;

    @Getter
    @Setter
    private String startTimeStr;

    @Getter
    @Setter
    private String endTimeStr;

    @Getter
    @Setter
    private String plateNumber;

    @Getter
    @Setter
    private List<byte[]> vehIds;

    @Getter
    @Setter
    private List<String> vidList;

    @Getter
    @Setter
    private byte[] vidBytes;

    @Getter
    @Setter
    private List<byte[]> offlineVids;

    @Getter
    @Setter
    private List<String> offlineVidList;

}
