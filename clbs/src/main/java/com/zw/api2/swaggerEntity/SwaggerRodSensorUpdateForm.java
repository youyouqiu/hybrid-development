package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;


/***
 @Author zhengjc
 @Date 2019/2/14 17:55
 @Description 修改油耗传感器
 @version 1.0
 **/
@Data
public class SwaggerRodSensorUpdateForm {

    /**
     * 传感器id
     */
    @ApiParam(value = "传感器id", required = true)
    private String  id;
    /**
     * 传感器型号
     */
    @ApiParam(value = "油杆传感器型号", required = true)
    private String sensorNumber;

    /**
     * 传感器长度
     */
    @ApiParam(value = "油杆传感器长度(长度mm)", required = true)
    private String sensorLength;

    /**
     * 奇偶效验
     */
    @ApiParam(value = "奇偶校验(1:奇校验;2:偶校验;3:无校验)", required = true,defaultValue = "3")
    private Short oddEvenCheck;

    /**
     * 滤波系数
     */
    @ApiParam( value = "滤波系数(1:实时;2:平滑;3:平稳)", required = true, defaultValue = "2")
    private String filteringFactor;

    /**
     * 波特率
     */
    @ApiParam(value = "波特率(1:2400;2:4800;3:9600;4:19200;5:38400;6:57600;7:115200)",defaultValue = "3")
    private String baudRate;

    /**
     * 补偿使能
     */
    @ApiParam(value = "补偿使能", required = true,defaultValue = "1")
    private Short compensationCanMake;
}
