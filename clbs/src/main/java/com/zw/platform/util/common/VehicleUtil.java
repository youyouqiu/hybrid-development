package com.zw.platform.util.common;

import com.zw.adas.domain.define.setting.AdasSettingListDo;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.param.form.CommandParametersForm;
import com.zw.platform.domain.vas.f3.SensorConfig;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 车辆工具类
 */
public class VehicleUtil {
    private static final Logger log = LogManager.getLogger(VehicleUtil.class);

    private VehicleUtil() {
    }

    /**
     * 根据车牌颜色的整型值获取其实际意义的值
     * @author Liubangquan
     */
    public static String getPlateColorStr(String plateColor) {
        return PlateColor.getNameOrBlankByCode(plateColor);
    }

    public static List<String> distinctMonitorIds(String monitorIds) {
        if (monitorIds == null) {
            return Collections.emptyList();
        }
        String[] monitorIdArr = monitorIds.split(",");
        return Arrays.stream(monitorIdArr).distinct().collect(Collectors.toList());
    }

    /**
     * 获得道路类型
     */
    public static String getRoadTypeStr(Integer roadType) {
        if (roadType == null) {
            return null;
        }
        switch (roadType) {
            case 1:
                return "高速路";
            case 2:
                return "都市高速路";
            case 3:
                return "国道";
            case 4:
                return "省道";
            case 5:
                return "县道";
            case 6:
                return "乡村道路";
            default:
                return "其他道路";
        }
    }

    /**
     * 车辆排序
     */
    public static List<String> sortVehicles(Set<String> vehicleIds) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new ArrayList<>();
        }
        List<String> configSortList =
            RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
        // 获取用户有序的车的id
        List<String> sortVehicleList = new ArrayList<>();
        // 获取排好序的车辆id
        if (CollectionUtils.isNotEmpty(configSortList)) {
            for (String moId : configSortList) {
                if (vehicleIds.contains(moId)) {
                    sortVehicleList.add(moId);
                }
            }
        }
        return sortVehicleList;
    }

    /**
     * 从redis中获取监控对象绑定信息
     */
    public static BindDTO getBindInfoByRedis(String moId) {
        if (StringUtils.isBlank(moId)) {
            return null;
        }
        Map<String, String> monitorInfoMap =
            RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(moId));
        if (MapUtils.isEmpty(monitorInfoMap)) {
            return null;
        }
        return MapUtil.mapToObj(monitorInfoMap, BindDTO.class);
    }

    /**
     * 从redis中获取监控对象绑定信息
     */
    public static BindDTO getBindInfoByRedis(String moId, List<String> fields) {
        if (StringUtils.isBlank(moId)) {
            return null;
        }
        if (!fields.contains("id")) {
            fields.add("id");
        }
        Map<String, String> monitorInfoMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(moId), fields);
        if (MapUtils.isEmpty(monitorInfoMap)) {
            return null;
        }
        return MapUtil.mapToObj(monitorInfoMap, BindDTO.class);
    }

    /**
     * 从redis中批量获取监控对象绑定信息
     */
    public static Map<String, BindDTO> batchGetBindInfosByRedis(Collection<String> moIds) {
        if (CollectionUtils.isEmpty(moIds)) {
            return new HashMap<>(4);
        }
        List<Map<String, String>> monitorInfoMapList =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(new HashSet<>(moIds)));

        if (CollectionUtils.isEmpty(monitorInfoMapList)) {
            return new HashMap<>(4);
        }
        return monitorInfoMapList
            .stream()
            .filter(Objects::nonNull)
            .filter(MapUtils::isNotEmpty)
            .map(map -> MapUtil.mapToObj(map, BindDTO.class))
            .collect(Collectors.toMap(BindDTO::getId, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 从redis中批量获取监控对象绑定信息
     */
    public static Map<String, VehicleDTO> batchGetVehicleInfosFromRedis(Collection<String> moIds, List<String> fields) {
        if (CollectionUtils.isEmpty(moIds)) {
            return new HashMap<>(4);
        }
        List<Map<String, String>> monitorInfoMapList;
        if (fields != null) {
            if (!fields.contains("id")) {
                try {
                    fields.add("id");
                } catch (UnsupportedOperationException e) {
                    fields = new ArrayList<>(fields);
                    fields.add("id");
                }
            }
            monitorInfoMapList = RedisHelper
                .batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(new HashSet<>(moIds)), fields);
        } else {
            monitorInfoMapList = RedisHelper
                .batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(new HashSet<>(moIds)));
        }
        if (CollectionUtils.isEmpty(monitorInfoMapList)) {
            return new HashMap<>(4);
        }
        return monitorInfoMapList
            .stream()
            .filter(Objects::nonNull)
            .filter(MapUtils::isNotEmpty)
            .map(map -> MapUtil.mapToObj(map, VehicleDTO.class))
            .collect(Collectors.toMap(VehicleDTO::getId, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 从redis中批量获取监控对象绑定信息
     */
    public static Map<String, BindDTO> batchGetBindInfosByRedis(Collection<String> moIds, List<String> fields) {
        if (CollectionUtils.isEmpty(moIds)) {
            return new HashMap<>(4);
        }
        if (!fields.contains("id")) {
            fields.add("id");
        }
        List<Map<String, String>> monitorInfoMapList = RedisHelper
            .batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(new HashSet<>(moIds)), fields);

        if (CollectionUtils.isEmpty(monitorInfoMapList)) {
            return new HashMap<>(4);
        }
        return monitorInfoMapList
            .stream()
            .filter(Objects::nonNull)
            .filter(MapUtils::isNotEmpty)
            .map(map -> MapUtil.mapToObj(map, BindDTO.class))
            .collect(Collectors.toMap(BindDTO::getId, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 按照另一列表的顺序排序，如果没在另一列表出现，那默认排在最后面
     * <p>将使用预置的keyExtractor
     *
     * @param unsortedList 待排序列表
     * @param sortedIdList 排序对照列表
     * @param <T> 待排序元素类型
     */
    public static <T> void sort(@NonNull List<T> unsortedList, @NonNull List<String> sortedIdList) {
        if (CollectionUtils.isEmpty(unsortedList)) {
            return;
        }

        final Class<?> dataClass = unsortedList.get(0).getClass();
        final Function<T, String> keyExtractor = DefaultSortKeyExtractor.resolve(dataClass);
        if (null == keyExtractor) {
            // 觉得需要报错就把这里改成 throw exception
            log.error("未预置类型为[{}]的排序键，已自动降级为不排序", dataClass.getSimpleName());
        } else {
            sort(unsortedList, sortedIdList, keyExtractor);
        }
    }

    /**
     * 按照另一列表的顺序排序，如果没在另一列表出现，那默认排在最后面
     * <p>可灵活指定如何提取排序所需字段
     *
     * @param unsortedList   待排序列表
     * @param sortedIdList   排序对照列表
     * @param keyExtractor   字段提取方式
     * @param <T> 待排序元素类型
     */
    public static <T> void sort(@NonNull List<T> unsortedList,
                                @NonNull List<String> sortedIdList,
                                @NonNull Function<T, String> keyExtractor) {
        // 相对于使用Map<String, Integer>的优点：不需要每次比较都取两个对象的hashcode，不需要拆箱sort属性；缺点：不是原地排序
        final ArrayList<T> sortedData = unsortedList.stream()
                .map(e -> new SortWrapper<>(e, sortedIdList.indexOf(keyExtractor.apply(e))))
                .sorted(Comparator.comparingInt(o -> o.sort))
                .map(o -> o.data)
                .collect(Collectors.toCollection(() -> new ArrayList<>(unsortedList.size())));
        unsortedList.clear();
        unsortedList.addAll(sortedData);
    }

    private static class SortWrapper<T> {
        T data;
        int sort;

        public SortWrapper(T data, int sort) {
            this.data = data;
            this.sort = sort;
        }
    }

    /**
     * 预置排序字段解析器
     */
    private static class DefaultSortKeyExtractor {

        private static final Map<Class<?>, Function<?, String>> CLASS_SORT_KEY_EXTRACTOR = new HashMap<>();

        static {
            putSorter(OilVehicleSetting.class, OilVehicleSetting::getVId);
            putSorter(TransdusermonitorSet.class, TransdusermonitorSet::getVehicleId);
            putSorter(SensorConfig.class, SensorConfig::getVehicleId);
            putSorter(FuelVehicle.class, FuelVehicle::getVId);
            putSorter(MileageSensorConfig.class, MileageSensorConfig::getVehicleId);
            putSorter(VibrationSensorBind.class, VibrationSensorBind::getVId);
            putSorter(SwitchingSignal.class, SwitchingSignal::getVehicleId);
            putSorter(Map.class, map -> map.get("vehicleId").toString());
            putSorter(WorkHourSettingInfo.class, WorkHourSettingInfo::getVehicleId);
            putSorter(LoadVehicleSettingInfo.class, LoadVehicleSettingInfo::getVehicleId);
            putSorter(OBDManagerSettingForm.class, OBDManagerSettingForm::getVehicleId);
            putSorter(CommandParametersForm.class, CommandParametersForm::getVehicleId);
            putSorter(AdasSettingListDo.class, AdasSettingListDo::getVehicleId);
            putSorter(TyrePressureSettingForm.class, TyrePressureSettingForm::getVehicleId);
        }

        private static <T> void putSorter(Class<T> key, Function<T, String> value) {
            CLASS_SORT_KEY_EXTRACTOR.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public static <T> Function<T, String> resolve(Class<?> klass) {
            return (Function<T, String>) CLASS_SORT_KEY_EXTRACTOR.get(klass);
        }
    }
}
