package com.zw.api2.swaggerEntity;

import lombok.Data;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2019/2/15 20:18
 @Description 分页查询
 @version 1.0
 **/
@Data
public class SwaggerAssignmentQuery implements Serializable {
    private long page;
    private long limit;
    private String simpleQueryParam;
    private String groupId;
}
