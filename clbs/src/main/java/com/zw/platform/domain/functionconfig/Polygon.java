package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/8/9.
 */
@Data
public class Polygon implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 多边形的经纬度
     */
    private String polygonId;

    /**
     * 顺序
     */
    private Short sortOrder;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    //=========多边形主表==============
    private String id;
    private String name;
    private String type;
    private String description;

    /**
     * 颜色
     */
    private String colorCode;

    /**
     * 透明度
     */
    private String transparency;
}
