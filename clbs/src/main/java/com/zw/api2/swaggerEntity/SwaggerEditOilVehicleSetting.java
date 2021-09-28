package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;


@Data
@EqualsAndHashCode(callSuper = true)
public class SwaggerEditOilVehicleSetting extends SwaggerBindOilVehicleSetting {

    @ApiParam(value = ("邮箱2与车辆关联id(未绑定邮箱2不填),当修改前为单油箱，修改后为双油箱时,必填（新增邮箱2）"))
    private String newId2;

    /**
     * 油箱型号
     */
    @ApiParam(value = ("油箱型号"))
    private String type;


    /**
     * 油箱类型 油箱1 油箱2
     */
    @NotEmpty(message = "【油箱类型1】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【油箱类型1】填值错误！", regexp = "^[1]{1}$", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    /**
     * 油箱型号
     */
    @ApiParam(value = ("油箱类型 油箱1 油箱2"))
    private String oilBoxType;
}
