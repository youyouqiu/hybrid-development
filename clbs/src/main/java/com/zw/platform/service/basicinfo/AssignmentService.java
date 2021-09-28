package com.zw.platform.service.basicinfo;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * <p> Title: 从业人员管理Service </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author wangying
 * @deprecated  since 4.4.0 please use groupService to replace
 */
public interface AssignmentService {

    /**
     * 查询
     */
    List<Assignment> findAssignment(AssignmentQuery query);

    /**
     * 查询用户权限分组
     * @return List<Assignment>
     * @author wangying
     */
    List<Assignment> findUserAssignment(String userId, List<String> groupList);

    List<Assignment> findUserAssignmentNum(String userId, List<String> groupList, String monitorType,
        String deviceType);

    /**
     * 添加分组
     * @return boolean
     * @author wangying
     */

    boolean addAssignment(AssignmentForm form);

    /**
     * 根据id查询分组
     * @return Assignment
     * @author wangying
     */
    Assignment findAssignmentById(String id);

    Assignment findAssignmentByIdNum(String id);

    /**
     * 根据分组id查询车辆
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findVehicleByAssignmentId(String assignmentId);

    /**
     * 根据分组id查询监控对象
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> findMonitorByAssignmentId(String assignmentId);


    /**
     * 组装分组树结构
     * @return JSONArray
     * @author wangying
     */
    JSONArray getAssignmentTree();


    /**
     * 移除车辆
     * @return boolean
     * @author wangying
     */
    boolean removeAssignmentVehicle(String assignment, String vehicleList);


    /**
     * 递归获取当前组织及直属上级组织
     * @param allList    所有组织
     * @param id         当前组织id
     * @param returnList list
     * @author wangying
     */
    void getParentOrg(List<OrganizationLdap> allList, String id, List<OrganizationLdap> returnList) throws Exception;

    /**
     * 查询同一组织下是否有相同名称的分组
     * @return Assignment
     * @author wangying
     */
    List<Assignment> findByNameForOneOrg(String name, String groupId);

    /**
     * 修改时，查询非当前车辆以外，查询统一组织下分组名称是否重复
     */
    List<Assignment> findOneOrgAssiForNameRep(String id, String name, String groupId);


    /**
     * 根据组织查询组织下的分组（不带权限）
     */
    List<Assignment> findAssignmentByGroupId(String groupId);

    /**
     * 根据组织查询组织下的分组（不带权限）,排除当前分组
     */
    List<Assignment> findAssignByGroupIdExpectVehicle(String groupId, String assignmentId) throws Exception;

    /**
     * 下载分组模板
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导出
     * @param title    excel名称
     * @param type     导出类型（1:导出数据；2：导出模板）
     * @param response 文件
     */
    boolean exportAssignment(String title, int type, HttpServletResponse response) throws Exception;


    /**
     * 获取分组的监控对象信息
     * @param assignmentId 分组ID
     * @return 监控对象信息的JSON字符串
     */
    JSONArray getMonitorByAssignmentID(String assignmentId);

    /**
     * 组装分组的树结构
     * @param assignmentList 分组list
     * @param result         树结构
     * @param type           根节点是否可选
     * @param isBigData      是否是车的数量大于2000时
     */
    List<String> putAssignmentTree(List<Assignment> assignmentList, JSONArray result, String type, boolean isBigData);

    /**
     * 根据组织id查询其下的监控对象数量
     */

    Set<String> getMonitorCountByGroup(String groupId, String type, String deviceType);

    /**
     * 根据监控对象类型id list 查询分组对应的所属企业 name
     * @author yangyi
     */
    String getAssignsByMonitorId(String vehicleIds);


    List<Assignment> findUserAssignmentFuzzy(String userId, List<String> groupList, String query);
}
