package com.zw.platform.domain.functionconfig.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/8/9.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RectangleQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 矩形
     */
    private String id;

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

    /**
     * 左上角经度
     */
    private Double leftLongitude;

    /**
     * 左上角纬度
     */
    private Double leftLatitude;

    /**
     * 右下角经度
     */
    private Double rightLongitude;

    /**
     * 右下角纬度
     */
    private Double rightLatitude;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
}
