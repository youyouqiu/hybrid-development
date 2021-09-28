package com.zw.platform.basic.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.push.RedisKeyExpire.listener.KeyExpiredListener;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.CosUtil;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 张娟
 */
@Log4j2
@Component("redisHelper")
public final class RedisHelper {
    private static final Logger logger = LogManager.getLogger(RedisHelper.class);
    private static final Set<String> SENTINEL_SET = new HashSet<>();
    private static final Integer SCAN_COUNT = 5000;
    private static String password;
    /**
     * 超时时间6个小时
     */
    public static final int SIX_HOUR_REDIS_EXPIRE = 6 * 60 * 60;
    /**
     * 超时时间3个小时
     */
    public static final int THREE_HOUR_REDIS_EXPIRE = 3 * 60 * 60;

    /**
     * 超时时间半个小时
     */
    public static final int HALF_HOUR_REDIS_EXPIRE = 60 * 30;

    @Autowired
    public RedisHelper(@Value("${redis.sentinels}") String sentinels, @Value("${redis.password}") String password) {
        Collections.addAll(SENTINEL_SET, sentinels.split(","));
        RedisHelper.password = password;
    }

    private static JedisSentinelPool jedisPool = null;

    /**
     * 初始化Redis连接池
     */
    private static void initialPool() {
        // 可用连接实例的最大数目，默认值为8；
        // 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
        int maxActive = 200;
        // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
        int maxIdle = 50;
        // 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
        int maxWait = 10000;

        /**
         * redis读取数据超时时间增加到8s中
         */
        int readTimeOut = 8000;

        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxWaitMillis(maxWait);
            config.setMaxIdle(maxIdle);
            // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            config.setTestOnBorrow(true);
            if (!SENTINEL_SET.isEmpty()) {
                jedisPool = new JedisSentinelPool("mymaster", SENTINEL_SET, config, readTimeOut, password);
            }
        } catch (Exception e) {
            logger.error("First create JedisPool error.", e);
        }
    }

    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (jedisPool == null) {
            initialPool();
        }
    }

    /**
     * 同步获取Jedis实例
     * @return Jedis
     */
    private static Jedis getJedis(int dbIndex) {
        if (jedisPool == null) {
            poolInit();
        }
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
                jedis.select(dbIndex);
            }
        } catch (Exception e) {
            logger.error("Get jedis error.", e);
        }
        Objects.requireNonNull(jedis);
        return jedis;
    }

    /**
     * 执行Redis操作
     * @param database database
     * @param function function
     * @param <R>      返回值
     * @return R can be null
     */
    private static <R> R doRedis(int database, Function<Jedis, R> function) {
        try (Jedis jedis = getJedis(database)) {
            return function.apply(jedis);
        } catch (Exception e) {
            StackTraceElement []stack = Thread.currentThread().getStackTrace();
            Optional<StackTraceElement> first = Arrays.stream(stack).skip(1).limit(1).findFirst();
            if (first.isPresent()) {
                StackTraceElement stackTraceElement = first.get();
                log.error("redis操作报错,操作类为：{}，操作方法为：{}",
                    stackTraceElement.getClassName(), stackTraceElement.getMethodName(), e);
            }
            throw e;
        }
    }

    /**
     * 执行Redis批量操作
     * @param keys     keys
     * @param function function
     * @param database database
     */
    private static <T, R> Map<T, R> pipelineDo(int database, Collection<T> keys,
        BiFunction<T, Pipeline, Response<R>> function) {
        final Map<T, Response<R>> response = new LinkedHashMap<>(keys.size());
        try (Jedis jedis = getJedis(database)) {
            final Pipeline pipeline = jedis.pipelined();
            for (T key : keys) {
                response.put(key, function.apply(key, pipeline));
            }
            pipeline.sync();
        }
        return extractResponse(response);
    }

    /**
     * 执行Redis批量操作
     * @param keyMap   keyMap
     * @param function function
     * @param database database
     */
    private static <K, V, R> Map<K, R> pipelineDo(int database, Map<K, V> keyMap,
        TriFunction<K, V, Pipeline, Response<R>> function) {
        final Map<K, Response<R>> response = new LinkedHashMap<>(keyMap.size());
        try (Jedis jedis = getJedis(database)) {
            final Pipeline pipeline = jedis.pipelined();
            keyMap.forEach((k, v) -> response.put(k, function.apply(k, v, pipeline)));
            pipeline.sync();
        }
        return extractResponse(response);
    }

    /**
     * 拆response
     */
    private static <K, R> Map<K, R> extractResponse(Map<K, Response<R>> response) {
        final Map<K, R> result = new LinkedHashMap<>(response.size());
        response.forEach((k, v) -> Optional.ofNullable(v.get()).ifPresent(value -> result.put(k, value)));
        return result;
    }

    /**
     * 判断redis中key是存在
     * @param redisKey redisKey
     * @return true 存在 false 不存在
     */
    public static Boolean isContainsKey(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.exists(redisKey.get()));
    }

    /**
     * 批量设置string
     * @param stringMap
     */
    public static void batchAddToString(Map<RedisKey, String> stringMap) {
        if (MapUtils.isEmpty(stringMap)) {
            return;
        }
        int database = stringMap.keySet().iterator().next().database();
        pipelineDo(database, stringMap.entrySet(), (key, pipeline) -> pipeline.set(key.getKey().get(), key.getValue()));
    }

    /**
     * 批量设置一批key为同一个值
     * @param redisKeys
     * @param value
     */
    public static void batchAddToString(Collection<RedisKey> redisKeys, String value) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return;
        }
        int database = redisKeys.iterator().next().database();
        pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.set(key.get(), value));
    }

    /**
     * 发布订阅
     */
    public static void publish(int dbIndex, KeyExpiredListener keyExpiredListener) {

        try (Jedis jedis = getJedis(dbIndex)) {
            String patterns = "__keyevent@" + dbIndex + "__:expired";
            ;
            config(jedis);
            jedis.psubscribe(keyExpiredListener, patterns);
            ;
        } catch (Exception e) {
            logger.error("redis订阅key失效事件失败", e);
        }
    }

    private static void config(Jedis jedis) {
        String parameter = "notify-keyspace-events";
        List<String> notify = jedis.configGet(parameter);
        if ("".equals(notify.get(1))) {
            //过期事件
            jedis.configSet(parameter, "Ex");
        }
    }

    /**
     * 获取存在的key
     * @param redisKeys redisKey
     * @return 存在的redisKey
     */
    public static Set<RedisKey> isContainsKey(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashSet<>();
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, Boolean> result =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.exists(key.get()));
        return result.entrySet().stream().filter(e -> Boolean.TRUE.equals(e.getValue())).map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * 获取存在的key
     * @param redisKeys redisKey
     * @return 存在的redisKey
     */
    public static Map<String, RedisKey> isContainsKey(Map<String, RedisKey> redisKeys) {
        if (redisKeys.isEmpty()) {
            return new HashMap<>(0);
        }
        int database = redisKeys.values().iterator().next().database();
        final Map<String, Boolean> responseMap =
            pipelineDo(database, redisKeys, (k, v, pipeline) -> pipeline.exists(v.get()));

        final Map<String, RedisKey> result = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : responseMap.entrySet()) {
            if (Objects.equals(entry.getValue(), true)) {
                result.put(entry.getKey(), redisKeys.get(entry.getKey()));
            }
        }
        return result;
    }

    /**
     * 设置过期时间并返回设置设置的结果
     */
    public static Boolean expireKey(RedisKey redisKey, int second) {
        return doRedis(redisKey.database(), jedis -> jedis.expire(redisKey.get(), second) == 1);
    }

    /**
     * 设置过期时间并返回设置设置的结果
     * 必须为同一数据库
     */
    public static void expireKeys(List<RedisKey> redisKeys, int seconds) {
        if (CollectionUtils.isNotEmpty(redisKeys)) {
            final int database = redisKeys.iterator().next().database();
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.expire(key.get(), seconds));
        }
    }

    /**
     * 模糊匹配查出所有的key
     * @param pattern 模糊匹配
     * @return pattern
     */
    public static List<String> scanKeys(RedisKey pattern) {
        return doRedis(pattern.database(), jedis -> {
            List<String> result = new ArrayList<>();

            ScanParams sc = new ScanParams();
            sc.match(pattern.get());
            sc.count(SCAN_COUNT);
            String cursor = ScanParams.SCAN_POINTER_START;
            //遍历次数，当返回游标为0时，遍历结束
            int iterationNum = 0;
            while (!Objects.equals(ScanParams.SCAN_POINTER_START, cursor) || iterationNum == 0) {
                //SCAN 命令不能保证每次返回的值都是有序的，另外同一个 key 有可能返回多次
                ScanResult<String> scan = jedis.scan(cursor, sc);
                if (CollectionUtils.isNotEmpty(scan.getResult())) {
                    result.addAll(scan.getResult());
                }
                cursor = scan.getCursor();
                iterationNum++;
            }
            return result;
        });
    }

    /**
     * 删除模糊搜索的key
     * @param pattern   匹配的key
     * @param predicate 用来进行数据过滤
     */
    public static Long deleteScanKeys(RedisKey pattern, Predicate<String> predicate) {

        return doRedis(pattern.database(), jedis -> {
            Set<String> deleteKeySet = new HashSet<>();
            long deleteNum = 0;
            ScanParams sc = new ScanParams();
            sc.match(pattern.get());
            sc.count(SCAN_COUNT);
            String cursor = ScanParams.SCAN_POINTER_START;
            //遍历次数，当返回游标为0时，遍历结束
            int iterationNum = 0;
            while (!Objects.equals(ScanParams.SCAN_POINTER_START, cursor) || iterationNum == 0) {
                //SCAN 命令不能保证每次返回的值都是有序的，另外同一个 key 有可能返回多次
                ScanResult<String> scan = jedis.scan(cursor, sc);
                List<String> result = scan.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    deleteNum += result.size();
                    deleteKeySet.addAll(result);
                }
                cursor = scan.getCursor();
                iterationNum++;
            }

            if (CollectionUtils.isEmpty(deleteKeySet)) {
                return deleteNum;
            }
            Set<String> deleteFinalKeys = new HashSet<>();
            if (predicate != null) {
                for (String key : deleteKeySet) {
                    if (!predicate.test(key)) {
                        continue;
                    }
                    deleteFinalKeys.add(key);
                }
            } else {
                deleteFinalKeys = deleteKeySet;

            }
            if (!deleteFinalKeys.isEmpty()) {
                jedis.del(deleteFinalKeys.toArray(new String[0]));
            }
            return deleteNum;

        });
    }

    public static long deleteScanKeys(RedisKey pattern) {
        return deleteScanKeys(pattern, null);
    }

    /**
     * 删除redis的某个key值
     * @param redisKey redisKey
     * @return true if succeed deletion
     */
    public static boolean delete(RedisKey redisKey) {
        final Long deleted = doRedis(redisKey.database(), jedis -> jedis.del(redisKey.get()));
        return deleted != null && deleted == 1L;
    }

    /**
     * 批量进行删除redisKey--相同分区
     * @param redisKeys keys
     */
    public static void delete(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return;
        }
        final int database = CosUtil.getFirstData(redisKeys).database();
        final List<String> keys = redisKeys.stream().map(o -> o.get())
            .collect(Collectors.toCollection(() -> new ArrayList<>(redisKeys.size())));
        doDelete(database, keys);
    }

    /**
     * 批量删除key
     * <p>我们操作del可能频繁出现key量比较大的情况，导致redis阻塞，所以这里单独优化一下
     * @param database db
     * @param keys     keys to delete
     */
    private static void doDelete(int database, List<String> keys) {
        final int batchSize = 100;
        if (keys.size() < batchSize) {
            final String[] keyArr = keys.toArray(new String[0]);
            doRedis(database, jedis -> jedis.del(keyArr));
        } else {
            pipelineDo(database, Lists.partition(keys, batchSize),
                (keySublist, pipeline) -> pipeline.del(keySublist.toArray(new String[0])));
        }
    }

    /**
     * 批量进行删除redisKey--不同分区
     * @param keys keys
     */
    public static void deleteForDiffDb(Collection<RedisKey> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        Map<Integer, List<RedisKey>> dbKeyMap = new HashMap<>(16);
        List<RedisKey> tempKeyList;
        for (RedisKey redisKey : keys) {
            tempKeyList = dbKeyMap.get(redisKey.database());
            if (tempKeyList == null) {
                tempKeyList = new ArrayList<>();
            }
            tempKeyList.add(redisKey);
            dbKeyMap.put(redisKey.database(), tempKeyList);
        }

        for (Map.Entry<Integer, List<RedisKey>> entry : dbKeyMap.entrySet()) {
            delete(entry.getValue());
        }
    }

    /**
     * 按正则进行删除
     * @param pattern pattern
     */
    public static void delByPattern(RedisKey pattern) {
        List<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return;
        }
        final int database = pattern.database();
        pipelineDo(database, keys, (key, pipeline) -> pipeline.del(key));
    }

    /**
     * String类型————获取某个key的值
     * @param redisKey redisKey
     * @return valuse
     */
    public static String getString(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.get(redisKey.get()));
    }

    /**
     * String类型————存储string类型的值
     * @param redisKey redisKey
     * @param value    value
     */
    public static void setString(RedisKey redisKey, String value) {
        String formatValue = StringUtils.isNotEmpty(value) ? value : "";
        doRedis(redisKey.database(), jedis -> jedis.set(redisKey.get(), formatValue));
    }

    /**
     * String 类型————获取某个key的值，并把转换成集合（原本存的就是集合）
     * @param redisKey redisKey
     * @return 集合
     */
    public static List<String> getSetFromString(RedisKey redisKey) {
        String value = getString(redisKey);
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(value, String.class);
    }

    /**
     * String类型————存储string类型的值并设置过期时间
     * @param redisKey redisKey
     * @param value    value
     * @param seconds  缓存过期时间,单位:s
     */
    public static boolean setString(RedisKey redisKey, String value, int seconds) {
        String formatValue = StringUtils.isNotEmpty(value) ? value : "";
        SetParams setParams = new SetParams().ex(seconds);
        String result = doRedis(redisKey.database(), jedis -> jedis.set(redisKey.get(), formatValue, setParams));
        return result != null;
    }

    /**
     * String类型————当key不存在存储string类型的值并设置过期时间(用来做资源抢占)
     * @param redisKey redisKey
     * @param value    value
     * @param seconds  缓存过期时间,单位:s
     */
    public static boolean setStringNxAndExpire(RedisKey redisKey, String value, int seconds) {
        String formatValue = StringUtils.isNotEmpty(value) ? value : "";
        SetParams setParams = new SetParams().ex(seconds).nx();
        String result = doRedis(redisKey.database(), jedis -> jedis.set(redisKey.get(), formatValue, setParams));
        return result != null;
    }

    /**
     * String类型————存储string类型的值并设置过期时间 (key存在设置返回true  不存在不设置反false)
     * @param redisKey redisKey
     * @param value    value
     * @param seconds  缓存过期时间,单位:s
     */
    public static Boolean setStringNx(RedisKey redisKey, String value, int seconds) {
        String formatValue = StringUtils.isNotEmpty(value) ? value : "";
        return doRedis(redisKey.database(), jedis -> {
            long nx = jedis.setnx(redisKey.get(), formatValue);
            if (nx == 1) {
                jedis.expire(redisKey.get(), seconds);
                return true;
            }
            return false;
        });
    }

    /**
     * String类型————存储string类型的值并设置过期时间 (key存在替换过期时间)
     * @param redisKey redisKey
     * @param value    value
     * @param seconds  缓存过期时间,单位:s
     */
    public static void setStringEx(RedisKey redisKey, String value, int seconds) {
        String formatValue = StringUtils.isNotEmpty(value) ? value : "";
        doRedis(redisKey.database(), jedis -> jedis.setex(redisKey.get(), seconds, formatValue));
    }

    /**
     * String类型————批量进行存储
     * 必须为同一数据库
     * @param keyValueMap redis的key-value映射map
     */
    public static void setStringMap(Map<RedisKey, String> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        Set<Map.Entry<RedisKey, String>> entrySet = keyValueMap.entrySet();
        int database = entrySet.iterator().next().getKey().database();
        pipelineDo(database, entrySet, (entry, pipeline) -> pipeline.set(entry.getKey().get(), entry.getValue()));
    }

    /**
     * String类型————批量获取值
     * 必须为同一数据库
     * @param redisKeys redisKeys
     */
    public static List<String> batchGetString(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new ArrayList<>();
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, String> result =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.get(key.get()));
        return result.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    /**
     * String类型————批量string类型数据，并转换程对应的对象
     * 必须为同一数据库
     * @param redisKeys redisKeys
     */
    public static <T> List<T> batchGetStringObj(Collection<RedisKey> redisKeys, Class<T> cls) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new ArrayList<>();
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, String> result =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.get(key.get()));
        return result.values().stream().filter(StringUtils::isNotBlank).map(e -> JSONObject.parseObject(e, cls))
            .collect(Collectors.toList());
    }

    /**
     * String类型--根据key的正则匹配获取值
     * @param pattern key的正则表达
     * @return 匹配key的值
     */
    public static List<String> getStringByPattern(RedisKey pattern) {
        List<String> redisKeys = scanKeys(pattern);
        int database = pattern.database();
        Map<String, String> result = pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.get(key));
        return result.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    /**
     * String类型————批量获取值 返回map，传入多少key返回多少结果
     * 必须为同一数据库
     * @param redisKeyMap redisKeyMap key为返回结果时map的key
     */
    public static Map<String, String> batchGetStringMap(Map<String, RedisKey> redisKeyMap) {
        if (redisKeyMap.isEmpty()) {
            return new HashMap<>(0);
        }
        int database = redisKeyMap.values().iterator().next().database();
        return pipelineDo(database, redisKeyMap, (k, v, pipeline) -> pipeline.get(v.get()));
    }

    /**
     * list类型 --- 从头部批量插入值,按values的逆序放入
     * @param redisKey redisKey
     * @param values   值
     */
    public static void addToListTop(RedisKey redisKey, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        final String[] valueArr = values.toArray(new String[0]);
        doRedis(redisKey.database(), jedis -> jedis.lpush(redisKey.get(), valueArr));
    }

    /**
     * list类型 --- 从尾部批量插入值,按values的顺序存入 先进先出
     * @param redisKey redisKey
     * @param values   值
     */
    public static void addToListTail(RedisKey redisKey, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        final String[] valueArr = values.toArray(new String[0]);
        doRedis(redisKey.database(), jedis -> jedis.rpush(redisKey.get(), valueArr));
    }

    /**
     * list类型 --- 从头部插入元素
     * @param redisKey redisKey
     * @param value    value
     */
    public static void addToListTop(RedisKey redisKey, String value) {
        doRedis(redisKey.database(), jedis -> jedis.lpush(redisKey.get(), value));
    }

    /**
     * list类型---按照起始索引获取值
     * @param redisKey redisKey
     * @param start    起始索引
     * @param end      结束索引
     * @return 获取的值
     */
    public static List<String> getList(RedisKey redisKey, long start, long end) {
        return doRedis(redisKey.database(), jedis -> jedis.lrange(redisKey.get(), start, end));
    }

    /**
     * list类型 -- 获取对象数据，第一行存储的是对象类的全路径的情况
     * @param redisKey redisKey
     * @param start    起始条数
     * @param end      结束条数
     * @param <T>      返回数据类型
     * @return 返回数据列表
     */
    public static <T> List<T> getListObj(RedisKey redisKey, long start, long end) {
        return doRedis(redisKey.database(), jedis -> {
            List<T> list = new ArrayList<>();
            String key = redisKey.get();
            if (Boolean.FALSE.equals(jedis.exists(key.getBytes()))) {
                return list;
            }
            String clsStr = jedis.lrange(key, 0, 0).get(0);
            //获取redis中第一个类的全路径名称，利用反射生成对应的类对象
            Class<T> cls;
            try {
                cls = (Class<T>) Class.forName(clsStr);
            } catch (ClassNotFoundException e) {
                return list;
            }
            // 注意lrange（以及类似）命令：start、end都是包含的（左闭右闭），正数正着数，负数倒着数，允许越界（自动取边界值），详见Redis官方文档
            List<String> jsonStrList = jedis.lrange(key, start, end);
            if (CollectionUtils.isEmpty(jsonStrList)) {
                return list;
            }
            for (String s : jsonStrList) {
                try {
                    // json串转为实体
                    list.add(JSON.parseObject(s, cls));
                } catch (JSONException ignore) {
                    // 如果转换失败，则说明类型不匹配，跳过该对象
                }
            }

            return list;
        });
    }

    /**
     * list类型---获取全部值
     * @param redisKey redisKey
     * @return 获取的值
     */
    public static List<String> getList(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> {
            if (!jedis.exists(redisKey.get())) {
                return new ArrayList<>();
            } else {
                return jedis.lrange(redisKey.get(), 0, -1);
            }
        });
    }

    /**
     * list类型---移除并返回列表的第一个元素。
     * @param redisKey redisKey
     * @return 获取的值
     */
    public static String listLpop(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> {
            if (!jedis.exists(redisKey.get())) {
                return null;
            } else {
                return jedis.lpop(redisKey.get());
            }
        });
    }

    /**
     * list类型---添加数据列表 存放的第一条是数据对应类型的类名，后面未具体数据
     * @param redisKey     redisKey
     * @param values       数据
     * @param expireSecond 过期时间，单位s 为空不设置过期时间
     * @param <T>          插入的数据类型
     */
    public static <T> void addObjectToList(RedisKey redisKey, List<T> values, Integer expireSecond) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        doRedis(redisKey.database(), jedis -> {
            //先数据列表的顺序插入数据
            String[] valueArr = values.stream().map(JSON::toJSONString).toArray(String[]::new);
            jedis.rpush(redisKey.get(), valueArr);
            //在数据头部加入类名
            String className = values.get(0).getClass().getName();
            jedis.lpush(redisKey.get(), className);

            //设置缓存过期时间
            if (Objects.nonNull(expireSecond)) {
                jedis.expire(redisKey.get(), expireSecond);
            }
            return true;
        });
    }

    public static <T> List<T> getList(RedisKey redisKey, Class<T> clazz) {
        List<String> list = getList(redisKey);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(o -> JSONObject.parseObject(o, clazz)).collect(Collectors.toList());
    }

    public static <T> List<T> getList(RedisKey redisKey, long start, long end, Class<T> clazz) {
        List<String> list = getList(redisKey, start, end);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(o -> JSONObject.parseObject(o, clazz)).collect(Collectors.toList());
    }

    public static <T> Page<T> getPageList(RedisKey key, BaseQueryBean query, Class<T> clazz) {
        // 当前页数
        int curPageNum = query.getPage().intValue();
        // 每页条数
        long pageSize = query.getLimit();
        Long listSize = getListLen(key);
        listSize = listSize == null ? 0 : listSize;
        // 总页数
        long totalPages = (listSize - 1) / pageSize + 1;
        // 若当前页大于总页数，当前页设置为1
        if (curPageNum > totalPages) {
            curPageNum = 1;
        }
        // 开始位置
        long start = (curPageNum - 1) * pageSize;
        // 结束位置（判断是否最后一页）
        long end = curPageNum * pageSize > listSize ? (listSize - 1) : (curPageNum * pageSize - 1);
        List<T> pageList = getList(key, start, end, clazz);
        Page<T> result = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
        if (CollectionUtils.isNotEmpty(pageList)) {
            result.addAll(pageList);
            result.setTotal(listSize);
        } else {
            result.setTotal(0);
        }
        return result;
    }

    /**
     * list类型---获取长度
     * @param redisKey redisKey
     * @return 获取的值
     */
    public static Long getListLen(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.llen(redisKey.get()));
    }

    /**
     * set类型---获取长度
     */
    public static Long getSetLen(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.scard(redisKey.get()));
    }

    /**
     * list类型---删除list类型缓存中的元素
     * @param redisKey redisKey
     * @param value    value
     */
    public static void delListItem(RedisKey redisKey, String value) {
        doRedis(redisKey.database(), jedis -> jedis.lrem(redisKey.get(), 0, value));
    }

    /**
     * list类型---批量删除list类型缓存中的元素
     * @param redisKey redisKey
     * @param values   values
     */
    public static void delListItem(RedisKey redisKey, Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        pipelineDo(redisKey.database(), values, (value, pipeline) -> pipeline.lrem(redisKey.get(), 0, value));
    }

    /**
     * set类型 -- 单个添加
     * @param redisKey redisKey
     * @param value    value
     */
    public static void addToSet(RedisKey redisKey, String value) {
        doRedis(redisKey.database(), jedis -> jedis.sadd(redisKey.get(), value));
    }

    /**
     * set类型--批量添加
     * @param redisKey redisKey
     * @param values   values
     * @return count of added
     */
    public static long addToSet(RedisKey redisKey, Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return 0L;
        }
        final String[] valueArr = values.toArray(new String[0]);
        return doRedis(redisKey.database(), jedis -> jedis.sadd(redisKey.get(), valueArr));
    }

    /**
     * list类型--批量添加
     * @param redisKey redisKey
     * @param values   values
     */
    public static <T> void addToList(RedisKey redisKey, Collection<T> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        T firstData = CosUtil.getFirstData(values);
        String[] list;
        if (firstData.getClass().equals(String.class)) {
            list = values.toArray(new String[0]);
        } else {
            list = values.stream().map(JSONObject::toJSONString).toArray(String[]::new);
        }

        doRedis(redisKey.database(), jedis -> jedis.rpush(redisKey.get(), list));
    }

    /**
     * list类型--批量进行设置
     * 必须为同一数据库
     * @param keyValuesMap key和values的映射关系
     */
    public static void batchAddToList(Map<RedisKey, Collection<String>> keyValuesMap) {
        if (keyValuesMap == null || keyValuesMap.isEmpty()) {
            return;
        }

        int database = keyValuesMap.keySet().iterator().next().database();
        pipelineDo(database, keyValuesMap,
            (key, values, pipeline) -> pipeline.rpush(key.get(), values.toArray(new String[0])));
    }

    /**
     * set类型--批量进行设置
     * 必须为同一数据库
     * @param keyValuesMap key和values的映射关系
     */
    public static void batchAddToSet(Map<RedisKey, Collection<String>> keyValuesMap) {
        if (keyValuesMap == null || keyValuesMap.isEmpty()) {
            return;
        }

        int database = keyValuesMap.keySet().iterator().next().database();
        pipelineDo(database, keyValuesMap,
            (key, values, pipeline) -> pipeline.sadd(key.get(), values.toArray(new String[0])));
    }

    /**
     * set类型--批量进行删除
     * 必须为同一数据库
     * @param keyValuesMap key和values的映射关系
     */
    public static void batchDelSet(Map<RedisKey, Collection<String>> keyValuesMap) {
        if (keyValuesMap == null || keyValuesMap.isEmpty()) {
            return;
        }
        int database = keyValuesMap.keySet().iterator().next().database();
        pipelineDo(database, keyValuesMap,
            (key, values, pipeline) -> pipeline.srem(key.get(), values.toArray(new String[0])));
    }

    /**
     * set类型--批量进行设置
     * 必须为同一数据库
     * @param keyValuesMap key和values的映射关系
     */
    public static void batchDeleteSet(Map<RedisKey, Collection<String>> keyValuesMap) {
        if (keyValuesMap == null || keyValuesMap.isEmpty()) {
            return;
        }
        int database = keyValuesMap.keySet().iterator().next().database();
        pipelineDo(database, keyValuesMap,
            (key, values, pipeline) -> pipeline.srem(key.get(), values.toArray(new String[0])));
    }

    /**
     * 批量删除,集合中的key只能是同一个库的
     */
    public static void batchDelete(Collection<RedisKey> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        int database = collection.iterator().next().database();
        final String[] keys = collection.stream().map(o -> o.get()).toArray(String[]::new);
        doRedis(database, jedis -> jedis.del(keys));
    }

    /**
     * set类型--获取集合的所有元素
     * @param redisKey redisKey
     * @return 所有的集合元素
     */
    public static Set<String> getSet(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.smembers(redisKey.get()));
    }

    /**
     * 过滤member
     * @param redisKey redisKey
     * @return 所有的集合元素
     */
    public static Set<String> filterSetMembers(RedisKey redisKey, Set<String> members) {
        final int singleQueryLimit = 5;
        final String key = redisKey.get();
        if (members.size() < singleQueryLimit) {
            return doRedis(redisKey.database(), jedis -> {
                final Pipeline pipeline = jedis.pipelined();
                final Map<String, Response<Boolean>> responses = new HashMap<>(16);
                for (String member : members) {
                    responses.put(member, pipeline.sismember(key, member));
                }
                pipeline.sync();
                return responses.entrySet().stream()
                        .filter(o -> o.getValue().get())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
            });
        } else {
            members.retainAll(doRedis(redisKey.database(), jedis -> jedis.smembers(key)));
            return members;
        }
    }

    /**
     * set类型--批量获取集合的所有元素
     * 必须为同一数据库
     * @param redisKeys redisKeys
     * @return 所有的集合元素
     */
    public static Set<String> batchGetSet(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashSet<>();
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, Set<String>> result =
            pipelineDo(database, redisKeys, (redisKey, pipeline) -> pipeline.smembers(redisKey.get()));
        return result.values().stream().filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    /**
     * set类型 - 批量获取，并返回Map, key为uuid
     * 必须为同一数据库
     * @param redisKeys redisKeys
     * @return key为uuid-set
     */
    public static Map<String, Set<String>> batchGetSetReturnMap(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashMap<>(1);
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, Set<String>> result =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.smembers(key.get()));
        return result.entrySet().stream().collect(Collectors.toMap(e -> {
            final String key = e.getKey().get();
            return key.substring(key.lastIndexOf(":") + 1);
        }, Map.Entry::getValue, (o, p) -> o));
    }

    /**
     * set类型--删除set类型的redis中的元素
     * @param redisKey redisKey
     * @param value    value
     */
    public static void delSetItem(RedisKey redisKey, String value) {
        doRedis(redisKey.database(), jedis -> jedis.srem(redisKey.get(), value));
    }

    /**
     * set类型--返回集合中的一个随机元素
     * @param redisKey redisKey
     */
    public static String randomGetSetMember(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.srandmember(redisKey.get()));
    }

    /**
     * set类型--删除set类型的redis中的元素
     * @param redisKey redisKey
     * @param values   values
     * @return count of deleted
     */
    public static long delSetItem(RedisKey redisKey, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        final String[] valueArr = values.toArray(new String[0]);
        return doRedis(redisKey.database(), jedis -> jedis.srem(redisKey.get(), valueArr));
    }

    /**
     * hash类型--单个key的单个键值对的添加
     * @param redisKey redisKey
     * @param field    字段名
     * @param value    值
     */
    public static void addToHash(RedisKey redisKey, String field, String value) {
        if (value == null) {
            return;
        }
        doRedis(redisKey.database(), jedis -> jedis.hset(redisKey.get(), field, value));
    }

    /**
     * hash类型--单个key的批量键值对添加
     * @param redisKey      redisKey
     * @param fieldValueMap 键值对
     */
    public static void addToHash(RedisKey redisKey, Map<String, String> fieldValueMap) {
        if (fieldValueMap.isEmpty()) {
            return;
        }
        final Map<String, String> valueMap = fieldValueMap.entrySet().stream()
                .filter(e -> null != e.getValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, p) -> o,
                    () -> new HashMap<>(CommonUtil.ofMapCapacity(fieldValueMap.size()))));
        doRedis(redisKey.database(), jedis -> jedis.hmset(redisKey.get(), valueMap));
    }

    /**
     * hash类型--单个key的批量键值对添加
     * @param redisKey redisKey
     * @param map      键值对
     */
    public static <T> void addMapToHash(RedisKey redisKey, Map<String, T> map) {
        if (map.isEmpty()) {
            return;
        }
        pipelineDo(redisKey.database(), map, (fieldKey, fieldValue, pipeline) -> {
            String value;
            if (fieldValue.getClass().equals(String.class)) {
                value = fieldValue.toString();
            } else {
                value = JSON.toJSONString(fieldValue);
            }
            return pipeline.hset(redisKey.get(), fieldKey, value);
        });
    }

    /**
     * hash类型--批量key进行hash添加操作
     * 必须为同一数据库
     * @param keyValueMap key-键值对的映射关系
     */
    public static void batchAddToHash(Map<RedisKey, Map<String, String>> keyValueMap) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        int database = keyValueMap.keySet().iterator().next().database();
        pipelineDo(database, keyValueMap, (key, map, pipeline) -> pipeline.hmset(key.get(), map));
    }

    /**
     * hash类型-- 批量更新 适用于某一批缓存，需要修改的键值对相同
     * 必须为同一数据库
     * @param redisKeys     待更新的redis的key值
     * @param fieldValueMap 更新的键值对
     */
    public static void batchAddToHash(Collection<RedisKey> redisKeys, Map<String, String> fieldValueMap) {
        if (CollectionUtils.isEmpty(redisKeys) || fieldValueMap == null || fieldValueMap.isEmpty()) {
            return;
        }
        int database = redisKeys.iterator().next().database();
        pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.hmset(key.get(), fieldValueMap));
    }

    /**
     * hash类型--获取某个key的字段值
     * @param redisKey redisKey
     * @param field    field
     * @return value
     */
    public static String hget(RedisKey redisKey, String field) {
        return doRedis(redisKey.database(), jedis -> jedis.hget(redisKey.get(), field));
    }

    /**
     * hash类型--获取单个key所有的键值对
     * @param redisKey redisKey
     * @return 键值对
     */
    public static Map<String, String> hgetAll(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.hgetAll(redisKey.get()));
    }

    /**
     * hash类型--批量获取key所有的键值对 适用于field字段为ID的
     * 必须为同一数据库
     * @param redisKeys redisKeys
     * @return 键值对
     */
    public static Map<String, String> hgetAll(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashMap<>(16);
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, Map<String, String>> responses =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.hgetAll(key.get()));

        final Map<String, String> result = new HashMap<>(16);
        for (Map<String, String> response : responses.values()) {
            result.putAll(response);
        }
        return result;
    }

    /**
     * hash类型--获取单个key指定字的段值
     *
     * @param redisKey redisKey
     * @param field   fields
     * @return 值
     */
    public static Long hincrBy(RedisKey redisKey, String field, long incr) {
        return doRedis(redisKey.database(), jedis -> jedis.hincrBy(redisKey.get(), field, incr));
    }

    /**
     * hash类型--获取单个key指定字的段值
     * @param redisKey redisKey
     * @param fields   fields
     * @return 值
     */
    public static List<String> hmget(RedisKey redisKey, Collection<String> fields) {
        final String[] fieldArr = fields.toArray(new String[0]);
        return doRedis(redisKey.database(), jedis -> jedis.hmget(redisKey.get(), fieldArr));
    }

    /**
     * hash类型--获取filed字段集合
     * @param redisKey redisKey
     * @return keys
     * @deprecated 用hscan代替
     */
    public static Set<String> hkeys(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.hkeys(redisKey.get()));
    }

    /**
     * hash类型--获取filed字段集合
     * @param redisKeys redisKeys
     * @return keys
     * @deprecated 用hscan代替
     */
    public static Set<String> hkeys(Collection<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashSet<>();
        }
        int database = redisKeys.iterator().next().database();
        final Map<RedisKey, Set<String>> responses =
            pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.hkeys(key.get()));

        Set<String> result = new HashSet<>();
        for (Set<String> response : responses.values()) {
            result.addAll(response);
        }
        return result;
    }

    /**
     * hash类型--获取单个key指定字段的键值对
     * 过滤掉非空的值
     * @param redisKey redisKey
     * @param fields   fields
     * @return 键值对 key:对应的field value：field对应值
     */
    public static Map<String, String> getHashMapReturnNonNull(RedisKey redisKey, Collection<String> fields) {
        Map<String, String> valueMap = getHashMap(redisKey, fields);
        Map<String, String> result = new HashMap<>(CommonUtil.ofMapCapacity(valueMap.size()));
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * hash类型--获取单个key指定字段的键值对
     * @param redisKey redisKey
     * @param fields   fields
     * @return 键值对 key:对应的field value：field对应值
     */
    public static Map<String, String> getHashMap(RedisKey redisKey, Collection<String> fields) {
        return getHashMap(redisKey, fields.toArray(new String[0]));
    }

    /**
     * hash类型--获取单个key指定字段的键值对
     * @param redisKey redisKey
     * @param fields   fields
     * @return 键值对 key:对应的field value：field对应值
     */
    public static Map<String, String> getHashMap(RedisKey redisKey, String... fields) {
        int database = redisKey.database();
        if (fields == null || fields.length == 0) {
            return doRedis(database, jedis -> jedis.hgetAll(redisKey.get()));
        }
        final List<String> response = doRedis(database, jedis -> jedis.hmget(redisKey.get(), fields));

        Map<String, String> result = new HashMap<>((int) (fields.length / .75 + 1));
        for (int i = 0; i < response.size(); i++) {
            result.put(fields[i], response.get(i));
        }
        return result;
    }

    /**
     * hash类型 -- 批量获取key的指定字段的键值对
     * 会过滤掉null元素
     * @param redisKeys redisKeys
     * @param fields    fields
     * @return 键值对集合 Map<String, String> 是单个redisKey的值，key:对应的field value：field对应的值
     */
    public static List<Map<String, String>> batchGetHashMap(List<RedisKey> redisKeys, List<String> fields) {
        if (CollectionUtils.isEmpty(redisKeys) || CollectionUtils.isEmpty(fields)) {
            return Collections.emptyList();
        }
        int database = redisKeys.get(0).database();
        //使用管道进行批量获取
        final String[] fieldArr = fields.toArray(new String[0]);
        final Map<RedisKey, List<String>> responses =
            pipelineDo(database, redisKeys, (redisKey, pipeline) -> pipeline.hmget(redisKey.get(), fieldArr));

        final int kvMapSize = (int) (fieldArr.length / .75 + 1);
        return responses.values().stream().map(fieldValues -> {
            final Map<String, String> kvMap = new LinkedHashMap<>(kvMapSize);
            for (int i = 0; i < fieldValues.size(); i++) {
                kvMap.put(fieldArr[i], fieldValues.get(i));
            }
            return kvMap;
        }).collect(Collectors.toCollection(() -> new ArrayList<>(responses.size())));
    }

    /**
     * hash类型--批量获取hash类型key的全部键值对 适用于field字段为某个对象的属性字段的
     * <p>会过滤掉null元素
     *
     * @param redisKeys redisKeys
     * @return 键值对集合 键值对集合 Map<String, String> 是单个redisKey的值，key:对应的field value：field对应的值
     * @apiNote <b>key过多时会临时占用较多内存，且造成redis线程阻塞，请谨慎使用，其他批量key查询的同理<b/>
     */
    public static List<Map<String, String>> batchGetHashMap(List<RedisKey> redisKeys) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return Collections.emptyList();
        }
        int database = redisKeys.get(0).database();
        final Map<RedisKey, Map<String, String>> responses =
            pipelineDo(database, redisKeys, (redisKey, pipeline) -> pipeline.hgetAll(redisKey.get()));
        return new ArrayList<>(responses.values());
    }

    /**
     * hash类型--批量获取某种缓存中某个字段的值
     * 会过滤掉null元素
     * @param redisKeys redisKeys
     * @param field     field
     * @return 获取的集合
     */
    public static List<String> batchGetHashMap(List<RedisKey> redisKeys, String field) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new ArrayList<>();
        }
        int database = redisKeys.get(0).database();
        final Map<RedisKey, String> responses =
            pipelineDo(database, redisKeys, (redisKey, pipeline) -> pipeline.hget(redisKey.get(), field));
        return new ArrayList<>(responses.values());
    }

    /**
     * hash类型 - -批量获取某种缓存中某两个属性组成的Map
     * 会过滤掉null元素
     * @param redisKeys  redisKeys
     * @param keyField   map的key值对应的字段名称，一般是对象的主键
     * @param valueField map的value值对用的字段名称
     * @return keyField-valueField 对应的值组成的map
     */
    public static Map<String, String> batchGetHashMap(List<RedisKey> redisKeys, String keyField, String valueField) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashMap<>(16);
        }
        List<String> fields = Arrays.asList(keyField, valueField);
        List<Map<String, String>> resultList = batchGetHashMap(redisKeys, fields);
        Map<String, String> resultMap = new HashMap<>(CommonUtil.ofMapCapacity(resultList.size()));
        for (Map<String, String> entry : resultList) {
            String key = entry.get(keyField);
            String value = entry.get(valueField);
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                continue;
            }
            resultMap.put(key, value);
        }
        return resultMap;
    }

    /**
     * hash类型 - -批量获取某种缓存中多个属性组成的Map
     * 会过滤掉null元素
     * @param redisKeys   redisKeys
     * @param keyField    map的key值对应的字段名称，一般是对象的主键
     * @param valueFields map的value值对用的多个字段名称
     * @return keyField-valueField 对应的值组成的map
     */
    public static Map<String, Map<String, String>> batchGetHashMap(List<RedisKey> redisKeys, String keyField,
        String... valueFields) {
        if (CollectionUtils.isEmpty(redisKeys)) {
            return new HashMap<>();
        }
        List<String> fields = Lists.newLinkedList();
        fields.add(keyField);
        fields.addAll(Arrays.asList(valueFields));
        List<Map<String, String>> resultList = batchGetHashMap(redisKeys, fields);
        Map<String, Map<String, String>> resultMap = Maps.newHashMapWithExpectedSize(resultList.size());
        for (Map<String, String> entry : resultList) {
            String key = entry.get(keyField);

            if (StringUtils.isBlank(key)) {
                continue;
            }
            resultMap.put(key, entry);
        }
        return resultMap;
    }

    /**
     * hash类型--删除单个key的某个键值对
     * @param redisKey redisKey
     * @param field    field
     */
    public static void hdel(RedisKey redisKey, String field) {
        doRedis(redisKey.database(), jedis -> jedis.hdel(redisKey.get(), field));
    }

    /**
     * 获取所有
     * @param redisKey
     */
    public static List<String> hvals(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.hvals(redisKey.get()));
    }

    /**
     * hash类型--删除单个key的批量键值对
     * @param redisKey redisKey
     * @param fields   fields
     */
    public static void hdel(RedisKey redisKey, Collection<String> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        final String[] fieldArr = fields.toArray(new String[0]);
        doRedis(redisKey.database(), jedis -> jedis.hdel(redisKey.get(), fieldArr));
    }

    /**
     * hash类型--删除
     * @param redisKeyFieldsMap redisKeyFieldsMap
     */
    public static void hdel(Map<RedisKey, Collection<String>> redisKeyFieldsMap) {
        if (redisKeyFieldsMap == null || redisKeyFieldsMap.isEmpty()) {
            return;
        }
        int database = redisKeyFieldsMap.keySet().iterator().next().database();
        pipelineDo(database, redisKeyFieldsMap,
            (redisKey, fieldKeys, pipeline) -> pipeline.hdel(redisKey.get(), fieldKeys.toArray(new String[0])));
    }

    /**
     * hash类型--批量删除 适合同一批缓存删除相同的filed字段
     * @param redisKeys redis缓存key
     * @param fields    待删除的缓存字段
     */
    public static void hdel(Collection<RedisKey> redisKeys, Collection<String> fields) {
        if (CollectionUtils.isEmpty(redisKeys) || CollectionUtils.isEmpty(fields)) {
            return;
        }

        final String[] fieldArr = fields.toArray(new String[0]);
        int database = redisKeys.iterator().next().database();
        pipelineDo(database, redisKeys, (key, pipeline) -> pipeline.hdel(key.get(), fieldArr));
    }

    /**
     * hash类型--根据filed的匹配获取简直对
     * @param redisKey redisKey
     * @param pattern  filed
     * @return 键值对
     */
    public static List<Map.Entry<String, String>> hscan(RedisKey redisKey, String pattern) {
        return doRedis(redisKey.database(), jedis -> {
            ScanParams scanParams = new ScanParams().match(pattern).count(SCAN_COUNT);
            String cursor = ScanParams.SCAN_POINTER_START;
            List<Map.Entry<String, String>> result = new ArrayList<>();
            //遍历次数，当返回游标为0时，遍历结束
            int iterationNum = 0;
            while (!Objects.equals(ScanParams.SCAN_POINTER_START, cursor) || iterationNum == 0) {
                //SCAN 命令不能保证每次返回的值都是有序的，另外同一个 key 有可能返回多次
                ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(redisKey.get(), cursor, scanParams);
                if (CollectionUtils.isNotEmpty(scanResult.getResult())) {
                    result.addAll(scanResult.getResult());
                }
                cursor = scanResult.getCursor();
                iterationNum++;
            }
            return result;
        });
    }

    /**
     * 批量模糊删除
     */
    public static void delBatchByPatternSets(Set<RedisKey> patternSet) {
        if (CollectionUtils.isEmpty(patternSet)) {
            return;
        }
        final int database = patternSet.iterator().next().database();
        final Set<String> keys = getKeysByPatterns(patternSet);
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        doDelete(database, new ArrayList<>(keys));
    }

    /**
     * 根据模式批量匹配Key，返回Key的集合
     * @deprecated 使用scan代替
     */
    public static Set<String> getKeysByPatterns(Set<RedisKey> patternSet) {

        if (CollectionUtils.isEmpty(patternSet)) {
            return new HashSet<>(1);
        }
        final int database = patternSet.iterator().next().database();
        final Map<RedisKey, Set<String>> responses =
            pipelineDo(database, patternSet, (pattern, pipeline) -> pipeline.keys(pattern.get()));
        return responses.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * 查询value不包含主键key的场景，但又需要返回一个map关系的场景
     * @param keys
     * @param mapKeyFunction 组装key的操作
     * @return
     */
    public static Map<String, Set<String>> getMapSet(Collection<RedisKey> keys,
        Function<RedisKey, String> mapKeyFunction) {
        Map<String, Set<String>> result = new HashMap<>();
        RedisKey firstData = CosUtil.getFirstData(keys);
        if (null == firstData) {
            return Collections.emptyMap();
        }
        Map<RedisKey, Set<String>> redisKeyListMap =
            pipelineDo(firstData.database(), keys, (key, pipeline) -> pipeline.smembers(key.get()));

        if (mapKeyFunction == null) {
            //为空默认取最后冒号之后的值为mapkey
            int lastIndex = firstData.get().lastIndexOf(":");
            mapKeyFunction = e -> e.get().substring(lastIndex + 1);
        }
        for (Map.Entry<RedisKey, Set<String>> entry : redisKeyListMap.entrySet()) {
            result.put(mapKeyFunction.apply(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public static <T> Map<String, T> getMapHashData(Collection<RedisKey> keys,
        Function<RedisKey, String> mapKeyFunction, Class<T> cls) {
        Map<String, T> result = new HashMap<>();
        RedisKey firstData = CosUtil.getFirstData(keys);
        if (null == firstData) {
            return Collections.emptyMap();
        }
        Map<RedisKey, Map<String, String>> redisKeyListMap =
            pipelineDo(firstData.database(), keys, (key, pipeline) -> pipeline.hgetAll(key.get()));

        if (mapKeyFunction == null) {
            //为空默认取最后冒号之后的值为mapkey
            int lastIndex = firstData.get().lastIndexOf(":");
            mapKeyFunction = e -> e.get().substring(lastIndex);
        }
        for (Map.Entry<RedisKey, Map<String, String>> entry : redisKeyListMap.entrySet()) {
            Map<String, String> value = entry.getValue();
            if (MapUtils.isEmpty(value)) {
                continue;
            }
            result.put(mapKeyFunction.apply(entry.getKey()), mapToCLass(value, cls));
        }
        return result;
    }

    private static <T> T mapToCLass(Map<String, String> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }

    /**
     * 查询key的过期时间
     * @return
     */
    public static Long ttl(RedisKey key) {
        if (Objects.isNull(key)) {
            return -2L;
        }
        return doRedis(key.database(), jedis -> jedis.ttl(key.get()));
    }

    public static long incr(RedisKey key) {
        return Optional.ofNullable(doRedis(key.database(), jedis -> jedis.incr(key.get()))).orElse(1L);
    }

    /**
     * spop命令（随机删除并返回len个元素）
     */
    public static Set<String> setPop(RedisKey redisKey, int len) {
        return doRedis(redisKey.database(), jedis -> jedis.spop(redisKey.get(), len));
    }

    /**
     * spop命令（随机删除并返回1个元素）
     */
    public static String setPop(RedisKey redisKey) {
        return doRedis(redisKey.database(), jedis -> jedis.spop(redisKey.get()));
    }

    public interface TriFunction<A, B, C, R> {
        /**
         * @see Function#apply(Object)
         */
        R apply(A a, B b, C c);
    }
}
