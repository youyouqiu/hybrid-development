package com.zw.app.domain.monitor;

import lombok.Data;


@Data
public class WorkInfos {
    /**
     * 时间
     */
    private long posTime;

    /**
     * 传感器瞬时流量
     */
    private Double checkData;

    /**
     * 是否是有效数据 0:有效 1:无效 3:空白数据
     */
    private Integer effectiveData = 0;

    /**
     * 工时检查方式   0:电压比较式  1:油耗阈值式  2:油耗波动式
     */
    private Integer workInspectionMethod;

    /**
     * 工作状态   0:停机 1:工作 2:待机(油耗波动式专有)
     */
    private Integer workingPosition;

    /**
     * 持续时长
     */
    private Long continueTime;

    /**
     * 波动值
     */
    private Double fluctuateValue;

    /**
     * 阈值
     */
    private String thresholdValue;
}
