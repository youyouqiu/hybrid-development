package com.zw.platform.service.statistic;

import com.zw.platform.domain.statistic.LifecycleExpireStatisticQuery;
import com.zw.platform.domain.statistic.info.LifecycleExpireStatisticInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * @author zhouzongbo on 2018/12/10 10:19
 */
public interface LifecycleExpireStatisticService {

    List<LifecycleExpireStatisticInfo> findLifecycle(LifecycleExpireStatisticQuery query) throws Exception;

    int getExpireRemindDays() throws Exception;

    JsonResultBean findExportLifecycle(LifecycleExpireStatisticQuery query) throws Exception;
}
