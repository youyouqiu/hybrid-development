package com.zw.platform.domain.functionconfig;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 行政区域实体
 * @author yangyi
 */
@Data
public class Administration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 街道
     */
    private String street;

    /**
     * 描述
     */
    private String description;

    /**
     * 多边形的经纬度
     */
    private String polygonId;

    /**
     * 顺序
     */
    private int sortOrder;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 行政区域
     */
    private Integer regionCount;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

}
