package com.zw.platform.util;

import com.google.common.collect.Lists;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.domain.systems.form.DataCleanSettingForm;
import com.zw.platform.repository.modules.DataCleanDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * 数据清理功能
 *
 * 2020/10/27
 * @author denghuabing
 * @version V1.0
 **/
@Component
public class DataCleanUtil {

    private final Logger log = LogManager.getLogger(DataCleanUtil.class);

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private DataCleanDao dataCleanDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    public static Map<String, ScheduledFuture<?>> taskMap = new HashMap<>();

    private static final Integer BATCH_NUM = 1000;

    // @PostConstruct
    public void start() {
        DataCleanTaskThread tt = new DataCleanTaskThread();
        DataCleanSettingForm form = dataCleanDao.get();
        if (form == null) {
            return;
        }
        if (StringUtils.isNotBlank(form.getCleanType()) && StringUtils.isNotBlank(form.getTime())) {
            String time = form.getTime();
            String cron = getCron(time);
            ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(tt, new CronTrigger(cron));
            taskMap.put("task", future);
        }
    }

    public void changeTask(String time) {
        stop();
        DataCleanTaskThread tt = new DataCleanTaskThread();
        String cron = getCron(time);
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(tt, new CronTrigger(cron));
        taskMap.put("task", future);
    }

    public void stop() {
        ScheduledFuture<?> future = taskMap.get("task");
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * 转换时间规则
     */
    private String getCron(String time) {
        String[] split = time.split(":");
        return "0 " + split[1] + " " + split[0] + " * * ?";
    }

    private void cleanPositional(Integer month) {
        //暂时未实现
    }

    private void cleanAlarm(Integer month) {
        //暂时未实现
    }

    private void cleanMedia(Integer month) {
        String overtime = getOvertime(month);
        // 怕造成锁表  先查出id  在按id删除
        List<Map<String, String>> medias = dataCleanDao.getMedia(overtime);
        if (CollectionUtils.isNotEmpty(medias)) {
            List<String> ids = new ArrayList<>();
            List<String> urls = new ArrayList<>();
            medias.forEach(o -> {
                ids.add(o.get("id"));
                if (StringUtils.isNotBlank(o.get("mediaUrlNew"))) {
                    urls.add(o.get("mediaUrlNew"));
                }
            });
            List<List<String>> partitionIds = Lists.partition(ids, BATCH_NUM);
            partitionIds.forEach(o -> dataCleanDao.deleteMedia(o));
            if (CollectionUtils.isNotEmpty(urls)) {
                urls.forEach(o -> fastDFSClient.deleteFile(o));
            }
        }
    }

    private void cleanSpotCheck(Integer month) {
        String overtime = getOvertime(month);
        // 怕造成锁表  先查出id  在按id删除
        List<String> spotCheckIds = dataCleanDao.getSpotCheckIds(overtime);
        if (CollectionUtils.isNotEmpty(spotCheckIds)) {
            List<List<String>> partition = Lists.partition(spotCheckIds, BATCH_NUM);
            partition.forEach(o -> dataCleanDao.deleteSpotCheck(o));
        }
    }

    /**
     * 计算超时时间
     */
    private String getOvertime(Integer month) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusMonths(month);
        return DateUtil.YMD_HMS.format(localDateTime).orElseThrow(IllegalArgumentException::new);
    }

    class DataCleanTaskThread implements Runnable {

        @Override
        public void run() {
            log.info("定时清理数据：" + LocalDateTime.now());
            DataCleanSettingForm form = dataCleanDao.get();
            List<String> split = Arrays.asList(form.getCleanType().split(","));
            if (split.contains(DataCleanSettingForm.POSITIONAL_SETTING)) {
                cleanPositional(form.getMediaTime());
            }
            if (split.contains(DataCleanSettingForm.ALARM_SETTING)) {
                cleanAlarm(form.getMediaTime());
            }
            if (split.contains(DataCleanSettingForm.MEDIA_SETTING)) {
                cleanMedia(form.getMediaTime());
            }
            if (split.contains(DataCleanSettingForm.SPOT_CHECK_SETTING)) {
                cleanSpotCheck(form.getSpotCheckTime());
            }
        }
    }
}
