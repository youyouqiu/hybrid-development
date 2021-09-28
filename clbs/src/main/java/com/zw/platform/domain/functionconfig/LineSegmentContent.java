package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class LineSegmentContent implements Serializable {
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
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    private Integer maximumSpeed;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}