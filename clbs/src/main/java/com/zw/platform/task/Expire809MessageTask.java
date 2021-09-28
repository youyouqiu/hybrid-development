package com.zw.platform.task;

import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定期标记809消息过期，以免用户触发间隔太久导致数据量过大
 *
 * @author Zhang Yanhui
 * @since 2021/5/26 9:58
 */

@Slf4j
@Component
public class Expire809MessageTask {
    @Autowired
    private SuperPlatformMsgService superPlatformMsgService;

    @Scheduled(cron = "0 */30 * * * ?")
    public void execute() {
        try {
            superPlatformMsgService.updatePastData();
        } catch (Exception e) {
            log.error("执行定时过期未处理的809数据异常", e);
        }

    }
}
