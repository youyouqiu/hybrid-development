package com.zw.platform.domain.reportManagement;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 报警漏报实体
 * @date 2021/1/1916:56
 */
@Data
public class OmissionAlarmDTO {

    /**
     * 日期(格式:yyyyMMdd)
     */
    private String day;
    /**
     *路线偏离
     */
    private int courseDeviation;
    /**
     *不按规定路线行驶
     */
    private int refuseStipulatePathDriving;
    /**
     *	进线路
     */
    private int inLine;
    /**
     *	出线路
     */
    private int outLine;
    /**
     *超速
     */
    private int overSpeed;
    /**
     *人证不符
     */
    private int certificateAndPersonMismatch;
    /**
     *疲劳驾驶
     */
    private int fatigueDrive;
    /**
     *	同比
     */
    private double monthRate;
    /**
     *	环比
     */
    private double ringRate;

}
