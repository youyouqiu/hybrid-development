package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerSaveWirelessParam {

    @ApiParam(value = "拨号用户名", required = true)
    private String dailUserName;//拨号用户名

    @ApiParam(value = "拨号密码", required = true)
    private String dailPwd;//拨号密码

    @ApiParam(value = "地址", required = true)
    private String address;//地址

    @ApiParam(value = "TCP端口", required = true)
    private Integer tcpPort;//TCP端口

    @ApiParam(value = "固件版本", required = true)
    private String firmwareVersion;//固件版本

    @ApiParam(value = "车辆与油箱的绑定id", required = true)
    private String id;

    @ApiParam(value = "参数类型", required = true)
    private String commandType;

    @ApiParam(value = "车牌号", required = true)
    private String brand;

    @ApiParam(value = "传感器编号", required = true)
    private String sensorNumber;
}
