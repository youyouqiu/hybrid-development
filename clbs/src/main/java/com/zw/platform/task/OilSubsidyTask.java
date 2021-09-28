package com.zw.platform.task;

import com.zw.platform.service.oilsubsidy.LineManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author wanxing
 * @Title: 油补功能定时任务
 * @date 2020/10/1215:44
 */
@Component
public class OilSubsidyTask {

    @Autowired
    private LineManageService lineManageService;

    /**
     * 每天晚上2点
     */
    @Scheduled(cron = "${oil.subsidy.line.manage.1301.job}")
    public void upData1301Command() {
        lineManageService.upData1301Command();
    }

    /**
     * 每天晚上3点
     */
    @Scheduled(cron = "${oil.subsidy.line.manage.1302.job}")
    public void upData1302Command() {
        lineManageService.upData1302Command();
    }

}
