package com.zw.lkyw.domain.sendMessageReport;

import lombok.Data;

/**
 * @author denghuabing on 2019/12/31 15:57
 */
@Data
public class SendMessageReportData {

    /**
     * 序列号
     */
    private int num;
    private String monitorId;

    private String monitorName;

    /**
     * 车辆类型
     */
    private String objectType;

    private String plateColor;

    private transient Integer signColor;

    private String groupName;

    /**
     * 消息总条数
     */
    private Long totalNum;

    /**
     * 成功条数
     */
    private Long successNum;

    /**
     * 失败条数
     */
    private Long failNum;

    /**
     * 人工成功条数
     */
    private Long manualIssueSucNum;
    /**
     * 人工失败条数
     */
    private Long manualIssueFailNum;

    /**
     * 系统成功条数
     */
    private Long sysIssueSucNum;

    /**
     * 系统失败条数
     */
    private Long sysIssueFailNum;
}
