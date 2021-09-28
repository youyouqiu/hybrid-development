package com.zw.platform.basic.service;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * redis初始化缓存管理接口
 *
 * @author zhangjuan
 * @date 2020/9/25
 */
public interface CacheService {
    int BATCH_OPERATION = 1000;

    /**
     * redis初始化
     */
    void initCache();

    /**
     * 切分数组成几份
     *
     * @param originalList 原始数组
     * @return 被切分的子数组集合
     */
    default List<List<String>> cutList(List<String> originalList) {
        if (CollectionUtils.isEmpty(originalList)) {
            return new ArrayList<>();
        }
        int size = originalList.size();
        int count;
        if (size % BATCH_OPERATION == 0) {
            count = size / BATCH_OPERATION;
        } else {
            count = size / BATCH_OPERATION + 1;
        }
        List<List<String>> subLists = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            int startIndex = (i - 1) * BATCH_OPERATION;
            int endIndex = Math.min(size, i * BATCH_OPERATION);
            subLists.add(originalList.subList(startIndex, endIndex));
        }
        return subLists;
    }
}
