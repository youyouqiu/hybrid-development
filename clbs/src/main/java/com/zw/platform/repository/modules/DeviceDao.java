package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.form.DeviceForm;
import com.zw.platform.domain.basicinfo.form.DeviceGroupForm;
import com.zw.platform.domain.basicinfo.query.DeviceQuery;
import com.zw.platform.domain.infoconfig.dto.ConfigMonitorDTO;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 终端管理Dao
 * @author wangying
 */
@Deprecated
public interface DeviceDao {

    /**
     * 查询终端信息
     */
    List<DeviceInfo> findDevice();

    /**
     * 新增终端
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean addDevice(DeviceForm deviceForm);

    /**
     * 批量新增终端
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean addDeviceByBatch(@Param("list") Collection<DeviceForm> deviceForm);

    /**
     * 导入信息配置修改终端
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean updateDeviceByBatch(@Param("list") Collection<DeviceForm> deviceForm);

    /**
     * 修改终端
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean updateDevice(DeviceForm deviceForm);

    /**
     * 修改终端号（用于信息配置修改页面）
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean updateDeviceNumber(DeviceForm deviceForm);

    /**
     * 删除车辆
     * @param id
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean deleteDeviceById(String id);

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean deleteDeviceByBatch(String[] ids);

    /**
     * @param deviceNumber
     * @return
     */
    DeviceInfo findDeviceByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * @param id
     * @return
     */
    DeviceInfo findDeviceById(@Param("id") String id);

    List<DeviceInfo> findDeviceByIds(@Param("ids") List<String> ids);

    /**
     * 根据用户查询其组织下的设备
     * @param groupId
     * @param query
     * @return
     * @author Fan Lu
     */
    List<Map<String, Object>> findDeviceByUser(@Param("groupList") List<String> groupId,
        @Param("param") DeviceQuery query);

    /**
     * 查询所有设备，无权限控制
     * @return
     * @author Fan Lu
     */
    List<Map<String, Object>> findAllDevice();

    /**
     * 新增终端与组织关联关系
     * @param deviceForm
     * @return
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_GROUP)
    boolean addDeviceGroup(DeviceGroupForm deviceForm);

    /**
     * 修改 终端与组织关联关系
     * @param deviceForm
     * @return
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_GROUP)
    boolean updateDeviceGroup(DeviceGroupForm deviceForm);

    /**
     * @param ids
     * @return boolean
     * @Description:批量删除终端与组织的关联关系
     * @exception:
     * @author: wangying
     * @time:2017年1月18日 下午3:49:21
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_GROUP)
    boolean deleteDeviceGroupByBatch(String[] ids);

    /**
     * 查询设备及其组织
     * @param id
     * @return
     * @author Fan Lu
     */
    Map<String, Object> findDeviceGroupById(String id);

    /**
     * 批量导入设备组织关联
     * @param formList
     * @return
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_GROUP)
    boolean addDeviceGroupByBatch(@Param("list") Collection<DeviceGroupForm> formList);

    DeviceInfo findbyDevice(String deviceNumber);

    /**
     * 查询device是否已经绑定组织
     * @param id id
     * @return 是否绑定组织
     * @author Fan Lu
     */
    public int getIsBand(String id);

    /**
     * 根据设备id查询设备名称
     * @param id
     * @return
     */
    String getDevice(String id);

    /**
     * 查询所有绑定的车
     * @return
     */
    List<DeviceInfo> findAllBindDevice(String type);

    List<DeviceInfo> findAllBindPerson(String type);

    int cheackIsBand(String id);

    /**
     * 修改设备时查询设备编号是否重复(排除当前设备编号)
     * @param id 设备id，deviceNumber 设备编号
     * @return
     */
    DeviceInfo findIsExist(@Param("id") String id, @Param("deviceNumber") String deviceNumber);

    boolean updateChannelID(String channelID, String deviceNumber);

    String getChannelIDByVid(String vid);

    /**
     * 根据终端id修改通讯类型
     * @param did
     * @param deviceType
     * @return boolean
     * @throws
     * @Title: updateDeviceType
     * @author Liubangquan
     */
    boolean updateDeviceType(String did, String deviceType);

    /**
     * 根据终端id修改功能类型
     * @param did
     * @param functionalType
     * @return boolean
     * @throws
     * @Title: updateDeviceFunctionalType
     * @author Liubangquan
     */
    boolean updateDeviceFunctionalType(String did, String functionalType);

    String findDeviceGroupId(String id);

    /**
     * 根据终端编号查询终端组织id
     */
    String fingGroupIdByDeviceNumber(String deviceNumber);

    /**
     * 根据车辆id查询终端组织id
     */
    String fingGroupIdByVehicleId(String vehicleId);

    int getDeviceCount(@Param("groupList") List<String> userOrgListId, @Param("param") DeviceQuery query);

    /**
     * 根据车辆id查询终端通讯类型
     * @return
     * @throws Exception
     */
    String getDeviceTypeByVid(String vehicleId);

    /**
     * 根据车辆名查询终端通讯类型
     * @return
     * @throws Exception
     */
    String getDeviceTypeByBrand(String brand);

    /**
     * 根据人名查询终端通讯类型
     * @return
     * @throws Exception
     */
    String getDeviceTypeByPnumber(String peopleNumber);

    List<Map<String, String>> groupAndDeviceMap(@Param(value = "deviceIds") List<String> deviceIds);

    List<Map<String, String>> deviceIdAndGroupId(@Param(value = "deviceIds") List<String> deviceIds);

    /**
     * 根据设备id获取鉴权码
     * @param deviceId
     * @return
     */
    String getDeviceAuthCode(@Param(value = "deviceId") String deviceId);

    /**
     * 根据终端厂商名称查询终端型号名称
     * @param name
     * @return
     */
    List<String> getTerminalTypeNameByFacturerName(String name);

    TerminalTypeInfo geTerminalTypeInfoByName(String name);

    /**
     * 获得终端的终端型号
     * @param deviceId 终端id
     * @return TerminalTypeInfo
     */
    TerminalTypeInfo getTerminalTypeInfoDeviceId(@Param("deviceId") String deviceId);

    TerminalTypeInfo getTerminalTypeInfoBy(@Param("terminalType") String terminalType,
        @Param("terminalManufacturer") String terminalManufacturer);

    /**
     * 0100注册时信息不一致修改制造商id和终端型号
     * @param form
     * @return
     */
    boolean updateDeviceManufacturer(DeviceForm form);

    /**
     * key: 终端厂商和终端型号, value: id
     * @return
     */
    List<TerminalTypeInfo> getAllTermialTypeKey();

    /**
     * 通过终端类型、终端厂商、终端型号获取终端id
     * @param deviceType           终端类型
     * @param terminalManufacturer 终端厂商
     * @param terminalType         终端型号
     * @param groupId              终端所属组织uuid
     * @return list集合
     */
    Set<String> advancedQueryGetDeviceId(@Param("deviceType") String deviceType,
        @Param("terminalManufacturer") String terminalManufacturer, @Param("terminalType") String terminalType,
        @Param("groupId") String groupId);

    /**
     * 获取所有存在的Mac地址
     * @return
     */
    Set<String> getAllMacAddress();

    /**
     * 终端以及终端信息
     * @return list
     */
    List<ConfigMonitorDTO> findAllDeviceConfig();

    /**
     * 查询所有的终端类型
     * TODO 通过对比了几个平台后发现, 这里最多的一个平台就只有100中类型
     * @return list
     */
    List<VideoChannelSetting> findAllTerminalType();
}
