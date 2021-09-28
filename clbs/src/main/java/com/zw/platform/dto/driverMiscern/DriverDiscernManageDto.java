package com.zw.platform.dto.driverMiscern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 15:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDiscernManageDto implements Serializable {
    private static final long serialVersionUID = 5482142105810116482L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 名称
     */
    private String monitorName;
    /**
     * 车牌颜色
     */
    private String plateColor;
    /**
     * 企业
     */
    private String orgName;
    /**
     * 分组
     */
    private String assignmentName;

    /**
     * 驾驶员个数
     */
    private Integer driverNum;
    /**
     * 最近查询时间
     */
    private String latestQueryTimeStr;
    /**
     * 查询成功时间
     */
    private String querySuccessTimeStr;
    /**
     * 最近下发时间
     */
    private String latestIssueTimeStr;
    /**
     * 下发状态 0:等待下发; 1:下发失败; 2:下发中; 3:下发成功
     */
    private Integer issueStatus;
    /**
     * 下发结果 0:终端已应答 1:终端未应答 2:终端离线
     */
    private Integer issueResult;

    /**
     * 查询结果 0:终端已应答 1:终端未应答 2:终端离线
     */
    private Integer queryResult;

    /**
     * 下发人
     */
    private String issueUsername;
}
