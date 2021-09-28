package com.zw.platform.push.factory;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.push.command.AlarmMessageDTO;
import com.zw.platform.push.command.RealTimeVideoCommand;
import com.zw.platform.push.command.ResourceListCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 报警联动策略 -> 消费线程
 * 主要消费: redis 12分区中的“实时视频”和"上传音视频资源列表数据"
 * @author create by zhouzongbo on 2020/9/27.
 */

@Slf4j
@Component
@DependsOn("redisHelper")
public class AlarmLinkageTaskExecutor {

    /**
     * 线程最大等待时间
     * <p>目前期望值1s消费到消息
     */
    private static final int MAX_SLEEP_TIME = 2000;

    private static final Object LOCK = new Object();

    private static final AtomicBoolean RUN = new AtomicBoolean();

    public static final Map<Integer, AlarmChainHandler> HANDLERS = new ConcurrentHashMap<>();

    @Autowired
    private RealTimeVideoCommand realTimeVideoCommand;

    @Autowired
    private ResourceListCommand resourceListCommand;

    @Autowired
    private HandleAlarmHandler handleAlarmHandler;

    /**
     * 固定线程池
     */
    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;

    private void initMap() {
        HANDLERS.put(AlarmMessageDTO.REALTIME_VIDEO_MSG_TYPE, realTimeVideoCommand);
        HANDLERS.put(AlarmMessageDTO.RESOURCE_LIST_MSG_TYPE, resourceListCommand);
        HANDLERS.put(AlarmMessageDTO.ALARM_HANDLE_RESULT_MSG_TYPE, handleAlarmHandler);
    }

    @PostConstruct
    private void init() {
        initMap();
        // 避免容器刷新导致跑两次
        if (!RUN.compareAndSet(false, true)) {
            return;
        }
        longTaskExecutor.execute(() -> {
            Thread.currentThread().setName("alarm-chain-handler");
            synchronized (LOCK) {
                int sleepTime = 0;
                RedisKey redisKey;
                String messageStr;
                AlarmMessageDTO alarmMessage;
                AlarmChainHandler handler;
                while (RUN.get()) {
                    try {
                        redisKey = HistoryRedisKeyEnum.LINKPAGE_TO_PLATFORM.of();
                        messageStr = RedisHelper.listLpop(redisKey);
                        if (StringUtils.isEmpty(messageStr) || "nil".equals(messageStr)) {
                            if (sleepTime < MAX_SLEEP_TIME) {
                                sleepTime += 200;
                            }
                            // 只是单纯的等待, 避免没有数据空转
                            LOCK.wait(sleepTime);
                        } else {
                            sleepTime = 0;
                            alarmMessage = JSON.parseObject(messageStr, AlarmMessageDTO.class);
                            handler = HANDLERS.get(alarmMessage.getMsgType());
                            try {
                                if (null != handler) {
                                    handler.handle(alarmMessage);
                                }
                            } catch (Exception e) {
                                log.error("处理报警联动时出错", e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理报警联动异常", e);
                    }
                }
            }
        });
    }

    @PreDestroy
    public void close() {
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        RUN.set(false);
    }
}
