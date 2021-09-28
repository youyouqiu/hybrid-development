package com.zw.app.service.videoResource;

import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;

import java.util.List;

/**
 * app视频回放service
 */
public interface AppVideoResourceService {

    /**
     * 根据车辆id查询视频通道列表
     */
    List<VideoChannelSetting> getVideoResourceChannel(String vehicleId);

    /**
     * 获取音视频参数异常
     * @param monitorId 监控对象id
     * @return AppResultBean
     */
    AppResultBean getAudioAndVideoParameters(String monitorId);
}
