package com.zw.platform.service.winchstatistics;


import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.f3.WinchStatistics;

import java.util.List;


public interface VeerStatisticalService {
    List<WinchStatistics> getInfoDtails(String vehicleId, String startTime, String endTime)
        throws Exception;

    List<TransdusermonitorSet> getVehiceInfo(String groupId);
}
