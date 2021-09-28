package com.zw.platform.repository.modules;

import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.MonitorDeviceTypeDO;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.PersonnelInfo;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.EditConfig;
import com.zw.platform.domain.infoconfig.RelateConfig;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.domain.infoconfig.form.GroupForConfigForm;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import com.zw.platform.util.imports.lock.dto.ConfigDo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息配置Dao层接口 <p>Title: ConfigDao.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @author Liubangquan
 * @version 1.0
 * @date 2016年7月26日上午11:00:52
 */
@Deprecated
public interface ConfigDao {
    /*新sql, 按道理重新创建一个类会更好一点*/

    /**
     * 查询单张表的config信息
     * @param configId configId
     * @return ConfigForm
     */
    ConfigList getConfig(String configId);

    /*新sql*/

    Map<String, Object> get(final String id);

    /**
     * 根据config ID 查询信息
     * @param id id
     * @return ConfigList list
     */
    ConfigList findById(String id);

    /**
     * 根据config的id获取config_professional数据
     * @param id id
     * @return List<ProfessionalForConfigFrom> list
     * @throws BusinessException e
     * @Title: getProfessionalForConfigListByConfigId get
     * @author Liubangquan
     */
    List<ProfessionalForConfigFrom> getProfessionalForConfigListByConfigId(@Param("id") String id)
        throws BusinessException;

    /**
     * 获取车辆信息列表
     * @return List<VehicleInfo>
     * @throws @Title: getVehicleInfoList
     * @author Liubangquan
     */
    List<VehicleInfo> getVehicleInfoList(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("id") String id);

    /**
     * 获取人员信息列表
     * @return List<VehicleInfo>
     * @throws @Title: getVehicleInfoList
     * @author Liubangquan
     */
    List<VehicleInfo> getPersonelInfoList(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("id") String id);

    /**
     * 根据信息配置表id查询 车辆信息
     * @param configId
     * @return List<VehicleInfo>
     * @throws @Title: getVehicleInfoByConfigId
     * @author Liubangquan
     */
    List<VehicleInfo> getVehicleInfoByConfigId(String configId);

    /**
     * 获取当前用户下未绑定的车辆信息
     * @return
     */
    List<VehicleInfo> getVehicleInfoListForUser(@Param("userId") String userId);

    /**
     * 获取人员信息列表
     * @return List<Personnel>
     * @throws @Title: getPeopleInfoList
     * @author Liubangquan
     */
    List<Personnel> getPeopleInfoList();

    /**
     * 获取物品信息列表
     * @return List<ThingInfo>
     * @throws @Title: getThingInfoList
     * @author Liubangquan
     */
    List<ThingInfo> getThingInfoList();

    /**
     * 获取外设信息列表
     * @return List<DeviceInfo>
     * @throws @Title: getDeviceInfoList
     * @author Liubangquan
     */
    List<DeviceInfo> getDeviceInfoList(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取外设信息列表
     * @return List<DeviceInfo>
     * @throws @Title: getDeviceInfoList
     * @author Liubangquan
     */
    List<DeviceInfo> getDeviceInfoListForPeople(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取sim卡信息列表
     * @return List<SimcardInfo>
     * @throws @Title: getSimcardInfoList
     * @author Liubangquan
     */
    List<SimcardInfo> getSimcardInfoList(@Param("groupList") List<String> groupList, @Param("id") String id);

    /**
     * 获取从业人员信息列表
     * @return List<ProfessionalsInfo>
     * @throws @Title: getProfessionalsInfoList
     * @author Liubangquan
     */
    List<ProfessionalsInfo> getProfessionalsInfoList(@Param("groupList") List<String> list);

    /**
     * 根据从业人员姓名获取从业人员id
     * @param name
     * @return List<ProfessionalsInfo>
     * @Title: getProfessionalInfoByName
     * @author Liubangquan
     */
    List<ProfessionalsInfo> getProfessionalInfoByName(@Param("name") String name);

    /**
     * 新增车辆信息
     * @return boolean
     * @throws @Title: addVehicleInfo
     * @author Liubangquan
     */
    boolean addVehicleInfo(ConfigForm form);

    /**
     * 新增信息配置信息
     * @return boolean
     * @throws @Title: addConfig
     * @author Liubangquan
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean addConfig(ConfigForm form);

    boolean addConfigProfessionals(ProfessionalForConfigFrom professionalForConfigFrom);

    @ImportDaoLock(ImportTable.ZW_M_CONFIG_PROFESSIONALS)
    boolean addConfigProfessionalList(@Param("list") Collection<ProfessionalForConfigFrom> list);

    /**
     * 查询信息配置列表
     * @return List<ConfigList>
     * @throws @Title: findByPage
     * @author Liubangquan
     */
    List<ConfigList> find(@Param("param") final ConfigQuery query, @Param("userId") String userId,
        @Param("groupList") List<String> userOrgListId);

    /**
     * 查询所有车的绑定关系列表，无权限控制
     * @return List<ConfigList>
     * @throws @Title: findAllConfig
     * @author fanlu
     */
    List<ConfigList> findAllConfig();

    /**
     * 根据id删除config
     * @param id
     * @return int
     * @Title: delete
     * @author Liubangquan
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    int delete(String id);

    /**
     * 删除配置与从业人员关联表
     * @param id
     * @return int
     * @Title: deleteConfigProfessionals
     * @author Liubangquan
     */
    int deleteConfigProfessionals(String id);

    /**
     * 删除服务期限
     * @param configId
     * @return int
     * @throws @Title: deleteLifecycleByConfigId
     * @author Liubangquan
     */
    int deleteLifecycleByConfigId(String configId);

    /**
     * 批量导入
     * @param importList
     * @return boolean
     * @throws BusinessException
     * @Title: addConfigByBatch
     * @author Liubangquan
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean addConfigByBatch(@Param("importList") Collection<ConfigImportForm> importList);

    /**
     * 批量导入
     * @param importList
     * @return boolean
     * @throws BusinessException
     * @Title: addConfigByBatch
     * @author Liubangquan
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean addConfigTransportByBatch(List<ConfigTransportImportForm> importList);

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
     * 修改
     * @param configForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean updateConfig(ConfigForm configForm);

    List<ConfigDetailsQuery> configDetailsall();

    /**
     * 查询vehileId 通过deviceId
     * @param deviceId
     * @return
     */
    String getVehicleIdByDeviceID(String deviceId);

    VehicleInfo getVehicleByDeviceNew(final String id);

    PeopleInfo getPeopleInfoByDevice(final String id);

    ThingInfo getThingInfoByDevice(final String id);

    PersonnelInfo getPeopleByDevice(final String id);

    ConfigForm getIsBand(@Param("vid") String vid, @Param("did") String did, @Param("cid") String cid,
        @Param("pid") String pid);

    List<ConfigForm> getBandList(@Param("vids") List<String> vids, @Param("dids") List<String> dids,
        @Param("cids") List<String> cids, @Param("pids") List<String> pids);

    List<ConfigForm> getIsBands(@Param("vid") String vid, @Param("did") String did, @Param("cid") String cid,
        @Param("pid") String pid);

    List<DeviceInfo> getDeviceNumByVid(final String id);

    List<GroupForConfigForm> isBnadP(@Param("id") String id);

    List<GroupForConfigForm> isBnadPeo(@Param("id") String id);

    List<GroupForConfigForm> isBnadG(@Param("id") String id);

    /**
         * @param vehicleIds
     * @return List<SimcardInfo>
     * @throws @Title: 根据车辆id查询sim卡数据
     * @author wangying
     */
    List<SimcardInfo> getSimcardByVid(@Param("vehicleIds") final List<String> vehicleIds);

    /**
     * 查询组织下是否有device
     * @param id
     * @return 是否有device
     * @author Fan Lu
     */
    public int isBandDevice(String id);

    /**
     * 查询组织下是否有simcard
     * @param id
     * @return 是否有simcard
     * @author Fan Lu
     */
    public int isBandSimcard(String id);

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
     * 根据车辆id获取其组织信息
     * @param configId
     * @return List<String>
     * @Title: getGroupListByConfigId
     * @author Liubangquan
     */
    List<String> getGroupListByConfigId(@Param("configId") String configId);

    /**
     * 修改判断
     */
    EditConfig getBrand(String band);

    EditConfig getDevice(String id);

    EditConfig getSim(String id);

    /**
     * 根据车辆id查询其分组信息
     * @param vehicleId 车辆Id
     * @return List<String>
     * @author Liubangquan
     */
    List<String> getAssignmentVehicleByVid(String vehicleId);

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
     * @author wangying
     */
    Integer isBandAssignment(String id);

    /**
     * 判断是否绑定围栏
     * @param id group_id
     * @return int
     * @author wangying
     */
    Integer isBandFence(String id);

    /**
     * 根据车辆id查询sim卡信息
     * @param vehicleId 车辆Id
     * @return SimcardInfo
     * @author wangying
     */
    SimcardInfo getSimByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据企业id和分组名称获取分组id
     * @param orgId          所属企业id
     * @param assignmentName 分组名称
     * @return String
     * @author Liubangquan
     */
    String getAssignmentIdByOrgId_name(@Param("orgId") String orgId, @Param("assignmentName") String assignmentName);

    /**
     * 根据Id获取设备编号
     * @param id configId
     * @return 设备编号
     */
    String getDeviceNumber(@Param("id") String id);

    /**
     * 根据Id获取设备类型
     * @param id configId
     * @return 设备类型
     */
    String getDeviceType(@Param("id") String id);

    /**
     * 根据Id获取设备编号
     * @param id 设备Id
     * @return 设备编号
     */
    String getDNumber(@Param("id") String id);

    /**
     * 根据信息配置id获取车辆id
     * @param configId 绑定表Id
     * @return String 车辆Id
     * @author Liubangquan
     */
    String getVehicleIdByConfigId(String configId);

    /**
     * 根据车辆id获取车辆终端绑定表id
     * @param vehicleId 车辆Id
     * @return String 绑定表Id
     * @author Liubangquan
     */
    String getConfigIdByVehicleId(String vehicleId);

    /**
     * 根据车辆Id查询设备编号
     * @param id 车辆Id
     * @return 设备编号
     */
    String getDeNumber(@Param("id") String id);

    /**
     * 根据绑定id集合查询绑定表
     * @param ids 绑定Id
     * @return ConfigDetailsQuery
     */
    List<ConfigDetailsQuery> getConfigByConfigIds(@Param("ids") List<String> ids);

    /**
     * 查询上线率
     * @param vehicleList 车辆Id
     * @return ConfigList
     */
    List<ConfigList> findOnline(@Param("vehicleList") List<String> vehicleList);

    /**
     * 根据关联id查询关联信息(车辆)
     * @param configId 关联Id
     * @return 关联信息(车辆)
     */
    RelateConfig findRelateConfigById(@Param("id") String configId);

    /**
     * 根据关联id查询关联信息(人员)
     * @param configId 关联Id
     * @return 关联信息(人员)
     */
    RelateConfig findPeopleRelateConfigById(@Param("id") String configId);

    /**
     * 根据车辆id查询关联信息
     * @param vehicleIds 车辆Id
     * @return 关联信息
     */
    List<RelateConfig> findRelateInfoByVId(@Param("ids") List<String> vehicleIds);

    /**
     * 根据关联id查询监控对象类型
     * @param configId 绑定表Id
     * @return 监控对象类型
     */
    String findMonitorTypeById(@Param("id") String configId);

    /**
     * 根据终端id查询sim卡号
     * @param deviceId 终端Id
     * @return SIM卡信息
     */
    SimcardInfo getSimByDeviceId(String deviceId);

    /**
     * 根据车辆id查找设备id
     * @param ids 车辆Id
     * @return 设备Id
     */
    Set<String> getDeviceIdsByVIds(@Param("ids") Set<String> ids);

    /**
     * 根据车辆id查找设备id
     * @param id 车辆Id
     * @return 设备Id
     */
    String getDeviceIdByVId(@Param("id") String id);

    /**
     * 根据终端id查找车辆id
     * @param id 终端Id
     * @return 车辆Id
     */
    String getVehicleIdByDeviceId(@Param("id") String id);

    String getDeviceTypeByVId(@Param("id") String id);

    /**
     * 根据车辆id查询车辆config信息
     * @param vehicleIds 车辆Id列表
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findVehicleInfoByVehicleId(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 获取用户权限下的绑定数据数量
     * @param userId        用户Id
     * @param userOrgListId 用户全向列表
     * @param query         查询参数
     * @return int
     */
    Integer getConfigCount(@Param("userId") String userId, @Param("groupList") List<String> userOrgListId,
        @Param("param") ConfigQuery query);

    /**
     * 根据Sim卡Id获取绑定的车辆Id
     * @param simCardId SIM卡Id
     * @return 车辆Id
     */
    Map<String, String> findVehicleIdBySimCardId(String simCardId);

    /**
     * 根据Sim卡Id获取绑定的人Id
     * @param simCardId
     * @return
     */
    Map<String, String> findPeopleIdBySimCardId(String simCardId);

    /**
     * 根据Sim卡Id获取绑定的物Id
     * @param simCardId
     * @return
     */
    Map<String, String> findThingIdBySimCardId(String simCardId);

    /**
     * 注: （公用）查询人车物绑定信息, 不包含权限验证
     * 根据监控对象(人、车、物)ID查询监控对象及设备信息
     * @param monitoryId 监控对象Id
     * @return Map
     * @author zhouzb
     */
    Map<String, Object> findMonitoryConfigByMonitoryId(String monitoryId);

    Map<String, String> findVehicleInfoBySimCardNumber(String simCard);

    /**
     * 根据车辆ID查询对应的configId
     * @param vehicleIdList vehicleIdList
     * @return list
     */
    List<String> findConfigIdByVehicleIdList(@Param("vehicleIdList") List<String> vehicleIdList);

    /**
     * 根据配置ID查找监控对象ID
     * @param configIdList configIdList
     * @return list
     */
    List<ConfigList> findConfigListByConfigIdList(@Param("configIdList") List<String> configIdList);

    /**
     * 根据configId, 删除人员绑定关系
     * @param configIds
     */
    void deleteProfessionalsByConfigIds(@Param("configIds") List<String> configIds);

    /**
     * 删除监控对象与分组的绑定关系
     * @param monitorIds monitorIds
     */
    void deleteMonitorAssignmentByMonitorIds(@Param("monitorIds") Set<String> monitorIds);

    /**
     * 批量删除信息配置
     * @param configIds configIds
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    void deleteByConfigIds(@Param("configIds") List<String> configIds);

    /**
     * 删除服务周期
     * @param lifecycleSet lifecycleSet
     */
    void deleteLifecycleById(@Param("lifecycleSet") Set<String> lifecycleSet);

    /**
     * 根据监控对象id获取绑定表id
     */
    List<String> getConfigIdByMonitorIds(@Param("monitorIds") List<String> monitorIds);

    /**
     * 根据id查询simId
     */
    String getSimIdById(@Param("id") String id);

    Map<String, String> getVehicleByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    List<String> getAssignmentNamesByVehicleId(@Param("vehicleId") String vehicleId);

    List<Map<String, String>> getProfessionalInfoByConfigId(@Param("configId") String configId);

    String getCategoryIconByCategoryId(@Param("categoryId") String categoryId);

    List<String> findBandSimcardConfigIds(@Param("simCardIds") Set<String> simCardIds);

    /**
     * 通过车牌号获取车辆密码
     * @param brand 车牌号
     * @return map
     */
    Map<String, String> getConfigByBrand(@Param("brand") String brand);

    /**
     * 更新车辆密码
     * @param vehiclePassword 车辆密码
     * @return boolean true or false
     * @Param vehicleId 车辆id
     */
    boolean updateVehiclePassword(@Param("vehiclePassword") String vehiclePassword,
        @Param("vehicleId") String vehicleId);

    /**
     * 通过车辆id，获取配置信息
     * @param vid 车辆id
     * @return list l
     */
    List<Map<String, String>> getConfigByVehicle(String vid);

    /**
     * 通过车辆id，从业人员id 获取指定一条数据。
     * @param cardNumber 从业人员 从业资格证号
     * @param vid        车辆id
     * @return map m
     */
    Map<String, String> getConfigByPidAndVid(@Param("cardNumber") String cardNumber, @Param("vid") String vid);

    /**
     * 查询所有的从业人员信息
     * @return list
     */
    List<ProfessionalsInfo> findAllProfessional();

    /**
     * 批量新增绑定关系
     * @param configs configs
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_CONFIG)
    boolean addConfigForms(@Param("configs") Collection<ConfigDo> configs);

    /**
     * 根据终端id查询入网状态
     * @param deviceIds
     * @return
     */
    @MapKey("keyName")
    Map<String, BaseKvDo<String, Integer>> getNetWorkByDeviceIds(@Param("deviceIds") Collection<String> deviceIds);

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
}
