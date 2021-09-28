package com.zw.platform.service.infoconfig;

import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.Group;

import java.util.List;


public interface InfoFastInputService {
    /**
     * 获取车辆信息
     * @return
     * @throws Exception
     */
    List<VehicleInfo> getVehicleInfoList() throws Exception;

    /**
     * 获取人员信息
     * @return
     * @throws Exception
     */
    List<VehicleInfo> getPeopleInfoList() throws Exception;

    /**
     * 获取物品信息
     * @return
     * @throws Exception
     */
    List<ThingInfo> getThingInfoList() throws Exception;

    /**
     * 获取终端信息
     * @return
     * @throws Exception
     */
    List<DeviceInfo> getdeviceInfoList() throws Exception;

    /**
     * 获取终端信息(人员)
     * @return
     * @throws Exception
     */
    List<DeviceInfo> getDeviceInfoListForPeople() throws Exception;

    /**
     * 获取sim卡信息
     * @return
     * @throws Exception
     */
    List<SimcardInfo> getSimcardInfoList() throws Exception;

    List<Group> getgetGroupList() throws Exception;

    /**
     * 扫码录入随机生成车牌号
     * @param sim
     * @author hujun
     * @return
     */
    String getRandomNumbers(String sim, int monitorType) throws Exception;
}
