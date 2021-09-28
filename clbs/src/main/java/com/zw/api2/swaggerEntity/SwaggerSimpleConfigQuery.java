package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;


/***
 @Author zhengjc
 @Date 2019/2/14 11:31
 @Description 列表查询接收对象
 @version 1.0
 **/
@Data
public class SwaggerSimpleConfigQuery {
    /**
     * 起始页
     */
    @ApiParam(name = "start", value = "起始页",defaultValue = "0")
    private Integer start;
    /**
     * 每页显示的条数
     */
    @ApiParam(name = "length", value = "每页显示的条数",defaultValue = "10")
    private Integer length;
    /**
     * 搜索框的模糊查询条件
     */
    @ApiParam(name = "simpleQueryParam", value = "监控对象/终端号/SIM卡号")
    private String simpleQueryParam;
}
