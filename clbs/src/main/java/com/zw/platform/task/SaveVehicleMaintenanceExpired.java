package com.zw.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.service.VehicleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/12/11 10:32
 */
public class SaveVehicleMaintenanceExpired implements Job {
    private static final Logger log = LogManager.getLogger(SaveVehicleMaintenanceExpired.class);

    /**
     * 定时任务,每天00:00时启动触发器,查询出车辆保养到期的车辆数据,存入redis;
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx;
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {
                VehicleService vehicleService = appCtx.getBean(VehicleService.class);
                if (vehicleService != null) {
                    // 保养有效期到期的车辆
                    Set<Object> maintenanceExpiredVehicleIds = new HashSet<>();
                    List<String> vehicleIds = vehicleService.getVehicleIdsByMaintenanceExpired();
                    if (CollectionUtils.isNotEmpty(vehicleIds)) {
                        maintenanceExpiredVehicleIds.addAll(vehicleIds);
                    }
                    // 保养里程数不为null的车辆
                    Map<String, BaseKvDo<String, Integer>> vehicleIdMaintenanceMileageMap =
                        vehicleService.getVehicleIdsByMaintenanceMileageIsNotNull();

                    if (MapUtils.isEmpty(vehicleIdMaintenanceMileageMap)) {
                        return;
                    }
                    Collection<BaseKvDo<String, Integer>> values = vehicleIdMaintenanceMileageMap.values();
                    Set<RedisKey> vehicleLocationKeys = values.stream()
                            .map(e -> HistoryRedisKeyEnum.MONITOR_LOCATION.of(e.getKeyName()))
                            .collect(Collectors.toSet());
                    List<String> vehicleLocations = RedisHelper.batchGetString(vehicleLocationKeys);

                    for (String location : vehicleLocations) {
                        JSONObject vehicleLocationInfo = JSONObject.parseObject(location);
                        JSONObject msgBody = vehicleLocationInfo.getJSONObject("data").getJSONObject("msgBody");
                        String vehicleId = msgBody.getJSONObject("monitorInfo").getString("monitorId");
                        Double gpsMileage = msgBody.getDouble("gpsMileage");
                        BaseKvDo<String, Integer> vehMile = vehicleIdMaintenanceMileageMap.get(vehicleId);
                        if (vehMile == null || gpsMileage == null) {
                            continue;
                        }
                        Integer maintainMileage = vehMile.getFirstVal();
                        if (gpsMileage > maintainMileage) {
                            maintenanceExpiredVehicleIds.add(vehicleId);
                        }

                    }

                    RedisKey expireKey = HistoryRedisKeyEnum.EXPIRE_MAINTENANCE.of();

                    RedisHelper.setString(expireKey, JSONObject.toJSONString(maintenanceExpiredVehicleIds));
                }
            }
        } catch (Exception e) {
            log.error("查询车辆保养到期的车辆数据,并存入redis失败", e);
        }
    }
}
