package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.app.entity.appOCR.VehicleDrivingLicenseUploadEntity;
import com.zw.app.entity.appOCR.VehiclePhotoUpLoadEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoUploadEntity;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReport;
import com.zw.platform.domain.basicinfo.AdministrativeDivisionsInfo;
import com.zw.platform.domain.basicinfo.AssignmentInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.BatchUpdateVehicleForm;
import com.zw.platform.domain.basicinfo.form.SynchronizeVehicleForm;
import com.zw.platform.domain.basicinfo.form.UserVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.basicinfo.form.VehicleGroupForm;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.dto.ConfigMonitorDTO;
import com.zw.platform.domain.reportManagement.VehicleOperationStatusReport;
import com.zw.platform.domain.statistic.DictionaryInfo;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.util.imports.BusinessScopeConfigForm;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车辆Dao
 * @author wangying
 */
@Deprecated
public interface VehicleDao {
    /**
     * 根据条件查询记录总数
     * @param query 查询参数
     * @return intfindParmId
     */
    int countByParams(@Param("condition") VehicleQuery query);

    /**
     * 查询车辆信息
     * @param query 查询参数
     * @return 车辆信息
     */
    List<VehicleInfo> findVehicle(VehicleQuery query);

    List<Map<String, String>> findAllVehicleGroupId();

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void updateVehicleGroupId(@Param(value = "list") List<Map<String, String>> list);

    /**
     * 查询用户权限的车 + 用户所属组织及下级组织的车 + 用户所创建的车
     * @param userId   用户Id
     * @param userName 用户名称
     * @param groupId  组织Id
     * @param query    查询参数
     * @return List<Map < String, Object>>
     */
    List<Map<String, Object>> findVehicleWithUserId(@Param("userId") String userId, @Param("userName") String userName,
        @Param("groupList") List<String> groupId, @Param("param") VehicleQuery query);

    /**
     * 查询车辆，（查询用户权限的车+用户所属组织及下级组织的车）
     * @param userId  用户Id
     * @param groupId 组织Id
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleByUserAndGroup(@Param("userId") String userId,
        @Param("groupList") List<String> groupId);

    /**
     * 查询车辆及关联关系
     * @param groupId 组织Id
     * @return List<Map < String, Object>>
     * @author wangying
     */
    List<VehicleInfo> findVehicleGroup(@Param("groupId") String groupId);

    /**
     * 查询所有已绑定车辆
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findAllVehicleGroup();

    /**
     * 根据车辆id查询组id
     * @param vehicleId 车辆Id
     * @return String
     * @author wangying
     */
    List<String> findOrgByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据userId查询该user权限内的车辆
     * @param userId 用户Id
     * @param query  查询参数
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleUser(@Param("userId") String userId, @Param("param") VehicleQuery query);

    /**
     * 根据userId查询该user权限内的车辆以及终端，sim卡的map
     * @param userId 用户Id
     * @param query  查询参数
     * @return List<Map < String, Object>>
     * @author wangying
     */
    List<Map<String, Object>> findVehicleUserMap(@Param("userId") String userId, @Param("param") VehicleQuery query);

    /**
     * 根据userId查询该user权限内的车辆, 并且车辆已绑定
     * @param userId 用户Id
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleBindUser(@Param("userId") String userId);

    /**
     * 根据车辆编号查询车辆信息
     * @param vehicleNumber 车辆编号
     * @return 车辆信息
     */
    VehicleInfo findVehicleByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);

    /**
     * 根据车辆id查询车辆
     * @param id 车辆ID
     * @return 车辆信息
     */
    VehicleInfo findVehicleById(@Param("id") String id);

    /**
     * 根据车牌号查询车辆
     * @param brand 车牌号
     * @return VehicleInfo
     * @Title: findVehicleByBrand
     * @author Liubangquan
     */
    VehicleInfo findVehicleByBrand(@Param("brand") String brand);

    /**
     * 根据人员编号查询人员监控对象详细信息
     * @param brand 人员编号
     * @return 监控对象信息
     */
    List<Map<String, String>> findPeopleByNumber(String brand);

    /**
     * 根据车牌号、颜色找车辆ID
     * @param brand 车牌号
     * @param color 颜色
     * @return 车辆ID
     */
    String findvehicleByBrandAndColor(@Param("brand") String brand, @Param("color") Integer color);

    /**
     * 新增车辆
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addVehicle(VehicleForm vehicleForm);

    /**
     * 批量新增车辆
     * @param vehicleForm 车辆集合
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addVehicleByBatch(@Param("list") Collection<VehicleForm> vehicleForm);

    List<VehicleForm> findAll();

    /**
     * 通过车牌号查询id和车牌号
     * @param brands 车牌号
     * @return List<VehicleForm>
     */
    List<VehicleForm> findIdAndBrandsByBrands(@Param("brands") Collection<String> brands);

    /**
     * 查询车辆信息和绑定
     * @return list
     */
    List<ConfigMonitorDTO> findAllVehicleConfig();

    /**
     * 修改车辆
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicle(VehicleForm vehicleForm);

    /**
     * 修改车辆车牌号
     * @param form 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleBrand(VehicleForm form);

    /**
     * 批量修改车辆
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean batchUpdateVehicle(BatchUpdateVehicleForm vehicleForm);

    /**
     * 修改车辆燃油信息
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleFuelType(VehicleForm vehicleForm);

    /**
     * 删除车辆
     * @param id 车辆Id
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteVehicleById(String id);

    /**
     * 批量删除
     * @param ids 车辆Ids
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteVehicleByBatch(String[] ids);

    /**
     * 新增用户和车的关联
     * @param form bean
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addUserVehicle(UserVehicleForm form);

    /**
     * 批量新增用户和车的关联
     * @param list 需要增加的集合
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addUserVehicleByBatch(List<UserVehicleForm> list);

    /**
     * 根据userId删除用户和车组的关联
     * @param userId 用户Id
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserAssignByUserId(String userId);

    /**
     * 根据多个userId删除用户和车组的关联
     * @param userList 用户Id集合
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserAssignByUsers(@Param("userList") Collection<String> userList);

    /**
     * 根据用户id和车组id删除车组和用户的关联
     * @param userId         用户Id
     * @param assignmentList 企业Id集合
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserAssByUserAndAssign(@Param("userId") String userId,
        @Param("assignmentList") List<String> assignmentList);

    /**
     * 根据从业人员Id查询车辆信息
     * @param professionalId 从业人员Id
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleByProfessionalId(@Param("professionalId") String professionalId);

    /**
     * 根据vehicleId删除用户和车的关联
     * @param vehicleId 车辆Id
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserVehicleByVehicleId(String vehicleId);

    /**
     * 根据车组id查询车和人的关联
     * @param assignmentId 组织Id
     * @param userList     用户Id集合
     * @return List<String>
     * @author wangying
     */
    List<String> findUserAssignByAid(@Param("assignmentId") String assignmentId,
        @Param("userList") List<String> userList);

    /**
     * 根据车组id及userList 删除用户和车的关联
     * @param userIds      用户Id集合
     * @param assignmentId 企业Id
     * @return boolean
     * @author wangying
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserAssignByUsersAid(@Param("assignmentId") String assignmentId,
        @Param("userIds") List<String> userIds);

    /**
     * 查询车辆类型
     * @return List<VehicleType>
     */
    List<VehicleType> getVehicleTypeList();

    /**
     * 根据车牌查找车辆
     * @param brand 车牌号
     * @return 车辆信息
     */
    VehicleInfo findByVehicle(String brand);

    /**
     * 查询用户权限的车 + 用户所属组织及下级组织的车（并绑定）
     * @param userId  用户Id
     * @param groupId 组织Id
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleByUserGroupConfig(@Param("userId") String userId,
        @Param("groupList") List<String> groupId);

    /**
     * 根据车辆id查询车辆及设备信息
     * @param id 车辆Id
     * @return Map<String, Object>
     * @author wangying
     */
    Map<String, Object> findVehicleDeviceByVId(String id);

    /**
     * 根据车辆id查询监控对象及设备信息
     * @param id 车辆Id
     * @return Map<String, Object>
     * @author wangying
     */
    Map<String, Object> findMonitorDeviceByVId(String id);

    /**
     * 根据人员id查询设备信息
     * @param id 人员id
     * @return
     */
    Map<String, Object> findDeviceByPeopleId(String id);

    /**
     * 根据物品id查询设备信息
     * @param id 人员id
     * @return
     */
    Map<String, Object> findDeviceByThingId(String id);

    String getDeviceNumber(String id);

    /**
     * 新增车辆与组织关联关系
     * @param groupForm 车辆与组织关联bean
     * @return 是否成功
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addVehicleGroup(@Param("groupForm") List<VehicleGroupForm> groupForm);

    /**
     * 修改车辆与组织关联关系
     * @param id 车辆Id
     * @return 是否成功
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleGroup(String id);

    /**
     * 批量删除车辆与组织的绑定关系
     * @param ids 车辆Id数组
     * @return 是否成功
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteVehicleGroupByBatch(String[] ids);

    /**
     * 批量导入车辆组织关联
     * @param list 车辆组织关系List
     * @return 是否导入成功
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_C_VEHICLE_GROUP)
    boolean addVehicleGroupByBatch(@Param("list") Collection<VehicleGroupForm> list);

    /**
     * 查询车辆是否已经绑定组织
     * @param id 车辆Id
     * @return 是否绑定组织
     * @author Fan Lu
     */
    int getIsBand(String id);

    /**
     * 通过机型id查询是否绑定车辆
     * @param id
     * @return
     */
    int getIsBandVehicleByBrandModelsId(String id);

    /**
     * 批量通过机型id查询是否绑定车辆
     * @param ids
     * @return
     */
    int getIsBandVehicleByBrandModelsIdByBatch(List<String> ids);

    /**
     * 根据设备编号查询车辆
     * @param deviceNumber 设备编号
     * @return VehicleInfo
     * @author wangying
     */
    VehicleInfo findVehicleByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * 3.0平台增值服务功能调用 查询车辆，（查询用户权限的车+用户所属组织及下级组织的车）
     * @param userId    用户Id
     * @param groupList 分组List
     * @return List<VehicleInfo>
     * @author Liubangquan
     */
    List<VehicleInfo> findVehicleByUserAndGroup_for_vas(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 3.0平台增值服务调用方法：查询车辆，（查询用户权限的车+用户所属组织及下级组织的车）
     * @param userId    用户Id
     * @param groupList 分组List
     * @return 车辆信息
     */
    List<VehicleInfo> findVehicleByUserAndGroup_for_vas_mileage(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 查询用户权限分组下的车（当前登录用户所属企业及下级企业）+未分组的车（当前登录用户所属企业及下级企业）
     * @param userId  用户Id
     * @param groupId 分组Id
     * @param query   查询参数
     * @return List<Map < String, Object>>
     * @author FanLu
     */
    List<Map<String, Object>> findAllVehicle(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("param") VehicleQuery query);

    /**
     * 查询所有的车（有分组的和游离的车），无权限控制
     * @return List<Map < String, Object>>
     * @author FanLu
     */
    List<Map<String, Object>> findVehicleWithOutAuth();

    /**
     * 获取所有的车、SIM卡，终端 Id和车牌号，SIM卡号，终端号，便于模糊搜索
     * @return List<Map < String, String>>
     */
    List<Map<String, String>> findAllVehicelSimDevice();

    /**
     * 查询用户权限分组下的车（当前登录用户所属企业）+未分组的车（当前登录用户所属企业）
     * @param userId  用户Id
     * @param groupId 分组Id
     * @param query   查询参数
     * @return List<Map < String, Object>>
     * @author FanLu
     */
    List<Map<String, Object>> findGroupVehicle(@Param("userId") String userId, @Param("groupId") String groupId,
        @Param("param") VehicleQuery query);

    /**
     * 查询用户权限分组下的车（当前登录用户所属企业）
     * @param userId   用户Id
     * @param assignId 组织Id
     * @param query    查询参数
     * @return List<Map < String, Object>>
     * @author FanLu
     */
    List<Map<String, Object>> findAssignVehicle(@Param("userId") String userId, @Param("assignId") String assignId,
        @Param("param") VehicleQuery query);

    /**
     * 根据车辆ID查询车牌号
     * @param vehicleId 车辆Id
     * @return 车牌号
     * @author wjy
     */
    VehicleInfo vehicleIdByVehicle(String vehicleId);

    /**
     * 根据车辆ID查询终端ID
     * @param vehicleId 车辆Id
     * @return 终端Id
     * @author wjy
     */
    String getDevice(String vehicleId);

    /**
     * 根据车辆ID查询SIM卡ID
     * @param vehicleId 车辆Id
     * @return SIM卡Id
     */
    String getSIMcard(String vehicleId);

    /**
     * 查询用户权限的车(订阅所有车)
     * @param userId    用户Id
     * @param groupList 组织Id List
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findAllSendVehicle(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 根据分组id查询分组下的车辆总数
     * @param assignmentId 分组Id
     * @return int
     * @author Liubangquan
     */
    int findVehicleCountByAssignment(String assignmentId);

    /**
     * 获得所有分组id及其分组下监控对象的数量
     * @return List<AssignmentInfo>
     * @author hujun
     */
    List<AssignmentInfo> getAllAssignmentVehicleNumber(List<String> aids);

    /**
     * 修改时，查询非当前车辆以外，车牌号是否重复
     * @param id    车辆Id
     * @param brand 车牌
     * @return 车辆信息
     */
    VehicleInfo findVehicleForBrandRep(@Param("id") String id, @Param("brand") String brand);

    /**
     * 根据车牌查询组织Id
     * @param brand 车牌
     * @return 组织Id
     */
    String getGroupID(String brand);

    /**
     * 根据车牌查询车辆类型
     * @param brand 车牌号
     * @return 类型
     */
    String getFuelType(String brand);

    /**
     * 根据组织id获取组织下所有分组下的车辆数量
     * @param groupId 组织Id
     * @return int
     */
    int getCountByGroupId(@Param("groupId") String groupId);

    /**
     * 根据组织id获取组织下所有分组下的车辆id list
     * @param groupId 组织Id
     * @return 车辆Id List
     */
    List<String> findVehicleByGroupAssign(@Param("groupId") String groupId);

    /**
     * 根据车辆id集合查询车辆实体 list
     * @param ids 车辆id
     * @return 车辆实体
     */
    List<VehicleInfo> findVehicleByIds(@Param("ids") List<String> ids);

    /**
     * 根据车辆Id查询参数下发Id
     * @param vid 车辆id
     * @return 参数下发id
     */
    List<String> findParmId(String vid);

    /**
     * 根据车牌号查询参数下发状态
     * @param simId 车牌号
     * @return 参数下发状态
     */
    List<Map<String, String>> findParmStatus(String simId);

    /**
     * 根据车牌号查询参数下发状态
     * @param brand 人员编号
     * @return 参数下发状态
     */
    List<Map<String, String>> findParmStatusByPeople(String brand);

    /**
     * 根据车辆ID查询车辆所在组织ID
     * @param id 车辆id
     * @return 组织id
     */
    String findVehicleGroupId(String id);

    /**
     * 查询所有车辆用途
     * @return 车辆用途名称List
     */
    List<String> findAllVehiclePurpose();

    /**
     * 新增车辆用途
     * @param vehiclePurposes 车辆用途实体
     * @return 是否成功
     */
    boolean addVehiclePurpose(VehiclePurposeForm vehiclePurposes);

    /**
     * 车辆用途分页查询
     * @param query 查询参数
     * @return 分页结果
     */
    Page<VehiclePurposeQuery> findVehiclePurpose(VehiclePurposeQuery query);

    /**
     * 根据id获取车辆用途实体
     * @param id 车辆用途Id
     * @return 车辆用途实体
     */
    VehiclePurpose getPurposeById(String id);

    /**
     * 修改车辆用途
     * @param form 车辆用途实体
     * @return 是否成功
     */
    boolean updateVehiclePurpose(VehiclePurposeForm form);

    /**
     * 根据id删除车辆用途
     * @param id 车辆用途id
     * @return 是否删除成功
     */
    boolean deletePurpose(String id);

    /**
     * 根据id批量删除车辆用途
     * @param ids 车辆用途id集合
     * @return 是否删除成功
     */
    boolean deleteVehiclePurposeMuch(List<String> ids);

    /**
     * 查询车辆用途导出
     * @return 导出车辆用途集合
     */
    List<VehiclePurposeForm> findExport();

    /**
     * 批量导入车辆用途
     * @param importList 导入车辆用途集合
     * @return 是否导入成功
     * @author tangshunyu
     */
    boolean addVehiclePurposeMore(List<VehiclePurposeForm> importList);

    /**
     * 根据车辆用途名称查询车辆用途实体
     * @param purposeCategory 车辆用途名称
     * @return 车辆用途实体
     */
    List<VehiclePurpose> findVehiclePurposes(String purposeCategory);

    /**
     * 查询所有车辆用途
     * @return list
     */
    List<VehiclePurpose> findVehicleCategory();

    /**
     * 查询所有燃料类型
     * @return list
     */
    List<FuelType> findFuelType();

    /**
     * 查询所有燃料类型
     * @return list
     */
    List<String> findAllFuelType();

    /**
     * 根据车辆用途名称查询用途id
     * @param purposeCategory 车辆用途名称
     * @return 车辆用途Id
     */
    VehiclePurpose findPurposeIdByName(String purposeCategory);

    /**
     * 根据车辆用途id查询用途名称
     * @param id 车辆用途Id
     * @return 用途名称
     */
    String findVehiclePurposeById(String id);

    /**
     * 查询车辆颜色
     * @return 车牌和颜色对应List<Map>
     */
    List<Map<String, Object>> findVehicleColor();

    /**
     * 根据车牌号查询车牌颜色
     * @param brand 车牌号
     * @return 车牌颜色编号
     */
    String findColorByBrand(String brand);

    /**
     * 根据车辆Id查询车辆类型
     * @param vehicleId 车辆Id
     * @return 车辆类型
     */
    String getVehicleType(String vehicleId);

    /**
     * 车讯车辆数量
     * @param userId  用户Id
     * @param groupId 企业Id
     * @param query   查询参数
     * @return int
     */
    int getVehicleCount(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("param") VehicleQuery query);

    /**
     * 获取分组下车辆数量
     * @param userId   用户Id
     * @param assignId 分组Id
     * @param query    查询参数
     * @return int
     */
    int getAssignVehicleCount(@Param("userId") String userId, @Param("assignId") String assignId,
        @Param("param") VehicleQuery query);

    /**
     * 查询组织下车辆数量
     * @param userId  用户Id
     * @param groupId 组织Id
     * @param query   查询参数
     * @return int
     */
    int getGroupVehicleCount(@Param("userId") String userId, @Param("groupId") String groupId,
        @Param("param") VehicleQuery query);

    /**
     * 根据id查询车牌集合
     * @param ids 车辆Id集合
     * @return 车牌集合
     */
    List<String> findBrandsByIds(@Param("ids") Collection<String> ids);

    String findBrandById(@Param("vid") String vid);

    /**
     * 根据监控对象名称模糊匹配监控对象
     * @param userId  用户Id
     * @param groupId 组织Id
     * @param name    监控对象名称
     * @return 监控对象
     */
    List<VehicleInfo> findMonitorByName(@Param("userId") String userId, @Param("groupList") List<String> groupId,
        @Param("name") String name);

    /**
     * 根据车辆id查询SIM卡号、设备号、设备类型
     * @param vehicleId 车辆Id
     * @return 车辆信息实体
     */
    VehicleInfo findNumberByVid(@Param("vehicleId") String vehicleId);

    /**
     * 通过终端id查询绑定的健康对象信息
     * @param deviceId 终端id
     * @return VehicleInfo
     */
    VehicleInfo getVehicleInfoByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据车辆id查询车辆及其燃油类型
     */
    VehicleInfo findVehicleAndFuelTypeByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据车id获取上级分组的id
     * @param vid
     * @param userId
     * @return
     */
    String getAssignmentIdByVid(@Param("vid") String vid, @Param("userId") String userId);

    /**
     * 清空货运字段
     * @param id vehicle_id
     * @return boolean
     * @author zhouzongbo
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateFreightTransportVehicleInfo(String id);

    /**
     * 清空工程机械字段
     * @param id vehicle_id
     * @return boolean
     * @author zhouzongbo
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateConstructionMachineryVehicleInfo(String id);

    List<String> findVehicleIdsByGroupId(String uuid);

    /**
     * 得到道路运输证达到提前提醒天数条件的车辆
     * @return
     */
    List<Object> getVehicleIdsByWillExpireRoadTransport();

    /**
     * 得到道路运输已经过期的车辆
     * @return
     */
    List<Object> getVehicleIdsByAlreadyExpireRoadTransport();

    /**
     * 得到行驶证达到提前提醒天数条件的车辆
     * @return
     */
    List<Object> getVehicleIdsByWillExpireLicense();

    /**
     * 得到行驶证已经过期的车辆
     * @return
     */
    List<Object> getVehicleIdsByAlreadyExpireLicense();

    /**
     * 得到保养有效期到期的车辆
     * @return
     */
    List<Object> getVehicleIdsByMaintenanceExpired();

    /**
     * 得到保养里程数不为null的车辆
     * @return
     */
    List<Map<String, Object>> getVehicleIdsByMaintenanceMileageIsNotNull();

    /**
     * 查询危险品运输车的车辆
     * @param
     * @return
     */
    List<Map<String, String>> findTransportList(@Param("brand") String brand, @Param("list") List<String> list);

    /**
     * 获取组织下的车量数量
     * @return
     */
    List<Map<String, Object>> getVehicleCountForGroup();

    /**
     * 根据人员id查询车辆及设备信息
     * @param id
     * @return
     */
    Map<String, Object> findPeopleDeviceByVId(String id);

    /**
     * 根据物品id查询车辆及设备信息
     * @param id
     * @return
     */
    Map<String, Object> findThingDeviceByVId(String id);

    List<Map<String, String>> getAllVehPurpose();

    List<Map<String, String>> getVidAndPurposeIdMap(@Param("vids") List<String> vids);

    @MapKey("vehicleId")
    Map<String, ConfigList> getVehicleConfigMap(@Param("vids") List<String> vids);

    /**
     * 根据监控对象id查询绑定id
     */
    String getConfigIdByVehId(String monitorId);

    Set<String> findAllMidsBytype(Integer type);

    List<String> getVehicleIdByGroupId(@Param("groupId") List<String> groupId);

    /**
     * 根据企业id查询所有所属企业是此id的车辆(不包含物品和人员)
     * @param groupId
     * @return
     */
    List<Map<String, String>> getOwnedVehicleIdByGroupId(@Param("groupId") List<String> groupId);

    /**
     * 更新车辆行政区划信息
     * @param administrativeDivisionsInfo
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void updateVehicleADInfo(@Param("vid") String vehicleId,
        @Param("data") AdministrativeDivisionsInfo administrativeDivisionsInfo);

    /**
     * 查询车辆信息
     * @param vehicleIds
     * @return 车辆信息
     */
    List<VehicleForm> findVehicleFormByIds(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 查询需要初始化行政区划代码的车辆id
     * @return
     */
    List<String> findNeedInitDivisionCodeVehicleIds();

    /**
     * 修改车辆
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateAdministrativeDivisionsCode(VehicleForm vehicleForm);

    /**
     * 查询车辆的运营状态信息
     */
    List<VehicleOperationStatusReport> getVehicleOperationStatusById(@Param("vehicleId") List<String> vehicleId);

    /**
     * 查询车辆运输证信息
     */
    VehicleTransportInfoUploadEntity getVehicleTransportNumber(String vehicleId);

    /**
     * 查询车辆行驶证信息
     */
    VehicleDrivingLicenseUploadEntity getVehicleDrivingLicense(String vehicleId);

    /**
     * 更新车辆运输证信息
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleTransportNumberInfo(VehicleTransportInfoUploadEntity entity);

    /**
     * 获取车辆信息
     * @param vehicleIds
     * @return
     */
    List<VideoCarouselReport> getVehicleInfoById(@Param("list") List<String> vehicleIds);

    /**
     * 更新车辆行驶证信息
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleDrivingLicenseInfo(VehicleDrivingLicenseUploadEntity entity);

    /**
     * 查询车辆图片
     */
    String getVehiclePhoto(String vehicleId);

    /**
     * 更新车辆照片
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehiclePhoto(VehiclePhotoUpLoadEntity entity);

    Set<String> searchVidsByParam(String param);

    List<Map<String, String>> getAllCargoGroupVids();

    /**
     * 通过车辆类型id 或者所有车辆类型为当前车辆车辆类型的车辆id
     * @param vehicleTypeId 车辆类型id
     * @return list
     */
    Set<String> getAllVehicleIdByVehicleTypeId(@Param("list") List<String> vehicleTypeId);

    Set<String> getMonitorNameIdByMonitorName(@Param("monitorName") String monitorName);

    List<String> findAllVehicleId();

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteVehicleByIds(@Param("list") List<String> vehicleIds);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteVehicleGroupByVehicleId(@Param("list") List<String> vehicleIds);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteAssignmentByVehicleId(@Param("list") List<String> vehicleIds);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteConfigByVehicleId(@Param("list") List<String> vehicleIds);

    Integer getMaintainMileage(@Param("vid") String vid);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateSynchronizeVehicle(@Param("form") SynchronizeVehicleForm form);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateMaintainMileage(@Param("vid") String vid, @Param("totalMile") Integer totalMile);

    List<Map<String, Object>> findGroupVehicleSize();

    Map<String, Object> findVehicleInfoById(String id);

    /**
     * 绑定经营范围
     * @param id
     * @param ids  经营范围 字典表的id
     * @param type 1 企业  2 车
     */
    void bindBusinessScope(@Param("id") String id, @Param("ids") List<String> ids, @Param("type") Integer type);

    boolean bindBusinessScopeForm(@Param("forms") Collection<BusinessScopeConfigForm> forms);

    void deleteBusinessScope(@Param("ids") List<String> ids);

    List<DictionaryInfo> findBusinessScope(String id);

    List<VehicleForm> findVehicleBusinessScope();

    /**
     * 通过车辆id获取组织Id
     * @param vehicleIds
     * @return
     */
    List<VehicleInfo> getVehicleByIds(@Param("ids") Collection<String> vehicleIds);
}
