package com.zw.platform.basic.repository;

import com.zw.app.entity.appOCR.VehicleDrivingLicenseUploadEntity;
import com.zw.app.entity.appOCR.VehiclePhotoUpLoadEntity;
import com.zw.app.entity.appOCR.VehicleTransportInfoUploadEntity;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReport;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.export.VehicleExportDTO;
import com.zw.platform.domain.basicinfo.AssignmentInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.SynchronizeVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.reportManagement.VehicleOperationStatusReport;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车辆管理DAO层
 * @author zhangjuan
 * @date 2020/9/25
 */
public interface NewVehicleDao {
    /**
     * 获取车辆按时间的排序ID
     * @return 顺序ID
     */
    List<String> getSortList();

    /**
     * 获取车辆信息初始化缓存的查询列表
     * @param ids 车辆ID
     * @return 车辆信息集合
     */
    List<VehicleDTO> initCacheList(@Param("ids") List<String> ids);

    /**
     * 获取车辆的个性化图标
     * @return 个性化图标
     */
    List<VehicleDTO> getIconList();

    /**
     * 获取车辆详情--信息全面
     * @param id id
     * @return 车辆详情
     */
    VehicleDTO getDetailById(@Param("id") String id);

    /**
     * 根据车牌号获取车辆信息--信息全面
     * @param brand brand
     * @return 车辆详情
     */
    VehicleDTO getDetailByBrand(@Param("brand") String brand);

    /**
     * 批量获取车辆详情--信息全面
     * 若查询数据量太大不建议使用该方法，消耗性能
     * @param ids id集合
     * @return 车辆详情
     */
    List<VehicleDTO> getDetailByIds(@Param("ids") Collection<String> ids);

    /**
     * 车辆添加
     * @param vehicleDO vehicleDO
     * @return true 添加成功 false 添加失败
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean insert(VehicleDO vehicleDO);

    /**
     * 根据ID获取车辆信息
     * @param id id
     * @return VehicleDO
     */
    VehicleDO getById(@Param("id") String id);

    /**
     * 根据车牌号获取监控对象
     * @param brand 车牌号
     * @return VehicleDO
     */
    VehicleDO getByBrand(@Param("brand") String brand);

    /**
     * 根据车辆ID获取车辆导出列表
     * @param ids ids
     * @return 导出列表
     */
    List<VehicleExportDTO> getExportList(@Param("ids") Collection<String> ids);

    /**
     * 车辆更新
     * @param vehicleDO vehicleDO
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean update(VehicleDO vehicleDO);

    /**
     * 车辆删除
     * @param ids ids
     * @return 删除数量
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    int delete(@Param("ids") Collection<String> ids);

    /**
     * 批量修改车辆信息
     * @param ids      车辆ID
     * @param vehicle  车辆信息，修改的内容非空
     * @param standard 车辆标准 0：通用，1：货运 2：工程机械
     * @return 更新条数
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    int batchUpdate(@Param("ids") Collection<String> ids, @Param("vehicle") VehicleDO vehicle,
        @Param("standard") Integer standard);

    /**
     * 查询所有的车辆车牌号和ID
     * @return 车辆信息列表
     */
    List<VehicleDO> findAll();

    /**
     * 查询企业下的车辆
     * @param orgIds 企业id
     * @return VehicleDO
     */
    List<VehicleDO> findByOrgIds(@Param("orgIds") Collection<String> orgIds);

    /**
     * 根据车辆名称和车牌颜色查询
     * @param brand      车辆名称
     * @param plateColor 车牌颜色
     * @return VehicleDO
     */
    VehicleDO findByBrandAndPlateColor(@Param("brand") String brand, @Param("plateColor") Integer plateColor);

    /**
     * 查询车辆运输证信息
     * @param vehicleId 车辆id
     * @return VehicleTransportInfoUploadEntity
     */
    VehicleTransportInfoUploadEntity getVehicleTransportNumber(String vehicleId);

    /**
     * 更新车辆运输证信息
     * @param entity entity
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleTransportNumberInfo(VehicleTransportInfoUploadEntity entity);

    /**
     * 查询车辆行驶证信息
     * @param vehicleId 车辆id
     * @return VehicleDrivingLicenseUploadEntity
     */
    VehicleDrivingLicenseUploadEntity getVehicleDrivingLicense(String vehicleId);

    /**
     * 更新车辆行驶证信息
     * @param entity entity
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleDrivingLicenseInfo(VehicleDrivingLicenseUploadEntity entity);

    /**
     * 更新车辆照片
     * @param entity entity
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehiclePhoto(VehiclePhotoUpLoadEntity entity);

    /**
     * 获取车辆信息
     * @param vehicleIds vehicleIds
     * @return VideoCarouselReport
     */
    List<VideoCarouselReport> getVehicleInfoById(@Param("list") List<String> vehicleIds);

    /**
     * 获取所有货运车辆id
     * @return List
     */
    List<Map<String, String>> getAllCargoGroupVehicleIds();

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
     * 根据车牌查找车辆
     * @param brand 车牌号
     * @return 车辆信息
     */
    VehicleInfo findByVehicle(String brand);

    /**
     * 同步车辆信息
     * @param form  form
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateSynchronizeVehicle(@Param("form") SynchronizeVehicleForm form);

    /**
     * 通过机型id查询是否绑定车辆
     * @param id id
     * @return int
     */
    int getIsBandVehicleByBrandModelsId(String id);

    /**
     * 查询用户权限的车(订阅所有车)
     * @param userId    用户Id
     * @param groupList 组织Id List
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findAllSendVehicle(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 根据车牌查询车辆类型
     * @param brand 车牌号
     * @return 类型
     */
    String getFuelType(String brand);

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
    List<String> findParamId(String vid);

    /**
     * 根据车牌号查询参数下发状态
     * @param simId 车牌号
     * @return 参数下发状态
     */
    List<Map<String, String>> findParamStatus(String simId);

    /**
     * 根据人员编号查询人员监控对象详细信息
     * @param brand 人员编号
     * @return 监控对象信息
     */
    List<Map<String, String>> findPeopleByNumber(String brand);

    /**
     * 根据id查询车牌集合
     * @param ids 车辆Id集合
     * @return 车牌集合
     */
    List<String> findBrandsByIds(@Param("ids") Collection<String> ids);

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
     * 3.0平台增值服务调用方法：查询车辆，（查询用户权限的车+用户所属组织及下级组织的车）
     * @param userId    用户Id
     * @param groupList 分组List
     * @return 车辆信息
     */
    List<VehicleInfo> findVehicleByUserAndGroupForVasMileage(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 3.0平台增值服务功能调用 查询车辆，（查询用户权限的车+用户所属组织及下级组织的车）
     * @param userId    用户Id
     * @param groupList 分组List
     * @return List<VehicleInfo>
     * @author Liubangquan
     */
    List<VehicleInfo> findVehicleByUserAndGroupForVas(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 查询企业下的监控对象id
     * @param groupId  groupId
     * @return List<String>
     */
    List<String> findVehicleIdsByGroupId(String groupId);

    /**
     * 根据分组id查询分组下的车辆总数
     * @param assignmentId 分组Id
     * @return int
     * @author Liubangquan
     */
    int findVehicleCountByAssignment(String assignmentId);

    /**
     * 获得所有分组id及其分组下监控对象的数量
     * @param assingmentIds assingmentIds
     * @return List<AssignmentInfo>
     * @author hujun List
     */
    List<AssignmentInfo> getAllAssignmentVehicleNumber(List<String> assingmentIds);

    /**
     * 修改车辆燃油信息
     * @param vehicleForm 车辆信息
     * @return 是否成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateVehicleFuelType(VehicleForm vehicleForm);

    /**
     * 查询车辆的运营状态信息
     * @param vehicleId vehicleId
     * @return List
     */
    List<VehicleOperationStatusReport> getVehicleOperationStatusById(@Param("vehicleId") List<String> vehicleId);

    /**
     * 批量添加车辆信息
     * @param vehicles 车辆信息
     * @return 是否添加成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean addByBatch(@Param("list") Collection<VehicleDO> vehicles);

    /**
     * 添加普货企业车辆表
     * @param list list
     * @return 是否添加成功
     */
    boolean addCargoGroupVehicle(@Param("list") Collection<CargoGroupVehicleDO> list);

    /**
     * 根据车牌号获取车辆基本信息
     * @param brands 车牌号集合 为空查询全部
     * @return 车辆基本信息
     */
    List<MonitorBaseDTO> getByBrands(@Param("brands") Collection<String> brands);

    /**
     * 根据企业获取车辆Id集合
     * @param orgId 车辆所属企业ID
     * @param ids   车辆ID集合 （空：企业下全部的车辆，不为空：根据企业Id进行过滤）
     * @return 企业下的车辆ID集合
     */
    Set<String> getByOrgId(@Param("orgId") String orgId, @Param("ids") Collection<String> ids);

    /**
     * 更新车牌号
     * @param id    车辆ID
     * @param brand 车牌号
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean updateBrand(@Param("id") String id, @Param("brand") String brand);

    /**
     * 根据车牌号获取扫字开头的车牌号
     * @param brand 车牌号
     * @return 车牌号
     */
    List<String> getScanByName(@Param("brand") String brand);

    /**
     * 更新车辆图标
     * @param ids    车辆Id集合
     * @param iconId 图标Id
     * @return 是否操作成功
     */
    boolean updateIcon(@Param("ids") Collection<String> ids, @Param("iconId") String iconId);

    /**
     * 查车辆信息
     * @param ids
     * @return
     */
    List<VehicleDO> getVehicleListByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过车牌获取车辆Id
     * @param brand
     * @return
     */
    String getIdByBrand(@Param("brand") String brand);

    /**
     * 通过车牌和颜色获取车辆Id
     * @param brand
     * @param vehicleColor
     * @return
     */
    String getIdByBrandAndColor(@Param("brand") String brand, @Param("vehicleColor") Integer vehicleColor);

    /**
     * 通过终端编号获取车信息
     * @param deviceNumber
     * @return
     */
    VehicleDTO getVehicleDTOByDeviceNumber(@Param("deviceNumber") String deviceNumber);

    /**
     * 通过车牌获取绑定信息
     * @param brand
     * @return
     */
    VehicleDTO getBindVehicleDTOByBrand(@Param("brand") String brand);

    /**
     * 通过车牌获取车辆的所属企业ID
     * @param vehicleNo
     * @return
     */
    String getOrgIdByBrand(@Param("brand") String vehicleNo);

    /**
     * 通过组织获取车辆Id
     * @param orgId
     * @return
     */
    Set<String> getVehicleIdsByOrgId(@Param("orgId") String orgId);

    /**
     * 通过终端id获取车辆信息
     * @param deviceId
     * @return
     */
    VehicleDTO getVehicleInfoByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 通过车辆id获取部分字段
     * @param vehicleId
     * @return
     */
    VehicleDTO getPartFieldById(@Param("id") String vehicleId);

    /**
     * 得到行驶证已经过期的车辆
     * @return
     */
    List<String> getVehicleIdsByAlreadyExpireLicense();

    /**
     * 得到行驶证达到提前提醒天数条件的车辆
     * @return
     */
    List<String> getVehicleIdsByWillExpireLicense();

    /**
     * 得到道路运输已经过期的车辆
     * @return
     */
    List<String> getVehicleIdsByAlreadyExpireRoadTransport();

    /**
     * 得到道路运输证达到提前提醒天数条件的车辆
     * @return
     */
    List<String> getVehicleIdsByWillExpireRoadTransport();

    /**
     * 得到保养有效期到期的车辆
     * @return
     */
    List<String> getVehicleIdsByMaintenanceExpired();

    /**
     * 得到保养里程数不为null的车辆
     * keyName-车辆id
     * firstVal-里程
     * @return
     */
    @MapKey("keyName")
    Map<String, BaseKvDo<String, Integer>> getVehicleIdsByMaintenanceMileageIsNotNull();

    /**
     * 根据运营类别查找车辆
     * @param vehiclePurposeId 运营类别Id
     * @return 车辆Id集合
     */
    Set<String> getByVehiclePurposeId(@Param("vehiclePurposeId") String vehiclePurposeId);

    Set<String> findAllMidsBytype(Integer type);

    /**
     * 获取分组下符合809转发条件的车辆id集合
     * @param groupIds 分组ID
     * @return 车辆Id集合
     */
    Set<String> get809ForwardIds(@Param("groupIds") Collection<String> groupIds);

    /**
     * 获取车辆基本信息
     * @param vehicleIds 车辆id
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> getVehicleByIds(@Param("ids") Collection<String> vehicleIds);

    /**
     * 查询所有的车辆id
     * @return
     */
    List<String> findAllVehicleId();

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteAssignmentByVehicleId(@Param("list") List<String> vehicleIds);

    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    void deleteConfigByVehicleId(@Param("list") List<String> vehicleIds);

    /**
     * 根据车辆类型
     * @param vehicleTypeId 车辆类型Id
     * @return 车辆id集合
     */
    Set<String> getByVehicleTypeId(@Param("vehicleTypeId") String vehicleTypeId);
}
