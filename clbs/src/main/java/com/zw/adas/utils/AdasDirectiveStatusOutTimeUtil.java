package com.zw.adas.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.zw.adas.service.defineSetting.AdasParamSettingService;
import com.zw.lkyw.domain.VideoInspectionData;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AdasDirectiveStatusOutTimeUtil {

    /**
     * 超时时间设置（默认60秒）
     */
    private static final long outTime = 60L;

    public static Cache<String, Set<String>> directiveStatusOutTimeCache;

    /**
     * 视频巡检功能缓存（下发后监听是否应答0001）
     */
    private static Cache<String, VideoInspectionData> videoInspectionCache;

    @Autowired
    private AdasParamSettingService adasParamSettingService;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;


    private static   final int refreshTime = 5;

    private HashedWheelTimer timer;

    @PostConstruct
    private void init() {
        directiveStatusOutTimeCache = Caffeine.newBuilder()
            .expireAfterAccess(outTime, TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> updateDirectiveStatus((String) key, (Set<String>) value)))
            .build();

        videoInspectionCache = Caffeine.newBuilder()
            .expireAfterWrite(outTime, TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> videoInspectionHandler(cause, (VideoInspectionData) value)))
            .build();
        timer = new HashedWheelTimer(1L, TimeUnit.SECONDS);
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                directiveStatusOutTimeCache.cleanUp();
                videoInspectionCache.cleanUp();
                //结束时候再次注册
                timer.newTimeout(this, refreshTime, TimeUnit.SECONDS);
            }
        };
        timer.newTimeout(task, refreshTime, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void close() {
        this.timer.stop();
    }

    private void updateDirectiveStatus(String userName, Set<String> directiveIdSet) {
        adasParamSettingService.updateDirectiveStatus(directiveIdSet);
        simpMessagingTemplateUtil.sendStatusMsg(userName, "/topic/active_security", "");
    }

    public void putVideoInspectionCache(String key, VideoInspectionData data) {
        videoInspectionCache.put(key, data);
    }

    /**
     * 视频巡检超时处理
     */
    public void videoInspectionHandler(RemovalCause cause, VideoInspectionData data) {
        if (Objects.nonNull(cause) && "EXPLICIT".equals(cause.name())) {
            return;
        }
        if (Objects.nonNull(cause) && "EXPIRED".equals(cause.name())) {
            data.setFailReason(VideoInspectionData.OVER_TIME_MSG);
            data.setStatus(1);
        }
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorId", data.getMonitorId());
        queryParam.put("monitorName", data.getMonitorName());
        queryParam.put("signColor", data.getPlateColor());
        queryParam.put("objectType", data.getObjectType());
        queryParam.put("groupName", data.getGroupName());
        queryParam.put("channelNum", data.getChannelNum().toString());
        queryParam.put("startTime", data.getStartTime());
        queryParam.put("status", data.getStatus().toString());
        queryParam.put("failReason", Objects.isNull(data.getFailReason()) ? null : data.getFailReason().toString());
        HttpClientUtil.send(PaasCloudUrlEnum.SAVE_VIDEO_INSPECTION_URL, queryParam);
    }

    /**
     * 移除
     */
    public static void removeVideoInspection(String key) {
        videoInspectionCache.invalidate(key);
    }

    public static boolean isContainVideoInspection(String key) {
        return Objects.nonNull(videoInspectionCache.getIfPresent(key));
    }

    public static VideoInspectionData getVideoInspection(String key) {
        return videoInspectionCache.getIfPresent(key);
    }

}
