package com.zw.platform.basic.dto;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车辆子类型实体类
 * @author zhangjuan
 */
@Data
public class VehicleSubTypeDTO {
    @NotNull(message = "【id】不能为空！", groups = {ValidGroupUpdate.class})
    @ApiParam(value = "id")
    private String id;

    @NotEmpty(message = "【车辆子类型】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【车辆子类型】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆子类型")
    private String subType;

    @ApiParam(value = "车辆类别Id")
    private String categoryId;

    @ApiParam(value = "车辆类别")
    private String category;

    @NotEmpty(message = "【车辆类型ID】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆类型ID")
    private String typeId;

    @ApiParam(value = "车辆类型")
    private String type;

    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "备注")
    private String description;


    @ApiParam(value = "行驶方式（0：自行；1：运输）")
    private Integer drivingWay;

    @ApiParam(value = "车辆类别图片id")
    private String iconId;

    @ApiParam(value = "车辆子类型图标名称")
    private String iconName;
}
