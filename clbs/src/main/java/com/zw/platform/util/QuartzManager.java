package com.zw.platform.util;

import com.zw.platform.domain.taskjob.TaskJobForm;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述:计划任务管理
 * @author zhengjc
 * @date 2019/9/29
 * @time 10:56
 */
@Service
public class QuartzManager {
    public final Logger log = LoggerFactory.getLogger(QuartzManager.class);
    @Autowired
    @Qualifier("schedulers")
    private Scheduler scheduler;

    /**
     * 添加任务
     * @param job
     * @throws SchedulerException
     */

    public void addJob(TaskJobForm job, Map<String, Object> param) {
        try {
            // 创建jobDetail实例，绑定Job实现类
            // 指明job的名称，所在组的名称，以及绑定job类

            Class<? extends Job> jobClass =
                (Class<? extends Job>) (Class.forName(job.getBeanClass()).newInstance().getClass());
            //任务名称和组构成任务key
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(job.getJobName(), job.getJobGroup()).build();
            // 定义调度触发规则
            // 使用cornTrigger规则
            // 触发器key
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup())
                .startAt(DateBuilder.futureDate(1, IntervalUnit.SECOND))
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).startNow().build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            for (String key :param.keySet()) {
                jobDataMap.put(key, param.get(key));
            }
            // 把作业和触发器注册到任务调度中
            scheduler.scheduleJob(jobDetail, trigger);
            // 启动
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            log.error("创建定时任务失败", e);
        }
    }

    /**
     * 获取所有计划中的任务列表
     * @return
     * @throws SchedulerException
     */
    public List<TaskJobForm> getAllJob() throws SchedulerException {
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<TaskJobForm> jobList = new ArrayList<>();
        for (JobKey jobKey : jobKeys) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                TaskJobForm job = getTaskJobForm(jobKey, trigger);
                jobList.add(job);
            }
        }
        return jobList;
    }

    private String getCornExpression(Trigger trigger) {
        if (trigger instanceof CronTrigger) {
            CronTrigger cronTrigger = (CronTrigger) trigger;
            return cronTrigger.getCronExpression();

        }
        return "";
    }

    /**
     * 所有正在运行的job
     * @return
     * @throws SchedulerException
     */
    public List<TaskJobForm> getRunningJob() throws SchedulerException {
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<TaskJobForm> jobList = new ArrayList<>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            TaskJobForm job = getTaskJobForm(executingJob);
            jobList.add(job);
        }
        return jobList;
    }

    private TaskJobForm getTaskJobForm(JobExecutionContext executingJob) throws SchedulerException {
        JobDetail jobDetail = executingJob.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        Trigger trigger = executingJob.getTrigger();
        TaskJobForm job = getTaskJobForm(jobKey, trigger);
        return job;
    }

    private TaskJobForm getTaskJobForm(JobKey jobKey, Trigger trigger) throws SchedulerException {
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        TaskJobForm job = new TaskJobForm();
        job.setJobName(jobKey.getName());
        job.setJobGroup(jobKey.getGroup());
        job.setDescription("触发器:" + trigger.getKey());
        job.setJobStatus(triggerState.name());
        job.setCronExpression(getCornExpression(trigger));
        return job;
    }

    /**
     * 暂停一个job
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void pauseJob(TaskJobForm scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个job
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void resumeJob(TaskJobForm scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个job
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void deleteJob(TaskJobForm scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.deleteJob(jobKey);

    }

    /**
     * 立即执行job
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void runAJobNow(TaskJobForm scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    /**
     * 更新job时间表达式
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void updateJobCron(TaskJobForm scheduleJob) throws SchedulerException {

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());

        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        scheduler.rescheduleJob(triggerKey, trigger);
    }
}