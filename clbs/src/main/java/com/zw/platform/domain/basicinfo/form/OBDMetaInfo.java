package com.zw.platform.domain.basicinfo.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OBDMetaInfo {

    /**
     * 数据流ID
     */
    private Integer id;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 数据流名称
     */
    private String displayName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 描述
     */
    private String desc;

    /**
     * 类型 0:乘用车; 1:商用车; 2:无;
     */
    private int type;

    /**
     * 值是否是数值类型（数值类型会自动处理小数点）
     */
    private boolean numeric;

    /**
     * 是否是默认展示列 true:默认展示; false:默认不展示;
     */
    private boolean showByDefault;
}
