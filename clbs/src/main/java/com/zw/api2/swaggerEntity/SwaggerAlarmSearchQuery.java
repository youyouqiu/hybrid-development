package com.zw.api2.swaggerEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author gfw
 @Date 2019/1/30 14:28
 @Description 分页查询报警列表
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerAlarmSearchQuery {
    /**
     * 启始页
     */
    private String start;
    /**
     * 每页查询数
     */
    private Long length;
}
