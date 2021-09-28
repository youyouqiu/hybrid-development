package com.zw.talkback.repository.mysql;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.ClusterInfo;
import com.zw.talkback.domain.basicinfo.InterlocutorInfo;
import com.zw.talkback.domain.basicinfo.TempAssignmentInterlocutor;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.basicinfo.query.AssignmentQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> Title: 群组管理Dao </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年10月10日上午9:07:35
 */
public interface ClusterDao {

    /**
     * 查询群组管理
     */
    List<Cluster> findAssignment(AssignmentQuery query);

    /**
     * 查询群组管理
     */
    List<Cluster> listAssignment(@Param("userId") String userId, @Param("query") AssignmentQuery query);

    /**
     * 根据车牌号查询群组管理
     * @param list
     * @return
     */
    List<Cluster> findGroupNameByBrand(List<String> list);

    /**
     * 批量查询群组数据
     * @param assignList 群组ID列表
     * @return 群组数据列表
     */
    List<Cluster> findAssignmentByBatch(@Param("assignList") List<String> assignList);


    /**
     * 批量查询群组数据 :监听分组用：查询分组删除群组后的信息
     * @param assignList 群组ID列表
     * @return 群组数据列表
     */
    List<Cluster> findAssignmentByBatchListener(@Param("assignList") List<String> assignList);

    /**
     * 查询用户群组权限（管理员）
     */
    List<Cluster> findUserAssignment(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 查询树节点下监控对象数量
     * @param assignList  群组ids
     * @param monitorType 监控对象类型
     * @param deviceType  终端类型
     * @return
     */
    List<Cluster> findAssignmentNum(@Param("assignList") List<Cluster> assignList,
        @Param("monitorType") String monitorType, @Param("deviceType") String deviceType);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 新增群组
     */
    boolean addAssignment(ClusterForm form);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 新增群组与组织关联表
     */
    boolean addGroupAssignment(AssignmentGroupForm form);

    /**
         * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 批量新增用户与群组的关联表
     */
    boolean addAssignmentUserByBatch(List<AssignmentUserForm> list);

    /**
         * @param id
     * @return Assignment
     * @throws @author wangying
     * @Title: 根据id查询群组
     */
    Cluster findAssignmentById(@Param("id") String id);

    Cluster findAssignmentByIdListener(@Param("id") String id);

    /**
     * 加计数
     * @param id
     * @return
     */
    Cluster findAssignmentByIdNum(@Param("id") String id);

    AssignmentGroupForm getGroupForm(@Param("id") String id);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 修改群组
     */
    boolean updateAssignment(ClusterForm form);

    /**
     * 补全
     * @param form
     * @return
     */
    boolean updateCluster(ClusterForm form);

    boolean updateCluster1(ClusterForm form);

    /**
         * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
         * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id查询监控对象list
     */
    List<VehicleInfo> findMonitorByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
     * 根据群组ID列表查询监控对象list（不包括设备，SIM卡，从业人员等信息）
     * @param assignmentIds 群组ID列表
     * @return 监控对象列表
     * @author chenfeng
     */
    List<VehicleInfo> findMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds,
        @Param("query") String query, @Param("deviceTypes") List<String> deviceTypes,
        @Param("webType") Integer webType);

    /**
     * 根据群组ID列表查询监控对象list的数量（不包括设备，SIM卡，从业人员等信息）
     * @param assignmentIds 群组ID列表
     * @return 监控对象列表
     * @author chenfeng
     */
    int countMonitorByAssignmentIdList(@Param("assignmentIds") List<String> assignmentIds);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType, @Param("webType") Integer webType);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询车辆id list
     */
    List<String> findVehicleIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    List<String> listVehiclesByAssignmentsIds(@Param("assignIds") List<String> assignIds);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询监控对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findMonitorByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceTypes") List<String> deviceTypes);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询监控对象id list
     */
    List<String> findMonitorIdsByAssignmentIds(@Param("assignmentIds") List<String> assignmentIds);

    /**
         * @param assignmentIds 群组id
     * @param queryParam    模糊搜索值
     * @param queryType     模糊搜索类型
     *                      （name:监控对象名称；deviceNumber:终端编号；simcardNumber：sim卡编号；
     *                      assignName：群组名称； professional：从业人员名称）
     * @param deviceTypes   指定需要查询的终端协议类型集合
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 模糊查询监控对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findMonitorByAssignmentIdsFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam, @Param("queryType") String queryType,
        @Param("deviceTypes") List<String> deviceTypes);

    /**
         * @param assignmentIds 群组id
     * @param queryParam    模糊搜索值
     * @param deviceType    设备类型
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 模糊查询车辆对象list（包括设备，SIM卡，从业人员等信息）
     */
    List<VehicleInfo> findVehicleByAssignmentIdsFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam, @Param("deviceType") String deviceType,
        @Param("webType") Integer webType);

    /**
         * @param assignmentIds
     * @param deviceType
     * @return List<VehicleInfo>
     * @throws @author lifudong
     * @Title: 根据群组id list,设备类型 查询监控对象list
     */
    List<VehicleInfo> findMonitorByAssignmentIdsAndType(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    /**
         * @param assignmentIds
     * @param deviceType
     * @return List<VehicleInfo>
     * @throws @author Fan Lu
     * @Title: 根据群组id list,设备类型 查询车辆list
     */
    List<VehicleInfo> findVehicleByAssignIdAndType(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    int countVehicleByAssignIdAndType(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    List<VehicleInfo> vehicleTruckTree(@Param("assignmentIds") List<String> assignmentIds);

    /**
         * @param id
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组
     */
    boolean deleteAssignment(String id);

    /**
         * @param assignmentId
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组与企业关联
     */
    boolean deleteAssignmentGroupByAssId(String assignmentId);

    /**
         * @param assignmentId
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组与user关联表
     */
    boolean deleteAssignmentUserByAssId(String assignmentId);

    /**
         * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组(批量)
     */
    boolean deleteAssignmentByBatch(List<String> list);

    /**
         * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组与企业关联(批量)
     */
    boolean deleteAssignmentGroupByAssIdByBatch(List<String> list);

    /**
         * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 删除群组与user关联表(批量)
     */
    boolean deleteAssignmentUserByAssIdByBatch(List<String> list);

    // ------------add by liubq 2016-10-12 start-----------------------------------


    boolean deleteAssignmentByVehicle(String vehicleId);

    /**
     * 新增车辆群组信息
     * @param form
     * @return boolean
     * @throws @author Liubangquan
     * @Title: addAssignmentVehicle
     */
    boolean addAssignmentVehicle(AssignmentVehicleForm form);
    // ------------add by liubq end-----------------------------------

    boolean addAssignVehicleList(@Param("list") Collection<AssignmentVehicleForm> list);

    /**
     * 查询车辆群组
     * @param vehicleId
     * @return
     */
    String getGroups(@Param("vehicleId") String vehicleId, @Param("userId") String userId);

    /**
     * 查询车辆类型
     * @param vehicleId
     * @return
     */
    String getType(@Param("vehicleId") String vehicleId);

    /**
         * @param vehicleId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据车辆id查询群组
     */
    List<Cluster> findAssignmentByVehicleId(@Param("vehicleId") String vehicleId, @Param("userId") String userId,
        @Param("groupList") List<String> groupList);


    /**
     * TODO 查询同一组织下是否有相同名称的群组
     * @param name
     * @param groupId
     * @return Assignment
     * @throws @author wangying
     * @Title: findByNameForOneOrg
     */
    List<Cluster> findByNameForOneOrg(@Param("name") String name, @Param("groupId") String groupId);

    List<Cluster> findByNameForOne(@Param("name") String name);

    /**
     * 修改时，查询非当前车辆以外，查询统一组织下群组名称是否重复
     * @param id
     * @param name
     * @param groupId
     * @return
     */
    List<Cluster> findOneOrgAssiForNameRep(@Param("id") String id, @Param("name") String name,
        @Param("groupId") String groupId);

    /**
         * @param groupId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据组织查询组织下所有群组
     */
    List<Cluster> findAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 根据组织id列表查询组织列表下所有群组
     * @param groupIdList 组织列表
     * @return 群组列表
     */
    List<Cluster> findAssignmentByGroupIdList(@Param("groupIdList") List<String> groupIdList);

    /**
         * @param groupId
     * @param assignmentId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据组织查询组织下所有群组, 排除当前群组
     */
    List<Cluster> findAssignByGroupIdExpectVehicle(@Param("groupId") String groupId,
        @Param("assignmentId") String assignmentId);

    /**
     * 批量新增群组
     * @param list
     * @return
     */
    boolean addAssignmentByBatch(List<ClusterForm> list);

    /**
     * 批量新增群组与组织关联表
     * @param list
     * @return
     */
    boolean addGroupAssignmentByBatch(List<AssignmentGroupForm> list);

    /**
     * 删除车辆与群组绑定关系
     * @param deleteVehiclePer
     * @return
     */
    boolean deleteVehiclePer(AssignmentVehicleForm deleteVehiclePer);

    boolean deleteVehiclePerBatch(@Param("assignmentId") String assignmentId, @Param("ids") List<String> ids);

    /**
     * 批量新增车辆与群组的绑定关系
     * @param addVehiclePer
     * @return
     */
    boolean addVehiclePer(@Param("list") List<AssignmentVehicleForm> addVehiclePer);

    /**
     * 查询群组组织ID
     * @param id
     * @return
     */
    String findAssignsGroupId(String id);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询监控对象 车list
     */
    List<VehicleInfo> vehicleFindByAssignmentId(@Param("assignmentIds") List<String> assignmentIds,
        @Param("deviceType") String deviceType);

    /**
         * @param assignmentIds
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据群组id list 查询监控对象人list
     */
    List<VehicleInfo> peopleFindByAssignmentId(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据传感器类型,群组id list 查询绑定传感器的监控对象list
     */
    List<VehicleInfo> findTempSensoreVehicleByAssignmentId(@Param("sensorType") int sensorType,
        @Param("assignmentIds") List<String> assignmentIds);

    /**
         * @param assignmentIds
     * @return List<String> 群组的所属企业
     * @throws @author fanlu
     * @Title: 根据群组id list 查询群组对应的所属企业 list
     */
    List<String> findAssignsGroupIds(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据群组id list 查询群组对应的所属企业 name
     * @param assignmentIds
     * @return
     * @author fanlu
     */
    List<String> findAssignNames(@Param("assignmentIds") List<String> assignmentIds);

    String findAssignName(@Param("assignmentId") String assignmentId);

    /**
     * 根据监控对象类型id list 查询群组对应的所属企业 name
     * @param vehicleIds
     * @return
     * @author yangyi
     */
    List<Cluster> getAssignsByMonitorId(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 根据监控对象类型和终端类型查询车辆数量
     * @param monitorType
     * @param deviceType
     * @return
     */
    List<String> getMonitorCount(@Param("monitorType") String monitorType, @Param("deviceType") String deviceType,
        @Param("assignmentIds") List<String> assignmentList);

    /**
     * 查询用户权限下的所有群组id
     * @param userId
     * @param groupList
     * @return
     */
    List<String> getAllAssignmentByGroupIds(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 查询当前企业存在的货运分组
     * @param groupId
     * @return
     */
    List<ClusterForm> getTransportAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 获取最后一个货运分组编号
     * @param groupId
     * @return
     */
    Set<Integer> findTransportAssignmentByGroupId(@Param("groupId") String groupId);

    /**
     * 计算当前企业下的群组数量(每个企业只能有100个分则)
     * @param groupId groupId
     * @return
     */
    ClusterForm getGroupHasAssignmentNumber(String groupId);

    /**
     * 获取当前企业下的其他群组数量
     * @param groupId
     * @return
     */
    Integer getGroupNotLikeFreightSize(@Param("groupId") String groupId);

    String getAssignPhoneByIds(@Param("assignIds") String assignIds);

    /**
     * 获取群组下符合809转发条件的群组id、车辆id集合
     * @param assigns
     * @return
     * @throws Exception
     */
    List<Cluster> find809ForwardAssignmentNum(@Param("assigns") List<JSONObject> assigns) throws Exception;

    /**
     * 获取群组下符合809转发条件的车辆id集合
     * @param assigns
     * @return
     * @throws Exception
     */
    Set<String> get809ForwardCount(@Param("assigns") List<String> assigns) throws Exception;

    /**
     * 查询用户权限下的所有群组名称
     * @param userId userId
     * @return list
     */
    List<ClusterForm> getAssignmentNameByUserId(@Param("userId") String userId);

    /**
     * 查询用户拥有企业权限下的车辆(用户拥有群组权限的监控对象,监控对象所属企业用户也要有权限)
     * @param assignmentIds
     * @param groupIdList
     * @return
     */
    List<String> findMonitorIdsByAssignIdsAndGroupIds(@Param("assignmentIds") List<String> assignmentIds,
        @Param("groupIds") List<String> groupIdList);

    /**
     * 获得企业下的车辆数
     * @param groupIdList
     * @param assignmentIdList
     * @param groupId
     * @return
     */
    Integer getGroupVehicleCount(@Param("groupIdList") List<String> groupIdList,
        @Param("assignmentIdList") List<String> assignmentIdList, @Param("groupId") String groupId);

    /**
     * 查询用户权限下的所有群组id
     * @param userId userId
     * @return list
     */
    List<String> getAssignmentIdByUserId(@Param("userId") String userId);

    /**
     * 获得拥有群组的用户id
     * @param groupIds
     * @return
     */
    List<String> findAssignUserIdsByGroupIds(List<String> groupIds);

    /**
     * 模糊查询群组
     * @param userId
     * @param groupList
     * @return
     */
    List<Cluster> findUserAssignmentFuzzy(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("query") String query);

    List<VehicleInfo> findVehicleByAssignmentFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam);

    List<VehicleInfo> findMonitorByAssignmentFuzzy(@Param("assignmentIds") List<String> assignmentIds,
        @Param("queryParam") String queryParam);

    /**
     * 查询群组权限下的离职人员
     * @param assignmentIds
     * @return
     */
    List<VehicleInfo> findLeaveJobPeople(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 查询指定用户下指定组织的群组ids
     * @param userId
     * @param groupId
     * @return
     */
    List<String> getAssignmentIds(@Param("userId") String userId, @Param("groupId") String groupId);

    /**
     * 查询用户拥有的任务群组
     * @param userId
     * @param groupId
     * @return 用户拥有的任务群组
     */
    List<Cluster> findUserOwnTaskAssignment(@Param("userId") String userId, @Param("groupId") String groupId);

    /**
     * 查询用户拥有的临时群组
     * @param userId
     * @param groupId
     * @return 用户拥有的临时群组
     */
    List<Cluster> findUserOwnTemporaryAssignment(@Param("userId") String userId, @Param("groupId") String groupId);

    /**
     * 通过名称模糊查找群组下的对讲对象
     * @param assignmentIds 群组id
     * @param name          模糊搜索名称
     * @return 群组下的对讲对象
     */
    List<InterlocutorInfo> findInterlocutorByAssignmentIdsAndNameFuzzy(
        @Param("assignmentIds") List<String> assignmentIds, @Param("name") String name);

    /**
     * 查询对讲对象任务组信息
     * @param interlocutorIdList 对讲对象id集合
     * @return 任务组信息
     */
    List<ClusterInfo> findInterlocutorTaskAssignmentInfo(@Param("interlocutorIdList") List<String> interlocutorIdList);

    /**
     * 添加对讲组
     * @param form 对讲组
     * @return boolean
     */
    boolean addInterlocutorAssignment(ClusterForm form);

    /**
     * 添加对讲组成员
     * @param interlocutorInfoList 对讲对象信息
     * @return boolean
     */
    boolean addInterlocutorAssignmentMember(@Param("interlocutorInfoList") List<InterlocutorInfo> interlocutorInfoList);

    /**
     * 解除群组下的监控对象绑定
     * @param assignmentId 群组id
     * @return boolean
     */
    boolean unbindAssignmentAndMonitor(@Param("assignmentId") String assignmentId);

    /**
     * 查找群组内的监控对象id 人
     * @param assignmentId 群组id
     * @return List<String>
     */
    List<String> findMonitorIdsByAssignmentId(@Param("assignmentId") String assignmentId);

    /**
     * 查找群组和群组下的监控对象数量信息
     * @param id 群组id
     * @return Assignment
     */
    Cluster findAssignmentAndMonitorNumById(@Param("id") String id);

    /**
     * 通过对讲群组id查询群组信息
     * @param intercomGroupId 对讲群组id
     * @return Assignment
     */
    Cluster findAssignmentByIntercomGroupId(@Param("intercomGroupId") Long intercomGroupId);

    /**
     * 通过对讲群组id查询群组信息 批量
     * @param intercomGroupIdList 对讲群组id 集合
     * @return List<Assignment>
     */
    List<Cluster> findAssignmentByIntercomGroupIdBatch(@Param("intercomGroupIdList") List<Long> intercomGroupIdList);

    /**
     * 获得用户创建的临时组
     * @param userName 创建人
     * @return
     */
    List<Cluster> findTemporaryAssignmentByUserName(@Param("userName") String userName);

    Integer getAssignmentNumberOfMonitor(String id);

    List<ClusterForm> getAssignmentGropuIdByName(String name);

    boolean changeRecordingSwitch(Cluster cluster);

    /**
     * 查询在临时组内的对讲对象信息
     * @param interlocutorIdList 对讲对象
     * @return 临时组内的对讲对象信息
     */
    List<TempAssignmentInterlocutor> findInTemporaryAssignmentInterlocutorInfo(
        @Param("interlocutorIdList") List<Long> interlocutorIdList);

    /**
     * 查询在临时组内的对讲对象ID
     * @param interlocutorIdList 对讲对象
     * @return 临时组内的对讲对象id
     */
    List<Long> findInTemporaryAssignmentInterlocutorId(@Param("interlocutorIdList") List<Long> interlocutorIdList);

    /**
     * 添加临时组内对讲对象
     * @param assignmentId                   群组id
     * @param intercomGroupId                对讲群组id
     * @param tempAssignmentInterlocutorList 对讲对象id
     */
    void addTemporaryAssignmentInterlocutorId(@Param("assignmentId") String assignmentId,
        @Param("intercomGroupId") Long intercomGroupId,
        @Param("tempAssignmentInterlocutorList") List<TempAssignmentInterlocutor> tempAssignmentInterlocutorList);

    /**
     * 删除临时组内成员 通过对讲群组id
     * @param intercomGroupId 对讲群组id
     * @param interlocutorId  对讲对象id (不传就是删除临时组内所有对讲对象)
     */
    void delTemporaryAssignmentInterlocutorByIntercomGroupId(@Param("intercomGroupId") Long intercomGroupId,
        @Param("interlocutorId") Long interlocutorId);

    /**
     * 删除临时组内成员 通过群组id
     * @param assignmentIdList 群组id集合
     */
    void delTemporaryAssignmentInterlocutorByAssignmentIdList(@Param("assignmentIdList") List<String> assignmentIdList);

    boolean deleteKnobsByMonitorId(@Param("vehicleIds") List<String> vehicleIds);

    boolean updateAssignKnobs(AssignmentVehicleForm form);

    boolean updateAssignKnob(AssignmentVehicleForm form);

    List<ClusterForm> findAll();

    /**
     * @param userId  用户的id
     * @param groupId 组织id
     * @return 用户所属组织权限下的平台分组id和对讲平台群组id
     */
    List<Map<String, Object>> findAssignmentMap(@Param("userId") String userId, @Param("groupId") String groupId);

    Set<Long> findOtherTemporaryAssignmentUserIds(@Param("userIdSet") Set<Long> userIdSet,
        @Param("temporaryAssignment") Set<String> temporaryAssignment);

    Map<String, String> findAssignmentInfoMap(Long defaultGroupId);

    String findAssignmentName(String id);

    /**
     * 获取监控对象的群组信息
     * @param monitorId monitorId
     * @return 群组信息
     */
    List<Cluster> findAssignmentByMonitorId(@Param("monitorId") String monitorId);

    /**
     * 获取用户对讲群组
     * @param userId 用户ID
     * @return 群组IDs
     */
    List<String> getUserAssignIds(@Param("userId") String userId);

    List<Cluster> getTackbackAssignsByMonitorId(@Param("monitorId") String monitorId);

    List<String> getAssignIdsForMonitor(@Param("monitorId") String monitorId);

    List<AssignmentVehicleForm> getAssignVehicleList(@Param("monitorIds") List<String> monitorIds);


}
