package com.zw.platform.domain.realTimeVideo;


import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class VideoSleepSetting extends BaseFormBean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String vehicleId;

    private Integer wakeupHandSign; // 是否手动唤醒

    private Integer wakeupConditionSign; // 是否启用唤醒条件

    /**
     * 条件唤醒条件 唤醒条件类型;休眠唤醒模式中bit0为1时此字段有效，否则置0；按位设置: 0表示不设置
     * 1表示设置:Bit0:紧急报警;Bit1:碰撞侧翻报警;Bit2:车辆开门
     */
    private Integer wakeupCondition;

    private Integer wakeupTimeSign; // 是否启用定时唤醒(0 启用,1 停用)

    private Integer wakeupTime; // 唤醒日期

    private Integer wakeupTimeFlag; // 选中的唤醒时间段

    private String wakeupTime1; // 时间段1唤醒时间

    private String wakeupClose1; // 时间段1关闭时间

    private String wakeupTime2; // 时间段2唤醒时间

    private String wakeupClose2; // 时间段2关闭时间

    private String wakeupTime3; // 时间段3唤醒时间

    private String wakeupClose3; // 时间段3关闭时间

    private String wakeupTime4; // 时间段4唤醒时间

    private String wakeupClose4; // 时间段4关闭时间

}