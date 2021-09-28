package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ponghj
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkHourInfo {
    private static final long serialVersionUID = 1L;

    private String id;

    private byte[] vehicleId;

    /**
     * gps时间
     */
    private long vtime = 0L;

    private String vtimeStr = "";

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 速度
     */
    private String speed;

    /**
     * 车牌号
     */
    private String plateNumber;

    // 工时传感器

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
    private String continueTimeStr;

    /**
     * 波动值
     */
    private Double fluctuateValue;

    /**
     * 检测数据
     */
    private Double checkData;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 是否是有效数据 0:有效 1:无效 3:空白数据
     */
    private Integer effectiveData = 0;
}
