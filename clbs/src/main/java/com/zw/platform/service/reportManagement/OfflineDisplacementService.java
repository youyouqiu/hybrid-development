package com.zw.platform.service.reportManagement;

import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.domain.reportManagement.query.OfflineDisplacementQuery;

/**
 * @author wanxing
 * @Title: 离线位移日报表
 * @date 2020/10/2011:03
 */
public interface OfflineDisplacementService {

    /**
     * 分页接口
     * @param query
     * @return
     */
    PassCloudResultBean queryList(OfflineDisplacementQuery query);

    /**
     * 处理单条
     * @param monitorIds
     * @param offlineMoveEndTime
     * @param handleResult
     * @param remark
     * @throws Exception
     */
    void deal(String monitorIds, String offlineMoveEndTime, String handleResult, String remark);

    /**
     * 批量处理
     * @param primaryKeys
     * @param handleResult
     * @param remark
     * @throws Exception
     */
    void batchDeal(String primaryKeys, String handleResult, String remark);

}
