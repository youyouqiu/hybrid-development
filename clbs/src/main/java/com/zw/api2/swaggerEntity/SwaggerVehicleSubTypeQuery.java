package com.zw.api2.swaggerEntity;

import lombok.Data;

/***
 @Author gfw
 @Date 2019/2/15 19:04
 @Description 分页查询子类型列表
 @version 1.0
 **/
@Data
public class SwaggerVehicleSubTypeQuery {
    private long page;
    private long limit;
    private String simpleQueryParam;
}
