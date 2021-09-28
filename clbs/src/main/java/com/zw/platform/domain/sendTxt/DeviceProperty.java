package com.zw.platform.domain.sendTxt;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
public class DeviceProperty {
    private Integer deviceType;
    private String manufacturerId;
    private String deviceModule;
    private String deviceNumber;
    private String ICCID;
    private String deviceHardwareVersions;
    private String deviceFirmwareVersions;
    private Integer GNSSModuleParam;
    private Integer communicationModuleParam;
}
