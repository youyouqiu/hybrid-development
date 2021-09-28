package com.zw.platform.domain.leaderboard;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class OrgEvent implements Serializable {

    private long time;//（时间）

    private int smoke;//风险事件抽烟

    private int phone;//风险时间接打电话

    private int leftOffset;//风险事件车道左偏移

    private int rightOffset;//风险事件车道右偏移

    private int changeLanes;//风险事件频繁变道

    private int eyeClose;//风险事件闭眼

    private int yawning;//风险事件打哈欠

    private int distance;//风险事件车距过近

    private int pedestrianCollisions;//风险事件行人碰撞

    private int accelerate;//风险事件急加速

    private int slowDown;//风险事件急减速

    private int turn;//风险事件急转弯

    private int obstacles;//风险事件障碍物

    private int abnormalPosture;//风险事件长时间不目视前方

    private int inconformityCertificate;//风险事件人证不符

    private int noDriverDetected;//风险事件驾驶员不在驾驶位置

    private int vehicleCrash;//驾驶员不在驾驶位置

    private int roadMarkTransfinite;//道路标识超限

    private int keepOut; //红外遮挡

    private int infraredBlocking;//红外阻断

    public List<Map<String, String>> getOrgEventList() {
        List<Map<String, String>> result = new ArrayList<>();
        result.add(getEventMap(Event.SMOKE, smoke));
        result.add(getEventMap(Event.PHONE, phone));
        result.add(getEventMap(Event.LEFT_OFFSET, leftOffset));
        result.add(getEventMap(Event.RIGHT_OFFSET, rightOffset));
        result.add(getEventMap(Event.CHANGE_LANES, changeLanes));
        result.add(getEventMap(Event.EYE_CLOSE, eyeClose));
        result.add(getEventMap(Event.YAWNING, yawning));
        result.add(getEventMap(Event.DISTANCE, distance));
        result.add(getEventMap(Event.ACCELERATE, accelerate));
        result.add(getEventMap(Event.SLOW_DOWN, slowDown));
        result.add(getEventMap(Event.TURN, turn));
        result.add(getEventMap(Event.ABNORMAL_POSTURE, abnormalPosture));
        result.add(getEventMap(Event.INCONFORMITY_CERTIFICATE, inconformityCertificate));
        result.add(getEventMap(Event.NO_DRIVER_DETECTED, noDriverDetected));
        result.add(getEventMap(Event.VEHICLE_CRASH, vehicleCrash));
        result.add(getEventMap(Event.ROAD_MARK_TRANSFINITE, roadMarkTransfinite));
        result.add(getEventMap(Event.PEDESTRIAN_COLLISIONS, pedestrianCollisions));
        result.add(getEventMap(Event.KEEP_OUT, keepOut));
        result.add(getEventMap(Event.INFRARED_BLOCKING, infraredBlocking));
        result.sort((o1, o2) -> new Integer(o2.get("value")).compareTo(new Integer(o1.get("value"))));
        return result;

    }

    private Map<String, String> getEventMap(Event event, int value) {
        Map<String, String> data = new HashMap<>();
        data.put("name", event.getEventName());
        data.put("value", value + "");
        return data;
    }

}
