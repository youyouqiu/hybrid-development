package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewMonitorDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 监控对象实现类
 * @date 2020/11/615:27
 */
@Service("monitorService")
public class MonitorServiceImpl implements MonitorService {
    @Autowired
    private NewMonitorDao newMonitorDao;

    @Autowired
    private GroupService groupService;

    @Autowired
    private NewProfessionalsDao professionalsDao;

    @Autowired
    private NewConfigDao configDao;

    @Override
    public List<String> getMonitorIdByOrgId(String orgId) {
        return newMonitorDao.getMonitorIdByOrgId(orgId);
    }

    @Override
    public Map<String, BaseKvDo<String, String>> getMonitorIdNameMap(Collection<String> monitorIds, String search) {
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = new HashMap<>();
        if (CollectionUtils.isEmpty(monitorIds)) {
            return monitorIdNameMap;
        }
        monitorIdNameMap = newMonitorDao.getMonitorIdNameMap(monitorIds, search);
        return monitorIdNameMap;
    }

    @Override
    public Map<String, ClientVehicleInfo> getMonitorStatus(Collection<String> monitorIds) {
        List<RedisKey> statusRedisKeys = HistoryRedisKeyEnum.MONITOR_STATUS.ofs(monitorIds);
        List<String> result = RedisHelper.batchGetString(statusRedisKeys);
        Map<String, ClientVehicleInfo> monitorStatusMap = new HashMap<>(CommonUtil.ofMapCapacity(result.size()));
        for (String status : result) {
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(status, ClientVehicleInfo.class);
            monitorStatusMap.put(clientVehicleInfo.getVehicleId(), clientVehicleInfo);
        }
        return monitorStatusMap;
    }

    @Override
    public Set<String> getAllOnLineMonitor() {
        List<String> onLineKeys = RedisHelper.scanKeys(HistoryRedisKeyEnum.MONITOR_STATUS_FUZZY.of());
        Set<String> monitorIds = new HashSet<>();
        for (String redisKey : onLineKeys) {
            monitorIds.add(redisKey.substring(0, redisKey.lastIndexOf("-")));
        }
        return monitorIds;
    }

    @Override
    public Map<String, ClientVehicleInfo> getAllMonitorStatus() {
        List<String> result = RedisHelper.getStringByPattern(HistoryRedisKeyEnum.MONITOR_STATUS_FUZZY.of());
        Map<String, ClientVehicleInfo> monitorStatusMap = new HashMap<>(CommonUtil.ofMapCapacity(result.size()));
        for (String status : result) {
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(status, ClientVehicleInfo.class);
            monitorStatusMap.put(clientVehicleInfo.getVehicleId(), clientVehicleInfo);
        }
        return monitorStatusMap;
    }

    @Override
    public Set<String> getOnLineMonIds(Collection<String> monitorIds) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new HashSet<>();
        }
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.MONITOR_STATUS.ofs(monitorIds);
        List<String> monitorStatusJsonStrList = RedisHelper.batchGetString(redisKeys);
        Set<String> onlineMoIds = new HashSet<>();
        for (String monitorStatusJsonStr : monitorStatusJsonStrList) {
            if (StringUtils.isBlank(monitorStatusJsonStr)) {
                continue;
            }
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(monitorStatusJsonStr, ClientVehicleInfo.class);
            onlineMoIds.add(clientVehicleInfo.getVehicleId());
        }
        return onlineMoIds;
    }

    @Override
    public Set<String> getOffLineMonIds(Collection<String> monitorIds) {
        Set<String> onLineMonIds = getOnLineMonIds(monitorIds);
        return monitorIds.stream().filter(id -> !onLineMonIds.contains(id)).collect(Collectors.toSet());
    }

    @Override
    public List<String> findSendParmId(String monitorId) {
        return newMonitorDao.findSendParmId(monitorId);
    }

    @Override
    public Map<String, MonitorAccStatus> getAccAndStatus(Collection<String> monitorIds) {
        return getAccAndStatus(monitorIds, true);
    }

    @Override
    public Map<String, MonitorAccStatus> getAccAndStatus(Collection<String> monitorIds, boolean needAccStatus) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new HashMap<>(16);
        }
        //获取监控对象的在线状态
        Map<String, ClientVehicleInfo> monitorStatusMap = getMonitorStatus(monitorIds);
        if (monitorStatusMap.isEmpty()) {
            return new HashMap<>(16);
        }
        //获取在线监控对象的ACC状态
        Set<String> onlineMonitorIds = monitorStatusMap.keySet();
        List<RedisKey> locationRedisKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(onlineMonitorIds);
        List<String> locationList = RedisHelper.batchGetString(locationRedisKeys);
        Map<String, Integer> monitorAccMap = null;
        if (needAccStatus) {
            monitorAccMap = new HashMap<>(CommonUtil.ofMapCapacity(locationList.size()));
            for (String locationStr : locationList) {
                JSONObject location = JSON.parseObject(locationStr);
                Integer acc = location.getJSONObject("data").getJSONObject("msgBody").getInteger("acc");
                String monitorId = location.getJSONObject("desc").getString("monitorId");
                monitorAccMap.put(monitorId, acc);
            }
        }

        //构建监控对象状态和ACC状态
        Map<String, MonitorAccStatus> result = new HashMap<>(CommonUtil.ofMapCapacity(onlineMonitorIds.size()));
        for (String monitorId : onlineMonitorIds) {
            Integer status = monitorStatusMap.get(monitorId).getVehicleStatus();
            Integer accStatus = Objects.isNull(monitorAccMap) ? null : monitorAccMap.get(monitorId);
            result.put(monitorId, new MonitorAccStatus(status, accStatus));
        }
        return result;
    }

    @Override
    public Set<String> getOnceOnLineIds(Collection<String> monitorIds) {
        List<RedisKey> locationRedisKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(monitorIds);
        Set<RedisKey> existedKeys = RedisHelper.isContainsKey(locationRedisKeys);
        Set<String> filterMonitorIds = new HashSet<>();
        for (RedisKey redisKey : existedKeys) {
            filterMonitorIds.add(redisKey.get().substring(0, redisKey.get().lastIndexOf("-")));
        }
        return filterMonitorIds;
    }

    @Override
    public Set<String> getMonitorByGroupOrOrgDn(String pid, String type, Integer status) {
        List<String> groupIds;
        if (Objects.equals(type, "org")) {
            groupIds = groupService.getUserGroupByOrgDn(pid).stream().map(GroupDTO::getId).collect(Collectors.toList());
        } else {
            groupIds = Collections.singletonList(pid);
        }
        List<RedisKey> redisKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupIds);
        Set<String> monitorIds = RedisHelper.batchGetSet(redisKeys);
        if (CollectionUtils.isEmpty(monitorIds) || Objects.isNull(status)) {
            return monitorIds;
        }
        Map<String, MonitorAccStatus> monitorAccStatusMap = getAccAndStatus(monitorIds, false);
        Set<String> onlineIds = monitorAccStatusMap.keySet();
        //获取在线的监控对象
        if (Objects.equals(status, 1)) {
            //获取在线的求交集
            monitorIds.retainAll(onlineIds);
        } else {
            //获取不在线的，剔除所有在线的监控对象
            monitorIds.removeAll(onlineIds);
        }
        return monitorIds;
    }

    @Override
    public void filterByMonitorType(String monitorType, Set<String> monitorIds) {
        if (monitorIds.isEmpty()) {
            return;
        }
        RedisKey sortRedisKey;
        switch (monitorType) {
            case "vehicle":
                sortRedisKey = RedisKeyEnum.VEHICLE_SORT_LIST.of();
                break;
            case "people":
                sortRedisKey = RedisKeyEnum.PEOPLE_SORT_LIST.of();
                break;
            case "thing":
                sortRedisKey = RedisKeyEnum.THING_SORT_LIST.of();
                break;
            default:
                sortRedisKey = null;
                break;
        }
        if (Objects.isNull(sortRedisKey)) {
            return;
        }

        List<String> sortIds = RedisHelper.getList(sortRedisKey);
        if (CollectionUtils.isEmpty(sortIds)) {
            monitorIds.clear();
            return;
        }

        Set<String> filterIds = new HashSet<>();
        for (String monitorId : sortIds) {
            if (monitorIds.contains(monitorId)) {
                filterIds.add(monitorId);
            }
        }
        monitorIds.clear();
        monitorIds.addAll(filterIds);

    }

    @Override
    public void filterByKeyword(String keyword, String queryType, Set<String> monitorIds) {
        if (monitorIds.isEmpty()) {
            return;
        }
        Set<String> fuzzyIds;
        switch (queryType) {
            case "name":
                fuzzyIds = MonitorUtils.fuzzySearchBindMonitorIds(keyword);
                break;
            case "simCardNumber":
                fuzzyIds = MonitorUtils.fuzzySearchBySim(keyword);
                break;
            case "deviceNumber":
                fuzzyIds = MonitorUtils.fuzzySearchByDevice(keyword);
                break;
            case "professional":
                fuzzyIds = professionalsDao.getBindMonitorIdsByKeyword(keyword);
                break;
            case "monitorId":
                fuzzyIds = Collections.singleton(keyword);
                break;
            default:
                fuzzyIds = null;
                break;
        }
        if (CollectionUtils.isEmpty(fuzzyIds)) {
            monitorIds.clear();
        } else {
            monitorIds.retainAll(fuzzyIds);
        }
    }

    @Override
    public void filterByDeviceType(Collection<String> deviceTypes, Set<String> ownIds) {
        if (Objects.isNull(deviceTypes) || CollectionUtils.isEmpty(ownIds)) {
            return;
        }
        List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_PROTOCOL.ofs(deviceTypes);
        Set<String> monitorIds = RedisHelper.hkeys(redisKeys);
        ownIds.retainAll(monitorIds);
    }

    @Override
    public void filterByVehicleTypeName(String veTypeName, Set<String> ownIds) {
        if (CollectionUtils.isEmpty(ownIds)) {
            return;
        }
        List<VehicleTypeDTO> vehicleTypes = TypeCacheManger.getInstance().getVehicleTypes(null);
        Set<String> vehicleTypeIds =
            vehicleTypes.stream().filter(o -> Objects.equals(veTypeName, o.getType())).map(VehicleTypeDTO::getId)
                .collect(Collectors.toSet());
        if (vehicleTypeIds.isEmpty()) {
            ownIds.clear();
            return;
        }
        Map<String, String> monitorIdTypeMap = MonitorUtils.getKeyValueMap(ownIds, "id", "vehicleType");
        Set<String> filterIds = new HashSet<>();
        for (String monitorId : ownIds) {
            String vehicleTypeId = monitorIdTypeMap.get(monitorId);
            if (StringUtils.isNotBlank(vehicleTypeId) && vehicleTypeIds.contains(vehicleTypeId)) {
                filterIds.add(monitorId);
            }
        }
        ownIds.clear();
        ownIds.addAll(filterIds);
    }

    @Override
    public void filterByOnlineStatus(Map<String, MonitorAccStatus> accAndStatusMap, Set<String> monitorIds,
        Integer onlineStatus) {
        if (monitorIds.isEmpty()) {
            return;
        }
        if (accAndStatusMap == null) {
            accAndStatusMap = getAccAndStatus(monitorIds, false);
        }
        switch (onlineStatus) {
            case 1:
                //获取在线监控对象
                monitorIds.clear();
                monitorIds.addAll(accAndStatusMap.keySet());
                break;
            case 0:
            case 7:
                //未上线，剔除在线的监控对象
                monitorIds.removeAll(accAndStatusMap.keySet());
                break;
            case 8:
                //获取离线监控对象 保留当前不在线但上过线的监控对象
                monitorIds.removeAll(accAndStatusMap.keySet());
                Set<String> onceOnLineIds = getOnceOnLineIds(monitorIds);
                monitorIds.retainAll(onceOnLineIds);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 9:
                monitorIds.clear();
                for (Map.Entry<String, MonitorAccStatus> entry : accAndStatusMap.entrySet()) {
                    Integer status = entry.getValue().getStatus();
                    if (Objects.equals(status, Vehicle.getPassStatus(onlineStatus))) {
                        monitorIds.add(entry.getKey());
                    }
                }
                break;
            default:
                monitorIds.clear();
        }

    }

    @Override
    public Set<String> getDeviceIdByMonitor(Collection<String> monitorIds) {
        return configDao.getDeviceIdByMonitorId(monitorIds);
    }

}
