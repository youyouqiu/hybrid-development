package com.zw.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.VehicleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class SaveExpireLicense implements Job {
    private static final Logger log = LogManager.getLogger(SaveExpireLicense.class);

    /**
     * 定时任务,每天00:00时启动触发器,查询出行驶证有效期和提前提醒天数满足条件的车辆数据,存入redis;
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx;
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {
                VehicleService vehicleService = appCtx.getBean(VehicleService.class);
                if (vehicleService != null) {
                    // 即将到期
                    RedisKey expireKey = HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of();
                    List<String> willExpireVehicleIds = vehicleService.getVehicleIdsByWillExpireLicense();
                    RedisHelper.setString(expireKey, JSONObject.toJSONString(willExpireVehicleIds));
                    // 已经到期
                    List<String> alreadyExpireVehicleIds = vehicleService.getVehicleIdsByAlreadyExpireLicense();
                    RedisKey alreadyExpireKey = HistoryRedisKeyEnum.ALREADY_EXPIRE_DRIVING_LICENSE.of();
                    RedisHelper.setString(alreadyExpireKey, JSONObject.toJSONString(alreadyExpireVehicleIds));
                }
            }
        } catch (Exception e) {
            log.error("查询行驶证有效期和提前提醒天数满足条件的车辆数据,并存入redis;失败.");
        }

    }
}
