package com.zw.platform.domain.functionconfig.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 路段信息
 *
 * @author  Tdz
 * @create 2017-04-13 10:39
 **/
@Data
public class LineSegmentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
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

    private String maximumSpeed;

    private String offset;

    private String overspeedTime;

    private String overlengthThreshold;

    private String shortageThreshold;

    /**
     * 限速夜间最高速度时长(3658新增)
     */
    private Integer nightMaxSpeed;



}
