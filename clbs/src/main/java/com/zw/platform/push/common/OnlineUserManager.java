package com.zw.platform.push.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.service.core.UserOnlineRecordService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.IPAddrUtil;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@DependsOn("redisHelper")
public class OnlineUserManager {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserOnlineRecordService userOnlineRecordService;

    private ScheduledExecutorService executorService;

    private final String serviceIp;

    private Cache<String, String> onlineUserRecords;

    @Value("${websocket.hreat}")
    private long heartbeat;

    @Value("${websocket.clear}")
    private long expire;

    public OnlineUserManager() {
        this.serviceIp = IPAddrUtil.getInstance().getServiceIp();
    }

    @PostConstruct
    public void init() {
        initCache(); //放到构造函数中的话@Value的值会为0，所以只能放在@PostConstruct方法中
        startHeartbeatTask();
        loadFromRedis();
    }

    private void initCache() {
        this.onlineUserRecords = Caffeine.newBuilder()
            .expireAfterAccess(expire, TimeUnit.SECONDS)
            .removalListener((String username, String recordId, RemovalCause cause) -> {
                //不同的ClassLoader会导致LDAP查询时ClassCastException，需要使用spring的ClassLoader来避免这个问题
                Thread.currentThread().setContextClassLoader(userOnlineRecordService.getClass().getClassLoader());
                if (cause == RemovalCause.EXPIRED) {
                    userOnlineRecordService.addUserOffline(username, recordId);
                    RedisHelper.hdel(HistoryRedisKeyEnum.SERVICE_USER.of(serviceIp), username);
                }
            })
            .build();
    }

    private void startHeartbeatTask() {
        final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
            .namingPattern("schedule-pool-user-online")
            .daemon(true)
            .build();
        executorService = new ScheduledThreadPoolExecutor(1, threadFactory);
        executorService.scheduleAtFixedRate(() -> {
            onlineUserRecords.cleanUp();
            for (String username : onlineUserRecords.asMap().keySet()) {
                messagingTemplate.convertAndSendToUser(username, ConstantUtil.WEB_SOCKET_HEALTH, "isOk");
            }
        }, 0, heartbeat, TimeUnit.SECONDS);
    }

    /**
     * 服务重启，bean加载完后，更新用户的下线时间(服务器关闭导致用户的上下线记录没有下线时间的记录)
     */
    private void loadFromRedis() {
        Map<String, String> userData = RedisHelper.hgetAll(HistoryRedisKeyEnum.SERVICE_USER.of(serviceIp));
        onlineUserRecords.putAll(userData);
    }

    @PreDestroy
    public void exit() {
        executorService.shutdown();
    }

    /**
     * 判断用户是否在线
     * @param username 用户名称
     * @return true 在线，false 不在线
     */
    public boolean isOnline(String username) {
        return onlineUserRecords.getIfPresent(username) != null;
    }

    /**
     * 刷新缓存过期时间
     * @param username 用户名称
     */
    public void refresh(String username) {
        onlineUserRecords.getIfPresent(username);
    }

    public void recordOnline(String username) {
        String recordId = userOnlineRecordService.addUserOnlineRecord(username);
        if (recordId == null) {
            return;
        }
        RedisHelper.addToHash(HistoryRedisKeyEnum.SERVICE_USER.of(serviceIp), username, recordId);
        onlineUserRecords.put(username, recordId);
    }

    public Set<String> getOnlineUsers() {
        return onlineUserRecords.asMap().keySet();
    }
}
