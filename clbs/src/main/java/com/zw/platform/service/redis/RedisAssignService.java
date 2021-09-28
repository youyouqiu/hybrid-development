package com.zw.platform.service.redis;

import java.util.List;

public interface RedisAssignService {


    /**
     * 更新分组的监控人员信息
     * @param groupId 分组ID
     * @param delList 待删除的监控人员ID列表
     * @param addList 待添加的监控人员ID列表
     */
    void updateUsersByAssignmentId(String groupId, List<String> delList, List<String> addList);
}
