package com.zw.talkback.service.dispatch;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * 监控调度服务层接口
 */
public interface MonitoringDispatchService {

    /**
     * 调度服务登录
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean dispatchLoginIn() throws Exception;

    /**
     * 调度服务登出并清除临时组
     * @param userName 用户名称
     * @throws Exception Exception
     */
    void dispatchLoginOut(String userName) throws Exception;

    /**
     * 获得对讲对象树
     * @param interlocutorStatus 对讲对象状态 0:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getInterlocutorTree(Integer interlocutorStatus) throws Exception;

    /**
     * 通过名称模糊搜索对讲对象
     * @param queryParam 搜索条件
     * @param queryType  搜索类型 name:对讲对象名称; assignment:分组名称;
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean fuzzySearchInterlocutor(String queryParam, String queryType) throws Exception;

    /**
     * 查询对讲组内用户
     * @param intercomGroupId    对讲群组id
     * @param interlocutorStatus 对讲对象状态 0:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getInterlocutorAssignmentMember(Long intercomGroupId, Integer interlocutorStatus) throws Exception;

    /**
     * 查询对讲用户
     * @param interlocutorId 对讲对象id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getInterlocutorInfoById(Long interlocutorId) throws Exception;

    /**
     * 查找对讲对象,通过画的圆形区域
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param longitude      经度
     * @param latitude       纬度
     * @param radius         半径
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean findInterlocutorByCircleArea(String assignmentId, String assignmentType, Double longitude,
        Double latitude, Double radius) throws Exception;

    /**
     * 查找对讲对象,通过画的矩形区域
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param leftLongitude  矩形区域左上角的经度
     * @param leftLatitude   矩形区域左上角的纬度
     * @param rightLongitude 矩形区域右下角的经度
     * @param rightLatitude  矩形区域右下角的纬度
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean findInterlocutorByRectangleArea(String assignmentId, String assignmentType, Double leftLongitude,
        Double leftLatitude, Double rightLongitude, Double rightLatitude) throws Exception;

    /**
     * 查找对讲对象,通过固定对象
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean findInterlocutorByFixedInterlocutor(String assignmentId, String assignmentType) throws Exception;

    /**
     * 获得技能列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getAllSkillList() throws Exception;

    /**
     * 获得对讲机型列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getAllIntercomModeList() throws Exception;

    /**
     * 获得驾照类别列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getAllDriverLicenseCategoryList() throws Exception;

    /**
     * 获得资格证列表
     * @return JsonResultBean
     * @throws Exception Exception
     */

    JsonResultBean getAllQualificationList() throws Exception;

    /**
     * 获得血型列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getAllBloodTypeList() throws Exception;

    /**
     * 查找对讲对象,通过固定条件
     * @param assignmentId             如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType           分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param skillIds                 技能id
     * @param intercomModelIds         对讲机型id
     * @param driverLicenseCategoryIds 驾照类别id
     * @param qualificationIds         资格证id
     * @param gender                   性别 1:男; 2:女
     * @param bloodTypeIds             血型id
     * @param ageRange                 年龄范围 ps:20,50
     * @param longitude                经度
     * @param latitude                 纬度
     * @param radius                   半径
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean findInterlocutorByFixedCondition(String assignmentId, String assignmentType, String skillIds,
        String intercomModelIds, String driverLicenseCategoryIds, String qualificationIds, String gender,
        String bloodTypeIds, String ageRange, Double longitude, Double latitude, Double radius) throws Exception;

    /**
     * 判读对讲对象任务组是否超出限制
     * @param interlocutorIds 对讲对象id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean judgeInterlocutorTaskAssignmentNumIsOverLimit(String interlocutorIds) throws Exception;

    /**
     * 创建任务组
     * @param assignmentName  任务组名称
     * @param interlocutorIds 组内对讲对象id
     * @param ipAddress       ip
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean addTaskAssignmentAndMember(String assignmentName, String interlocutorIds, String ipAddress)
        throws Exception;

    /**
     * 创建临时组
     * @param assignmentName  分组名称
     * @param ipAddress       ip
     * @param intercomGroupId 对讲组id
     * @param interlocutorIds 组内对讲对象id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean addTemporaryAssignment(String assignmentName, String ipAddress, Long intercomGroupId,
        String interlocutorIds) throws Exception;

    /**
     * 解散任务组
     * @param assignmentId   分组id
     * @param assignmentType 分组类型 1：固定组；2：任务组; 3:临时组
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean unbindAssignmentAndMonitor(String assignmentId, String assignmentType) throws Exception;

    /**
     * 判断分组是否能加入对讲对象
     * @param assignmentId   分组id
     * @param assignmentType 分组类型 1：固定组；2：任务组; 3:临时组
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean judgeAssignmentIfJoinMonitor(String assignmentId, String assignmentType) throws Exception;

    /**
     * 加入任务组
     * @param assignmentId    任务组id
     * @param interlocutorIds 组内对讲对象id
     * @param ipAddress       ip
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean insertTaskAssignmentAndMember(String assignmentId, String interlocutorIds, String ipAddress)
        throws Exception;

    /**
     * 加入临时组 记录日志
     * @param intercomGroupId 对讲群组id
     * @param interlocutorIds
     * @param ipAddress       ip
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean insertTemporaryAssignmentRecordLog(Long intercomGroupId, String interlocutorIds, String ipAddress)
        throws Exception;

    /**
     * 踢出任务组内对讲对象
     * @param assignmentId   分组id
     * @param interlocutorId 对讲对象id
     * @param ipAddress      ip
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean removeTaskAssignmentInterlocutor(String assignmentId, Long interlocutorId, String ipAddress)
        throws Exception;

    /**
     * 踢出临时组内对讲对象记录日志
     * @param intercomGroupId 对讲群组id
     * @param interlocutorId  对讲对象id
     * @param ipAddress       ip
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean removeTemporaryAssignmentInterlocutorRecordLog(Long intercomGroupId, Long interlocutorId,
        String ipAddress) throws Exception;

    /**
     * admin用户需要移除admin的所属企业,修改树的顶级节点为admin下的第一个组织
     * @param currentOrganization          当前组织信息
     * @param userOwnAuthorityOrganizeInfo 用户拥有的组织信息
     * @return OrganizationLdap
     */
    OrganizationLdap updateOrganizationStructure(OrganizationLdap currentOrganization,
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo);

}
