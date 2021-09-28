package com.zw.talkback.service.baseinfo;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.basicinfo.query.AssignmentQuery;
import com.zw.talkback.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> Title: 从业人员管理Service </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午5:09:25
 */
public interface ClusterService {

    /**
     * 查询
     * @param query
     * @return
     */
    List<Cluster> findAssignment(AssignmentQuery query) throws Exception;

    /**
     * 
     * @param userId
     * @param groupList
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 查询用户权限分组
     */
    List<Cluster> findUserAssignment(String userId, List<String> groupList) throws Exception;

    List<Cluster> findUserAssignmentNum(String userId, List<String> groupList, String monitorType, String deviceType)
        throws Exception;

    /**
     * 
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 添加分组
     */

    boolean addAssignment(ClusterForm form) throws Exception;

    /**
     * 
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 添加分组与企业关联表
     */
    boolean addAssignmentGroup(AssignmentGroupForm form) throws Exception;

    /**
     * 
     * @param assignmentIdList
     * @param groupId
     * @param session
     * @return boolean
     * @throws @author wangying
     * @Title: 为上级用户分配该分组的权限
     */
    boolean assignmentToSuperior(List<String> assignmentIdList, String groupId, HttpSession session) throws Exception;

    /**
     * 
     * @param form
     * @param assGroupform
     * @param ipAddress
     * @return void
     * @throws @author wangying
     * @Title: 新增分组并授权
     */
    JsonResultBean addAssignmentAndPermission(ClusterForm form,
        AssignmentGroupForm assGroupform, String ipAddress, boolean flag)
        throws Exception;

    /**
     * 
     * @param id
     * @return Assignment
     * @throws @author wangying
     * @Title: 根据id查询分组
     */
    Cluster findAssignmentById(String id) throws Exception;

    Cluster findAssignmentByIdNum(String id) throws Exception;

    /**
     * 
     * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: 修改分组
     */

    JsonResultBean updateAssignment(ClusterForm form, String ipAddress) throws Exception;

    /**
     * 
     * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id查询车辆
     */
    List<VehicleInfo> findVehicleByAssignmentId(String assignmentId) throws Exception;

    /**
     * 
     * @param assignmentId
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 根据分组id查询监控对象
     */
    List<VehicleInfo> findMonitorByAssignmentId(String assignmentId) throws Exception;

    /**
     * 
     * @param id
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组
     */
    JsonResultBean deleteAssignment(String id, String ipAddress) throws Exception;

    /**
     * 
     * @param ids
     * @return boolean
     * @throws @author wangying
     * @Title: 删除分组(批量)
     */
    JsonResultBean deleteAssignmentByBatch(List<String> ids, String ipAddress) throws Exception;

    /**
     * 
     * @return JSONArray
     * @throws @author wangying
     * @Title: 组装分组树结构
     */
    JSONArray getAssignmentTree() throws Exception;

    /**
     * 
     * @return JSONArray
     * @throws @author wangying
     * @Title: 组装分组树结构(可选)
     */
    JSONArray getEditAssignmentTree(String assignUserId) throws Exception;

    /**
     * 获取分配监控人员树
     * @param assignmentID
     * @return
     */
    String getAssignMonitorUserTree(String assignmentID) throws Exception;

    // ------------add by liubq 2016-10-12 start-----------------------------------

    // List<AssignmentVehicleForm> getAssignmentVehicle(String vehicleId);

    /**
     * 添加车辆分组信息
     * @param assignmentVehicleForm
     * @return boolean
     * @throws @author Liubangquan
     * @Title: addAssignmentVehicle
     */
    boolean addAssignmentVehicle(AssignmentVehicleForm assignmentVehicleForm) throws Exception;
    // ------------add by liubq end-----------------------------------

    boolean addAssignVehicleList(Collection<AssignmentVehicleForm> formList);

    /**
     * @param vehicleId
     * @return List<Assignment>
     * @throws @author wangying
     * @Title: 根据车辆查询车队
     */
    List<Cluster> findAssignmentByVehicleId(String vehicleId) throws Exception;

    /**
     * 根据用户组织更改更新分组权限
     * @param userId     用户id
     * @param oldGroupDn 旧组织DN
     * @param newGroupDn 新组织DN
     * @throws Exception
     */
    void updateUserGroup(String userId, String oldGroupDn, String newGroupDn) throws Exception;

    /**
     * 更新用户分组权限
     * @param userId  用户id
     * @param delList 待删除的分组id列表
     * @param addList 待添加的分组id列表
     * @throws Exception
     */
    void updateUserAssignments(String userId, List<String> delList, List<String> addList) throws Exception;

    /**
     * @param allList    所有组织
     * @param id         当前组织id
     * @param returnList list
     * @return void
     * @throws @author wangying
     * @Title: 递归获取当前组织及直属上级组织
     */
    void getParentOrg(List<OrganizationLdap> allList, String id, List<OrganizationLdap> returnList) throws Exception;

    /**
     * 查询同一组织下是否有相同名称的分组
     * @param name
     * @param groupId
     * @return Assignment
     * @throws @author wangying
     * @Title: findByNameForOneOrg
     */
    List<Cluster> findByNameForOneOrg(String name, String groupId) throws Exception;

    /**
     * 查询是否有相同名称的分组
     * @param name
     * @return
     * @throws Exception
     */
    List<Cluster> findByNameForOne(String name) throws Exception;



    /**
     * 修改时，查询非当前车辆以外，查询统一组织下分组名称是否重复
     * @param id
     * @param name
     * @param groupId
     * @return
     */
    List<Cluster> findOneOrgAssiForNameRep(String id, String name, String groupId) throws Exception;

    /**
     * 导入分组
     * @param file
     * @param session
     * @return
     */
    Map importAssignment(MultipartFile file, String ipAddress, HttpSession session) throws Exception;

    /**
     * 根据组织查询组织下的分组（不带权限）
     * @param groupId
     * @return
     */
    List<Cluster> findAssignmentByGroupId(String groupId);

    /**
     * 根据组织查询组织下的分组（不带权限）,排除当前分组
     * @param groupId
     * @return
     */
    List<Cluster> findAssignByGroupIdExpectVehicle(String groupId, String assignmentId) throws Exception;

    /**
     * 下载分组模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导出
     * @param title    excel名称
     * @param type     导出类型（1:导出数据；2：导出模板）
     * @param response 文件
     * @return
     */
    boolean exportAssignment(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 保存车组分配
     * @param vehiclePerAddList
     * @param vehiclePerDeleteList
     */
    JsonResultBean saveVehiclePer(List<AssignmentVehicleForm> vehiclePerAddList,
        List<AssignmentVehicleForm> vehiclePerDeleteList, String assignmentId, String ipAddress) throws Exception;

    /**
     * 查询分组管理组织id
     * @param id
     * @return
     */
    String findAssignmentGroupId(String id) throws Exception;

    /**
     * @param assignmentIds
     * @return List<String> 分组的所属企业
     * @throws @author fanlu
     * @Title: 根据分组id list 查询分组对应的所属企业 list
     */
    List<String> findAssignsGroupIds(List<String> assignmentIds);

    /**
     * @param assignmentIds
     * @return List<String> 分组的所属企业
     * @throws @author fanlu
     * @Title: 根据分组id list 查询分组对应的分组名称
     */

    List<String> findAssignNames(List<String> assignmentIds);

    /**
     * 获取分组的监控对象信息
     * @param assignmentId 分组ID
     * @return 监控对象信息的JSON字符串
     */
    JSONArray getMonitorByAssignmentID(String assignmentId) throws Exception;

    /**
     * 组装分组的树结构
     * @param clusterList 分组list
     * @param result         树结构
     * @param type           根节点是否可选
     * @param isBigData      是否是车的数量大于2000时
     * @return
     */
    List<String> putAssignmentTree(List<Cluster> clusterList, JSONArray result, String type, boolean isBigData);

    /**
     *      * @param type        节点是否勾选
     * @param isAssignNum 是否显示分组节点数量
     * @return JSONArray
     * @throws
     * @Title: 组装分组树结构(数量超过5000)
     * @author wangying
     */
    JSONArray getAssignmentTreeForBigData(String type, boolean isAssignNum) throws Exception;

    /**
     * 根据组织id查询其下的监控对象数量
     * @param statusType
     * @return
     */

    Set<String> getMonitorCountByGroup(String groupId, String type, String deviceType, String statusType)
        throws Exception;

    /**
     * 根据监控对象类型id list 查询分组对应的所属企业 name
     * @param vehicleIds
     * @return
     * @author yangyi
     */
    String getAssignsByMonitorId(String vehicleIds) throws Exception;

    /**
     * 检查用户分组权限
     * @param groupName 格式： "1,2,3"
     * @return zhouzongbo
     */
    String checkAssignment(String groupName);

    /**
     * 根据组织查询监控对象
     * @param groupIdList groupIdList
     * @param type        查询类型: monitor: 车人物, vehicle: 车
     * @param deviceType  type=车, 需要传入此参数
     * @return set
     * @throws Exception
     */
    Set<String> findMonitorByGroupId(List<String> groupIdList, String type, String deviceType) throws Exception;

    List<Cluster> findUserAssignmentFuzzy(String userId, List<String> groupList, String query) throws Exception;

    /**
     * 分组下离职人员
     * @param assignmentId
     * @return
     * @throws Exception
     */
    int countLeaveJobPeopleNum(String assignmentId) throws Exception;

    JsonResultBean getAssignmentNumberOfMonitor(String id);

    JsonResultBean changeRecordingSwitch(String ipAddress, String assignmentId, Integer flag);

    /**
     * 删除用户拥有临时组
     * @throws Exception Exception
     * @param userName
     */
    void delUserOwnTemporaryAssignment(String userName) throws Exception;

    List<ClusterForm> findAll();

    AssignmentGroupForm getGroupForm(String id);

    /**
     * 车辆权限树（除去传入的分组）
     *
     * @param type         树的类型，single,multiple
     * @param assignmentId 当前分配监控对象的分组id
     * @param queryParam
     * @param queryType    查询类型 监控对象 分组 企业
     * @return
     * @throws Exception
     */
    JSONArray vehicleTreeForAssign(String type, String assignmentId, String queryParam, String queryType)
        throws Exception;

    /**
     * 获取当前用户监控对象的所有组织和分组
     *
     * @param assignmentId 待分配监控对象的分组ID
     * @param queryParam
     * @param queryType
     * @return
     * @throws Exception
     */
    JSONArray listMonitorTreeParentNodes(String assignmentId, String queryParam, String queryType)
        throws Exception;


    /**
     * 获取当前用户在指定的分组下的所有监控对象
     *
     * @param assignmentId 指定的分组ID
     * @return
     * @throws Exception
     */
    JSONArray listMonitorsByAssignmentID(String assignmentId)
        throws Exception;

    /**
     * 获取当前用户除去待分配分组外的所有监控对象的数量
     *
     * @param assignmentId
     * @return
     * @throws Exception
     */
    int countMonitors(String assignmentId)
        throws Exception;
}
