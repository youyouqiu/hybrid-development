package com.zw.platform.dto.protocol;

import lombok.Data;

/***
 @Author lijie
 @Date 2021/1/6 10:38
 @Description 黑龙江终端信息上报实体
 @version 1.0
 **/
@Data
public class HeiDeviceInstallInfoDto extends  DeviceInstallInfoDto {

    /**
     * 设备序列号
     */
    private String terminalSerialNumber;

    /**
     * 设备硬件版本号
     */
    private String hardwareVersionNumber;

    /**
     * 设备软件版本号
     */
    private String softwareVersionNumber;

    /**
     * 生产日期
     */
    private Long manufactureTime;

    /**
     * 检验合格日期
     */
    private Long inspectionTime;

    /**
     * 运输企业
     */
    private String transportationEnterprises;

}
