package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分段限速
 *
 * @author  Tdz
 * @create 2017-04-11 14:52
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LineSegmentContentForm extends BaseFormBean implements Serializable {

    /**
     * 线段经纬度表
     */
    private String lineSegmentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;
    private String lineId = "";
    /**
     * 偏移量
     */
    private String offset;

    /**
     * 路段行驶过长阈值
     */
    private String overlengthThreshold;

    /**
     * 路段行驶不足阈值
     */
    private String shortageThreshold;

    /**
     * 路段最高速度
     */
    private String maximumSpeed;

    /**
     * 超速持续时间
     */
    private String overspeedTime;

    private String segmentSort;

    private int sumn;

    /**
     * 限速夜间最高速度时长(3658新增)
     */
    private String nightMaxSpeed;

    /**
     * 夜间限速时间段(3658新增)
     */
    private String nightLimitTime;


}
