package com.zw.platform.service.multimedia.impl;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.MultimediaRetrieval;
import com.zw.platform.domain.multimedia.MultimediaUpload;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.multimedia.Record;
import com.zw.platform.domain.param.TelBack;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.springframework.stereotype.Service;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
@Service
public class MultimediaServiceImpl implements MultimediaService {
    @Override
    public void photograph(String deviceId, Photograph photograph, String simcardNumber, Integer serialNumber,
        BindDTO vehicleInfo) {
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_MULTIMEDIA_DATA);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_PHOTOGRAPH, serialNumber, photograph, vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PHOTOGRAPH, deviceId);
    }

    @Override
    public void record(String deviceId, Record record, String simcardNumber, Integer serialNumber, String deviceType) {
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_MULTIMEDIA_DATA);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_RECORD_COMMAND, serialNumber, record, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_RECORD_COMMAND, deviceId);
    }

    @Override
    public void telListen(String deviceId, TelBack telBack, String simcardNumber, Integer serialNumber,
        String deviceType) {
        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_CALL_BACK, serialNumber, telBack, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_CALL_BACK, deviceId);
    }

    @Override
    public void multimediaRetrieval(String deviceId, MultimediaRetrieval multimediaRetrieval, String simcardNumber,
        Integer serialNumber, String deviceType) {
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_MULTIMEDIA_SEARCH_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(simcardNumber, ConstantUtil.T808_MULTIMEDIA_SEARCH, serialNumber, multimediaRetrieval,
                deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_MULTIMEDIA_SEARCH, deviceId);
    }

    @Override
    public void multimediaUpload(MultimediaUpload multimediaUpload, String deviceId, String simcardNumber,
        Integer serialNumber, String deviceType) {
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_MULTIMEDIA_DATA);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(simcardNumber, ConstantUtil.T808_MULTIMEDIA_UPLOAD, serialNumber, multimediaUpload,
                deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_MULTIMEDIA_UPLOAD, deviceId);
    }
}
