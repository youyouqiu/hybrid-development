package com.zw.platform.service.realTimeVideo;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.realTimeVideo.AudioParam;
import com.zw.platform.domain.realTimeVideo.AudioVideoRransmitForm;
import com.zw.platform.domain.realTimeVideo.DiskInfo;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.domain.realTimeVideo.VideoPlayResultDTO;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * 实时视频Service
 * @author hujun
 * @version 创建时间：2017年12月28日 下午4:00:04
 */
public interface VideoService {
    /**
     * 根据车辆id获取全部逻辑通道号并以树结构返回
     * @author hujun
     */
    JSONArray getChannelsByVehicleId(String vehicleId, boolean isChecked);

    /**
     * 根据多个车辆id获取全部逻辑通道号并以树结构返回
     * @author hujun
     * @Date 创建时间：2018年3月5日 下午2:11:42
     */
    JSONArray getChannelsByVehicleIds(String vehicleId, boolean isChecked);

    /**
     * 根据指令类型下发不同参数
     * @param form 视频指令下发参数实体
     * @author hujun
     */
    void sendParam(VideoSendForm form, String ipAddress, String equipmentType) throws Exception;

    /**
     * 下发消息ID:0x9102(实时视频,视频窗generalSubInfo口右键菜单控制视频实时传输的指令下发)
     */
    void sendRealTimeControl(AudioVideoRransmitForm form, String ipAddress) throws Exception;

    /**
     * 根据车id获取音频参数
     */
    AudioParam getAudioParam(String vehicleId);

    DiskInfo getDiskInfo();

    String saveToRedis(String vehicleId, String channelNumber);

    String saveToRedis(String vehicleId, String simcardNumber, String channelNumber,
                       String audioFormatStr, String deviceType);

    String getAudioFormat(Integer format);

    boolean saveMedia(MediaForm media, MultipartFile file);

    /**
     * 下发920F获取终端月视频资源
     * @param resourceListBean 下发信息
     * @param sessionId        websocket sessionId
     * @param userName         userName
     * @param ip               ip
     * @return JsonResultBean
     */
    JsonResultBean sendGetHistoryMonthInstruct(ResourceListBean resourceListBean, String sessionId, String userName,
        String ip);

    /**
     * 下发9205获取终端单天视频资源
     * @param resourceListBean 下发信息
     * @param sessionId        websocket sessionId
     * @param userName         userName
     * @param ip               ip
     * @return JsonResultBean
     */
    JsonResultBean sendGetHistoryDayInstruct(ResourceListBean resourceListBean, String sessionId, String userName,
        String ip);

    void setBasicsParam9101(VideoRequest vo, Integer audioTcpPort);

    /**
     * 设置监控对象基础信息
     * @author hujun
     * @since 创建时间：2018年1月4日 上午9:23:45
     */
    void setMonitorInfo(BindDTO bindDTO, VideoSendForm videoSendForm);

    /**
     * 保存视频巡检记录
     * @param videoPlayResultDTO 视频播放结果
     * @return JsonResultBean
     */
    JsonResultBean saveVideoInspectionRecord(VideoPlayResultDTO videoPlayResultDTO);

    /**
     * 保存视频抽查统计记录
     * @param vehicleId 车辆id
     * @return JsonResultBean
     */
    JsonResultBean saveVideoSpotCheckRecord(String vehicleId) throws Exception;
}
