package com.zw.platform.push.controller;

import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>
 * Title:订阅状态处理
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 */
public class SubscibeInfoCache {

    public static Map<String, Boolean> adasVehicleDirFlagMap = new HashMap<>();

    private static SubscibeInfoCache subscibeInfoCache;

    private static ConcurrentHashMap<String, FileUploadForm> subscibeMsgMap = new ConcurrentHashMap<>();

    public static synchronized  SubscibeInfoCache getInstance() {
        if (subscibeInfoCache == null) {
            subscibeInfoCache = new SubscibeInfoCache();
        }
        return subscibeInfoCache;
    }

    public SubscibeInfo getUserNameByMsgSnDid(int msgSn, String deviceId) {
        if (SubcibeTable.containsKey(msgSn + "&" + deviceId)) {
            return (SubscibeInfo) SubcibeTable.get(msgSn + "&" + deviceId);
        }
        return null;
    }

    public SubscibeInfo getUserNameByMsgSnDid(String msgSn, String deviceId) {
        if (SubcibeTable.containsKey(msgSn + "&" + deviceId)) {
            return (SubscibeInfo) SubcibeTable.get(msgSn + "&" + deviceId);
        }
        return null;
    }

    public void putTable(SubscibeInfo info, Long timeSecond) {
        if (info != null && StringUtils.isNotBlank(info.getDeviceid())) {
            String key = info.getMsgSn() + "&" + info.getDeviceid();
            if (!SubcibeTable.containsKey(key)) {
                if (timeSecond != null) {
                    SubcibeTable.put(key, info, timeSecond);
                } else {
                    SubcibeTable.put(key, info);
                }
            }
            //订阅
            WebSubscribeManager.getInstance().subscribeAck(info.getDeviceid(), info.getRespMsgId());
            //存储订阅消息总接收数量
            String countkey = info.getRespMsgId() + "_" + info.getDeviceid();
            SubscibeInfo temp = new SubscibeInfo(info.getDeviceid(), info.getRespMsgId());
            int count = 1;
            if (SubcibeTable.containsKey(countkey) && SubcibeTable.get(countkey) != null) {
                info = (SubscibeInfo) SubcibeTable.get(countkey);
                count = info == null ? 1 : info.getCount() + 1;
            }
            temp.setCount(count);
            SubcibeTable.put(countkey, temp, -1L);
        }
    }

    public void putTable(SubscibeInfo info) {
        putTable(info, null);
    }

    public void delTable(SubscibeInfo info) {
        String key = info.getMsgSn() + "&" + info.getDeviceid();
        if (SubcibeTable.containsKey(key)) {
            SubscibeInfo subscibeInfo = (SubscibeInfo) SubcibeTable.get(key);
            if (subscibeInfo == null || !Objects.equals(subscibeInfo.getType(), 1)) {
                SubcibeTable.remove(key);
            }
        }
        //存储订阅消息总接收数量
        Integer respMsgId = info.getRespMsgId();
        String deviceId = info.getDeviceid();
        key = respMsgId + "_" + deviceId;
        SubscibeInfo temp = new SubscibeInfo(info.getDeviceid(), info.getRespMsgId());
        int count = 0;
        if (SubcibeTable.containsKey(key)  && SubcibeTable.get(key) != null) {
            info = (SubscibeInfo) SubcibeTable.get(key);
            count = info == null ? 0 : info.getCount() - 1;
        }
        temp.setCount(count);
        if (count > 0) {
            SubcibeTable.put(key, temp);
        } else {
            SubcibeTable.remove(key);
            WebSubscribeManager.getInstance().canSubscribeAck(deviceId, respMsgId);
        }
    }

    public void delTable(int msgSn, String deviceId) {
        SubscibeInfo subscibeInfo = getUserNameByMsgSnDid(msgSn, deviceId);
        if (subscibeInfo != null) {
            delTable(subscibeInfo);
        }
    }

    /**
     *   0900的订阅删除，如果用上面那个会删不掉
     **/
    public void delTable0900(SubscibeInfo info) {
        String key = info.getMsgSn() + "&" + info.getDeviceid();
        if (SubcibeTable.containsKey(key)) {
            SubscibeInfo subscibeInfo = (SubscibeInfo) SubcibeTable.get(key);
            if (subscibeInfo == null) {
                SubcibeTable.remove(key);
            }
        }
        //存储订阅消息总接收数量
        Integer respMsgId = info.getRespMsgId();
        String deviceId = info.getDeviceid();
        key = respMsgId + "_" + deviceId;
        SubscibeInfo temp = new SubscibeInfo(info.getDeviceid(), info.getRespMsgId());
        int count = 0;
        if (SubcibeTable.containsKey(key)  && SubcibeTable.get(key) != null) {
            info = (SubscibeInfo) SubcibeTable.get(key);
            count = info == null ? 0 : info.getCount() - 1;
        }
        temp.setCount(count);
        if (count > 0) {
            SubcibeTable.put(key, temp);
        } else {
            SubcibeTable.remove(key);
            WebSubscribeManager.getInstance().canSubscribeAck(deviceId, respMsgId);
        }
    }

    public void pushSubscibeMsgMap(Integer msgSN, String deviceId, FileUploadForm info) {
        if (msgSN != null && StringUtils.isNotBlank(deviceId)
                && info != null) {
            subscibeMsgMap.put(msgSN + "_" + deviceId + "filesize", info);
        }
    }

    public FileUploadForm getSubscibeMsgMap(Integer msgSN, String deviceId) {
        if (msgSN != null && StringUtils.isNotBlank(deviceId)) {
            String key = msgSN + "_" + deviceId + "filesize";
            if (subscibeMsgMap.containsKey(key)) {
                return subscibeMsgMap.get(key);
            }
        }
        return null;
    }

    public void removeSubscibeMsgMap(Integer msgSN, String deviceId) {
        if (msgSN != null && StringUtils.isNotBlank(deviceId)) {
            String key = msgSN + "_" + deviceId + "filesize";
            subscibeMsgMap.remove(key);
        }
    }

}
