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
 @Date 2019/2/14 19:53
 @Description 车辆类别新增
 @version 1.0
 **/
@Data
public class SwaggerVehicleStyleUpdateForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;

    @ApiParam(value = "车辆类别id")
    private String vehicleCategory;

    @ApiParam(value = "车辆类型图片id")
    private String ico;


    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "备注")
    @ApiParam(value = "备注")
    private String description;
    /**
     * 标准(车辆类别中的字段，不需要导出)（0：通用；1：货运；2：工程机械）
     */
    @ApiParam(value = "标准(车辆类别中的字段，不需要导出)（0：通用；1：货运；2：工程机械）")
    private String standard;
}
