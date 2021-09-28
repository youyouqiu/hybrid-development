package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.common.OfflineDisplacementBatchDeal;
import com.zw.platform.util.common.OfflineDisplacementDeal;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author wanxing
 * @Title: 离线位移日报表处理类
 * @date 2020/10/2210:11
 */
@Data
public class OfflineDisplacementDealDTO {

    /**
     * 监控对象ID
     */
    @NotNull(message = "监控对象不能为空", groups = {OfflineDisplacementDeal.class})
    private String monitorId;

    /**
     * 离线位移结束时间
     */
    @NotNull(message = "离线位移结束时间不能为空", groups = {OfflineDisplacementDeal.class})
    private String offlineMoveEndTime;

    /**
     * 处理方式
     */
    @NotNull(message = "处理方式不能为空", groups = {OfflineDisplacementDeal.class, OfflineDisplacementBatchDeal.class})
    private String handleResult;

    @NotNull(message = "主键不能为空", groups = {OfflineDisplacementBatchDeal.class})
    private String primaryKeys;

    /**
     * 描述
     */
    private String remark;
}
