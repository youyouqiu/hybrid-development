package com.zw.api2.swaggerEntity;

import lombok.Data;

/***
 @Author gfw
 @Date 2019/2/14 14:39
 @Description 查询车辆列表实体
 @version 1.0
 **/
@Data
public class SwaggerVehicleQuery {
    /**
     * 启始页
     */
    private long page;
    /**
     * 每页大小
     */
    private long limit;
    /**
     * 查询参数
     */
    private String simpleQueryParam;
    /**
     * 组织名称
     */
    private String groupName;

    /**
     * 组织类型
     */
    private String groupType;
}
