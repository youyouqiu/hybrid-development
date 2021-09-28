package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;

/**
 * 分段
 *
 * @author  Tdz
 * @create 2017-04-12 14:26
 **/
@Data
public class LineSegmentForm extends BaseFormBean implements Serializable {
    private Integer offset;

    /**
     * 路段行驶过长阈值
     */
    private Integer overlengthThreshold;

    /**
     * 路段行驶不足阈值
     */
    private Integer shortageThreshold;

    /**
     * 路段最高速度
     */
    private Double maximumSpeed;

    /**
     * 超速持续时间
     */
    private Integer overspeedTime;
    /**
     * 线id
     */
    private String lineId;

    private Integer segmentSort;

    /**
     * 限速夜间最高速度时长(3658新增)
     */
    private Integer nightMaxSpeed;

    /**
     * 夜间限速时间段(3658新增)
     */
    private String nightLimitTime;

}
