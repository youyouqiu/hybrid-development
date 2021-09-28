package com.zw.platform.domain.regionmanagement;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:56
 */
@Data
public class FenceTypeInfo implements Serializable {
    private static final long serialVersionUID = -1423517259633934215L;

    /**
     * 围栏种类id
     */
    private String id;

    /**
     * 围栏种类名称
     */
    private String fenceTypeName;

    /**
     * 颜色编码
     */
    private String colorCode;

    /**
     * 透明度
     */
    private String transparency;

    /**
     * 绘制方式 1:多边形; 2:圆; 3:路线; 4:标注; 多个逗号分隔
     */
    private String drawWay;

    /**
     * 备注
     */
    private String remark;

    /**
     * 围栏种类已经绘制的围栏类型
     */
    private String alreadyDrawFence;
}
