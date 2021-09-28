package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;


import javax.validation.constraints.Size;
import java.io.Serializable;

/***
 @Author gfw
 @Date 2019/2/14 19:53
 @Description 车辆类型新增
 @version 1.0
 **/
@Data
public class SwaggerVehicleTypeFormAdd implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 车辆类别
     */
    @NotEmpty(message = "【车辆类别】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "车辆类别id")
    private String vehicleCategory;
    /**
     * 车辆类型
     */
    @NotEmpty(message = "【车辆类型】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【车辆类型】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "车辆类型")
    private String vehicleType;
    /**
     * 备注
     */
    private String description;
}
