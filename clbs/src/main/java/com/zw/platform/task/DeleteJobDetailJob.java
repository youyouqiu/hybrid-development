package com.zw.platform.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;

@DisallowConcurrentExecution
public class DeleteJobDetailJob implements Job {

    private static Logger log = LogManager.getLogger(TimingStoredJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String adress = System.getProperty("clbs.root");
        String downloadPath = adress + "resourceVideo";
        File file = new File(downloadPath);
        String[] fileName;
        fileName = file.list();
        if (fileName != null) {
            for (int i = 0; i < fileName.length; i++) {
                File delete = new File(downloadPath + "/" + fileName[i]);
                delete.delete();
                log.info(fileName[i] + ": 每天凌晨2点该文件夹下所有视频删除");
            }
        }

    }
}
