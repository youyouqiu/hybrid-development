package com.zw.platform.task;


import com.zw.platform.repository.core.UnknowDeviceDao;
import org.quartz.*;
import org.springframework.context.ApplicationContext;


/**
 * Created by LiaoYuecai on 2017/2/27.
 */
@DisallowConcurrentExecution
public class ClearUnKnowDeviceJob implements Job {

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx = null;
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        }
        if (appCtx != null) {
            UnknowDeviceDao unknowDeviceDao = appCtx.getBean(UnknowDeviceDao.class);
            unknowDeviceDao.truncate();
        }
    }
}
