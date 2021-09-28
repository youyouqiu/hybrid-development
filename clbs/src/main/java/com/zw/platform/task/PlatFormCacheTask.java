package com.zw.platform.task;

import com.zw.platform.service.basicinfo.InitRedisCacheService;
import com.zw.platform.service.basicinfo.impl.InitRedisCacheServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

/**
 * @Author: zjc
 * @Description:平台缓存初始化任务
 * @Date: create in 2020/9/16 9:12
 */
public class PlatFormCacheTask implements Job {

    private static final Logger logger = LogManager.getLogger(PlatFormCacheTask.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ApplicationContext appCtx = null;
        try {
            appCtx = (ApplicationContext) jobExecutionContext.getScheduler().getContext().get("applicationContextKey");
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        }
        if (appCtx != null) {
            try {
                InitRedisCacheService initRedisCacheService = appCtx.getBean(InitRedisCacheServiceImpl.class);
                logger.info("开始缓存初始化");
                initRedisCacheService.addCacheToRedis();
                logger.info("结束缓存初始化");
            } catch (Exception e) {
                logger.error("平台缓存初始化定时任务执行报错", e);
            }
        }

    }
}
