package com.zw.platform.service.monitoring;

import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.util.common.JsonResultBean;

public interface ForwardVideoService {
    String getForwardedMonitorId(String plateNumber);

    boolean anonymousLogin();

    JsonResultBean sendUploadOrder(String ip, FileUploadForm form) throws Exception;

    /**
     * 获取音视频参数
     * @param monitorId 监控对象id
     * @return JsonResultBean
     */
    JsonResultBean getAudioAndVideoParameters(String monitorId);
}
