package com.zw.platform.service.sensor;

import com.zw.platform.domain.param.RemoteUpgradeTask;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.util.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhouzongbo on 2019/1/16 15:42
 */
public class RemoteUpgradeToWeb implements RemoteUpgradeTaskSend {

    private static final Logger logger = LoggerFactory.getLogger(RemoteUpgradeToWeb.class);
    /**
     * 当前所处阶段0: 平台往f3下发包; 1: 终端向外设下发;
     */
    private Integer currentStatus = 0;

    /**
     * 平台->f3:总包数
     */
    private Integer totalPackageSize;

    /**
     * 完成包数
     */
    private Integer successPackageSize;

    /**
     * 监控对象ID
     */
    private String monitorId;

    public RemoteUpgradeToWeb(Integer currentStatus, Integer totalPackageSize, Integer successPackageSize,
        String monitorId) {
        this.currentStatus = currentStatus;
        this.totalPackageSize = totalPackageSize;
        this.successPackageSize = successPackageSize;
        this.monitorId = monitorId;
    }

    @Override
    public void onSend(SimpMessagingTemplateUtil simpMessagingTemplateUtil, RemoteUpgradeTask remoteUpgradeTask) {
        /* 发送状态消息至web端 */
        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEB_SOCKET_REMOTE_UPGRADE_TYPE, remoteUpgradeTask);
        logger
            .info("REMOTE UPGRADE推送数据到web,总包数={},完成包数={},状态={},{}", totalPackageSize, successPackageSize, currentStatus,
                monitorId);
    }
}
