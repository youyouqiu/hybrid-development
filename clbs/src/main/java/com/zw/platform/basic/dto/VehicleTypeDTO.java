package com.zw.platform.basic.dto;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车辆类型实体类
 *
 * @author zhangjuan
 */
@Data
public class VehicleTypeDTO {
    @NotNull(message = "【id】不能为空！", groups = {ValidGroupUpdate.class})
    @ApiParam(value = "id")
    private String id;

    @NotEmpty(message = "【车辆类别Id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆类别Id")
    private String categoryId;

    @ExcelField(title = "车辆类别")
    @ApiParam(value = "车辆类别")
    private String category;

    @ExcelField(title = "车辆类型")
    @NotEmpty(message = "【车辆类型】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【车辆类型】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆类型")
    private String type;

    @ExcelField(title = "保养里程间隔(KM)")
    @ApiParam(value = "保养里程间隔(KM)")
    private Integer serviceCycle;

    @ExcelField(title = "备注")
    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "备注")
    private String description;

    /**
     * 识别码
     */
    private String codeNum;
}
