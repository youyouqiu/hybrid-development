package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分段内容
 *
 * @author  Tdz
 * @create 2017-04-12 15:37
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SegmentContentForm extends BaseFormBean implements Serializable {
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
}
