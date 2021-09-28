package com.zw.platform.task;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.repository.vas.RiskEventConfigDao;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.spring.InitData;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 定时保存辆实时位置信息到redis
 * 2018/8/1 15:55
 *
 * @author zhangsq
 */
@Component
public class AdasGuideTask {
    private static final Logger logger = LogManager.getLogger(AdasGuideTask.class);

    @Autowired
    private InitData initData;

    @Autowired
    private RiskEventConfigDao riskEventConfigDao;

    @Value("${ftp.path}")
    private String ftpPath;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void execute() {
        try {
            logger.info("----------------定时保存辆实时位置信息到redis开始----------------");
            initData.setVehiclePositionalInfo2Redis();
            logger.info("----------------定时保存辆实时位置信息到redis结束----------------");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("定时保存辆实时位置信息到redis异常", e);
        }

    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void executes() {
        try {
            initData.getPositionalInfos();
        } catch (Exception e) {
            logger.error("定时把redis最后一条位置信息更新到内存异常", e);
        }
    }

    /**
     * 每天晚上10点执行创建目录
     */
    @Scheduled(cron = "0 0 22 * * ? ")
    public void createFtpDirectory() {
        logger.info("----------------定时创建多媒体证据Ftp路径开始----------------");
        createFtpMediaDirectory(true);
    }

    private String assembleFilePath(String vehicleId) {
        // 组装文件路径(ADAS/车id前两位/完整车id/年月/)
        return ftpPath + "/" + vehicleId.substring(0, 2) + "/" + vehicleId + "/" + getCurrentDay() + "/";
    }

    public static String getCurrentDay() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String assembleTodayFilePath(String vehicleId) {
        // 组装文件路径(ADAS/车id前两位/完整车id/年月/)
        return ftpPath + "/" + vehicleId.substring(0, 2) + "/" + vehicleId + "/" + getNowCurrentDay() + "/";
    }

    public static String getNowCurrentDay() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public void createFtpMediaDirectory(boolean isScheduled) {
        FTPClient ftp = null;
        long start = System.currentTimeMillis();
        try {
            List<String> vehicleIds = riskEventConfigDao.findAllRiskConfig();
            ftp = FtpClientUtil.getFTPClient(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, ftpPath);
            String mediaPath;
            for (String vehicleId : vehicleIds) {
                mediaPath = isScheduled ? assembleFilePath(vehicleId) : assembleTodayFilePath(vehicleId);
                // 创建目录
                boolean flag = FtpClientUtil.createDir(mediaPath, ftp);
                if (!flag) {
                    logger.info("----------------创建多媒体证据Ftp路径失败----------------");
                }
            }
            logger.info("----------------创建多媒体证据Ftp路径结束消耗时间为：" + (System.currentTimeMillis() - start) / 1000 + "秒");
            //更新标记
            updateMark(isScheduled);
        } catch (Exception e) {
            logger.error("创建多媒体证据Ftp路径异常", e);
        } finally {
            FtpClientUtil.closeFtpConnect(ftp);
        }
    }

    private void updateMark(boolean isScheduled) {
        String time;
        time = isScheduled ? getCurrentDay() : getNowCurrentDay();
        SubscibeInfoCache.adasVehicleDirFlagMap.clear();
        SubscibeInfoCache.adasVehicleDirFlagMap.put(time, true);
        RedisHelper.setString(HistoryRedisKeyEnum.ADAS_VEHICLE_MEDIADIR_FLAG.of(time), "1", 24 * 60 * 60);
    }

}
