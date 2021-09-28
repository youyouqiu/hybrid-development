package com.zw.platform.repository.modules;

import com.zw.platform.domain.topspeed_entering.DeviceRegister;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/2/28.
 */
public interface DeviceRegisterDao {
    List<DeviceRegister> findListByDeviceId(String deviceId);

    List<DeviceRegister> findByDeviceId(String deviceId);

    /**
     * 查询在数据库且没绑定的终端信息
     * @param groupId
     * @return
     */
    List<DeviceRegister> findListInUser(@Param("groupList") List<String> groupId);

    /**
     * 查询不在数据库且没绑定的终端信息
     * @param groupId
     * @return
     */
    List<DeviceRegister> findListOutUser(@Param("groupList") List<String> groupId);

    void deleteByDeviceId(String deviceId);

    /**
     * 根据唯一标识查询非法设备注册表的制造商ID和终端型号
     */
    DeviceRegister getRegisterInfo(@Param("uniqueNumber") String uniqueNumber);
}
