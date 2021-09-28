package com.zw.platform.domain.scheduledmanagement;

import lombok.Data;

import java.io.Serializable;

/**
 * 排班项信息
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:25
 */
@Data
public class SchedulingItemInfo implements Serializable {
    private static final long serialVersionUID = 1933481271121789186L;
    /**
     * 排班项id
     */
    private String id;

    /**
     * 排班id
     */
    private String scheduledInfoId;

    /**
     * 控制类别；1:围栏；2:RFID；3:NFC; 4:二维码;
     */
    private Integer controlType;

    /**
     * 围栏id
     */
    private String fenceInfoId;

    private String fenceName;

    /**
     * 排班项开始时间str类型
     */
    private String startTime;

    /**
     * 排班项结束时间str类型
     */
    private String endTime;

    /**
     * 关联报警（1:上班未到岗; 2:上班离岗; 3:超时长停留;）
     */
    private String relationAlarm;

    /**
     * 停留时间（分钟）
     */
    private int residenceTime = 30;

}
