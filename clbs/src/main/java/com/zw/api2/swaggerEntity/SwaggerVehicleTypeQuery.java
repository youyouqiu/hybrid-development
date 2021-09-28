package com.zw.api2.swaggerEntity;

import lombok.Data;

/***
 @Author gfw
 @Date 2019/2/15 14:00
 @Description 分页查询车辆类型信息
 @version 1.0
 **/
@Data
public class SwaggerVehicleTypeQuery {

    /**
     * 启始页
     */
    private long page;
    /**
     * 每页显示大小
     */
    private long length;
    /**
     * 参数
     */
    private String simpleQueryParam;
}
