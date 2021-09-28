package com.zw.platform.domain.topspeed_entering;

import lombok.Data;

import java.util.Date;

/**
 * Created by LiaoYuecai on 2017/2/28.
 */
@Data
public class DeviceRegister {
    private String id;
    private String deviceId;//终端编号
    private String brand;//车牌号
    private String simNumber;//sim卡号
    private String deviceType;//终端类型
    private Integer status;
    private String uniqueNumber;//唯一标识
    private String fakeIp;//伪IP
    private Date updateDataTime;//修改时间
    /**
     * 注册信息-制造商ID
     */
    private String manufacturerId;

    /**
     * 注册信息-终端型号
     */
    private String deviceModelNumber;

    private String provinceId; //省市id
    private String cityId; //市域id
    private Integer plateColor; //车牌颜色
}
