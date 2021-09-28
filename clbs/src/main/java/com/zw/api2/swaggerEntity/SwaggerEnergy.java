package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class SwaggerEnergy {

    @ApiParam(value = "车辆id",required = true)
    private String vehicleId;//车辆id

    @ApiParam(value = "行驶油耗基准(可输入）")
    private Double travelBase;//行驶油耗基准(可输入）

    @ApiParam(value = "怠速油耗基准(可输入）")
    private Double idleBase;//怠速油耗基准(可输入）

    @ApiParam(value = "节油产品安装日期yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date installTime;//节油产品安装日期

    @ApiParam(value = "怠速阈值")
    private Integer idleThreshold;//怠速阈值
}
