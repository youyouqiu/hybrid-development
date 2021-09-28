package com.zw.platform.repository.realTimeVideo;

import com.zw.lkyw.domain.VideoInspectionData;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface VideoChannelSettingDao {
    /**
     * 根据车辆id查询视频通道列表
     * @param vehicleId
     * @return
     */
    List<VideoChannelSetting> getVideoChannelByVehicleId(String vehicleId);

    /**
     * 根据车辆id查询视频通道列表
     * @param vehicleId
     * @return
     */
    List<VideoChannelSetting> getAppVideoChannel(String vehicleId);

    /**
     * 根据车辆id集合查询视频通道列表
     * @param vehicleIds
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月5日 下午1:59:04
     */
    List<VideoChannelSetting> getVideoChannelByVehicleIds(@Param("list") Collection<String> vehicleIds);

    /**
     * 根据车辆id和通道号查询视频通道
     * @param vehicleId
     * @return
     */
    VideoChannelSetting getVideoChannelByVehicleIdAndChannelNo(@Param("vehicleId") String vehicleId,
        @Param("channelNo") Integer channelNo);

    /**
     * 获取车辆对应逻辑通道号视频参数设置
     * @param vehicleId 监控对象id
     * @param logicChannel 逻辑通道号
     * @return VideoChannelSetting
     */
    VideoChannelSetting getVideoChannelByVehicleIdAndLogicChannel(@Param("vehicleId") String vehicleId,
        @Param("logicChannel") Integer logicChannel);

    /**
     * 修改视频通道
     * @param videoChannel
     * @return
     */
    int updateVideoChannel(VideoChannelSetting videoChannel);

    /**
     * 根据车辆id和物理通道号删除音视频通道
     * @param vehicleId
     * @param physicsChannel
     */
    void deleteVideoChannelSetting(@Param("vehicleId") String vehicleId,
        @Param("physicsChannel") Integer physicsChannel);

    /**
     * 根据车辆id和逻辑通道号删除音视频通道
     * @param vehicleId
     * @param logicChannel
     */
    void deleteVideoChannelSettingByLogicChannel(@Param("vehicleId") String vehicleId,
        @Param("logicChannel") Integer logicChannel);

    /**
     * 根据车辆id删除音视频通道
     * @param vehicleId
     */
    void delete(@Param("vehicleId") String vehicleId);

    /**
     * 批量删除
     * @param monitorIds
     */
    void deleteBatch(@Param("monitorIds") Set<String> monitorIds);

    @ImportDaoLock(ImportTable.ZW_M_VIDEO_CHANNEL_SETTING)
    boolean addVideoChannels(@Param("videoChannelSettingList") Collection<VideoChannelSetting> videoChannelSettingList);

    /**
     * 批量删除监控对象
     * @param monitorIds monitorIds
     */
    void deleteMoreByMonitorIds(@Param("monitorIds") Set<String> monitorIds);

    /**
     * 根据车id和逻辑通道号  查询物理通道号
     * @param vid
     * @param logicChannel
     * @return
     */
    VideoInspectionData getVehicleInfoAndPhysicsChannel(@Param("vid") String vid,
        @Param("logicChannel") Integer logicChannel);
}