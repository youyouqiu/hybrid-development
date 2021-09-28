package com.zw.platform.domain.vas.alram.query;

import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报警查询Query add by fanlu 2016/12/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmSearchQuery extends BaseQueryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;// 报警类型

    private Long alarmStartTime;// 报警开始时间

    public Long getAlarmStartTime() {
        if (alarmStartTime != null) {
            alarmStartTime = DateUtil.getMillisecond(alarmStartTime);
        }
        return alarmStartTime;
    }

    private Long alarmEndTime;// 报警结束时间

    public Long getAlarmEndTime() {
        if (alarmEndTime != null) {
            alarmEndTime = DateUtil.getMillisecond(alarmEndTime);
        }
        return alarmEndTime;
    }

    private int status;// 状态

    private int alarmSource; // 报警来源

    private String deviceType; // 通讯类型

    private int pushType; // 全局报警状态
}
