package com.zw.platform.repository.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface VideoSleepSettingDao {
    /**
     * 根据车辆id查询视频休眠参数
     * @param vehicleId
     * @return
     */
    VideoSleepSetting getVideoSleepByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 修改视频休眠参数
     * @param videoSleep
     * @return
     */
    int updateVideoSleep(VideoSleepSetting videoSleep);

    /**
     * 添加视频休眠参数
     * @param videoSleep
     * @return
     */
    int saveVideoSleep(VideoSleepSetting videoSleep);

    /**
     * 删除视频休眠参数
     * @param vehicleId
     */
    void delete(@Param("vehicleId") String vehicleId);

    void deleteBatch(@Param("monitorIds") Set<String> monitorIds);
}