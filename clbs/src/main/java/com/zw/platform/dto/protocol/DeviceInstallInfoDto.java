package com.zw.platform.dto.protocol;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/28 14:45
 */
@Data
public class DeviceInstallInfoDto {
    /**
     * 车辆所属运营商平台唯一编码（部平台过检编 号）
     */
    private String platformId;
    /**
     * 设备厂商名称
     */
    private String producer;
    /**
     * 设备型号
     */
    private String terminalModel;
    /**
     * 设备编号
     */
    private String terminalId;
    /**
     * 安装时间
     */
    private Long installTime;
    /**
     * 安装单位
     */
    private String installCompany;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 是否符合要求，0：否，1：是
     */
    private Integer complianceRequirements;
}
