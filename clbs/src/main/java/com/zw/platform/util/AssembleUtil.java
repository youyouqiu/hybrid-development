package com.zw.platform.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 组装参数工具类
 * <p>
 * 用得太频繁了，代码很长，只能封装一下
 *
 * @author Zhang Yanhui
 * @since 2020/4/15 16:41
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssembleUtil {

    /**
     * 根据sourceData生成一个map，key由keyMapper生成，value为元素本身
     * <p>
     * value有重复时取第一条数据（增强健壮性）
     *
     * @param sourceData 原始数据
     * @param keyMapper  生成map的key的方法
     * @param <K>        map的k类型
     * @param <V>        map的v类型
     * @return map
     */
    public static <K, V> Map<K, V> convertToMap(Collection<V> sourceData,
        Function<V, K> keyMapper) {
        return collectionToMap(sourceData, keyMapper);
    }

    /**
     * listMapper根据sourceData生成list，再根据keyMapper生成一个map，value为元素本身
     * <p>
     * value有重复时取第一条数据（增强健壮性）
     *
     * @param sourceData 原始数据
     * @param listMapper list生成方法（往往是SQL查询/其他service调用）
     * @param keyMapper  生成map的key的方法
     * @param <S>        源数据类型
     * @param <K>        map的k类型
     * @param <V>        map的v类型
     * @return map
     */
    public static <S, K, V> Map<K, V> convertToMap(Collection<S> sourceData,
        Function<Collection<S>, Collection<V>> listMapper,
        Function<V, K> keyMapper) {
        return convertToMap(sourceData, listMapper, keyMapper, Function.identity(), retainFirst());
    }

    /**
     * listMapper根据sourceData生成list，再根据keyMapper和valueMapper生成一个map
     * <p>
     * value有重复时取第一条数据（增强健壮性）
     *
     * @param sourceData  原始数据
     * @param listMapper  list生成方法（往往是SQL查询/其他service调用）
     * @param keyMapper   生成map的key的方法
     * @param valueMapper 生成map的value的方法
     * @param <S>         源数据类型
     * @param <K>         map的k类型
     * @param <V>         map的v类型
     * @return map
     */
    public static <S, T, K, V> Map<K, V> convertToMap(Collection<S> sourceData,
        Function<Collection<S>, Collection<T>> listMapper,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper) {
        return convertToMap(sourceData, listMapper, keyMapper, valueMapper, retainFirst());
    }

    /**
     * listMapper根据sourceData生成list，再根据keyMapper和valueMapper生成一个map
     *
     * @param sourceData    原始数据
     * @param listMapper    list生成方法（往往是SQL查询/其他service调用）
     * @param keyMapper     生成map的key的方法
     * @param valueMapper   生成map的value的方法
     * @param mergeFunction value重复时的处理方式
     * @param <S>           源数据类型
     * @param <T>           list元素类型
     * @param <K>           map的k类型
     * @param <V>           map的v类型
     * @return map
     */
    public static <S, T, K, V> Map<K, V> convertToMap(Collection<S> sourceData,
        Function<Collection<S>, Collection<T>> listMapper,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper,
        BinaryOperator<V> mergeFunction) {
        return CollectionUtils.isEmpty(sourceData)
                // 这个map是只读的，如果有修改的需要，改成new HashMap<>()
                ? new HashMap<>()
                : collectionToMap(listMapper.apply(sourceData), keyMapper, valueMapper, mergeFunction,
                suitSizedMap(sourceData));
    }

    /**
     * (sql)单个入数转map
     * @param sourceData 源数据
     * @param listMapper list生成方法（往往是SQL查询/其他service调用）
     * @param keyMapper 生成map的key的方法
     * @param valueMapper 生成map的value的方法
     * @param <S> 源数据类型
     * @param <T> list元素类型
     * @param <K> 源数据类型
     * @param <V> map的v类型
     * @return map
     */
    public static <S, T, K, V> Map<K, V> convertToMap(S sourceData,
        Function<S, Collection<T>> listMapper,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper) {
        return Objects.isNull(sourceData)
                ? new HashMap<>()
                : collectionToMap(listMapper.apply(sourceData), keyMapper, valueMapper);
    }

    /**
     * 阔以替换convertToMap
     * 集合转换为map, 如果数据冲突默认返回上一次的值
     * @param source 数据源
     * @param keyMapper 生成map的key方法
     * @param <K> map的key类型
     * @param <V> map的value类型
     * @return map
     */
    public static <K, V> Map<K, V> collectionToMap(Collection<V> source, Function<V, K> keyMapper) {
        return CollectionUtils.isEmpty(source)
                ? new HashMap<>()
                : collectionToMap(source, keyMapper, Function.identity(), retainFirst());
    }

    /**
     * 集合转换为map, 如果数据冲突默认返回上一次的值
     * @param source 数据源
     * @param keyMapper 生成map的key方法
     * @param valueMapper 生成map的value方法
     * @param <T> 源数据类型
     * @param <K> map的key类型
     * @param <V> map的value类型
     * @return map
     */
    public static <T, K, V> Map<K, V> collectionToMap(Collection<T> source,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper) {
        return CollectionUtils.isEmpty(source)
                ? new HashMap<>()
                : source.stream().collect(Collectors.toMap(keyMapper, valueMapper, retainFirst()));
    }

    /**
     * 集合转换为map, 如果数据冲突默认返回上一次的值
     * @param source 数据源
     * @param keyMapper 生成map的key方法
     * @param valueMapper 生成map的value方法
     * @param mergeFunction key冲突的数据集合方法
     * @param <T> 源数据类型
     * @param <K> map的key类型
     * @param <V> map的value类型
     * @return map
     */
    public static <T, K, V> Map<K, V> collectionToMap(Collection<T> source,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper,
        BinaryOperator<V> mergeFunction) {
        return CollectionUtils.isEmpty(source)
                ? new HashMap<>()
                : source.stream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * 集合转换为map, 如果数据冲突默认返回上一次的值
     * @param source 数据源
     * @param keyMapper 生成map的key方法
     * @param valueMapper 生成map的value方法
     * @param mergeFunction key冲突的数据集合方法
     * @param supplier 自行一容器类型
     * @param <T> 源数据类型
     * @param <K> map的key类型
     * @param <V> map的value类型
     * @return map
     */
    public static <T, K, V> Map<K, V> collectionToMap(Collection<T> source,
        Function<T, K> keyMapper,
        Function<T, V> valueMapper,
        BinaryOperator<V> mergeFunction,
        Supplier<HashMap<K, V>> supplier) {
        return CollectionUtils.isEmpty(source)
                ? new HashMap<>()
                : source.stream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, supplier));
    }

    /**
     * 如果key重复了就保留第一个元素
     *
     * @param <T> 类型
     * @return 第一个元素
     */
    public static <T> BinaryOperator<T> retainFirst() {
        return (o, p) -> o;
    }

    /**
     * 根据集合size生成具有合适的初始化容量的map
     *
     * @param collection 集合，这里只取它的size，传入整个集合方便lambda方法引用
     * @param <K>        k 类型
     * @param <V>        v 类型
     * @return 空map，初始化容量
     */
    public static <K, V> Supplier<HashMap<K, V>> suitSizedMap(Collection<?> collection) {
        final float loadFactor = 0.75f;
        final int initCapacity = (int) (collection.size() / loadFactor) + 1;
        return () -> new HashMap<>(initCapacity);
    }
}