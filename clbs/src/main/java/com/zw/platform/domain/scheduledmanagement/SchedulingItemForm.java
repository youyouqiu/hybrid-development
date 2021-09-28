package com.zw.platform.domain.scheduledmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 16:47
 */
@Data
public class SchedulingItemForm extends BaseFormBean {
    private static final long serialVersionUID = -20628554272925751L;

    /**
     * 排班id
     */
    private String scheduledInfoId;
    /**
     * 控制类别；1:围栏；2:RFID；3:NFC; 4:二维码;
     */
    private Integer controlType;

    /**
     * 围栏信息id
     */
    private String fenceInfoId;

    /**
     * 围栏名称
     */
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
