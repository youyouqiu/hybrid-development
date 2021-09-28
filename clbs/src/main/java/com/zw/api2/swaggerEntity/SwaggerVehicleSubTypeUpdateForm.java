package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

/***
 @Author gfw
 @Date 2019/2/15 19:08
 @Description 编辑
 @version 1.0
 **/
@Data
public class SwaggerVehicleSubTypeUpdateForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    /**
     * 车辆类别id
     */
    @Size(max = 255, message = "【车辆子类型名】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆子类型名")
    @ApiParam(value = "车辆子类型名")
    private String vehicleSubtypes;

    /**
     * 行驶方式（0：自行；1：运输）
     */
    @Size(max = 2, message = "【行驶方式】长度不超过2！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "行驶方式")
    @ApiParam(value = "行驶方式（0：自行；1：运输）")
    private String drivingWay;

    @Size(max = 50, message = "【备注】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注")
    @ApiParam(value = "备注")
    private String description;

    @ApiParam(value = "车辆子类型对应的父类型id")
    private String pid;

    @ApiParam(value = "车辆类型图片id")
    private String icoId;
}
