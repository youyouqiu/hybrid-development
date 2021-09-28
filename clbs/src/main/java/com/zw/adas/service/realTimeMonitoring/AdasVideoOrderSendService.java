package com.zw.adas.service.realTimeMonitoring;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.realTimeVideo.VideoControlSend;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;

/**
 * 实时视频指令下发
 * @author lijie
 * @version 创建时间：2019年6月10日 上午9:51:58
 */
public interface AdasVideoOrderSendService {

    /**
     * 实时监控传输请求下发（0x9101）对讲
     * @param form 下发对象信息
     * @param vo   实时音视频传输请求实体
     * @author lijie
     */
    String sendTalkBack(VideoSendForm form, VideoRequest vo, String riskNumber) throws Exception;

    boolean sendVideoControl(String vehicleId, VideoControlSend vc, BindDTO bindDTO, String talkStartTime,
        String warningTime, String riskId);
}
