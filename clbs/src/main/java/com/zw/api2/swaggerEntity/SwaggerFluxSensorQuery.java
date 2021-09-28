package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SwaggerFluxSensorQuery /*extends SwaggerPageQuery*/ {

    @ApiParam(value = "按照流量传感器型号进行模糊搜索")
    private String simpleQueryParam;
}
