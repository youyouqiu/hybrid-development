package com.cb.platform.service.speedingStatistics;

import com.cb.platform.domain.speedingStatistics.quey.UpSpeedGroupQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.JsonResultBean;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/18 10:39
 */
public interface UpSpeedStatisticsGroupService {
    /**
     * 图表数据查询
     * @param groupId
     * @param time
     * @return
     */
    JsonResultBean findGraphicalStatistics(String groupId, String time, String is);

    /**
     * 列表分页查询
     * @param query
     */
    PassCloudResultBean speedingStatisticsList(UpSpeedGroupQuery query);

    PassCloudResultBean upSpeedInfoList(UpSpeedGroupQuery query);

    PassCloudResultBean rankInfo(UpSpeedGroupQuery query);

    OfflineExportInfo exportOrgListData(UpSpeedGroupQuery query);

    OfflineExportInfo exportOrgSpeedDetailsData(UpSpeedGroupQuery query);
}
