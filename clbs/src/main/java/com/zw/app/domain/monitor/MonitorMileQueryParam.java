package com.zw.app.domain.monitor;

import com.cb.platform.domain.MileageStatisticInfo;
import lombok.Data;

import java.util.List;

@Data
public class MonitorMileQueryParam {
    private String monitorId;

    private String queryStartMonth;

    private String queryEndMonth;

    private List<MileageStatisticInfo> offLineData;

    private boolean appFlag;
}
