package com.zw.platform.repository.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.AudioParam;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VideoSettingDao {
    /**
     * 根据车辆id查询4个表的主键，和是否有数据，以及是否下发过 
     * @param vehicleId
     * @return
     */
    Map<String, String> getIdsAndIsSettingByVehicleId(String vehicleId);

    /**
     * 查询4个表里设置过音视频参数的车辆id和车牌号 A
     */
    List<Map<String, String>> getVehicleIdAndBrand();

    /**
     * 查询4个表里设置过音视频参数的车辆id和车牌号 B
     */
    Set<String> getVehicleIdBySettingVideo();

    /**
     * 根据车辆id和通道号查询视频参数
     * @param vehicleId
     * @param logicChannel
     * @return
     */
    VideoSetting getVideoParamByVehicleIdAndLogicChannel(@Param("vehicleId") String vehicleId,
        @Param("logicChannel") Integer logicChannel);

    /**
     * 保存视频参数
     * @param videoSetting
     * @return
     */
    int saveVideoParam(VideoSetting videoSetting);

    /**
     * 修改视频参数
     * @param videoSetting
     * @return
     */
    int updateVideoParam(VideoSetting videoSetting);

    /**
     * 根据车辆id查询该车辆的视频参数列表
     * @param vehicleId
     * @return
     */
    List<VideoSetting> getVideoParamByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据车辆id删除该车辆的视频参数列表
     * @param vehicleId
     */
    void delete(@Param("vehicleId") String vehicleId);
    
    void deleteVideoParam(@Param("vehicleId") String vehicleId, @Param("logicChannel") Integer logicChannel);

    String getPhysicsChannel(@Param("vehicleId") String vehicleId, @Param("logicChannel") Integer logicChannel);

    /**
     * 根据车辆id查询4个表的主键，和是否有数据，以及是否下发过
     * @param vehicleIds
     * @return
     */
    List<VideoSetting> findIdsAndIsSettingByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);

    void deleteBatch(@Param("monitorIds") Set<String> monitorIds);

    /**
     * 获取音频参数
     * @param vehicleId
     * @return
     */
    AudioParam getVideoParam(@Param(value = "vehicleId") String vehicleId);

    Integer findAudioFormatByDeviceId(@Param("id") String id);
}