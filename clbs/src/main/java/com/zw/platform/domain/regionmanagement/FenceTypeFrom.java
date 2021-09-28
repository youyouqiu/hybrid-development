package com.zw.platform.domain.regionmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 14:26
 */
@Data
public class FenceTypeFrom extends BaseFormBean {
    private static final long serialVersionUID = -906405931604575872L;

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
     * 绘制方式 1:多边形; 2:圆; 3:路线; 4:标注; 5:行政区域 多个逗号分隔
     */
    private String drawWay;

    /**
     * 备注
     */
    private String remark;

    private String groupId;
}
