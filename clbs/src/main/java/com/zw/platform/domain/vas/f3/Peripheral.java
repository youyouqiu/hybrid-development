package com.zw.platform.domain.vas.f3;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 * @date 2017年05月08日 17:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Peripheral extends BaseFormBean {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 外设名称
     */
    @ExcelField(title = "外设名称")
    private String name;

    /**
     * 外设ID
     */
    @ExcelField(title = "外设ID")
    private String identId;

    /**
     * 外设消息长度
     */
    @ExcelField(title = "外设消息长度")
    private Integer msgLength;

}
