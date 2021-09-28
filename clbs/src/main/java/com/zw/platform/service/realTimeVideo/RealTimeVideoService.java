package com.zw.platform.service.realTimeVideo;

import com.zw.lkyw.domain.SendMsgBasicInfo;
import com.zw.lkyw.domain.SendMsgMonitorInfo;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/**
 * @author Chen Feng
 * @version 1.0 2018/12/12
 */
public interface RealTimeVideoService {
    /**
     * 批量下发文本信息
     */
    JsonResultBean sendTextByBatch(SendTxt sendTxt, List<String> vehicleIdList, String ipAddress);

    /**
     * 批量下发tts读播
     */
    JsonResultBean sendTtsByBatch(String sendTextContent, String vehicleIds, String ipAddress);

    /**
     * 获得下发文本信息状态列表
     */
    JsonResultBean getSendTextStatusList(String vehicleIds) throws Exception;

    /**
     * 获得io信息
     */
    JsonResultBean getIoSignalInfo(String monitorId, String type);

    SendMsgBasicInfo getBasicInfo(SendTxt txt, Integer swiftNumber);

    Map<String, SendMsgMonitorInfo> assblemSendMsgMonitorInfo(List<String> singletonList);

    /**
     * 获取音视频参数
     * @param monitorId 监控对象id
     * @return JsonResultBean
     */
    JsonResultBean getAudioAndVideoParameters(String monitorId);
}
