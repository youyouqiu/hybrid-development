package com.zw.platform.task;

import com.zw.platform.repository.vas.DeviceUpgradeDao;
import com.zw.platform.service.monitoring.CommandParametersService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/***
 @Author zhengjc
 @Date 2019/9/30 9:14
 @Description 下发升级任务
 @version 1.0
 **/

public class UpgradeTaskJob implements Job {

    public static final Logger logger = LogManager.getLogger(UpgradeTaskJob.class);

    @Autowired
    CommandParametersService commandParametersService;

    @Autowired
    DeviceUpgradeDao deviceUpgradeDao;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("定时下发终端升级任务！");
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            deviceUpgradeDao.updateUpgradeStrategy(jobDataMap.getString("id"));
            commandParametersService.sendParamByCommandType(jobDataMap.getString("vehicleId"),
                138, jobDataMap.getString("upgradeType"));
            context.getScheduler().deleteJob(context.getJobDetail().getKey());
            context.getScheduler().getCalendarNames();
        } catch (Exception e) {
            logger.error("定时下发终端升级任务异常！", e);
        }
    }
}
