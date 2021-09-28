package com.zw.platform.service.redis.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.service.redis.RedisAssignService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RedisAssignServiceImpl implements RedisAssignService {

    /**
     * 更新分组的监控人员信息
     * @param groupId 分组ID
     * @param delList      待删除的监控人员ID列表
     * @param addList      待添加的监控人员ID列表
     */
    @Override
    public void updateUsersByAssignmentId(String groupId, List<String> delList, List<String> addList) {
        // 如果待添加和删除的列表都为空则直接返回
        if (delList.isEmpty() && addList.isEmpty()) {
            return;
        }
        Map<RedisKey, Collection<String>> valueMap =
            delList.stream().collect(Collectors.toMap(
                RedisKeyEnum.USER_GROUP::of, o -> Collections.singletonList(groupId)));
        // 删除分组授权
        RedisHelper.batchDeleteSet(valueMap);
        // 添加分组授权
        valueMap =
            addList.stream().collect(Collectors.toMap(
                RedisKeyEnum.USER_GROUP::of, o -> Collections.singletonList(groupId)));
        RedisHelper.batchAddToSet(valueMap);
    }

}
