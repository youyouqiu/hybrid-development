package com.zw.platform.service.realTimeVideo;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.realTimeVideo.VideoSpecialSetting;

/**
 * @author zhangsq
 * @date 2018/3/22 17:23
 */
public interface VideoSpecialSettingService {

    void saveVideoSpecialSetting(VideoSpecialSetting videoSpecialSetting, String ipAddress) throws Exception;

    JSONObject getOne();

}
