/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * 油箱信息Form
 * <p>Title: FuelTankForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月25日下午2:09:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerTheoryFuelTank implements Serializable {
    private static final long serialVersionUID = -4658040495054717642L;

    @NotEmpty(message = "【油箱形状】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【油箱形状】输入错误，只能输入1,2,3,4;其中1:长方体,2:圆柱形,3:D形,4:椭圆形！", regexp = "^[1-4]{1}$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "油箱形状")
    @ApiParam(value = ("油箱形状,只能输入1,2,3,4;其中1:长方体,2:圆柱形,3:D形,4:椭圆形"), required = true)
    private String shape = "";

    @NotEmpty(message = "【长度(mm)】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【长度(mm)】输入错误，请输入合法的数字！", regexp = "^(?:[1-9][0-9]*(?:\\.[0-9]+)?|0\\.(?!0+$)[0-9]+)$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "长度(mm)")
    @ApiParam(value = ("长度(mm)"), required = true)
    private String boxLength = "";

    @NotEmpty(message = "【宽度(mm)】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【宽度(mm)】输入错误，请输入合法的数字！", regexp = "^(?:[1-9][0-9]*(?:\\.[0-9]+)?|0\\.(?!0+$)[0-9]+)$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "宽度(mm)")
    @ApiParam(value = ("宽度(mm)"), required = true)
    private String width = "";

    @NotEmpty(message = "【高度(mm)】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【高度(mm)】输入错误，请输入合法的数字！", regexp = "^(?:[1-9][0-9]*(?:\\.[0-9]+)?|0\\.(?!0+$)[0-9]+)$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "高度(mm)")
    @ApiParam(value = ("高度(mm)"), required = true)
    private String height = "";

    @NotEmpty(message = "【壁厚(mm)】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【壁厚(mm)】输入错误，请输入1-10的正整数！", regexp = "^[1][0]$|^[1-9]{1}$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "壁厚(mm)")
    @ApiParam(value = ("壁厚(mm)"), required = true)
    private String thickness = "";

    /**
     * 下圆角半径
     */
    @ExcelField(title = "下圆角半径(mm)")
    @ApiParam(value = ("下圆角半径(mm)"), required = true)
    private String buttomRadius;

    /**
     * 上圆角半径
     */
    @ExcelField(title = "上圆角半径(mm)")
    @ApiParam(value = ("下圆角半径(mm)"), required = true)
    private String topRadius;

}
