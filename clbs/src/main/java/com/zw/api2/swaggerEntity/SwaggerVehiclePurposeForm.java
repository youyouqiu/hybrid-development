package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerVehiclePurposeForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用途类别
     */
    @ApiParam(value = "用途类别")
    @Size(max = 20, message = "【运营类别】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String purposeCategory;

    /**
     * 车辆用途说明
     */
    @ApiParam(value = "车辆用途说明")
    @Size(max = 50, message = "【说明】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String description;
}
