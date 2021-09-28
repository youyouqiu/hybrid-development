package com.zw.platform.domain.accessPlatform;

import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccessPlatformForm extends BaseFormBean implements Serializable {
    /**
     * 平台名称
     */
    @ExcelField(title = "平台名称")
    private String platformName;

    /**
     *  类型（上级平台，同级平台）
     */
    private String type;

    /**
     * 状态（开启,关闭）
     */
    @ExcelField(title = "状态")
    private String status;

    /**
     * ip地址
     */
    @ExcelField(title = "IP地址")
    private String ip;


}
