package com.zw.platform.basic.util;

import com.google.common.collect.Lists;
import com.zw.platform.util.AssembleUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据库分批操作工具
 * @author zhangjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DbUtils {
    /**
     * 默认查询分区大小
     */
    private static final int DEFAULT_QUERY_SIZE = 1000;
    /**
     * 默认新增分区大小
     */
    private static final int DEFAULT_ADD_SIZE = 500;

    /**
     * 按默认分区数进行分批查询 --不支持按查询列表排序
     * @param source        查询条件
     * @param queryFunction 查询方法
     * @param <R>           分区条数
     * @param <S>           查询条件类型
     * @return 查询结果
     */
    public static <R, S> List<R> partitionQuery(List<S> source, Function<List<S>, List<R>> queryFunction) {
        return partitionQuery(source, queryFunction, DEFAULT_QUERY_SIZE);
    }

    /**
     * 分批查询--返回结果按查询列表进行排序
     * @param source        查询条件
     * @param queryFunc     查询方法
     * @param sortFiledFunc 生成排序Map的方法
     * @param <R>           返回结果类型
     * @param <S>           查询条件类型
     * @return 查询结果
     */
    public static <R, S> List<R> partitionSortQuery(List<S> source, Function<List<S>, List<R>> queryFunc,
        Function<R, S> sortFiledFunc) {
        return partitionSortQuery(source, queryFunc, sortFiledFunc, DEFAULT_QUERY_SIZE);
    }

    /**
     * 批量添加或更新数据
     * @param source   操作数据源
     * @param function 更新方法
     * @param <S>      操作数据源类型
     */
    public static <S> void partitionUpdate(List<S> source, Function<List<S>, Boolean> function) {
        partitionUpdate(source, function, DEFAULT_ADD_SIZE);
    }

    /**
     * 批量添加或更新数据
     * @param source        操作数据源
     * @param function      更新方法
     * @param partitionSize 分区条数
     * @param <S>           操作数据源类型
     */
    public static <S> void partitionUpdate(List<S> source, Function<List<S>, Boolean> function, int partitionSize) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        for (List<S> partition : Lists.partition(source, partitionSize)) {
            function.apply(partition);
        }
    }

    /**
     * 分批查询--不支持按查询列表排序
     * @param source        查询条件
     * @param queryFunction 查询方法
     * @param partitionSize 分区条数
     * @param <R>           返回结果类型
     * @param <S>           查询条件类型
     * @return 查询结果
     */
    public static <R, S> List<R> partitionQuery(List<S> source, Function<List<S>, List<R>> queryFunction,
        int partitionSize) {
        if (CollectionUtils.isEmpty(source)) {
            return Lists.newArrayList();
        }
        List<R> result = new ArrayList<>();
        for (List<S> partition : Lists.partition(source, partitionSize)) {
            result.addAll(queryFunction.apply(partition));
        }
        return result;
    }

    /**
     * 分批查询--支持按查询列表排序
     * @param source        查询条件
     * @param queryFunc     查询方法
     * @param sortFiledFunc 生成排序Map的方法
     * @param partitionSize 分区条数
     * @param <R>           返回结果类型
     * @param <S>           查询条件类型
     * @return 查询结果
     */
    public static <R, S> List<R> partitionSortQuery(List<S> source, Function<List<S>, List<R>> queryFunc,
        Function<R, S> sortFiledFunc, int partitionSize) {
        if (CollectionUtils.isEmpty(source)) {
            return Lists.newArrayList();
        }
        List<R> result = new ArrayList<>();
        for (List<S> partition : Lists.partition(source, partitionSize)) {
            List<R> tempResult = queryFunc.apply(partition);
            Map<S, R> resultMap = AssembleUtil.collectionToMap(tempResult, sortFiledFunc);
            for (S value : partition) {
                if (resultMap.containsKey(value)) {
                    result.add(resultMap.get(value));
                }
            }
        }
        return result;
    }
}
