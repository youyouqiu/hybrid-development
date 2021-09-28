package com.zw.platform.domain.vas.alram.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

/**
 * 单车登录报警查询query实体
 * @author XK
 */

@Data
public class SingleVehicleAlarmSearchQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 报警类型
     */
    private String alarmType;

    /**
     * 报警开始时间
     */
    private String alarmStartTime;

    /**
     * 报警结束时间
     */
    private String alarmEndTime;

    /**
     * 处理状态
     */
    private int status;

    /**
     * 报警来源
     */
    private int alarmSource;

    /**
     * 通讯类型
     */
    private String deviceType;

    /**
     * 全局报警状态
     */
    private int pushType;

    /**
     * 车牌号
     */
    private String brand;
}
