package com.zw.platform.task;

import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.Date8Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @Description: 执行删除历史数据
 * @Author zhangqiang
 * @Date 2020/5/28 13:54
 */
@Component
public class ExportTask {

    private static final long DELETE_TIME_THRESHOLD = 3L;

    private static final long UPDATE_TIME_THRESHOLD = 1L;

    private static final int FAILURE_STATE = 3;

    private Logger logger = LogManager.getLogger(ExportTask.class);
    @Autowired
    private OfflineExportService offlineExportService;

    @Autowired
    private FastDFSClient fastDFSClient;

    //每天执行一次删除操作和修改超过一天没有成功下载的任务状态
    @Scheduled(cron = "0 30 0 * * ?")
    public void execute() {
        try {
            String deleteTime = Date8Utils.getCurrentTime(LocalDateTime.now().minusDays(DELETE_TIME_THRESHOLD));
            Set<String> realPathSet = offlineExportService.selectExportRealPath(deleteTime);
            if (realPathSet != null && realPathSet.size() > 0) {
                realPathSet.forEach(realPath -> fastDFSClient.deleteFile(realPath));
            }
            offlineExportService.deleteOfflineExport(deleteTime);
            String updateTime = Date8Utils.getCurrentTime(LocalDateTime.now().minusDays(UPDATE_TIME_THRESHOLD));
            offlineExportService.updateExportStatus(updateTime, FAILURE_STATE);
        } catch (Exception e) {
            logger.error("执行定时删除离线导出报表数据异常", e);
        }

    }
}
