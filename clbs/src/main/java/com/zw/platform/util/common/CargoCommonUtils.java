package com.zw.platform.util.common;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/9/2 15:23
 @Description 货运车辆通用工具类
 @version 1.0
 **/
public class CargoCommonUtils {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(CargoCommonUtils.class);

    private static final String GROUP_CARGO_KEY = "group_cargo_vehicle_";

    /**
     * 根据企业id获取企业下货运车辆id
     * @param groupIds
     * @return
     */
    public static Set<String> getGroupCargoVids(String... groupIds) {
        List<String> orgIdList = Arrays.asList(groupIds);
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.ofs(orgIdList);
        return RedisHelper.batchGetSet(redisKeys);
    }

    /**
     * 批量新增企业货运车辆到缓存中
     * @param orgIdAndVidsMap
     */
    public static void batchSetGroupCargoVids(Map<String, Set<String>> orgIdAndVidsMap) {
        Map<RedisKey, Collection<String>> setKeyValueMap = new HashMap<>(16);
        orgIdAndVidsMap.forEach((orgId, vidSet) ->
                setKeyValueMap.put(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId), vidSet));
        RedisHelper.batchAddToSet(setKeyValueMap);
    }

    /**
     * 批量新增企业货运车辆到缓存中
     * @param orgId 企业UUID
     * @param vids vids
     */
    public static void setGroupCargoVids(String orgId, Set<String> vids) {
        RedisKey redisKey = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId);
        RedisHelper.addToSet(redisKey, vids);
    }

    /**
     * 新增企业货运车辆到缓存中
     * @param orgId 企业UUID
     * @param vid vid
     */
    public static void setGroupCargoVid(String orgId, String vid) {
        RedisKey redisKey = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId);
        RedisHelper.addToSet(redisKey, vid);
    }

    /**
     * 批量删除企业货运车辆到缓存中
     * @param orgId 企业UUID
     * @param vids 车辆ID集合
     */
    public static void remGroupCargoVids(String orgId, Set<String> vids) {
        RedisKey redisKey = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId);
        RedisHelper.delSetItem(redisKey, vids);
    }

    /**
     * 删除企业货运车辆到缓存中
     * @param orgId 企业UUID
     * @param vid vid
     */
    public static void removeGroupCargoVid(String orgId, String vid) {
        RedisKey redisKey = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId);
        RedisHelper.delSetItem(redisKey, vid);
    }

    /**
     * 批量删除企业货运车辆到缓存中
     * @param orgIdAndVidsMap
     */
    public static void batchRemGroupCargoVids(Map<String, Set<String>> orgIdAndVidsMap) {
        Map<RedisKey, Collection<String>> setKeyValueMap = new HashMap<>(16);
        orgIdAndVidsMap.forEach((orgId, vidSet) ->
                setKeyValueMap.put(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(orgId), vidSet));
        RedisHelper.batchDeleteSet(setKeyValueMap);
    }

    /**
     * 根据企业id获取企业下的普货车id
     */
    public static Map<String, Set<String>> getCargoVehicleIdByGroupId(String... groupIds) {
        List<String> groupIdLists = Arrays.asList(groupIds);
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.ofs(groupIdLists);
        return RedisHelper.batchGetSetReturnMap(redisKeys);
        // Map<String, List<String>> groupCargoVehicleId = new HashMap<>();
        // if (groupIds == null || groupIds.length == 0) {
        //     return groupCargoVehicleId;
        // }
        // List<String> groupIdLists = Arrays.asList(groupIds);
        // Jedis jedisTen = null;
        // try {
        //     jedisTen = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
        //     // 获取到1分区所有
        //     Set<String> groupCargoVehicleKey = jedisTen.keys("group_cargo_vehicle_*");
        //     if (CollectionUtils.isEmpty(groupCargoVehicleKey)) {
        //         return groupCargoVehicleId;
        //     }
        //     Map<String, Response<Set<String>>> resultData = new HashMap<>();
        //     Pipeline pipelined = jedisTen.pipelined();
        //     for (String redisKey : groupCargoVehicleKey) {
        //         String groupUuId = redisKey.substring(redisKey.lastIndexOf("_") + 1);
        //         if (groupIdLists.contains(groupUuId)) {
        //             resultData.put(groupUuId, pipelined.sunion(redisKey));
        //         }
        //     }
        //     pipelined.sync();
        //     for (Map.Entry<String, Response<Set<String>>> entry : resultData.entrySet()) {
        //         String groupId = entry.getKey();
        //         Set<String> redisKey = entry.getValue().get();
        //         if (StringUtils.isBlank(groupId) || CollectionUtils.isEmpty(redisKey)) {
        //             continue;
        //         }
        //         groupCargoVehicleId.put(groupId, new ArrayList<>(redisKey));
        //     }
        //     return groupCargoVehicleId;
        // } catch (Exception e) {
        //     log.error("从redis获取企业下的货运车辆id异常", e);
        //     return null;
        // } finally {
        //     RedisHelper.returnResource(jedisTen);
        // }
    }
}
