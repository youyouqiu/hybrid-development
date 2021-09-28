package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.query.GroupPageQuery;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.driverDiscernManage.VehicleAssignmentInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 分组Dao
 * @date 2020/10/2614:22
 */
public interface GroupDao {

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean add(GroupDO groupDo);

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean batchAdd(@Param("list") List<GroupDO> groupDo);

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean update(GroupDO groupDo);

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean delete(String id);

    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT)
    boolean delBatch(@Param("ids") Collection<String> ids);

    /**
     * 通过id进行获取
     * @param id
     * @return
     */
    GroupDO getById(@Param("id") String id);

    /**
     * 通过id进行获取
     * @param id
     * @return
     */
    List<GroupDO> getByIds(@Param("ids") Collection<String> id);

    /**
     * 通过用户uuid 车id 查询用户权限下车的分组
     * @param userId
     * @param vehicleId
     * @return
     */
    String getUserVehicleGroups(@Param("vehicleId") String vehicleId, @Param("userId") String userId);

    /**
     * 分页模糊查询分组
     * @param query
     * @return
     */
    List<GroupDO> getListByKeyword(GroupPageQuery query);

    /**
     * 通过分组名称模糊搜索分组
     * @param fuzzyName 模糊的分组名称
     * @return List<GroupDO>
     */
    List<GroupDO> getListByFuzzyName(String fuzzyName);

    /**
     * 分页模糊查询分组
     * @param query  查询条件
     * @param userId 用户Id
     * @return
     */
    List<GroupDO> getListByKeywordAndUerId(@Param("query") GroupPageQuery query, @Param("userId") String userId);

    /**
     * 通过企业ID 获取企业下的分组
     * @param values orgId
     * @return 分组集合
     */
    List<GroupDO> getGroupListByGroupIds(@Param("orgIds") Collection<String> values);

    /**
     * 通过企业Id查询企业下的分组信息
     * @param orgId
     * @return
     */
    List<GroupDTO> getGroupsByOrgId(@Param("orgId") String orgId);

    /**
     * 通过企业Id查询企业下的分组id
     * @param orgId
     * @return
     */
    List<String> getGroupIdsByOrgId(@Param("orgId") String orgId);

    /**
     * 通过企业Id查询企业下的分组信息
     * @param orgIds
     * @return
     */
    List<GroupDTO> getGroupsByOrgIds(@Param("orgIds") Collection<String> orgIds);

    /**
     * 通过 id 获取名字
     * @param groupIdList
     * @return
     */
    List<String> getNamesByIds(@Param("ids") Collection<String> groupIdList);

    /**
     * 判断名称是否重复
     * @param name
     * @param orgId
     * @param groupId
     * @return
     */
    int checkNameExist(@Param("name") String name, @Param("orgId") String orgId, @Param("groupId") String groupId);

    /**
     * 获取当前分组下监控对象的数量
     * @param id
     * @return
     */
    GroupDTO getMonitorCountById(String id);

    /**
     * 获取企业下用户拥有权限分组信息及分组下的监控对象数量
     * @param orgIds 组织Id 可为空 为空查询全部组织
     * @param userId 用户ID 可为空
     * @return 分组详情
     */
    List<GroupDTO> getMonitorCountByOrgId(@Param("orgIds") Collection<String> orgIds, @Param("userId") String userId);

    /**
     * 根据关键字搜索分组--分组不包含监控对象数量的属性
     * @param orgIds  组织ID 为空全部组织
     * @param userId  用户ID 为空 全部用户
     * @param keyword 关键字 为空 不进行过滤
     * @return 分组列表
     */
    List<GroupDTO> getUserGroupList(@Param("orgIds") Collection<String> orgIds, @Param("userId") String userId,
        @Param("keyword") String keyword);

    /**
     * 符合809转发条件的车辆数量
     * @param assigns
     * @return
     */
    List<GroupDTO> find809ForwardVehicle(@Param("groupIds") List<GroupDTO> assigns);

    /**
     * 查询分组的联系电话，多个按照逗号隔开
     * @param groupIds
     * @return
     */
    String getGroupPhoneByIds(@Param("groupIds") String groupIds);

    /**
     * 获得企业下的车辆数
     * @param orgIdList
     * @param assignmentIdList
     * @param orgId
     * @return
     */
    Integer getGroupVehicleCount(@Param("orgIdList") List<String> orgIdList,
        @Param("assignmentIdList") Collection<String> assignmentIdList, @Param("orgId") String orgId);

    /**
     * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id list 查询监控对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findMonitorByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceTypes") List<String> deviceTypes);

    /**
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
        @Param("queryParam") String queryParam, @Param("queryType") String queryType,
        @Param("deviceTypes") List<String> deviceTypes);

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
     * @param list
     * @return
     */
    List<Assignment> findGroupNameByBrand(List<String> list);

    /**
     * 查询用户分组权限（管理员）
     */
    List<Assignment> findUserAssignment(@Param("userId") String userId, @Param("orgIds") List<String> orgIds);

    /**
     * 查询树节点下监控对象数量
     * @param assignList    分组ids
     * @param monitorType   监控对象类型
     * @param deviceTypeSet 终端类型
     * @return
     */
    List<Assignment> findAssignmentNum(@Param("assignList") List<Assignment> assignList,
        @Param("monitorType") String monitorType, @Param("deviceTypeSet") Set<String> deviceTypeSet);

    /**
     * 查询用户分组权限（管理员1120优化）
     */
    List<Assignment> findUserAssignmentFilterName(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("name") String name);

    /**
     * 根据id查询分组
     * @param id
     * @return Assignment
     */
    Assignment findAssignmentById(@Param("id") String id);

    /**
     * 加计数
     * @param id
     * @return
     */
    Assignment findAssignmentByIdNum(@Param("id") String id);

    /**
     * 根据分组id查询车辆list
     * @param assignmentId assignmentId
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findVehicleByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
     * 根据分组id查询监控对象list
     * @param assignmentId assignmentId
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findMonitorByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
     * 根据车辆和分组删除车辆和分组的关联
     * @param assignmentId
     * @param vehicleList
     * @return boolean
     */
    boolean deleteAssignmentVehicleByVidAid(@Param("assignmentId") String assignmentId,
        @Param("vehicleList") List<String> vehicleList);

    /**
     * TODO 查询同一组织下是否有相同名称的分组
     * @param name
     * @param groupId
     * @return Assignment
     */
    List<Assignment> findByNameForOneOrg(@Param("name") String name, @Param("groupId") String groupId);

    /**
     * 修改时，查询非当前车辆以外，查询统一组织下分组名称是否重复
     * @param id
     * @param name
     * @param groupId
     * @return
     */
    List<Assignment> findOneOrgAssiForNameRep(@Param("id") String id, @Param("name") String name,
        @Param("groupId") String groupId);

    /**
     * 根据组织查询组织下所有分组
     * @param groupId
     * @return List<Assignment>
     */
    List<Assignment> findAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 根据组织查询组织下所有分组, 排除当前分组
     * @param groupId
     * @param assignmentId
     * @return List<Assignment>
     */
    List<Assignment> findAssignByGroupIdExpectVehicle(@Param("groupId") String groupId,
        @Param("assignmentId") String assignmentId);

    /**
     * 根据分组id list 查询监控对象id list
     * @param assignmentIds
     * @return List<VehicleInfo>
     */
    List<String> findMonitorIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据分组id list 查询车辆id list
     * @param assignmentIds
     * @return List<VehicleInfo>
     */
    List<String> findVehicleIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    /**
     * 根据监控对象类型id list 查询分组对应的所属企业 name
     * @param vehicleIds
     * @return
     */
    List<Assignment> getAssignsByMonitorId(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 模糊查询分组
     * @param userId
     * @param groupList
     * @return
     */
    List<Assignment> findUserAssignmentFuzzy(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("query") String query);

    /**
     * 批量新增用户与分组的关联表
     * @param list
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean addAssignmentUserByBatch(@Param("list") Collection<AssignmentUserForm> list);

    /**
     * 根据分组id list 模糊查询车辆对象list（包括设备，SIM卡，从业人员等信息）
     * @param assignmentIds 分组id
     * @param queryParam    模糊搜索值
     * @param deviceTypes   模糊搜索类型
     *                      （name:监控对象名称；deviceNumber:终端编号；simcardNumber：
     *                      sim卡编号；assignName：分组名称； professional：从业人员名称）
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findVehicleByAssignmentIdsFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam, @Param("deviceTypes") List<String> deviceTypes,
        @Param("webType") Integer webType);

    /**
     * 根据分组ID列表查询监控对象list（不包括设备，SIM卡，从业人员等信息）
     * @param assignmentIds 分组ID列表
     * @return 监控对象列表
     */
    List<VehicleInfo> findMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds,
        @Param("query") String query, @Param("deviceTypes") List<String> deviceTypes,
        @Param("webType") Integer webType);

    /**
     * 根据分组ID列表查询监控对象list的数量（不包括设备，SIM卡，从业人员等信息）
     * @param assignmentIds 分组ID列表
     * @return 监控对象列表
     */
    int countMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds);

    List<VehicleInfo> vehicleTruckTree(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据分组id list 查询车辆list
     * @param assignmentIds
     * @return List<VehicleInfo>
     */
    List<VehicleInfo> findVehicleByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType, @Param("webType") Integer webType);

    /**
     * 根据传感器类型,分组id list 查询绑定传感器的监控对象list
     */
    List<VehicleInfo> findTempSensoreVehicleByAssignmentId(@Param("sensorType") int sensorType,
        @Param("assignmentIds") List<String> assignmentIds);

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

    List<VehicleInfo> findVehicleByAssignmentFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam);
}
