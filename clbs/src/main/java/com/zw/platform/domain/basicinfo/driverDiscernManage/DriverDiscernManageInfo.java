package com.zw.platform.domain.basicinfo.driverDiscernManage;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 驾驶员识别管理实体
 * @Author Tianzhangxu
 * @Date 2020/9/27 11:08
 */
@Data
public class DriverDiscernManageInfo implements Serializable {
    private static final long serialVersionUID = 3488463387538590148L;

    private String id;
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 名称
     */
    private String monitorName;

    /**
     * 车牌颜色（1蓝，2黄，3黑，4白，9其他）
     */
    private Integer plateColor;

    /**
     * 企业ID
     */
    private String orgId;

    /**
     * 驾驶员个数
     */
    private Integer driverNum;

    /**
     * 最近查询时间
     */
    private Date latestQueryTime;

    /**
     * 查询成功时间
     */
    private Date querySuccessTime;

    /**
     * 最近下发时间
     */
    private Date latestIssueTime;

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
