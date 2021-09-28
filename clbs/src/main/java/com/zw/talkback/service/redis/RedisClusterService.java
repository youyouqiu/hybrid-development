package com.zw.talkback.service.redis;

import com.alibaba.fastjson.JSONArray;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface RedisClusterService {

    /**
     * 添加用户分组权限
     * @param userList 用户id列表
     * @param assignList 分组id列表
     * @param session
     */
    void addUserAssignments(List<String> userList, List<String> assignList, HttpSession session);

    /**
     * 删除分组相关缓存
     * @param assignList 分组列表
     */
    void deleteAssignmentsCache(List<Cluster> assignList);

    /**
     * 更新分组名称
     * @param id 分组id
     * @param oldName 分组原名称
     * @param newName 分组新名称
     */
    void updateAssignment(String id, String oldName, String newName);

    /**
     * 获取用户的分组信息
     * @param userID 用户ID
     * @return 分组信息
     */
    JSONArray getAssignmentByUserID(String userID);

    /**
     * 更新用户的分组授权
     * @param userID 用户ID
     * @param delList 待删除的分组ID列表
     * @param addList 待添加的分组ID列表
     */
    void updateAssignmentsByUserID(String userID, List<String> delList, List<String> addList) throws Exception;

    /**
     * 更新分组的监控对象信息
     * @param delList 待删除的监控对象ID列表
     * @param addList 待添加的监控对象ID列表
     */
    void updateVehiclesCache(List<AssignmentVehicleForm> delList, List<AssignmentVehicleForm> addList);
}
