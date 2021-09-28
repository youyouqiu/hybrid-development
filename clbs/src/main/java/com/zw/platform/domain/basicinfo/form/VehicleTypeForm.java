package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleTypeForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiParam(value = "车辆类别id")
    private String vehicleCategory; // 车辆类别

    @ApiParam(value = "车辆类型图片id")
    private String ico;

    @Size(max = 20, message = "【车辆类别】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆类别")
    @ApiParam(value = "车辆类别")
    private String category; // 车辆类别

    @NotEmpty(message = "【车辆类型】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【车辆类型】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆类型")
    @ApiParam(value = "车辆类型")
    private String vehicleType; // 车辆类型

    /**
     * 保养里程间隔（km）   不超过5位正整数
     */
    @ExcelField(title = "保养里程间隔(KM)")
    private Integer serviceCycle;

    @Size(max = 50, message = "【备注】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注")
    @ApiParam(value = "备注")
    private String description; // 类型描述

    /**
     * 标准(车辆类别中的字段，不需要导出)（0：通用；1：货运；2：工程机械）
     */
    @ApiParam(value = "标准(车辆类别中的字段，不需要导出)（0：通用；1：货运；2：工程机械）")
    private String standard;

    private String icoName;

    public VehicleTypeDTO convertType() {
        VehicleTypeDTO vehicleType = new VehicleTypeDTO();
        vehicleType.setCategoryId(this.vehicleCategory);
        vehicleType.setType(this.vehicleType);
        vehicleType.setServiceCycle(this.serviceCycle);
        vehicleType.setDescription(this.description);
        vehicleType.setId(this.getId());
        return vehicleType;
    }

    public VehicleTypeDO convertTypeDo() {
        VehicleTypeDO vehicleType = new VehicleTypeDO();
        vehicleType.setId(this.getId());
        vehicleType.setVehicleCategory(this.vehicleCategory);
        vehicleType.setVehicleType(this.vehicleType);
        vehicleType.setDescription(this.description);
        vehicleType.setCreateDataTime(this.getCreateDataTime());
        vehicleType.setCreateDataUsername(this.getCreateDataUsername());
        vehicleType.setUpdateDataTime(this.getUpdateDataTime());
        vehicleType.setUpdateDataUsername(this.getUpdateDataUsername());
        vehicleType.setFlag(this.getFlag());
        vehicleType.setIcoId(this.ico);
        vehicleType.setServiceCycle(this.serviceCycle);
        return vehicleType;
    }

    public VehicleCategoryDTO convertCategory() {
        VehicleCategoryDTO category = new VehicleCategoryDTO();
        category.setId(this.getId());
        category.setStandard(Integer.valueOf(this.getStandard()));
        category.setDescription(this.description);
        category.setCategory(this.vehicleCategory);
        category.setIconId(this.ico);
        category.setIconName(this.icoName);
        return category;
    }

}
