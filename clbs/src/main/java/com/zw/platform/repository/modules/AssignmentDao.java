package com.zw.platform.repository.modules;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.AssignmentData;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.driverDiscernManage.VehicleAssignmentInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.infoconfig.dto.AssignmentVehicleDto;
import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * <p> Title: 分组管理Dao </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2016年10月10日上午9:07:35
 */
@Deprecated
public interface AssignmentDao {

    /**
     * 查询分组管理
     */
    List<Assignment> findAssignment(AssignmentQuery query);

    /**
     * 查询分组管理
     */
    List<Assignment> listAssignment(@Param("userId") String userId, @Param("query") AssignmentQuery query);

    /**
     * 根据车牌号查询分组管理
     *
     * @param list
     * @return
     */
    List<Assignment> findGroupNameByBrand(List<String> list);

    /**
     * 批量查询分组数据
     *
     * @param assignList 分组ID列表
     * @return 分组数据列表
     */
    List<Assignment> findAssignmentByBatch(@Param("assignList") List<String> assignList);

    /**
     * 查询用户分组权限（管理员）
     */
    List<Assignment> findUserAssignment(@Param("userId") String userId, @Param("orgIds") List<String> orgIds);


    /**
     * 根据车id查询分组
     */
    List<Assignment> findUserAssignmentByVehicleId(@Param("userId") String userId,
        @Param("vehicleIds") List<String> vehicleIds);


    /**
     * 查询用户分组权限（管理员1120优化）
     */
    List<Assignment> findUserAssignmentFilterName(@Param("userId") String userId,
                                                  @Param("groupList") List<String> groupList,
                                        @Param("name") String name);

    /**
     * 查询树节点下监控对象数量
     *
     * @param assignList  分组ids
     * @param monitorType 监控对象类型
     * @param deviceTypeSet  终端类型
     * @return
     */
    List<Assignment> findAssignmentNum(@Param("assignList") List<Assignment> assignList,
                                       @Param("monitorType") String monitorType,
                                       @Param("deviceTypeSet") Set<String> deviceTypeSet);

    /**
         *
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 新增分组
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean addAssignment(AssignmentForm form);

    /**
         *
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 新增分组与组织关联表
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_GROUP)
    boolean addGroupAssignment(AssignmentGroupForm form);

    /**
     * 查询所有用户分组权限（管理员）
     */
    List<AssignmentData> findAllUserAssignment(@Param("userIdList")
        Collection<String> userIdList, @Param("groupList") Collection<String> groupList);


    /**
         *
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 批量新增用户与分组的关联表
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean addAssignmentUserByBatch(@Param("list") Collection<AssignmentUserForm> list);

    /**
         *
     * @param id
     * @return Assignment
     * @throws @author wangying
     * @Title: 根据id查询分组
     */
    Assignment findAssignmentById(@Param("id") String id);

    /**
     * 加计数
     *
     * @param id
     * @return
     */
    Assignment findAssignmentByIdNum(@Param("id") String id);

    /**
         *
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 修改分组
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean updateAssignment(AssignmentForm form);

    /**
         *
     * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
         *
     * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id查询监控对象list
     */
    List<VehicleInfo> findMonitorByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
     * 根据分组ID列表查询监控对象list（不包括设备，SIM卡，从业人员等信息）
     *
     * @param assignmentIds 分组ID列表
     * @return 监控对象列表
     * @author chenfeng
     */
    List<VehicleInfo> findMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds,
                                                    @Param("query") String query,
                                                    @Param("deviceTypes") List<String> deviceTypes,
                                                    @Param("webType") Integer webType);

    /**
     * 根据分组ID列表查询监控对象list的数量（不包括设备，SIM卡，从业人员等信息）
     *
     * @param assignmentIds 分组ID列表
     * @return 监控对象列表
     * @author chenfeng
     */
    int countMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
                                                 @Param("deviceType") String deviceType,
                                                 @Param("webType") Integer webType);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询车辆id list
     */
    List<String> findVehicleIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
                                               @Param("deviceType") String deviceType);

    List<String> listVehiclesByAssignmentsIds(@Param("assignIds") List<String> assignIds);

    /**
     * 根据分组查询监控对象ID
     * @param assignIds assignIds
     * @return 监控对象ID集合
     */
    List<AssignmentVehicleDto> listMonitorIdByAssignmentsIds(@Param("assignIds") Collection<String> assignIds);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询监控对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findMonitorByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
                                                 @Param("deviceTypes") List<String> deviceTypes);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询监控对象id list
     */
    List<String> findMonitorIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds);

    /**
         *
     * @param assignmentIds 分组id
     * @param queryParam    模糊搜索值
     * @param queryType     模糊搜索类型
     *                      （name:监控对象名称；deviceNumber:终端编号；simcardNumber：sim卡编号；
     *                      assignName：分组名称； professional：从业人员名称）
     * @param deviceTypes   指定需要查询的终端协议类型集合
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 模糊查询监控对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findMonitorByAssignmentIdsFuzzy(@Param("assignmentIds") List<String> assignmentIds,
                                                      @Param("queryParam") String queryParam,
                                                      @Param("queryType") String queryType,
                                                      @Param("deviceTypes") List<String> deviceTypes);

    /**
         *
     * @param assignmentIds 分组id
     * @param queryParam    模糊搜索值
     * @param deviceTypes     模糊搜索类型
     *                      （name:监控对象名称；deviceNumber:终端编号；simcardNumber：
     *                      sim卡编号；assignName：分组名称； professional：从业人员名称）
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 模糊查询车辆对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findVehicleByAssignmentIdsFuzzy(@Param("assignmentIds") List<String> assignmentIds,
                                                      @Param("queryParam") String queryParam,
                                                      @Param("deviceTypes") List<String> deviceTypes,
                                                      @Param("webType") Integer webType);

    /**
         *
     * @param assignmentIds
     * @param deviceType
     * @return List<VehicleInfo>
     * @throws @author lifudong
     * @Title: 根据分组id list,设备类型 查询监控对象list
     */
    List<VehicleInfo> findMonitorByAssignmentIdsAndType(@Param("assignmentIds") List<String> assignmentIds,
                                                        @Param("deviceType") String deviceType);

    /**
         *
     * @param assignmentIds
     * @param deviceType
     * @return List<VehicleInfo>
     * @throws @author Fan Lu
     * @Title: 根据分组id list,设备类型 查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignIdAndType(@Param("assignmentIds") List<String> assignmentIds,
                                                   @Param("deviceType") String deviceType);

    int countVehicleByAssignIdAndType(@Param("assignmentIds") List<String> assignmentIds,
                                      @Param("deviceType") String deviceType);

    List<VehicleInfo> vehicleTruckTree(@Param("assignmentIds") List<String> assignmentIds);

    /**
         *
     * @param id
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean deleteAssignment(String id);

    /**
         *
     * @param assignmentId
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组与企业关联
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_GROUP)
    boolean deleteAssignmentGroupByAssId(String assignmentId);

    /**
         *
     * @param assignmentId
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组与user关联表
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean deleteAssignmentUserByAssId(String assignmentId);

    /**
         *
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组(批量)
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean deleteAssignmentByBatch(List<String> list);

    /**
         *
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组与企业关联(批量)
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_GROUP)
    boolean deleteAssignmentGroupByAssIdByBatch(List<String> list);

    /**
         *
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组与user关联表(批量)
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean deleteAssignmentUserByAssIdByBatch(List<String> list);

    // ------------add by liubq 2016-10-12 start-----------------------------------

    /**
     * 根据车辆id删除车辆分组信息
     *
     * @param vehicleId
     * @return boolean
     * @throws @author Liubangquan
     * @Title: deleteAssignmentVehicle
     */
    boolean deleteAssignmentVehicle(String vehicleId);

    /**
     * 新增车辆分组信息
     *
     * @param form
     * @return boolean
     * @throws @author Liubangquan
     * @Title: addAssignmentVehicle
     */
    boolean addAssignmentVehicle(AssignmentVehicleForm form);
    // ------------add by liubq end-----------------------------------

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_VEHICLE)
    boolean addAssignVehicleList(@Param("list") Collection<AssignmentVehicleForm> list);

    /**
     * 查询车辆分组
     *
     * @param vehicleId
     * @return
     */
    String getGroups(@Param("vehicleId") String vehicleId, @Param("userId") String userId);

    /**
     * 查询车辆类型
     *
     * @param vehicleId
     * @return
     */
    String getType(@Param("vehicleId") String vehicleId);

    /**
         *
     * @param vehicleId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据车辆id查询分组
     */
    List<Assignment> findAssignmentByVehicleId(@Param("vehicleId") String vehicleId, @Param("userId") String userId,
                                               @Param("groupList") List<String> groupList);

    /**
         *
     * @param assignmentId
     * @param vehicleList
     * @return boolean
     * @throws @author wangying
     * @Title: 根据车辆和分组删除车辆和分组的关联
     */
    boolean deleteAssignmentVehicleByVidAid(@Param("assignmentId") String assignmentId,
                                            @Param("vehicleList") List<String> vehicleList);

    /**
     * TODO 查询同一组织下是否有相同名称的分组
     *
     * @param name
     * @param groupId
     * @return Assignment
     * @throws @author wangying
     * @Title: findByNameForOneOrg
     */
    List<Assignment> findByNameForOneOrg(@Param("name") String name, @Param("groupId") String groupId);

    /**
     * 修改时，查询非当前车辆以外，查询统一组织下分组名称是否重复
     *
     * @param id
     * @param name
     * @param groupId
     * @return
     */
    List<Assignment> findOneOrgAssiForNameRep(@Param("id") String id, @Param("name") String name,
                                              @Param("groupId") String groupId);

    /**
         *
     * @param groupId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据组织查询组织下所有分组
     */
    List<Assignment> findAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 根据组织id列表查询组织列表下所有分组
     *
     * @param groupIdList 组织列表
     * @return 分组列表
     */
    List<Assignment> findAssignmentByGroupIdList(@Param("groupIdList") Collection<String> groupIdList);

    /**
         *
     * @param groupId
     * @param assignmentId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据组织查询组织下所有分组, 排除当前分组
     */
    List<Assignment> findAssignByGroupIdExpectVehicle(@Param("groupId") String groupId,
                                                      @Param("assignmentId") String assignmentId);

    /**
     * 批量新增分组
     *
     * @param list
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean addAssignmentByBatch(@Param("list") Collection<AssignmentForm> list);

    /**
     * 批量新增分组与组织关联表
     *
     * @param list
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_GROUP)
    boolean addGroupAssignmentByBatch(@Param("list") Collection<AssignmentGroupForm> list);

    /**
     * 删除车辆与分组绑定关系
     *
     * @param deleteVehiclePer
     * @return
     */
    boolean deleteVehiclePer(AssignmentVehicleForm deleteVehiclePer);

    /**
     * 批量新增车辆与分组的绑定关系
     *
     * @param addVehiclePer
     * @return
     */
    boolean addVehiclePer(@Param("list") List<AssignmentVehicleForm> addVehiclePer);

    /**
     * 查询分组组织ID
     *
     * @param id
     * @return
     */
    String findAssignsGroupId(String id);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询监控对象 车list
     */
    List<VehicleInfo> vehicleFindByAssignmentId(@Param("assignmentIds") List<String> assignmentIds,
                                                @Param("deviceTypeSet") Set<String> deviceTypeSet);

    /**
         *
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询监控对象人list
     */
    List<VehicleInfo> peopleFindByAssignmentId(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据传感器类型,分组id list 查询绑定传感器的监控对象list
     */
    List<VehicleInfo> findTempSensoreVehicleByAssignmentId(@Param("sensorType") int sensorType,
                                                           @Param("assignmentIds") List<String> assignmentIds);

    /**
         *
     * @param assignmentIds
     * @return List<String> 分组的所属企业
     * @throws @author fanlu
     * @Title: 根据分组id list 查询分组对应的所属企业 list
     */
    List<String> findAssignsGroupIds(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据分组id list 查询分组对应的所属企业 name
     *
     * @param assignmentIds
     * @return
     * @author fanlu
     */
    List<String> findAssignNames(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据监控对象类型id list 查询分组对应的所属企业 name
     *
     * @param vehicleIds
     * @return
     * @author yangyi
     */
    List<Assignment> getAssignsByMonitorId(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 根据监控对象类型和终端类型查询车辆数量
     *
     * @param monitorType
     * @param deviceType
     * @return
     */
    List<String> getMonitorCount(@Param("monitorType") String monitorType, @Param("deviceType") String deviceType,
                                 @Param("assignmentIds") List<String> assignmentList);

    /**
     * 查询用户权限下的所有分组id
     *
     * @param userId
     * @param groupList
     * @return
     */
    List<String> getAllAssignmentByGroupIds(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 查询当前企业存在的货运分组
     *
     * @param groupId
     * @return
     */
    List<AssignmentForm> getTransportAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 获取最后一个货运分组编号
     *
     * @param groupId
     * @return
     */
    Set<Integer> findTransportAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 计算当前企业下的分组数量(每个企业只能有100个分则)
     *
     * @param groupId groupId
     * @return
     */
    AssignmentForm getGroupHasAssignmentNumber(String groupId);

    List<AssignmentForm> getGroupAssignmentNumbers(@Param("list") Collection<String> groupIds);

    /**
     * 获取当前企业下的其他分组数量
     *
     * @param groupId
     * @return
     */
    Integer getGroupNotLikeFreightSize(@Param("groupId") String groupId);

    String getAssignPhoneByIds(@Param("assignIds") String assignIds);

    /**
     * 获取分组下符合809转发条件的分组id、车辆id集合
     *
     * @param assigns
     * @return
     * @throws Exception
     */
    List<Assignment> find809ForwardAssignmentNum(@Param("assigns") List<JSONObject> assigns)
        throws Exception;

    /**
     * 获取分组下符合809转发条件的车辆id集合
     *
     * @param assigns
     * @return
     * @throws Exception
     */
    Set<String> get809ForwardCount(@Param("assigns") List<String> assigns)
        throws Exception;

    /**
     * 查询用户权限下的所有分组名称
     *
     * @param userId userId
     * @return list
     */
    List<String> getAssignmentNameByUserId(@Param("userId") String userId);

    /**
     * 查询用户拥有企业权限下的车辆(用户拥有分组权限的监控对象,监控对象所属企业用户也要有权限)
     *
     * @param assignmentIds
     * @param groupIdList
     * @return
     */
    List<String> findMonitorIdsByAssignIdsAndGroupIds(@Param("assignmentIds") List<String> assignmentIds,
                                                      @Param("groupIds") List<String> groupIdList);

    /**
     * 获得企业下的车辆数
     *
     * @param groupIdList
     * @param assignmentIdList
     * @param groupId
     * @return
     */
    Integer getGroupVehicleCount(@Param("groupIdList") List<String> groupIdList,
                                 @Param("assignmentIdList") List<String> assignmentIdList,
                                 @Param("groupId") String groupId);

    /**
     * 查询用户权限下的所有分组id
     *
     * @param userId userId
     * @return list
     */
    List<String> getAssignmentIdByUserId(@Param("userId") String userId);

    /**
     * 获得拥有分组的用户id
     *
     * @param groupIds
     * @return
     */
    List<String> findAssignUserIdsByGroupIds(List<String> groupIds);

    /**
     * 模糊查询分组
     *
     * @param userId
     * @param groupList
     * @return
     */
    List<Assignment> findUserAssignmentFuzzy(@Param("userId") String userId, @Param("groupList") List<String> groupList,
                                             @Param("query") String query);

    List<VehicleInfo> findVehicleByAssignmentFuzzy(@Param("assignmentIds") List<String> assignmentIds,
                                                   @Param("queryParam") String queryParam);

    List<VehicleInfo> findMonitorByAssignmentFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam);

    /**
     * 根据行业类别模糊搜索监控对象
     */
    List<VehicleInfo> findVehicleByTradeFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam);

    List<VehicleInfo> findVehicleByAssignmentIdsAndDeviceTypes(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceTypes") List<String> deviceTypes,
        @Param("webType") Integer webType);

    /**
     * 根据监控对象ID,获取分组数据
     * @param monitorIdList monitorIdList
     * @return list
     */
    List<AssignmentVehicleForm> findAssignsByMonitorIdList(@Param("monitorIdList") Set<String> monitorIdList);

    List<String> findAssignNamesByIds(List<String> assignmentIds);

    List<Assignment> findAllAssignmentNum(@Param("assignList") List<Assignment> assignList,
                                       @Param("deviceTypeSet") Set<String> deviceTypeSet);

    /**
     * 根据监控对象ID，获取车辆分组名关联关系
     * @param monitorIdList monitorIdList
     * @return list
     */
    List<VehicleAssignmentInfo> findVehicleAndAssignNameInfos(@Param("monitorIdList") Collection<String> monitorIdList);

    /**
     * 通过分组名称模糊查询分组id
     * @param fuzzyAssignmentName 分组名称
     * @return Set<String>
     */
    Set<String> findAssignmentIdByFuzzyAssignmentName(String fuzzyAssignmentName);
}
