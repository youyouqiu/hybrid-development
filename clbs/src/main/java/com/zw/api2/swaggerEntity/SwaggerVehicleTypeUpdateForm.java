package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;

/***
 @Author gfw
 @Date 2019/2/15 15:01
 @Description 修改车辆类型信息
 @version 1.0
 **/
@Data
public class SwaggerVehicleTypeUpdateForm {
    /**
     * 类别id
     */
    private String id;

    /**
     * 车辆类别id
     */
    @NotEmpty(message = "【车辆类别】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【车辆类型】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String vehicleCategory;

    /**
     * 车辆类型名称
     */
    @NotEmpty(message = "【车辆类型】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【车辆类型】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String vehicleType;

    /**
     * 备注
     */
    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String description;
}
