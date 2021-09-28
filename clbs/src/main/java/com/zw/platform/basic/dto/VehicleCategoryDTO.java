package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 车辆类别实体类
 *
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
public class VehicleCategoryDTO {
    @NotNull(message = "【id】不能为空！", groups = {ValidGroupUpdate.class})
    @ApiParam(value = "id")
    private String id;

    @NotNull(message = "【车辆类别】不能为空！", groups = {ValidGroupUpdate.class, ValidGroupAdd.class})
    @Size(max = 20, message = "【车辆类别】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆类别")
    private String category;


    @NotNull(message = "【车辆类别图标】不能为空！", groups = {ValidGroupUpdate.class, ValidGroupAdd.class})
    @ApiParam(value = "车辆类别图片id")
    private String iconId;


    @ApiParam(value = "车辆类别图标名称")
    private String iconName;

    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "车辆类别详情")
    private String description;

    @ApiParam(value = "标准（0：通用；1：货运；2：工程机械）")
    private Integer standard;


    /**
     * DO转DTO的构造函数
     */
    public VehicleCategoryDTO(VehicleCategoryDO vehicleCategoryDO) {
        this.id = vehicleCategoryDO.getId();
        this.category = vehicleCategoryDO.getVehicleCategory();
        this.iconId = vehicleCategoryDO.getIco();
        this.description = vehicleCategoryDO.getDescription();
        this.standard = vehicleCategoryDO.getStandard();
    }
}
