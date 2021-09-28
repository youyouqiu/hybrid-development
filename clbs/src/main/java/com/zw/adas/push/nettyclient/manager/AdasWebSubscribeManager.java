package com.zw.adas.push.nettyclient.manager;

import com.alibaba.fastjson.JSON;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.MsgUtil;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by lijie on 2019/6/11.
 */
@Log4j2
public enum AdasWebSubscribeManager {
    INSTANCE;

    private Map<String, Channel> channelMap;

    AdasWebSubscribeManager() {
        channelMap = new ConcurrentHashMap<>();
    }

    public static AdasWebSubscribeManager getInstance() {
        return AdasWebSubscribeManager.INSTANCE;
    }


    private Set<String> addPrefix(String prefix, Set<String> deviceIds) {
        Set<String> devices = new HashSet<>(deviceIds.size());
        for (String deviceId : deviceIds) {
            devices.add(prefix + deviceId);
        }
        return devices;
    }


    /**
     * 广播消息
     */
    public void sendMsgToAll(Object data, Integer msgId) {
        log.info("下发消息, ID:{}, 内容:{}", String.format("0x%04X", msgId), JSON.toJSONString(data));
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            channel.writeAndFlush(MsgUtil.getMsg(msgId, data));
        }
    }

    /**
     * 广播消息
     */
    public void sendMsgToAll(Object data, Integer msgId, String deviceId) {
        log.info("下发消息, ID:{}, 内容:{}", String.format("0x%04X", msgId), JSON.toJSONString(data));
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            channel.writeAndFlush(MsgUtil.getMsg(msgId, deviceId, data));
        }
    }

    /**
     * 下发围栏线路调整
     */
    public void sendMsgToAll(Object data, Integer msgId, VehicleInfo vehicleInfo) {
        log.info("下发消息, ID:{}, 内容:{}", String.format("0x%04X", msgId), JSON.toJSONString(data));
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            channel.writeAndFlush(MsgUtil.getMsg(msgId, data, vehicleInfo));
        }
    }

    public void putChannel(String key, Channel channel) {
        channelMap.put(key, channel);
    }

    public void removeChannel(String key) {
        channelMap.remove(key);
    }

    public Channel getChannel(String key) {
        return channelMap.get(key);
    }

    public void remove(Channel channel) {
        for (Map.Entry entry : channelMap.entrySet()) {
            if (entry.getValue() == channel) {
                channelMap.remove(entry.getKey());
            }
        }
    }


}
