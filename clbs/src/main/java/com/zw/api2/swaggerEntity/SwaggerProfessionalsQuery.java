package com.zw.api2.swaggerEntity;

import lombok.Data;

/***
 @Author gfw
 @Date 2019/2/15 19:19
 @Description 从业人员查询分页
 @version 1.0
 **/
@Data
public class SwaggerProfessionalsQuery {
    private Long page;

    private Long limit;

    private String  simpleQueryParam;

    private String  groupName;

}
