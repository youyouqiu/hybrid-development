package com.zw.platform.service.redis.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.RedisKeys.SensorType;
import com.zw.platform.util.common.RedisSensorQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RedisVehicleServiceImpl implements RedisVehicleService {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private UserService userService;

    private static final Pattern COMMA_SPLIT = Pattern.compile(",");

    private static final Pattern SEP_SPLIT = Pattern.compile(RedisKeys.SEPARATOR);

    @Override
    public List<String> getUserVehicles(Map<String, Object> map, String monitorType, Integer protocol)
        throws InterruptedException {

        // 由于redis.getAll方法效率不高,顾考虑开线程去单独执行
        // 获取协议下的车的缓存
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String groupId = (String) map.get("groupId");
        String assignmentId = (String) map.get("assignmentId");
        String query = (String) map.get("query");
        String userName = userId.substring(userId.indexOf("=") + 1, userId.indexOf(","));
        // 根据协议获取和终端绑定的车的缓存
        final Future<Map<String, String>> vehicleProtocolFuture = this.asyncGetMonitorsByProtocol(protocol);

        // 根据传感器类型获取和传感器绑定的车
        final Future<Map<String, String>> vehicleSensorFuture =
                StringUtils.isNotEmpty(monitorType) && StringUtils.isNotEmpty(query)
                        ? this.asyncGetVehiclesByMonitorType(monitorType)
                        : null;
        Set<String> vehicleSet = getVehiclesByUserId(groupId, assignmentId, userName);

        // 主线程要等待子线程结束,才能运行以下的代码
        Set<String> vehicleIds = new HashSet<>();
        try {
            final Map<String, String> vehicleProtocol = vehicleProtocolFuture.get();
            vehicleIds = filterByBrand(vehicleProtocol, query, vehicleSet);
            if (null != vehicleSensorFuture) {
                final Map<String, String> vehicleSensor = vehicleSensorFuture.get();
                if (MapUtils.isNotEmpty(vehicleSensor)) {
                    filterSensor(vehicleSensor, vehicleSet, vehicleProtocol, vehicleIds, query);
                }
            }
        } catch (ExecutionException ignored) {
            // Do nothing
        }

        Set<String> sortAssignVehicle = sortVehicles(vehicleIds);

        // 最后返回的list集合
        return new LinkedList<>(sortAssignVehicle);
    }

    private Set<String> sortVehicles(Set<String> vehicleIds) {
        // 获取有序的车id
        List<String> vehicleIdSort = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
        // 获取用户有序的车的id
        Set<String> sortAssignVehicle = new LinkedHashSet<>();
        // 获取排好序的车辆id
        if (vehicleIdSort != null && !vehicleIdSort.isEmpty()) {
            for (String vid : vehicleIdSort) {
                if (vehicleIds.contains(vid)) {
                    sortAssignVehicle.add(vid);
                }
            }
        }
        return sortAssignVehicle;
    }

    private Set<String> sortVehicles(Set<String> vehicleIds, Map<String, String> sensorVehicles) {
        // 获取有序的车id
        List<String> vehicleIdSort = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
        // 获取用户有序的车的id
        Set<String> sortAssignVehicle = new LinkedHashSet<>();
        // 获取排好序的车辆id
        if (vehicleIdSort != null && !vehicleIdSort.isEmpty()) {
            for (String vid : vehicleIdSort) {
                if (sensorVehicles.containsKey(vid)) {
                    String[] bindInfo = COMMA_SPLIT.split(sensorVehicles.get(vid));
                    for (String bind : bindInfo) {
                        String[] bindVale = SEP_SPLIT.split(bind);
                        //通用羅輯中 各模塊維護的緩存了兩種  一種為直接存儲的型號名稱  一種為型號名稱和特殊字符和id拼接
                        if (bindVale.length < 2) {
                            sortAssignVehicle.add(vid);
                            continue;
                        }
                        String id = bindVale[1];
                        sortAssignVehicle.add(vid + RedisKeys.SEPARATOR + id);
                    }
                } else if (vehicleIds.contains(vid)) {
                    sortAssignVehicle.add(vid);
                }
            }
        }
        return sortAssignVehicle;
    }

    /**
     * 根据车牌过滤车辆列表
     * @param vehicleProtocol 已绑定终端的车
     * @param query           查询条件
     * @param vehicleSet      当前用户能监控的车
     * @return 车辆id列表
     */
    private Set<String> filterByBrand(Map<String, String> vehicleProtocol, String query, Set<String> vehicleSet) {
        Set<String> vehicleIds = new HashSet<>();
        if (vehicleProtocol.size() > 0) {
            // 使用协议过滤车
            for (Map.Entry<String, String> entry : vehicleProtocol.entrySet()) {
                // 排除不是当前用户的车
                if (!vehicleSet.contains(entry.getKey())) {
                    continue;
                }
                if ((StringUtils.isEmpty(query) || entry.getValue().contains(query)) && !entry.getValue()
                    .contains("扫")) {
                    // 只获取用户包含
                    vehicleIds.add(entry.getKey());
                }
            }
        }
        return vehicleIds;
    }

    @Override
    public List<String> getVehicleByType(RedisSensorQuery query, String monitorType) throws InterruptedException {
        Integer protocol = query.getProtocol();
        // 根据协议获取和终端绑定的车的缓存
        List<Integer> protocols = ProtocolEnum.getProtocols(protocol);
        final Future<Map<String, String>> vehicleProtocolFuture = this.asyncGetMonitorsByProtocol(protocols);

        // 根据传感器类型获取和传感器绑定的车
        final Future<Map<String, String>> future =
            StringUtils.isNotEmpty(monitorType) ? asyncGetVehiclesByMonitorType(monitorType) : null;
        String userName = SystemHelper.getCurrentUsername();
        Set<String> vehicleSet = getVehiclesByUserId(query.getOrgId(), query.getGroupId(), userName);

        Map<String, String> sensorVehicles = new HashMap<>();
        Set<String> vehicleIds = new HashSet<>();
        try {
            final Map<String, String> vehicleProtocol = vehicleProtocolFuture.get();
            vehicleIds = filterByBrand(vehicleProtocol, query.getQuery(), vehicleSet);
            if (null != future) {
                final Map<String, String> vehicleSensor = future.get();
                if (MapUtils.isNotEmpty(vehicleSensor)) {
                    filterBySensor(sensorVehicles, vehicleSensor, vehicleProtocol, query.getQuery(), vehicleSet,
                            vehicleIds);
                }
            }
        } catch (ExecutionException ignored) {
            // Nothing to do.
        }
        Set<String> sortAssignVehicle = sortVehicles(vehicleIds, sensorVehicles);
        return new LinkedList<>(sortAssignVehicle);
    }

    @Override
    public void addVehicleSensorBind(int sensorType, String vid, String sid, String number) {
        RedisKey key = getSensorKeyByType(sensorType);
        addSensorBind(key, vid, sid, number);
    }

    private void addSensorBind(RedisKey key, String vid, String sid, String number) {
        if (number == null) {
            return;
        }
        String sensorNumber = RedisHelper.hget(key, vid);
        if (sensorNumber == null || sensorNumber.isEmpty()) {
            // 不存在该key，新增
            RedisHelper.addToHash(key, vid, number + RedisKeys.SEPARATOR + sid);
            return;
        }
        sensorNumber = sensorNumber + "," + number + RedisKeys.SEPARATOR + sid;
        RedisHelper.addToHash(key, vid, sensorNumber);
    }

    @Override
    public void delVehicleSensorBind(int sensorType, String vid, String sid, String number) {
        delSensorBind(getSensorKeyByType(sensorType), vid, sid, number);
    }

    @Override
    public void delVehicleTankBind(String vid, String sid, String number) {
        delSensorBind(RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of(), vid, sid, number);
    }

    @Override
    public void delVehicleOilSensorBind(String vid, String sid, String sensorNumber) {
        delSensorBind(RedisKeyEnum.VEHICLE_OIL_SENSOR_LIST.of(), vid, sid, sensorNumber);
    }

    private void delSensorBind(RedisKey key, String vid, String sid, String number) {
        String bindInfo = RedisHelper.hget(key, vid);
        if (bindInfo == null) {
            return;
        }
        String[] items = COMMA_SPLIT.split(bindInfo);
        final String magic = number + RedisKeys.SEPARATOR + sid;
        List<String> bindList = Arrays.stream(items).filter(o -> !magic.equals(o)).collect(Collectors.toList());
        if (bindList.isEmpty()) {
            RedisHelper.hdel(key, vid);
            return;
        }
        bindInfo = String.join(",", bindList);
        RedisHelper.addToHash(key, vid, bindInfo);
    }

    @Override
    public void delAllSensorBind(String vid, int sensorType) {
        RedisKey key = getSensorKeyByType(sensorType);
        if (null == key) {
            return;
        }
        RedisHelper.hdel(key, vid);
    }

    @Override
    public void delAllSensorBindByVehicleId(String vid) {
        delAllSensorBind(vid, 0);
        delAllSensorBind(vid, 1);
        delAllSensorBind(vid, 2);
        delAllSensorBind(vid, 3);
        delAllSensorBind(vid, 4);
        delAllSensorBind(vid, 5);
        delAllSensorBind(vid, 6);
        delAllSensorBind(vid, 7);
    }

    @Override
    public void updateVehicleSensorBind(TransdusermonitorSet set) {
        RedisKey key = getSensorKeyByType(set.getSensorType());
        String bindInfo = RedisHelper.hget(key, set.getVehicleId());
        if (bindInfo == null) {
            return;
        }
        String newBindValue = set.getSensorNumber() + RedisKeys.SEPARATOR + set.getId();
        List<String> bindList = new ArrayList<>();
        String[] items = COMMA_SPLIT.split(bindInfo);
        for (String item : items) {
            if (item.contains(set.getId())) {
                bindList.add(newBindValue);
                continue;
            }
            bindList.add(item);
        }
        bindInfo = String.join(",", bindList);
        RedisHelper.addToHash(key, set.getVehicleId(), bindInfo);
    }

    /**
     * 修改油箱型号后,更新油箱型号和车辆的缓存
     * @param oilName 油箱型号
     * @param list    油箱缓存
     */
    @Override
    public void updateVehicleOilBoxCache(String oilName, List<OilVehicleSetting> list) {
        String value;
        String[] oilBox;
        List<String> tankList;
        for (OilVehicleSetting m : list) {
            value = RedisHelper.hget(RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of(), m.getVehicleId());
            oilBox = COMMA_SPLIT.split(value);
            tankList = new ArrayList<>();
            for (String box : oilBox) {
                if (box.contains(m.getId())) {
                    tankList.add(oilName + RedisKeys.SEPARATOR + m.getId());
                    continue;
                }
                tankList.add(box);
            }
            RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of(),
                    m.getVehicleId(), String.join(",", tankList));
        }
    }

    private RedisKey getSensorKeyByType(int sensorType) {
        final RedisKey key;
        // todo 改成常量
        switch (sensorType) {
            case 3:
                key = RedisKeyEnum.VEHICLE_ROTATE_MONITOR_LIST.of();
                break;
            case 2:
                key = RedisKeyEnum.VEHICLE_WET_MONITOR_LIST.of();
                break;
            case 1:
                key = RedisKeyEnum.VEHICLE_TEMPERATURE_MONITOR_LIST.of();
                break;
            case 0:
                key = RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST.of();
                break;
            case 4:
                key = RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST.of();
                break;
            case 5:
                key = RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST.of();
                break;
            case 6:
                key = RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST.of();
                break;
            case 7:
                key = RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST.of();
                break;
            default:
                key = null;
        }
        return key;
    }

    private void filterBySensor(Map<String, String> sensorVehicles, Map<String, String> vehicleSensor,
        Map<String, String> vehicleProtocol, String query, Set<String> userVehicles, Set<String> brandVehicles) {
        for (Map.Entry<String, String> vehicleEntry : vehicleSensor.entrySet()) {
            String vehicleId = vehicleEntry.getKey();
            // 排除不是协议的车也排除
            if (!vehicleProtocol.containsKey(vehicleId)) {
                continue;
            }
            // 排除不是当前用户的车
            if (!userVehicles.contains(vehicleEntry.getKey())) {
                continue;
            }
            String[] sensors = COMMA_SPLIT.split(vehicleEntry.getValue());
            for (String sensor : sensors) {
                if (!StringUtils.isEmpty(sensor) && !"null".equals(sensor)) {
                    String[] items = SEP_SPLIT.split(sensor);
                    // 查询条件为空，车牌号匹配或者当前传感器型号匹配查询条件，添加车辆id
                    if (items.length < 2 && ((StringUtils.isEmpty(query) || brandVehicles.contains(vehicleId) || (
                        vehicleProtocol.containsKey(vehicleId) && items[0].contains(query))))) {
                        sensorVehicles.put(vehicleId, vehicleId);
                        continue;
                    }
                    if (StringUtils.isEmpty(query) || brandVehicles.contains(vehicleId) || (
                        vehicleProtocol.containsKey(vehicleId) && items[0].contains(query))) {
                        String value = sensorVehicles.get(vehicleId);
                        String sensorInfo = value == null
                                ? vehicleId + RedisKeys.SEPARATOR + items[1]
                                : value + "," + vehicleId + RedisKeys.SEPARATOR + items[1];
                        sensorVehicles.put(vehicleId, sensorInfo);
                    }
                }
            }
        }
    }

    private Set<String> getVehiclesByUserId(String orgId, String groupId, String userName) {
        Set<String> orgGroupIds = RedisHelper.getSet(RedisKeyEnum.ORG_GROUP.of(orgId));

        // 得到用户——分组缓存
        Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(userName));

        List<RedisKey> redisKeys = new ArrayList<>();
        for (String group : groupIds) {

            if (!StringUtils.isEmpty(orgId) && !orgGroupIds.contains(group)) {
                continue;
            }

            if (!StringUtils.isEmpty(groupId) && !(group.equals(groupId))) {
                continue;
            }

            redisKeys.add(RedisKeyEnum.GROUP_MONITOR.of(group));
        }
        return RedisHelper.batchGetSet(redisKeys);
    }

    private Future<Map<String, String>> asyncGetVehiclesByMonitorType(String monitorType) {
        return taskExecutor.submit(() -> getVehiclesByMonitorType(monitorType));
    }

    private Map<String, String> getVehiclesByMonitorType(String monitorType) {
        return SensorType.SENSOR_KEY.p2bOptional(monitorType)
                .map(RedisKey::of)
                .map(RedisHelper::hgetAll)
                .orElseGet(HashMap::new);
    }

    /**
     * 根据协议查询监控对象 异步
     *
     * @param protocol 协议
     * @return 监控对象id -> 监控对象编号
     */
    private Future<Map<String, String>> asyncGetMonitorsByProtocol(Integer protocol) {
        return taskExecutor.submit(() -> {
            final Map<String, String> map = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_PROTOCOL.of(protocol));
            return Optional.ofNullable(map).orElseGet(HashMap::new);
        });
    }

    /**
     * 根据协议查询监控对象 异步
     *
     * @param protocols 协议
     * @return 监控对象id -> 监控对象编号
     */
    private Future<Map<String, String>> asyncGetMonitorsByProtocol(Collection<Integer> protocols) {
        final List<RedisKey> keys =
                protocols.stream().map(RedisKeyEnum.MONITOR_PROTOCOL::of).collect(Collectors.toList());
        return taskExecutor.submit(() -> RedisHelper.hgetAll(keys));
    }

    private void filterSensor(Map<String, String> map, Set<String> userSet, Map<String, String> vehicleProtocol,
        Set<String> purposeSet, String query) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String[] sensorNum = SEP_SPLIT.split(entry.getValue());
            if ((sensorNum[0].contains(query)) && userSet.contains(entry.getKey()) && vehicleProtocol
                .containsKey(entry.getKey())) {
                purposeSet.add(entry.getKey());
            }
        }
    }

    @Override
    public Map<String, String> getVehicleBindByType(String monitorType) throws Exception {
        // 根据传感器类型获取和传感器绑定的车
        return StringUtils.isEmpty(monitorType) ? new HashMap<>(0) : getVehiclesByMonitorType(monitorType);
    }

    @Override
    public void delVehicleWorkHourBind(String vehicleId, String id, String sensorNumber) {
        delSensorBind(RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST.of(), vehicleId, id, sensorNumber);
    }

    @Override
    public void delOBDSettingBind(String vehicleId, String id, String sensorNumber) {
        delSensorBind(RedisKeyEnum.OBD_SETTING_MONITORY_LIST.of(), vehicleId, id, sensorNumber);
    }

    /**
     * 删除载重传感器
     */
    @Override
    public void delLoadBind(String vehicleId, String id, String sensorNumber) {
        delSensorBind(RedisKeyEnum.LOAD_SETTING_MONITORY_LIST.of(), vehicleId, id, sensorNumber);
    }

    @Override
    public List<String> getOilSensorVehicle(RedisSensorQuery query) throws InterruptedException {
        Integer protocol = query.getProtocol();
        // 根据协议获取和终端绑定的车的缓存
        final Future<Map<String, String>> vehicleProtocolFuture = this.asyncGetMonitorsByProtocol(protocol);

        // 获取绑定油位传感器的监控对象
        final Future<Map<String, String>> future = asyncGetVehiclesByMonitorType(SensorType.SENSOR_TYRE_OIL_MONITOR);
        String userName = SystemHelper.getCurrentUsername();
        Set<String> vehicleSet = getVehiclesByUserId(query.getOrgId(), query.getGroupId(), userName);

        Map<String, String> sensorVehicles = new HashMap<>();
        Set<String> vehicleIds = new HashSet<>();
        try {
            final Map<String, String> vehicleProtocol;
            vehicleProtocol = vehicleProtocolFuture.get();
            vehicleIds = filterByBrand(vehicleProtocol, query.getQuery(), vehicleSet);
            if (null != future) {
                final Map<String, String> vehicleSensor = future.get();
                if (MapUtils.isNotEmpty(vehicleSensor)) {
                    filterBySensor(sensorVehicles, vehicleSensor, vehicleProtocol, query.getQuery(),
                            vehicleSet, vehicleIds);
                }
            }
        } catch (ExecutionException ignored) {
            // Do nothing.
            // 此处vehicleIds初始化后更新两次，更新失败后取最新值作为“降级”，因此一开始初始化也是有必要的
        }
        Set<String> sortAssignVehicle = sortVehicles(vehicleIds, sensorVehicles);
        return new LinkedList<>(sortAssignVehicle);
    }

    @Override
    public void delAllSensorBindByMonitorIds(List<String> monitorIds) {
        deleteAllSensorBind(monitorIds, 0);
        deleteAllSensorBind(monitorIds, 1);
        deleteAllSensorBind(monitorIds, 2);
        deleteAllSensorBind(monitorIds, 3);
        deleteAllSensorBind(monitorIds, 4);
        deleteAllSensorBind(monitorIds, 5);
        deleteAllSensorBind(monitorIds, 6);
        deleteAllSensorBind(monitorIds, 7);
    }

    private void deleteAllSensorBind(List<String> monitorIds, Integer sensorType) {
        RedisKey key = getSensorKeyByType(sensorType);
        if (null == key) {
            return;
        }
        RedisHelper.hdel(key, monitorIds);
    }
}
