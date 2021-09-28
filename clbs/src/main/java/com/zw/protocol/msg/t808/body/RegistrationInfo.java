package com.zw.protocol.msg.t808.body;

import lombok.Data;

/**
 * 0100注册信息上报数据
 */
@Data
public class RegistrationInfo {

    private Integer provinceId; // 省域Id

    private Integer cityId; // 市域Id

    private String venderName; // 制造商

    private String deviceType; //终端型号

    private String deviceImei; //终端Id

    private Integer plateColor; // 车牌颜色

    private String plateLicense; //车牌标识
}
