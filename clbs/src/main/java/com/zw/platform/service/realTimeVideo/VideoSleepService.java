package com.zw.platform.service.realTimeVideo;


import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import com.zw.platform.util.common.JsonResultBean;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午4:11:02
* 类说明:
*/
public interface VideoSleepService {
    /**
     * 根据车辆id查询视频休眠参数
     * @return
     * @throws Exception
     */
    JsonResultBean getVideoSleep(String vehicleId) throws Exception;

    /**
     * 添加或修改视频休眠参数
     * @return
     * @throws Exception
     */
    void saveVideoSleep(VideoSleepSetting videoSleep) throws Exception;
}
