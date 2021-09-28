package com.zw.platform.task;

import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 功能描述:定时扫描下发短信缓存
 * @author zhengjc
 * @date 2020/1/2
 * @time 14:10
 */
@Component
public class SendMsgCacheMonitorTask {

    private static final Logger logger = LogManager.getLogger(SendMsgCacheMonitorTask.class);

    @Autowired
    private SendMsgCache sendMsgCache;

    @Scheduled(cron = "0/5 * * * * ?")
    public void executeInitRedisTime() {
        try {
            sendMsgCache.scanCache();
        } catch (Exception e) {
            logger.error("定时刷新下发短信缓存失败", e);
        }

    }
}
