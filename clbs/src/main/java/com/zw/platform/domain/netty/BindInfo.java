package com.zw.platform.domain.netty;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/9/7.
 */
@Data
public class BindInfo {
    private String deviceId; //先绑定设备id

    private String deviceNumber;

    private String oldDeviceType; //原协议类型

    private String deviceType; //协议类型

    private String simCard;

    private String fakeIp;

    private VehicleInfo vehicleInfo;

    private PeopleInfo peopleInfo;

    private String oldDeviceId; //原绑定设备id

    private String oldIdentification; //原绑定标识

    private String authCode;

    private String identification; //现绑定标识

    private JSONObject monitorInfo; //监控对象信息

    // 终端注册信息
    private String manufacturerId;    // 注册信息-制造商ID

    private String deviceModelNumber; // 注册信息-终端型号

    /**
     * 5分区: 唯一标识
     */
    private String identificationAndDeviceType;

}
