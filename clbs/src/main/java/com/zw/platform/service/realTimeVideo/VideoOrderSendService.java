package com.zw.platform.service.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.FileUploadControl;
import com.zw.platform.domain.realTimeVideo.SendFileUpload;
import com.zw.platform.domain.realTimeVideo.VideoControl;
import com.zw.platform.domain.realTimeVideo.VideoControlSend;
import com.zw.platform.domain.realTimeVideo.VideoPlaybackControl;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.ws.entity.t808.oil.T808_0x8900;

import java.util.Map;

/**
 * 实时视频指令下发
 * @author hujun
 * @version 创建时间：2018年1月3日 上午9:51:58
 */
public interface VideoOrderSendService {
    /**
     * 实时音视频传输请求下发（0x9101）
     * @param vo            实时音视频传输请求实体
     * @author hujun
     */
    int sendVideoRequest(VideoSendForm form, VideoRequest vo);

    /**
     * 实时音视频传输控制下发
     * @author hujun
     */
    void sendVideoControl(VideoSendForm form, VideoControl vc);

    void sendVideoControl(VideoSendForm form, VideoControlSend control, String deviceType);

    /**
     * 查询音视频属性
     * @author hujun
     */
    void sendAttributeQuery(VideoSendForm form, String deviceType);

    /**
     * 音视频参数设置（通道号参数、休眠唤醒参数）
     * @author hujun
     */
    void sendVideoParamSetting(VideoSendForm form, Map<String, Object> videoParams);

    /**
     * 休眠唤醒下发
     * @author hujun
     */
    void sendVideoSleep(VideoSendForm form, T808_0x8900<?> t8080x8900, String deviceType);

    String sendTalkBack(VideoSendForm form, VideoRequest vo, String riskNumber) throws Exception;

    void sendFileUpload(VideoSendForm form, SendFileUpload sendFileUpload, String deviceType, Integer msgSN);

    Integer sendFileUploadControl(VideoSendForm form, FileUploadControl fileUploadControl, String deviceType);

    void sendPlaybackControl(VideoSendForm form, VideoPlaybackControl control, String deviceType);
}
