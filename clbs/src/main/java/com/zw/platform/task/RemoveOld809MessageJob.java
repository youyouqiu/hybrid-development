package com.zw.platform.task;

import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 移除过久的809查岗/督办消息
 *
 * @author Zhang Yanhui
 * @since 2020/9/30 17:12
 */

@Slf4j
@Component
public class RemoveOld809MessageJob {

    @Autowired
    private SuperPlatformMsgService superPlatformMsgService;

    @Scheduled(cron = "0 0 3 1 1/1 ?")
    public void execute() {
        try {
            superPlatformMsgService.deleteOldMessages();
        } catch (Exception e) {
            log.error("移除过久的809查岗/督办消息异常", e);
        }
    }
}
