package com.zw.platform.util;

import com.google.common.collect.ImmutableSet;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.reportManagement.VehAlarmCacheDTO;
import com.zw.platform.domain.reportManagement.VehDealMsgCacheDTO;
import com.zw.platform.domain.reportManagement.VehStateContainerDTO;
import com.zw.platform.util.common.Date8Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description: 车辆状态报警信息归类新
 * @Date: create in 2020/11/17 14:16
 */
public class VehStateAlarmUtil {

    /**
     * 设备异常报警集合
     */
    private static final Set<String> EQUIPMENT_FAILURE =
        ImmutableSet.of("4", "5", "6", "7", "8", "9", "10", "11", "12");

    /**
     * 超速报警类型
     */
    private static final Set<String> OVER_SPEED = ImmutableSet.of("1", "164");

    /**
     * 疲劳报警类型
     */
    private static final Set<String> TIRED = ImmutableSet.of("2");

    /**
     * 不按规定行驶报警
     */
    private static final Set<String> LINE = ImmutableSet.of("23");

    /**
     * 凌晨2-5点行驶报警(异动报警)
     */
    private static final Set<String> DAWN = ImmutableSet.of("77", "7702", "7703");
    /**
     * 遮挡摄像头报警
     */
    private static final Set<String> CAMERA = ImmutableSet
        .of("12611", "12612", "12613", "12614", "12615", "12616", "12617", "12618", "12619", "12620", "12621", "12622",
            "12623", "12624", "12625", "12626", "12627", "12628", "12629", "12630", "12631", "12632", "12633", "12634",
            "12635", "12636", "12637", "12638", "12639", "12640", "12641", "12642");

    private static final Set<String> OTHER = ImmutableSet
        .of("0", "20", "2011", "2012", "21", "2111", "2112", "22", "2211", "2212", "18", "19", "24", "25", "26", "27",
            "28", "31");
    private static final Set<String> ALL = new HashSet<>();

    static {
        ALL.addAll(EQUIPMENT_FAILURE);
        ALL.addAll(OVER_SPEED);
        ALL.addAll(TIRED);
        ALL.addAll(LINE);
        ALL.addAll(DAWN);
        ALL.addAll(CAMERA);
        ALL.addAll(OTHER);

    }

    private static void addAlarmVehIds(long toDayStartSecond, Set<String> alarmVehIds,
        Map<String, VehAlarmCacheDTO> todayAlarmCache, String vid, Set<String> alarmTypes) {
        for (String alarmType : alarmTypes) {
            String key = HistoryRedisKeyEnum.TIME_MONITOR_ALARM.of(toDayStartSecond, vid, alarmType).get();
            if (todayAlarmCache.get(key) != null) {
                alarmVehIds.add(vid);
            }

        }
    }

    /**
     * 获取所有的报警
     */
    private static Map<String, VehAlarmCacheDTO> getTodayVehAlarmCache(Collection<String> monitorIds) {
        long toDayStartSecond = Date8Utils.getTodayStartSecond();
        List<RedisKey> keys = new ArrayList<>();
        for (String monitorId : monitorIds) {
            for (String alarmType : ALL) {
                keys.add(HistoryRedisKeyEnum.TIME_MONITOR_ALARM.of(toDayStartSecond, monitorId, alarmType));
            }
        }
        return RedisHelper.getMapHashData(keys, e -> e.get(), VehAlarmCacheDTO.class);

    }

    public static VehStateContainerDTO getVehStateContainer(Set<String> orgVidList) {
        VehStateContainerDTO container = new VehStateContainerDTO();
        long todayStartSecond = Date8Utils.getTodayStartSecond();

        Map<String, VehAlarmCacheDTO> todayAlarmCache = getTodayVehAlarmCache(orgVidList);

        for (String vid : orgVidList) {
            addAlarmVehIds(todayStartSecond, container.getEquipmentFailureVidSet(), todayAlarmCache, vid,
                EQUIPMENT_FAILURE);
            addAlarmVehIds(todayStartSecond, container.getOverSpeedVidSet(), todayAlarmCache, vid, OVER_SPEED);
            addAlarmVehIds(todayStartSecond, container.getTiredVidSet(), todayAlarmCache, vid, TIRED);

            addAlarmVehIds(todayStartSecond, container.getLineVidSet(), todayAlarmCache, vid, LINE);

            addAlarmVehIds(todayStartSecond, container.getDawnVidSet(), todayAlarmCache, vid, DAWN);
            addAlarmVehIds(todayStartSecond, container.getCameraVidSet(), todayAlarmCache, vid, CAMERA);
            addAlarmVehIds(todayStartSecond, container.getOtherAlarmVidSet(), todayAlarmCache, vid, OTHER);
        }
        return container;
    }

    /**
     * 获取今天车辆报警下发短信和处理信息情况
     */
    public static Map<String, VehDealMsgCacheDTO> getTodayDealAlarCache(Collection<String> monitorIds) {
        long toDayStartSecond = Date8Utils.getTodayStartSecond();
        List<RedisKey> keys = monitorIds.stream()
                .map(e -> HistoryRedisKeyEnum.TIME_MONITOR_DEAL_MSG.of(toDayStartSecond, e))
                .collect(Collectors.toList());

        return RedisHelper.getMapHashData(keys, e -> e.get(), VehDealMsgCacheDTO.class);

    }
}
