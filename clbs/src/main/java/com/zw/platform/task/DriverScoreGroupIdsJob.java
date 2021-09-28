package com.zw.platform.task;

import com.zw.adas.service.driverScore.AdasDriverScoreService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.util.common.Date8Utils;
import com.zw.ws.common.PublicVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

/**
 * 功能描述:司机评分企业id定时任务分
 * @author zhengjc
 * @date 2019/10/19
 * @time 11:56
 */
public class DriverScoreGroupIdsJob implements Job {

    private static final Logger log = LogManager.getLogger(SaveExpireLicense.class);

    /**
     * 定时任务每月生成月末企业的id子属关系缓存
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx = null;
        LocalDateTime dateTime = LocalDateTime.now();

        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {

                AdasDriverScoreService adasDriverScoreService = appCtx.getBean(AdasDriverScoreService.class);
                if (adasDriverScoreService != null) {
                    adasDriverScoreService.getGroupIdMap();
                }
            }

            RedisKey key = HistoryRedisKeyEnum.DRIVER_SCORE_GROUP_IDS_JOB.of(Date8Utils.getValToHour(dateTime));
            RedisHelper.setString(key, "done", PublicVariable.REDIS_CACHE_TIMEOUT_HOUR);
        } catch (Exception e) {
            log.error("定时任务每月生成月末企业的id子属关系缓存.", e);
        }

    }

}
