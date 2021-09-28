package com.zw.platform.domain.reportManagement;

import lombok.Data;

/**
 * @author denghuabing on 2019/12/17 16:48
 */
@Data
public class SpeedAlarmData {

    private String monitorName;//车牌号

    private String assignmentName;//分组

    private String alarmSource;//报警来源

    private Long alarmStartTime;//报警开始时间

    private String alarmStartAddress;

    private String alarmStartSpeed;//报警开始车速

    private Long alarmEndTime;//报警结束时间

    private String alarmEndAddress;//报警结束位置

    private String alarmEndSpeed;//报警结束车速

    private Long duration;//报警时长

    private String alarmStartLocation;//报警开始位置(经纬度)

    private String alarmEndLocation;//报警结束位置(经纬度)
}
