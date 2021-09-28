package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 14:45
 */
@Data
public class PassCloudAlarmReport implements Serializable {
    private static final long serialVersionUID = -8437382083692725166L;

    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 所属企业
     */
    private String groupName;
    /**
     * 车辆类型
     */
    private String objectType;
    /**
     * 标识颜色
     */
    private String signColor;

    /**
     * 报警信息
     */
    List<PassCloudAlarmInfo> alarmInfo;
}
