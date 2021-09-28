package com.zw.platform.task;

import com.zw.platform.service.reportManagement.DriverDiscernStatisticsService;
import com.zw.platform.service.schedulingcenter.SchedulingManagementService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/13 9:40
 */
@Component
public class SchedulingMonitorTask {
    private static final Logger logger = LogManager.getLogger(SchedulingMonitorTask.class);

    @Autowired
    private SchedulingManagementService schedulingManagementService;

    @Autowired
    private DriverDiscernStatisticsService driverDiscernStatisticsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void executeInitRedisTime() {
        try {
            logger.info("定时保存需要计算离线报表的排班id到redis开始");
            schedulingManagementService.saveNeedCalculateOfflineReportScheduledIdToRedis();
            logger.info("定时保存需要计算离线报表的排班id到redis结束");
        } catch (Exception e) {
            logger.error("定时保存需要计算离线报表的排班id到redis异常", e);
        }

    }

    /**
     * 驾驶员身份上报定时删除照片
     * 没有1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteDriverReportPhoto() {
        try {
            driverDiscernStatisticsService.deletePhoto();
        } catch (Exception e) {
            logger.error("驾驶员身份上报定时删除照片异常", e);
        }

    }
}
