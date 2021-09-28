package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.ConfigProfessionalDO;
import com.zw.platform.basic.domain.MonitorDeviceTypeDO;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.PersonnelInfo;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.GroupForConfigForm;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息配置DAO层
 * @author zhangjuan
 */
public interface NewConfigDao {
    /**
     * 获取绑定监控对象的缓存配置
     * @param configIds 信息配置ID
     * @return 监控对象ID
     */
    List<String> getSortList(@Param("configIds") Collection<String> configIds);

    /**
     * 绑定从业人员
     * @param professionalList 从业人员列表
     * @return 添加条数
     */
    boolean bindProfessional(@Param("list") Collection<ConfigProfessionalDO> professionalList);

    /**
     * 绑定从业人员
     * @param professionalConfig 从业人员列表
     * @return 添加条数
     */
    boolean bindSingleProfessional(@Param("professionalConfig") ConfigProfessionalDO professionalConfig);

    /**
     * 解绑从业人员
     * @param configIds 配置信息ID
     * @return 操作是否成功
     */
    boolean unBindProfessional(@Param("configIds") Collection<String> configIds);

    /**
     * 根据信息配置ID获取绑定的从业人员ID
     * @param configId 信息配置ID
     * @return 从业人员Id
     */
    List<String> getProfessionalIdByConfigId(@Param("configId") String configId);

    /**
     * 添加
     * @param configDO 信息配置信息
     * @return 操作是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean insert(ConfigDO configDO);

    /**
     * 更新信息配置
     * @param configDO 信息配置信息
     * @return 操作是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean update(ConfigDO configDO);

    /**
     * 获取监控对象配置
     * @param configId 信息配置ID
     * @return 信息配置详情
     */
    ConfigDO getById(@Param("id") String configId);

    /**
     * 根据监控对象id获取信息配置
     * @param monitorId 监控对象ID
     * @return 信息配置详情
     */
    ConfigDO getByMonitorId(@Param("monitorId") String monitorId);

    /**
     * 根据监控对象id获取信息配置
     * @param monitorIds 监控对象IDs
     * @return 信息配置详情
     */
    List<ConfigDO> getByMonitorIds(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 批量删除信息配置
     * @param configIds configIds
     * @return 删除条数
     */
    int delete(@Param("configIds") Collection<String> configIds);

    /**
     * 批量插入信息配置
     * @param configList 信息配置
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean addByBatch(@Param("configList") Collection<ConfigDO> configList);

    /**
     * 获取监控对象、终端以及SIM卡是否被绑定
     * @param monitorId monitorId
     * @param simCardId simCardId
     * @param deviceId  deviceId
     * @return 绑定的信息配置ID
     */
    Set<String> getIsBind(@Param("monitorId") String monitorId, @Param("simCardId") String simCardId,
        @Param("deviceId") String deviceId);

    /**
     * 通过车辆id，获取配置信息
     * @param vid 车辆id
     * @return list l
     */
    List<Map<String, String>> getConfigByVehicle(String vid);

    /**
     * 批量更新信息配置表里的对讲信息ID
     * @param configList 信息配置列表
     * @return 操作是否成功
     */
    boolean updateIntercomInfoId(@Param("configList") Collection<ConfigDO> configList);

    /**
     * 通过通讯类型查询监控对象id
     * @param deviceTypes 协议类型
     * @return 监控对象id
     */
    Set<String> getMoIdByDeviceTypes(@Param("deviceTypes") Collection<String> deviceTypes);

    /**
     * 查询通讯类型下的车辆
     * @param deviceTypes 协议类型
     * @return List<MonitorProtocolTypeDO>
     */
    List<MonitorDeviceTypeDO> getMonitorByDeviceTypes(@Param("deviceTypes") Collection<String> deviceTypes);

    /**
     * 通过监控对象名称模糊查询监控对象id
     * @param fuzzyMoName 模糊的名称
     * @return Set<String>
     */
    Set<String> getMoIdsByFuzzyMoName(String fuzzyMoName);

    /**
     * 查询企业下的监控对象id
     * @param orgIds 企业id
     * @return Set<String>
     */
    Set<String> getMoIdsByOrgIds(@Param("orgIds") Collection<String> orgIds);

    /**
     * 通过监控对象类型 过滤监控对象id
     * @param moIds  需要过滤的健康对象id
     * @param moType 监控对象类型
     * @return Set<String>
     */
    Set<String> filterMoIdsByMoType(@Param("moIds") Collection<String> moIds, @Param("moType") String moType);

    /**
     * 根据监控对象获取终端ID
     * @param monitorIds 监控对象ID
     * @return 设备ID
     */
    Set<String> getDeviceIdByMonitorId(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 查询上线率
     * @param vehicleList 车辆Id
     * @return ConfigList
     */
    List<ConfigList> findOnline(@Param("vehicleList") List<String> vehicleList);

    /**
     * 通过车辆id，从业人员id 获取指定一条数据。
     * @param cardNumber 从业人员 从业资格证号
     * @param vid        车辆id
     * @return map m
     */
    Map<String, String> getConfigByPidAndVid(@Param("cardNumber") String cardNumber, @Param("vid") String vid);

    /**
     * 查询监控独享id通过deviceId
     * @param deviceId  deviceId
     * @return String
     */
    String getVehicleIdByDeviceId(String deviceId);

    /**
     * 获取人员信息列表
     * @return List<VehicleInfo>
     * @author Liubangquan
     */
    List<VehicleInfo> getPersonelInfoList(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("id") String id);

    /**
     * 根据信息配置表id查询 车辆信息
     * @param configId  configId
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> getVehicleInfoByConfigId(String configId);

    /**
     * 获取物品信息列表
     * @return List<ThingInfo>
     * @author Liubangquan
     */
    List<ThingInfo> getThingInfoList();

    /**
     * 获取外设信息列表
     * @param groupList  groupList
     * @param id id
     * @return List<DeviceInfo>
     */
    List<DeviceInfo> getDeviceInfoList(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取外设信息列表
     * @param groupList  groupList
     * @param id id
     * @return List<DeviceInfo>
     */
    List<DeviceInfo> getDeviceInfoListForPeople(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取sim卡信息列表
     * @param groupList  groupList
     * @param id id
     * @return List<SimcardInfo>
     */
    List<SimcardInfo> getSimcardInfoList(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取从业人员信息列表
     * @param list list
     * @return List<ProfessionalsInfo>
     */
    List<ProfessionalsInfo> getProfessionalsInfoList(@Param("groupList") List<String> list);

    /**
     * 查询信息配置列表
     * @param query         query
     * @param userId        userId
     * @param userOrgListId userOrgListId
     * @return List<ConfigList>
     */
    List<ConfigList> find(@Param("param") final ConfigQuery query, @Param("userId") String userId,
        @Param("groupList") List<String> userOrgListId);

    /**
     * 查询详情
     * @return List<ConfigDetailsQuery>
     */
    List<ConfigDetailsQuery> configDetailsall();

    /**
     * 根据终端编号查询
     * @param deviceNumber  deviceNumber
     * @return Map<String, String>
     */
    Map<String, String> getVehicleByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * 查询车辆绑定的分组名称
     * @param vehicleId  vehicleId
     * @return List
     */
    List<String> getAssignmentNamesByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 查询绑定的从业人员信息
     * @param configId  configId
     * @return List
     */
    List<Map<String, String>> getProfessionalInfoByConfigId(@Param("configId") String configId);

    /**
     * 查询车辆类别图标
     * @param categoryId categoryId
     * @return String
     */
    String getCategoryIconByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 查询车辆信息
     * @param deviceNumber 终端编号
     * @return VehicleInfo
     */
    VehicleInfo getVehicleByDeviceNew(final String deviceNumber);

    /**
     * 查询人员信息
     * @param deviceNumber 终端编号
     * @return PeopleInfo
     */
    PeopleInfo getPeopleInfoByDevice(final String deviceNumber);

    /**
     * 查询物品信息
     * @param deviceNumber 终端编号
     * @return ThingInfo
     */
    ThingInfo getThingInfoByDevice(final String deviceNumber);

    /**
     * 查询人员信息
     * @param deviceNumber 终端编号
     * @return PersonnelInfo
     */
    PersonnelInfo getPeopleByDevice(final String deviceNumber);

    /**
     * 判断是否绑定
     * @param vid 车id
     * @param did 终端id
     * @param cid sim卡id
     * @param pid pid
     * @return ConfigForm
     */
    ConfigForm getIsBand(@Param("vid") String vid, @Param("did") String did, @Param("cid") String cid,
        @Param("pid") String pid);

    /**
     * 判断是否绑定
     * @param vid 车id
     * @param did 终端id
     * @param cid sim卡id
     * @param pid pid
     * @return List<ConfigForm>
     */
    List<ConfigForm> getIsBands(@Param("vid") String vid, @Param("did") String did, @Param("cid") String cid,
        @Param("pid") String pid);

    List<GroupForConfigForm> isBnadP(@Param("id") String id);

    List<GroupForConfigForm> isBnadG(@Param("id") String id);

    /**
     * 查询车辆详情
     * @param id
     * @return
     */
    ConfigDetailsQuery configDetails(final String id);

    /**
     * 查询人员详情
     * @param id
     * @return
     */
    ConfigDetailsQuery peopleConfigDetails(final String id);

    /**
     * 查询物品详情
     * @param id
     * @return
     */
    ConfigDetailsQuery thingConfigDetails(final String id);

    /**
     * 根据config的id获取config_professional数据
     * @param id id
     * @return List<ProfessionalForConfigFrom> list
     */
    List<ProfessionalForConfigFrom> getProfessionalForConfigListByConfigId(@Param("id") String id);

    /**
     * 查询组织下是否有device
     * @param id
     * @return 是否有device
     * @author Fan Lu
     */
    int isBandDevice(String id);

    /**
     * 查询组织下是否有simcard
     * @param id
     * @return 是否有simcard
     * @author Fan Lu
     */
    int isBandSimcard(String id);

    /**
     * 根据当前登录用户返回其组织架构以及有权限的车辆列表
     * @param isAdmin
     * @param userId
     * @param userName
     * @param userOrgListId
     * @return
     */
    List<ConfigDetailsQuery> getOrgAndVehicle(@Param("isAdmin") boolean isAdmin, @Param("userId") String userId,
        @Param("userName") String userName, @Param("groupList") List<String> userOrgListId);

    /**
     * 根据configId获取车辆分组信息
     * @param configId 绑定表Id
     * @return List<Assignment>
     * @author Liubangquan
     */
    List<Assignment> getAssignmentByConfigId(String configId);

    /**
     * 根据configId获取人员分组信息
     * @param configId configId
     * @return List<Assignment>
     * @author Liubangquan
     */
    List<Assignment> getPeopleAssignmentByConfigId(String configId);

    /**
     * 根据configId获取物品分组信息
     * @param configId
     * @return
     */
    List<Assignment> getThingAssignmentByConfigId(String configId);

    /**
     * 判断是否绑定分组
     * @param id group_id
     * @return int
     */
    Integer isBandAssignment(String id);

    /**
     * 根据车辆id获取车辆终端绑定表id
     * @param vehicleId 车辆Id
     * @return String 绑定表Id
     * @author Liubangquan
     */
    String getConfigIdByVehicleId(String vehicleId);

    /**
     * 根据绑定id集合查询绑定表
     * @param ids 绑定Id
     * @return ConfigDetailsQuery
     */
    List<ConfigDetailsQuery> getConfigByConfigIds(@Param("ids") List<String> ids);

    /**
     * 根据关联id查询监控对象类型
     * @param configId 绑定表Id
     * @return 监控对象类型
     */
    String findMonitorTypeById(@Param("id") String configId);

    /**
     * 根据车辆id查找设备id
     * @param ids 车辆Id
     * @return 设备Id
     */
    Set<String> getDeviceIdsByVIds(@Param("ids") Set<String> ids);

    /**
     * 根据终端id查询入网状态
     * @param deviceIds
     * @return
     */
    @MapKey("keyName")
    Map<String, BaseKvDo<String, Integer>> getNetWorkByDeviceIds(@Param("deviceIds") Collection<String> deviceIds);

    /**
     * 通过车牌号获取车辆密码
     * @param brand 车牌号
     * @return map
     */
    Map<String, String> getConfigByBrand(@Param("brand") String brand);

    /**
     * 更新车辆密码
     * @param vehiclePassword 车辆密码
     * @param vehicleId       vehicleId
     * @return boolean true or false
     */
    boolean updateVehiclePassword(@Param("vehiclePassword") String vehiclePassword,
        @Param("vehicleId") String vehicleId);
}
