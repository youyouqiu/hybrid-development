package com.zw.platform.util;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 平台导入util
 * @author create by zhouzongbo on 2020/9/1.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlatformImportUtil {

    /**
     * 默认分区大小
     */
    public static final int DEFAULT_PARTITION_SIZE = 1000;

    /**
     * 分批更新
     * @param source      数据源
     * @param addFunction 新增function
     * @param <S>         数据源类型
     * @return true: 成功, false: 失败
     */
    public static <S> boolean partition(Collection<S> source, Function<Collection<S>, Boolean> addFunction) {
        return partition(source, addFunction, DEFAULT_PARTITION_SIZE);
    }

    /**
     * 分批更新
     * @param source        数据源
     * @param addFunction   新增function
     * @param partitionSize 分区条数, 默认每次1000条
     * @param <S>           数据源类型
     * @return true: 成功, false: 失败
     */
    public static <S> boolean partition(Collection<S> source, Function<Collection<S>, Boolean> addFunction,
        int partitionSize) {
        if (CollectionUtils.isEmpty(source)) {
            return false;
        }

        final List<List<S>> partitions = Lists.partition(new ArrayList<>(source), partitionSize);
        for (List<S> partition : partitions) {
            addFunction.apply(partition);
            // TODO 存储百分比在这里更新
        }

        return true;
    }
}
