package com.cb.platform.service.speedingStatistics;

import com.cb.platform.domain.speedingStatistics.quey.UpSpeedVehicleQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.JsonResultBean;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/20 15:26
 */
public interface UpSpeedStatisticsVehicleService {
    JsonResultBean upSpeedGraphicalInfo(UpSpeedVehicleQuery query);

    PassCloudResultBean upSpeedInfoList(UpSpeedVehicleQuery query);

    PassCloudResultBean speedingStatisticsList(UpSpeedVehicleQuery query);

    JsonResultBean rankInfo(UpSpeedVehicleQuery query);

    OfflineExportInfo exportVehListData(UpSpeedVehicleQuery query);

    OfflineExportInfo exportVehSpeedDetailsData(UpSpeedVehicleQuery query);
}
