package com.zw.platform.task;

import com.zw.platform.service.reportManagement.VehStateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 功能描述:定时删除车辆状态报表数据
 * @author zhengjc
 * @date 2020/1/2
 * @time 14:10
 */
@Component
public class VehStateReportDataDeleteTask {

    private static final Logger logger = LogManager.getLogger(VehStateReportDataDeleteTask.class);

    @Autowired
    private VehStateService vehStateService;

    /**
     * 每个月2020-12-01 02:03:04执行一次
     */
    @Scheduled(cron = "4 3 2 1 * ?")
    public void executeInitRedisTime() {
        try {
            vehStateService.deleteData();
        } catch (Exception e) {
            logger.error("定时删除车辆状态表数据", e);
        }

    }
}
