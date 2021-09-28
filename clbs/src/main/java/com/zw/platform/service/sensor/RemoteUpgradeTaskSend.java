package com.zw.platform.service.sensor;

import com.zw.platform.domain.param.RemoteUpgradeTask;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;

/**
 * @author zhouzongbo on 2019/1/16 15:42
 */
public interface RemoteUpgradeTaskSend {

    /**
     * 发送数据到web端
     */
    void onSend(SimpMessagingTemplateUtil simpMessagingTemplateUtil, RemoteUpgradeTask remoteUpgradeTask);
}
