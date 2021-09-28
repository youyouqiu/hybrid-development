package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SwaggerDeviceQuery /*extends SwaggerPageQuery */ {
    @ApiParam(value = "按照设备编号、设备名称、车牌号进行模糊搜索")
    private String simpleQueryParam;
}
