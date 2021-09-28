package com.zw.platform.domain.taskmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * 任务项信息
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:09
 */
@Data
public class TaskItem extends BaseFormBean {
    private static final long serialVersionUID = 4762080751869384557L;

    private String taskId;

    /**
     * 控制类别；1:围栏；2:RFID；3:NFC; 4:二维码;
     */
    private Integer controlType;

    /**
     * 围栏id
     */
    private String fenceInfoId;

    private String startTime;

    private String endTime;

    /**
     * 关联报警（1:任务未到岗; 2:任务离岗; ）
     */
    private String relationAlarm;

    private String fenceName;
}
