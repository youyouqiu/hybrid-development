package com.zw.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.service.basicinfo.VehicleInsuranceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 车辆保险定时任务 凌晨执行
 * @author zhouzongbo on 2018/5/11 13:50
 */
public class VehicleInsuranceJob implements Job {
    private static final Logger log = LogManager.getLogger(VehicleInsuranceJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext applicationContext;
        try {
            applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (Objects.nonNull(applicationContext)) {
                VehicleInsuranceService vehicleInsuranceService =
                    applicationContext.getBean(VehicleInsuranceService.class);
                if (Objects.nonNull(vehicleInsuranceService)) {
                    //保险单id缓存
                    RedisKey expireIdKey = HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID.of();
                    //保险到期车辆id的缓存
                    RedisKey expireVehIdKey = HistoryRedisKeyEnum.EXPIRE_INSURANCE.of();

                    List<Map<String, String>> list = vehicleInsuranceService.findExpireVehicleInsurance();
                    List<String> ids = new ArrayList<>();
                    List<String> vehicleIds = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(list)) {
                        for (Map<String, String> map : list) {
                            ids.add(map.get("id"));
                            vehicleIds.add(map.get("vehicleId"));
                        }

                    }
                    RedisHelper.delete(expireIdKey);
                    RedisHelper.addToListTail(expireIdKey, ids);
                    //保存保险到期的车辆id
                    RedisHelper.setString(expireVehIdKey, JSONObject.toJSONString(vehicleIds));
                }
            }
        } catch (Exception e) {
            log.error("车辆保险执行定时任务失败!", e);
        }
    }

}
