package com.zw.talkback.service.baseinfo;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.domain.basicinfo.FriendInfo;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;

import java.util.List;

/**
 * 对讲对象服务层
 */
public interface IntercomObjectService {

    boolean addIntercomInfo(IntercomObjectInfo intercomObjectInfo);

    /**
     * 定位绑定对象转换成对讲对象
     * @param form 录入绑定信息
     * @return 对讲对象
     * @throws Exception e
     */
    IntercomObjectInfo initIntercomObjectInfo(InConfigInfoForm form) throws Exception;

    /**
     * 缓存对讲对象
     * @param intercomObjectInfo 对讲对象
     */
    void addIntercomObjectCache(IntercomObjectInfo intercomObjectInfo);

    /**
     * 在对讲平台添加、修改对讲对象
     * @param configId  configId
     * @param ipAddress ipAddress
     * @return Boolean
     */
    JsonResultBean addIntercomInfoToIntercomPlatform(String configId, String ipAddress) throws Exception;

    /**
     * 批量生成对讲对象
     * @param intercomInfoIds intercomInfoIds
     * @param ipAddress       ipAddress
     * @return JsonResultBean
     */
    JsonResultBean addIntercomInfosToIntercomPlatform(String intercomInfoIds, String ipAddress) throws Exception;

    /**
     * 根据对讲对象详情
     * @param configId configId
     * @return IntercomObjectInfo
     * @throws Exception Exception
     */
    IntercomObjectInfo getIntercomBindInfoByConfigId(String configId) throws Exception;

    /**
     * 获取用户当前好友
     * @param userId userId
     * @return 用户当前好友
     */
    List<FriendInfo> findFriendList(Long userId);

    /**
     * 支持模糊查询
     * @param type       type
     * @param queryParam 查询条件
     * @return JSONArray
     * @throws Exception e
     */
    JSONArray findIntercomObjectTree(String type, String queryParam) throws Exception;

    /**
     * 根据分组ID,查询对讲对象
     * @param assignmentId assignmentId
     * @return JSONArray
     */
    JSONArray getIntercomObjectByAssignmentId(String assignmentId);

    /**
     * 好友设置树
     * @param queryParam queryParam
     * @return String
     */
    String getGroupAndUserTree(String queryParam);

    /**
     * 添加好友
     * @param userform    userform
     * @param monitorName monitorName
     * @param ipAddress   ipAddress
     * @return JsonResultBean
     */
    JsonResultBean addFriends(String userform, String monitorName, Long userId, String ipAddress);

    /**
     * 更新对讲对象是否录音状态
     * @param recordEnable 是否录音: 0: 不录音; 1: 录音
     * @param configId     configId
     * @param ipAddress    ipAddress
     * @return JsonResultBean
     */
    JsonResultBean updateRecordStatus(Integer recordEnable, String configId, String ipAddress);

    /**
     * 解绑监控对象关系
     * @param configIds  configIds
     * @param ipAddress  ipAddress
     * @param monitorIds monitorIds
     * @return JsonResultBean
     */
    JsonResultBean deleteIntercomObject(String configIds, String ipAddress, List<String> monitorIds);

    /**
     * 批量删除对讲对象
     * @param intercomObjectInfos intercomObjectInfos
     * @param isUpdateConfig      是否修改定位绑定相关数据 先解绑定位关系时，不需要在修改该参数
     * @return 删除成功的监控对象
     */
    List<String> deleteIntercomObjects(List<IntercomObjectInfo> intercomObjectInfos, boolean isUpdateConfig);
}
