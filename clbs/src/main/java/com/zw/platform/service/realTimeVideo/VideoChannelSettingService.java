package com.zw.platform.service.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午3:23:03
* 类说明:
*/
public interface VideoChannelSettingService {
    /**
     * 根据车辆id查询视频通道列表
     * @return
     * @throws Exception
     */
    JsonResultBean getVideoChannel(String vehicleId) throws Exception;

    /**
     * 根据车辆id查询视频通道列表
     * @return
     * @throws Exception
     */
    List<VideoChannelSetting> getVideoResourceChannel(String vehicleId) throws Exception;

    /**
     * 根据车辆id和物理通道号删除音视频通道
     * @param vehicleId
     * @param physicsChannel
     * @return
     * @throws Exception
     */
    void deleteVideoChannelSetting(String vehicleId, Integer physicsChannel) throws Exception;

}
