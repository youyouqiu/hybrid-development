package com.zw.platform.domain.vas.workhourmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 工时统计
 * Created by Tdz on 2016/9/27.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkHourQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private ShakeSensor sensor;//传感器数据
    private String vtime;//时间
    private String longtitude;//经度

    private String latitude;//纬度

    private String team;//车队
    private String brand;//车牌号
    private String workTimes;//工作次数

    /**
     * 车id
     */
    private String vehicleId;
    private byte[] vehicleIdBytes;
    /**
     * 开始时间
     */
    private String startTimeStr;
    private Long startTime;
    /**
     * 结束时间
     */
    private String endTimeStr;
    private Long endTime;
    /**
     * 发动机型号 0: 1#发动机  1: 2#发动机
     */
    private String sensorSequence;

    /**
     * 工作状态 0:停机 1:工作 2:待机 3:有效数据
     */
    private Integer workingPosition;
    /**
     * 排序
     */
    private String sort="ASC";
}
