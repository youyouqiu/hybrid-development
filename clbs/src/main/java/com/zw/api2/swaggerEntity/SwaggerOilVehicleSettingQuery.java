package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SwaggerOilVehicleSettingQuery extends SwaggerPageParamQuery {

    @ApiParam(value = "所属组织id")
    private String groupId;

    @ApiParam(value = "所属组织id")
    private String assignmentId;
}
