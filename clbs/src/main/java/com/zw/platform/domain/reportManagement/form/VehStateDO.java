package com.zw.platform.domain.reportManagement.form;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:和数据库一一对应
 * @Date: create in 2020/11/12 17:58
 */
@Data
public class VehStateDO {
    /**
     * 车辆所属企业uuid
     */
    private String oid;

    /**
     * 当天0点（秒值）
     */
    private Long time;

    /**
     * 车辆id
     */
    private String vid;

    /**
     * 下发短信数
     */
    private Integer msgNum;

    /**
     * 报警总数
     */
    private Integer alarmAll;

    /**
     * 超速报警数量
     */
    private Integer alarmSpeed;

    /**
     * 疲劳驾驶报警数量
     */
    private Integer alarmTired;

    /**
     * 不按规定路线行驶报警数量
     */
    private Integer alarmLine;

    /**
     * 凌晨2-5点行驶报警数量
     */
    private Integer alarmDawn;

    /**
     * 遮挡摄像头报警数量
     */
    private Integer alarmCamera;

    /**
     * 其它报警数量
     */
    private Integer alarmOther;

    /**
     * 终端状态 0：正常 1：设备故障
     */
    private Integer deviceStatus;

    /**
     * 当天第一条位置时间（秒值）
     */
    private Long posFirst;

    /**
     * 当天最后一条位置时间（秒值）
     */
    private Long posLast;

    /**
     * 报警处理数量
     */
    private Integer alarmHandled;



}
