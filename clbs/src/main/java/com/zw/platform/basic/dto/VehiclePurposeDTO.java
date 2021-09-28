package com.zw.platform.basic.dto;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车辆运营实体类
 *
 * @author zhangjuan
 */
@Data
public class VehiclePurposeDTO {

    @NotNull(message = "【id】不能为空！", groups = {ValidGroupUpdate.class})
    @ApiParam(value = "id")
    private String id;

    @Size(max = 20, min = 1, message = "【运营类别】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "运营类别")
    private String purposeCategory;

    @Size(max = 50, message = "【说明】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "备注")
    private String description;

    @ApiParam(value = "识别码")
    private String codeNum;

}
