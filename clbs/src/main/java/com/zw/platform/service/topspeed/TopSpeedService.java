package com.zw.platform.service.topspeed;

import com.zw.platform.domain.topspeed_entering.DeviceRegister;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/1.
 */
public interface TopSpeedService {
    /**
     * 查询在数据库且没绑定的终端信息
     * @param groupId
     * @param identifyNumber 唯一标识（0：设备号，1：SIM卡号）
     * @return
     */
    List<DeviceRegister> findDeviceData();


    void deleteByDeviceId(String deviceId);
}
