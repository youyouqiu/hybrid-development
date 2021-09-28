package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 电子围栏-圆-Form
 * @author wangjianyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CircleForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 新增或修改圆：0-新增；1-修改
     */
    @NotEmpty(message = "【新增或修改线路标识】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【新增或修改线路标识】填值错误！", regexp = "^[0-1]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String addOrUpdateCircleFlag = "0";

    /**
     * 被修改的圆形区域的id
     */
    private String circleId = "";

    @NotEmpty(message = "【区域名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【区域名称】长度不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "name")
    private String name; // 名称

    @Size(max = 100, message = "【描述】长度不能超过100个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "description")
    private String description; // 描述

    @ExcelField(title = "type")
    private String type; // 类型

    @NotEmpty(message = "【半径】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "radius")
    private Double radius; // 半径

    @NotEmpty(message = "【经度】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "longitude")
    private String longitude;//经度

    @NotEmpty(message = "【纬度】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "latitude")
    private String latitude;//纬度

    private String groupId;  // 所属企业

    /**
     * 围栏类别
     */
    private String typeId;
}
