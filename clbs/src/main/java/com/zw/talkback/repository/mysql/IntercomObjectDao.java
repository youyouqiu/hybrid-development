package com.zw.talkback.repository.mysql;

import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.FriendForm;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 对讲对象的DAO层
 */
public interface IntercomObjectDao {
    /**
     * 添加对讲对象
     * @param intercomObjectInfo 对讲对象
     * @return 是否成功
     */
    boolean addIntercomObject(IntercomObjectInfo intercomObjectInfo);

    /**
     * 批量添加对讲对象
     * @param intercomObjects intercomObjects
     * @return 是否添加成功
     */
    boolean addBatchIntercomObject(@Param("intercomObjects") List<IntercomObjectInfo> intercomObjects);

    IntercomObjectInfo getIntercomInfoByConfigId(@Param("configId") String configId);

    List<IntercomObjectInfo> findIntercomByConfigIds(@Param("configIdList") List<String> configIdList);

    boolean updateIntercomInfo(IntercomObjectInfo intercomObjectInfo);

    List<FriendForm> findFriedList(@Param("userId") Long userId);

    /**
     * 查询分组下的对讲对象
     * @param assignmentId assignmentId
     * @return IntercomObjectInfos
     */
    List<IntercomObjectInfo> findIntercomObjectByAssignmentIds(@Param("assignmentIds") List<String> assignmentId,
        @Param("queryParam") String queryParam);

    /**
     * 根据对讲对象删除好友
     * @param userId userId
     */
    void deleteIntercomFriendByUserId(@Param("userId") Long userId);

    boolean addFriendForm(@Param("friends") List<FriendForm> friends,
        @Param("createDataUsername") String createDataUsername);

    /**
     * 查询用户分组权限（管理员）
     */
    List<Assignment> findUserAssignment(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 更新录音状态
     * @param intercomObjectInfo intercomObjectInfo
     * @return 是否更新成功
     */
    boolean updateRecordStatus(IntercomObjectInfo intercomObjectInfo);

    void deleteIntercomInfoByIds(@Param("sortIntercomInfoIdList") List<String> sortIntercomInfoIdList);

    Map<String, Object> getMonitorId(Long userId);

    boolean addNotificationRecord(@Param("id") String id, @Param("userId") String uuid,
        @Param("receiveId") String receiveId, @Param("content") String content, @Param("date") Date date);

    List<Map<String, Object>> notificationRecordList(@Param("userId") String uuid,
        @Param("receiveId") String receiveId);

    IntercomObjectInfo getConfigByMonitorName(@Param("monitorName") String monitorName,
        @Param("monitorType") String monitorType);

    List<IntercomObjectInfo> getConfigByConfigIds(@Param("configIdList") List<String> configIdList);

    /**
     * 根据监控对象获取对讲对象
     * @param monitorIdList 监控对象ID
     * @return 对讲对象
     */
    List<IntercomObjectInfo> getIntercomObjectByMonitorIds(@Param("monitorIdList") List<String> monitorIdList);

    /**
     * 获取绑定基本信息
     * @param configId configId
     * @return IntercomObjectInfo
     */
    IntercomObjectInfo getConfigInfo(@Param("configId") String configId);

    IntercomObjectInfo getIntercomObjectBySim(@Param("simcardId") String simcardId);
}
