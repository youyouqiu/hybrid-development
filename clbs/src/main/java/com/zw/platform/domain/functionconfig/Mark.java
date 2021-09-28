package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangying
 * @version 1.0
 * @date 2016年8月8日下午1:34:08
 */
@Data
public class Mark implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 标注
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    private Integer radius;

    /**
     * 类型
     */
    private String type;

    /**
     * 所属企业
     */
    private String groupId;

    private String groupName;

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
     * 标注类型图标:0:水滴,1:圆圈
     */
    private Integer markIcon;

    /**
     * 颜色
     */
    private String colorCode;

    /**
     * 透明度
     */
    private String transparency;
}
