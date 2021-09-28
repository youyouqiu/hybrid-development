package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;


@Data
public class SwaggerPageParamQuery {
    /**
     * 开始记录
     */
    @ApiParam(value = "开始记录", required = true)
    private Long start;

    /**
     * 数量
     */
    @ApiParam(value = "数量", required = true)
    private Long length;

    private String simpleQueryParam;

}
