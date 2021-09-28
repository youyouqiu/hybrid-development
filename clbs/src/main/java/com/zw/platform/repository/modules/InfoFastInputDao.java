package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 2016/07/28
 * wangjianyu
 */
public interface InfoFastInputDao {
    /**
     * 查询车辆信息
     * @param userId  当前用户uuid
     * @param groupId 当前用户能查看的分组id集合
     * @return
     */
    List<VehicleInfo> getVehicleInfoList(@Param("userId") String userId, @Param("groupList") List<String> groupId);

    /**
     * 查询人员信息
     * @param userId
     * @param groupId
     * @return
     */
    List<VehicleInfo> getPeopleInfoList(@Param("userId") String userId, @Param("groupList") List<String> groupId);

    //物品信息
    List<ThingInfo> getThingInfoList();

    /**
     * 查询终端信息
     * @return
     */
    List<DeviceInfo> getdeviceInfoList(@Param("groupList") List<String> groupList);

    /**
     * 查询终端信息（人员）
     * @return
     */
    List<DeviceInfo> getDeviceInfoListForPeople(@Param("groupList") List<String> groupList);

    /**
     * 查询sim卡信息
     * @param groupList
     * @return
     */
    List<SimcardInfo> getSimcardInfoList(@Param("groupList") List<String> groupList);

    //分组信息
    List<Group> getGroupList();

    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    void addConfigList(Config1Form form);

    /**
     * 查询扫码车牌号(模糊查询)
     * @param afterFiveSims (A-Z的一个字母),afterFiveSims(sim卡后5位)
     * @return 车牌号
     */
    List<String> findScanVehicleByBrand(String afterFiveSims);

    /**
     * 查询扫码人员编号(模糊查询)
     * @param afterFiveSims (A-Z的一个字母),afterFiveSims(sim卡后5位)
     * @return 车牌号
     */
    List<String> findScanPeopleByBrand(String afterFiveSims);

    /**
     * 查询扫码物品编号号(模糊查询)
     * @param afterFiveSims (A-Z的一个字母),afterFiveSims(sim卡后5位)
     * @return 车牌号
     */
    List<String> findScanThingByBrand(String afterFiveSims);
}
