package com.zw.api.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName F3Sensor
 * @Descriation F3传感器基础
 * @Author nixiangqian
 * @Date 2019-10-08 9:41
 */
@Data
public class F3Sensor {

    @ApiModelProperty("外设ID")
    private Integer id;

    @ApiModelProperty("消息长度")
    private Integer len;

    /** 传感器异常报警 0正常，1异常 */
    @ApiModelProperty("传感器异常报警 0正常，1异常")
    private Integer unusual = 0;


    @ApiModelProperty("传感器异常信息")
    private Integer content;

    /** 重要数据标识 0 普通数据；1:重要数据； */
    @ApiModelProperty("重要数据标识 0 普通数据；1:重要数据；")
    private Integer important = 0;

    private Integer importent = 0;
}
