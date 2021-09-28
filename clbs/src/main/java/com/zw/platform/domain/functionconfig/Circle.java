package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/8/9.
 */
@Data
public class Circle implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 圆
     */
    private String id;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 半径
     */
    private Double radius;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    private Integer flag;

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

}
