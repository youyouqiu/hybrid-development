package com.zw.platform.util.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.basic.core.RedisHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * redis分页存储和查询 @author  Tdz
 * @since 2017-02-13 11:25
 * @deprecated 建议不要再使用
 **/
public class RedisUtil {


    public static <T> Page<T> queryPageList(RedisKey key, Function<String, RedisKey> keyFunc, BaseQueryBean query) {
        List<T> returnList;
        // 当前页数
        int curPage = query.getPage().intValue();
        // 每页条数
        long pageSize = query.getLimit();
        Long listSize = RedisHelper.getListLen(key);
        listSize = listSize == null ? 0 : listSize;
        // 总页数
        long pages = (listSize - 1) / pageSize + 1;
        // 若当前页大于总页数，当前页设置为1
        if (curPage > pages) {
            curPage = 1;
        }
        // 开始位置
        long start = (curPage - 1) * pageSize;
        // 结束位置（判断是否最后一页）
        long end = curPage * pageSize > listSize ? (listSize - 1) : (curPage * pageSize - 1);
        try {
            final List<String> ids = RedisHelper.getList(key, start, end);
            final List<RedisKey> keys = ids.stream().map(keyFunc).collect(Collectors.toList());
            final List<Map<String, String>> list = new ArrayList<>(keys.size());
            for (List<RedisKey> redisKeys : Lists.partition(keys, 1000)) {
                list.addAll(RedisHelper.batchGetHashMap(redisKeys));
            }
            returnList = new ArrayList<>(list.size());
            final TypeReference<T> type = new TypeReference<T>() {
            };
            for (Map<String, String> map : list) {
                T obj = JSON.parseObject(JSON.toJSONString(map), type);
                returnList.add(obj);
            }
        } catch (Exception e) {
            return new Page<>();
        }
        return RedisQueryUtil.getListToPage(returnList, query, listSize.intValue());
    }

    public static <T> Page<T> queryPageList(List<T> returnList, BaseQueryBean query, RedisKey redisKey) {
        Long listSize = RedisHelper.getListLen(redisKey);
        listSize = listSize == null ? 0 : listSize - 1;
        return RedisQueryUtil.getListToPage(returnList, query, listSize.intValue());
    }

    /**
     * 将数据转换成字符串
     * @deprecated
     */
    public static Map<String, String> getEncapsulationObject(Map<String, Object> object) {
        Map<String, String> billMap = new HashMap<>(object.size());
        for (String key : object.keySet()) {
            billMap.put(key, convertString(object.get(key)));
        }
        return billMap;
    }

    /**
     *    转换对象   当对象是一个时间类型 转成时间戳   其他类型都为字符串   @param object   @return       
     */
    private static String convertString(Object object) {
        Object obj = object;
        if (obj == null) {
            return "";
        } else if (obj instanceof Date) {
            obj = ((Date) obj).getTime();
            return obj.toString();
        }
        if (isBlank(obj.toString())) {
            return "";
        }
        return obj.toString();
    }

    /**
     *    判断字符串是否为空   @param str   @return       
     */
    public static boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 存储要到处的数据到缓存中
     */
    public static void storeExportDataToRedis(String tableName, List<?> exportData) {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisHelper.addObjectToList(HistoryRedisKeyEnum.TMP_EXPORT_DATA.of(userId, tableName), exportData,
                RedisHelper.SIX_HOUR_REDIS_EXPIRE);
    }

    /**
     * 从缓存中取出要要导入的数据，并删除redis缓存
     */
    public static <T> List<T> getExportDataFromRedis(String tableName) {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<T> exportData = RedisHelper.getListObj(HistoryRedisKeyEnum.TMP_EXPORT_DATA.of(userId, tableName), 1, -1);
        RedisHelper.delete(HistoryRedisKeyEnum.TMP_EXPORT_DATA.of(userId, tableName));
        return exportData;
    }
}
