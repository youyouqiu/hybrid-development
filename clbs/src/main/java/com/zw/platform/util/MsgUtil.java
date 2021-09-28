package com.zw.platform.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.MsgDescExtend;
import com.zw.protocol.msg.VideoMessage;
import com.zw.protocol.msg.VideoMsgDesc;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.protocol.msg.t808.T808MsgHead;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.T809MsgBody;
import com.zw.protocol.msg.t809.T809MsgHead;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Created by LiaoYuecai on 2017/6/26.
 */
public class MsgUtil {
    public static Message getMsg(Integer msgID, String deviceId, Object data) {
        MsgDesc desc = new MsgDesc();
        desc.setMsgID(msgID);
        desc.setDeviceId(deviceId);
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }

    public static T809Message getT809Message(Integer msgID, String serverIp, Integer centerId, T809MsgBody body) {
        T809Message message = new T809Message();
        T809MsgHead head = new T809MsgHead();
        head.setMsgID(msgID);
        head.setServerIp(serverIp);
        head.setMsgGNSSCenterId(centerId);
        message.setMsgHead(head);
        message.setMsgBody(body);
        return message;
    }

    public static T808Message get808Message(String mobile, int msgId, Integer msgSN, T808MsgBody body) {
        T808MsgHead msgHead = new T808MsgHead();
        msgHead.setMobile(mobile);
        msgHead.setMsgID(msgId);
        msgHead.setMsgSN(msgSN);

        T808Message message = new T808Message();
        message.setMsgHead(msgHead);
        message.setMsgBody(body);
        return message;
    }

    /**
     * 808消息体
     * @param mobile         手机号
     * @param msgId          msgId
     * @param msgSN          流水号
     * @param body           消息体
     * @param vehicleInfoObj obj
     */
    public static T808Message get808Message(String mobile, int msgId, Integer msgSN, T808MsgBody body,
        Object vehicleInfoObj) {
        T808MsgHead msgHead = new T808MsgHead();
        msgHead.setMobile(mobile);
        msgHead.setMsgID(msgId);
        msgHead.setMsgSN(msgSN);
        setDeviceType(vehicleInfoObj, msgHead);

        T808Message message = new T808Message();
        message.setMsgHead(msgHead);
        message.setMsgBody(body);
        return message;
    }

    private static void setDeviceType(Object vehicleInfoObj, T808MsgHead msgHead) {
        if (Objects.nonNull(vehicleInfoObj)) {
            String deviceType = "0";
            if (vehicleInfoObj instanceof VehicleInfo) {
                VehicleInfo vehicleInfo = (VehicleInfo) vehicleInfoObj;
                deviceType = vehicleInfo.getDeviceType();
            } else if (vehicleInfoObj instanceof JSONObject) {
                JSONObject obj = (JSONObject) vehicleInfoObj;
                deviceType = String.valueOf(obj.get("deviceType"));
            } else if (vehicleInfoObj instanceof BindDTO) {
                BindDTO vehicleInfo = (BindDTO) vehicleInfoObj;
                deviceType = vehicleInfo.getDeviceType();
            } else {
                Map map = (Map) vehicleInfoObj;
                deviceType = String.valueOf(map.get("deviceType"));
            }
            if (StringUtils.isNotEmpty(deviceType)) {
                msgHead.setType(DeviceInfo.judgeProtocolType(Integer.valueOf(deviceType)));
            }
            msgHead.setDeviceType(deviceType);
        }
    }

    public static T808Message get808Message(String mobile, int msgId, Integer msgSN, Integer protocolType,
        T808MsgBody body, String deviceType) {
        T808MsgHead msgHead = new T808MsgHead();
        msgHead.setMobile(mobile);
        msgHead.setMsgID(msgId);
        msgHead.setMsgSN(msgSN);
        if (Objects.nonNull(protocolType)) {
            msgHead.setType(protocolType);
        }
        msgHead.setDeviceType(deviceType);

        T808Message message = new T808Message();
        message.setMsgHead(msgHead);
        message.setMsgBody(body);
        return message;
    }

    public static T808Message get808Message(String mobile, int msgId, Integer msgSN, T808MsgBody body,
        String deviceType) {
        T808MsgHead msgHead = new T808MsgHead();
        msgHead.setMobile(mobile);
        msgHead.setMsgID(msgId);
        msgHead.setMsgSN(msgSN);
        if (StringUtils.isNotEmpty(deviceType)) {
            msgHead.setType(DeviceInfo.judgeProtocolType(Integer.valueOf(deviceType)));
            msgHead.setDeviceType(deviceType);
        }

        T808Message message = new T808Message();
        message.setMsgHead(msgHead);
        message.setMsgBody(body);
        return message;
    }

    public static Message getMsg(Integer msgID, Object data) {
        MsgDesc desc = new MsgDesc();
        desc.setMsgID(msgID);
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }

    public static Message getMsg(Integer msgId, T809Message data, String plantId) {
        MsgDesc desc = new MsgDesc();
        desc.setMsgID(msgId);
        desc.setT809PlatId(plantId);
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }

    public static Message getMsg(Integer msgID, Object data, T809MsgHead head) {
        MsgDesc desc = new MsgDesc();
        desc.setSourceDataType(head.getMsgID());
        desc.setSourceMsgSn(head.getMsgSn());
        desc.setMsgID(msgID);
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }

    public static VideoMessage getVideoMsg(Integer msgID, Object data) {
        VideoMsgDesc desc = new VideoMsgDesc();
        desc.setMsgId(msgID);
        VideoMessage message = new VideoMessage();
        message.setMsgDesc(desc);
        message.setData(data);
        return message;
    }

    public static Message getMsg(VehicleInfo vehicleInfo, Integer msgID, String deviceId, Object data) {
        MsgDesc desc = new MsgDesc();
        desc.setMsgID(msgID);
        desc.setDeviceId(deviceId);
        desc.setMonitorId(vehicleInfo.getId());
        desc.setMonitorName(vehicleInfo.getBrand());
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }

    public static JSONObject objToJson(Object obj) {
        return JSON.parseObject(JSONObject.toJSONString(obj));
    }

    /**
     * 线路调整下发新的格式
     * @param msgID
     * @param data
     * @param vehicleInfo
     * @return
     */
    public static Message getMsg(Integer msgID, Object data, VehicleInfo vehicleInfo) {
        MsgDescExtend desc = new MsgDescExtend();
        desc.setMsgID(msgID);
        desc.setDeviceId(vehicleInfo.getDeviceId());
        //下发线路新增
        desc.setT809PlatId(vehicleInfo.getT809PlatId());
        desc.setMonitorName(vehicleInfo.getBrand());
        desc.setPlateColor(vehicleInfo.getPlateColor());
        Message message = new Message();
        message.setDesc(desc);
        message.setData(data);
        return message;
    }
}
