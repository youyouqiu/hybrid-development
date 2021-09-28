package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangying
 * @version 1.0
 * @date 2016年8月8日下午6:11:43
 */
@Data
public class LineContent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 线的坐标
     */
    private String lineId;

    /**
     * 线名称
     */
    private String name;

    /**
     * 顺序
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

    /**
     * 点类型  0：围栏线点   1：监控点    2：监控线段
     */
    private String type = "0";

    /**
     * 逻辑删除标志
     */
    private Integer flag = 1;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 颜色
     */
    private String colorCode;

    /**
     * 透明度
     */
    private String transparency;

    /**
     * 描述
     */
    private String description;

    private String width;
}
