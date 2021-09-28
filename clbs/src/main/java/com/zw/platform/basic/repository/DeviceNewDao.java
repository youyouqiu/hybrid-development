package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.domain.DeviceInfoDo;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.query.DeviceQuery;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DeviceNewDao {
    /**
     * 新增终端
     * @param deviceDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean addDevice(DeviceDO deviceDO);

    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean addDeviceByBatch(@Param("list") Collection<DeviceDO> deviceList);

    /**
     * 根据终端型号id查询终端型号信息
     */
    TerminalTypeInfo getTerminalTypeInfo(String terminalTypeId);

    /**
     * 根据终端编号查询终端信息
     * @param deviceNumber
     * @return
     */
    DeviceInfoDo findByDeviceNumber(String deviceNumber);

    DeviceInfoDo findDeviceById(@Param("id") String id);

    /**
     * 查询所有的终端类型
     * TODO 通过对比了几个平台后发现, 这里最多的一个平台就只有100中类型
     * @return list
     */
    List<VideoChannelSetting> getDeviceChannelSetting();

    /**
     * 验证终端号是否存在
     * @param deviceNumber
     * @param id
     * @return
     */
    String getNoRepeatDeviceNumber(@Param("deviceNumber") String deviceNumber, @Param("id") String id);

    /**
     * 根据终端id查询绑定监控对象id
     * @param deviceId
     * @return
     */
    String getBindMonitorId(@Param("deviceId") String deviceId);

    /**
     * @param deviceIds
     * @return
     */
    List<String> getBindMonitorIds(@Param("deviceIds") List<String> deviceIds);

    /**
     * 修改终端
     * @param deviceDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean updateDevice(DeviceDO deviceDO);

    /**
     * 删除终端
     * @param id
     * @return
     */
    void deleteDeviceById(String id);

    /**
     * 批量删除终端
     * @param ids ids
     * @return 是否操作成功
     */
    boolean deleteByBatch(@Param("ids") Collection<String> ids);

    /**
     * 高级查询终端id
     */
    Set<String> advancedQueryGetDeviceId(DeviceQuery deviceQuery);

    /**
     * 根据终端id查询终端列表新
     * @param ids
     * @return
     */
    List<DeviceListDO> getDeviceList(@Param("ids") Collection<String> ids);

    /**
     * 根据终端编号查询绑定的监控对象id
     * @param deviceNumber
     * @return
     */
    String getMonitorIdByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * 获取所有存在的Mac地址
     * @return
     */
    Set<String> getAllMacAddress();

    /**
     * 查询终端厂商
     */
    List<String> getTerminalManufacturer();

    /**
     * 查询全部的终端型号(不包括模糊搜索)
     */
    List<TerminalTypeInfo> getAllTerminalType();

    Set<String> findAllDeviceNumber();

    /**
     * 根据id更新终端号
     * @param id
     * @param number
     * @return
     */
    boolean updateNumber(@Param("id") String id, @Param("number") String number);

    /**
     * 获取指定企业下的终端id
     * @param orgId
     * @return
     */
    Set<String> getOrgDeviceIds(@Param("orgId") String orgId);

    /**
     * 根据终端号查找终端
     * @param deviceNumbers 终端号 为空查询全部
     * @return 终端列表
     */
    List<DeviceDTO> getByNumbers(@Param("deviceNumbers") Collection<String> deviceNumbers);

    /**
     * 批量修改终端
     * @param deviceList 终端信息列表
     * @return 操作结果
     */
    @ImportDaoLock(ImportTable.ZW_M_DEVICE_INFO)
    boolean updateDeviceByBatch(@Param("list") Collection<DeviceDO> deviceList);

    /**
     * 获取终端列表顺序-按时间升序
     * @return 按时间升序的终端ID列表
     */
    List<String> getSortList();

    /**
     * 通过终端id获取
     * @param deviceIds
     * @return
     */
    List<DeviceDTO> getDeviceListByIds(@Param("ids") Collection<String> deviceIds);

    /**
     * 更新制造商
     * @param deviceDTO
     */
    void updateDeviceManufacturer(DeviceDTO deviceDTO);

    /**
     * 根据车辆名查询终端通讯类型
     * @return
     */
    String getDeviceTypeByBrand(String brand);

    /**
     * 根据人名查询终端通讯类型
     * @return
     */
    String getDeviceTypeByPnumber(String peopleNumber);

    /**
     * @param id
     * @return
     */
    DeviceInfo findById(@Param("id") String id);

    /**
     * @param deviceNumber
     * @return
     */
    DeviceInfo findDeviceByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * 根据用户查询其组织下的设备
     * @param groupId
     * @param query
     * @return
     * @author Fan Lu
     */
    List<Map<String, Object>> findDeviceByUser(@Param("groupList") List<String> groupId,
        @Param("param") com.zw.platform.domain.basicinfo.query.DeviceQuery query);

    /**
     * 查询设备及其组织
     * @param id
     * @return
     * @author Fan Lu
     */
    Map<String, Object> findDeviceGroupById(String id);

    DeviceInfo findbyDevice(String deviceNumber);

    /**
     * 根据终端编号查询终端组织id
     */
    String fingGroupIdByDeviceNumber(String deviceNumber);

}
