package com.zw.platform.task;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.service.basicinfo.LifecycleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Objects;

/**
 * @author zhouzongbo on 2018/12/25 9:06
 */
public class LifecycleExpireRemindJob implements Job {
    private static final Logger log = LogManager.getLogger(LifecycleExpireRemindJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        ApplicationContext applicationContext;
        try {
            applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (Objects.nonNull(applicationContext)) {
                LifecycleService lifecycleService = applicationContext.getBean(LifecycleService.class);
                if (Objects.nonNull(lifecycleService)) {
                    List<String> expireRemindList = lifecycleService.findLifecycleExpireRemindList();
                    RedisKey lifecycleExpireListRedisKey = HistoryRedisKeyEnum.LIFECYCLE_EXPIRE_LIST.of();
                    if (RedisHelper.isContainsKey(lifecycleExpireListRedisKey)) {
                        RedisHelper.delete(lifecycleExpireListRedisKey);
                    }
                    RedisKey lifecycleExpireStringRedisKey = HistoryRedisKeyEnum.LIFECYCLE_EXPIRE_STRING.of();
                    if (RedisHelper.isContainsKey(lifecycleExpireStringRedisKey)) {
                        RedisHelper.delete(lifecycleExpireStringRedisKey);
                    }
                    if (CollectionUtils.isNotEmpty(expireRemindList)) {
                        RedisHelper.addToList(lifecycleExpireListRedisKey, expireRemindList);
                        RedisHelper.setString(lifecycleExpireStringRedisKey, JSON.toJSONString(expireRemindList));
                    }
                    //服务已经到期
                    RedisKey alreadyExpireLifeCycleListRedisKey = HistoryRedisKeyEnum.ALREADY_EXPIRE_LIFE_CYCLE.of();
                    if (RedisHelper.isContainsKey(alreadyExpireLifeCycleListRedisKey)) {
                        RedisHelper.delete(alreadyExpireLifeCycleListRedisKey);
                    }
                    List<String> alreadyExpireRemindList = lifecycleService.findLifecycleAlreadyExpireRemindList();
                    if (CollectionUtils.isNotEmpty(alreadyExpireRemindList)) {
                        RedisHelper
                            .setString(alreadyExpireLifeCycleListRedisKey, JSON.toJSONString(alreadyExpireRemindList));
                    }
                }
            }

        } catch (Exception e) {
            log.error("服务到期提醒定时任务执行异常", e);
        }
    }

}
