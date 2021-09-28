package com.zw.platform.push.common;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.ThrottlingQueue;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.handler.device.DeviceMessageHandler;
import com.zw.platform.service.reportManagement.DriverDiscernStatisticsService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.AlarmDealMessage;
import com.zw.protocol.msg.IcCardMessage;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.netty.client.service.ClientMessageCleaner;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/21
 */
@Log4j2
@Component
@ManagedResource(objectName = "com.zw:name=MessageAsyncTaskExecutor")
public class MessageAsyncTaskExecutor {

    private static final int MAX_SIZE = 1000;

    private static final int REGISTER_MAX_SIZE = 10000;

    private static final int HANDLE_GPS_INFO_THREAD_SIZE = 2;

    @Value("${alarm.deal.batch.size}")
    private int alarmDealBatchSize;

    @Value("${alarm.deal.batch.time}")
    private int alarmDealBatchTime;

    @Value("${alarm.deal.batch.thread}")
    private boolean alarmDealBatchThread;

    @Value("${ic.card.thread.size}")
    private int icCardThreadSize;

    @Value("${message.async.alarm.thread.size:1}")
    private int alarmThreadCount;

    private static final List<LinkedBlockingQueue<IcCardMessage>> icCardQueueList = new ArrayList<>();

    private final ThrottlingQueue<String, Message> gpsMessageQueue;

    private final LinkedBlockingQueue<String> statusMessageQueue;

    private final LinkedBlockingQueue<AlarmDealMessage> alarmDealMessageQueue;

    /**
     * 同一个监控对象, 不同时段报警不一样, 所以不增加过滤
     */
    private final LinkedBlockingQueue<Message> alarmQueue;

    private final LinkedBlockingQueue<Message> driverIdentificationReportQueue;

    private final ThrottlingQueue<String, Message> registerQueue;

    /**
     * 监控对象ID
     */
    private final ThrottlingQueue<String, Message> monitorStatusQueue;

    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;

    @Autowired
    private AlarmFactory alarmFactory;

    @Autowired
    private DriverDiscernStatisticsService driverDiscernStatisticsService;

    @Autowired
    private ClientMessageCleaner clientMessageCleaner;

    @Autowired
    private WebClientHandleCom webClientHandleCom;

    @Autowired
    private WebSocketMessageDispatchCenter dispatchCenter;

    @Autowired
    private DeviceMessageHandler deviceMessageHandler;

    @Autowired
    private ServerParamList serverParamList;

    public MessageAsyncTaskExecutor() {
        this.gpsMessageQueue = new ThrottlingQueue<>(5000);
        this.statusMessageQueue = new LinkedBlockingQueue<>(MAX_SIZE);
        this.alarmQueue = new LinkedBlockingQueue<>(REGISTER_MAX_SIZE);
        this.registerQueue = new ThrottlingQueue<>(REGISTER_MAX_SIZE);
        this.monitorStatusQueue = new ThrottlingQueue<>(REGISTER_MAX_SIZE);
        this.alarmDealMessageQueue = new LinkedBlockingQueue<>(MAX_SIZE);
        this.driverIdentificationReportQueue = new LinkedBlockingQueue<>(MAX_SIZE);
    }

    @ManagedAttribute
    public int getGpsMessageQueueSize() {
        return gpsMessageQueue.size();
    }

    @ManagedAttribute
    public int getStatusMessageQueueSize() {
        return statusMessageQueue.size();
    }

    @ManagedAttribute
    public int getAlarmQueueSize() {
        return alarmQueue.size();
    }

    @ManagedAttribute
    public int getDriverIdentificationReportQueueSize() {
        return driverIdentificationReportQueue.size();
    }

    @ManagedAttribute
    public int getRegisterQueueSize() {
        return registerQueue.size();
    }

    @ManagedAttribute
    public int getMonitorStatusQueue() {
        return monitorStatusQueue.size();
    }

    @ManagedAttribute
    public int getAlarmDealMessageQueue() {
        return alarmDealMessageQueue.size();
    }

    @PostConstruct
    public void startTasks() {
        // startGpsTask();
        // startStatusTask();
        startAlarmTask();
        // startRegisterTask();
        // startMonitorStatusTask();
        // startDriverIdentificationReportTask();
        if (alarmDealBatchThread) {
            startAlarmDealTask();
        }
        // if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
        //     startIcCardTask();
        // }
    }

    private void startIcCardTask() {
        for (int i = 0; i < icCardThreadSize; i++) {
            LinkedBlockingQueue<IcCardMessage> queue = new LinkedBlockingQueue<>(2000);
            icCardQueueList.add(queue);
            longTaskExecutor.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!handleIcCardMessage(queue)) {
                        break;
                    }
                }
            });
        }
    }

    private boolean handleIcCardMessage(LinkedBlockingQueue<IcCardMessage> queue) {
        IcCardMessage icCardMessage;
        try {
            icCardMessage = queue.take();
            switch (icCardMessage.getType()) {
                case ConstantUtil.T808_DRIVER_INFO:
                    deviceMessageHandler.saveDriverInfoCollectionLog((Message) icCardMessage.getData());
                    break;
                case ConstantUtil.T808_MULTIMEDIA_DATA:
                    JSONObject jsonObject = (JSONObject) icCardMessage.getData();
                    webClientHandleCom.dealIcProfessionalPic(jsonObject.getString("deviceNumber"),
                        jsonObject.getString("path"));
                    break;
                default:
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("处理IcCard线程异常", e);
        }
        return true;
    }

    void offerIcCardMessage(IcCardMessage icCardMessage) {
        int index = Math.abs(icCardMessage.getVehicleId().hashCode() % icCardThreadSize);
        if (offerMessage(icCardMessage, icCardQueueList.get(index))) {
            return;
        }
        log.error("处理IcCard线程" + index + "号队列已满");
    }

    private void startGpsTask() {
        for (int i = 0; i < HANDLE_GPS_INFO_THREAD_SIZE; i++) {
            longTaskExecutor.execute(() -> {
                Message msg;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        msg = gpsMessageQueue.take();
                        clientMessageCleaner.getLocationInfo(msg);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("处理位置消息异常", e);
                    }
                }
            });
        }
    }

    private void startStatusTask() {
        longTaskExecutor.execute(() -> {
            String monitorId;
            RedisKey offlineKey;
            RedisKey alarmKey;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    monitorId = statusMessageQueue.take();
                    offlineKey = HistoryRedisKeyEnum.MONITOR_OFFLINE.of(monitorId);
                    alarmKey = HistoryRedisKeyEnum.MONITOR_ALARMING.of(monitorId);
                    RedisHelper.delete(Arrays.asList(offlineKey, alarmKey));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理状态消息异常", e);
                }
            }
        });
    }

    private void startAlarmTask() {
        for (int i = 0; i < alarmThreadCount; i++) {
            longTaskExecutor.execute(() -> {
                Message message;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        message = alarmQueue.take();
                        alarmFactory.createAlarm(message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("处理报警消息异常", e);
                    }
                }
            });
        }
    }

    private void startDriverIdentificationReportTask() {
        longTaskExecutor.execute(() -> {
            Message message;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    message = driverIdentificationReportQueue.take();
                    driverDiscernStatisticsService.saveReportHandle(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理驾驶员身份识别上报异常", e);
                }
            }
        });
    }

    private void startAlarmDealTask() {
        longTaskExecutor.execute(() -> {
            AlarmDealMessage alarmDealMessage;
            List<T809Message> t809Messages;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    alarmDealMessage = alarmDealMessageQueue.take();
                    t809Messages = alarmDealMessage.getT809MessageList();
                    WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                        .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG_LIST, t809Messages)
                            .assembleDesc809(alarmDealMessage.getT809PlatId()));
                    if (alarmDealBatchTime > 0 && alarmDealMessage.getT809MessageList().size() == alarmDealBatchSize) {
                        Thread.sleep(alarmDealBatchTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理报警处理消息异常", e);
                }
            }
        });
    }


    private void startRegisterTask() {
        longTaskExecutor.execute(() -> {
            Message msg;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    msg = registerQueue.take();
                    webClientHandleCom.updateRegionalInfo(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理报警消息异常", e);
                }
            }
        });
    }

    private void startMonitorStatusTask() {
        longTaskExecutor.execute(() -> {
            Message msg;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    msg = monitorStatusQueue.take();
                    dispatchCenter.pushCacheStatusNew(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理监控对象状态信息异常");
                }
            }
        });
    }

    void offerGpsMessage(Message gpsMessage) {
        if (commonOfferMonitory(gpsMessage, gpsMessageQueue)) {
            return;
        }
        log.error("位置消息队列已满");
    }

    void offerStatusMessage(String monitorId) {
        if (StringUtils.isEmpty(monitorId) || offerMessage(monitorId, statusMessageQueue)) {
            return;
        }
        log.error("状态消息队列已满");
    }

    public void offerAlarm(Message alarm) {
        if (offerMessage(alarm, alarmQueue)) {
            return;
        }
        log.error("报警消息队列已满");
    }

    void offerRegister(Message registerMessage) {
        if (commonOfferMonitory(registerMessage, registerQueue)) {
            return;
        }
        log.error("注册信息消息队列已满");
    }

    void offerMonitorStatus(Message message) {
        if (commonOfferMonitory(message, monitorStatusQueue)) {
            return;
        }
        log.error("监控对象状态消息队列已满");
    }

    public void offerDriverIdentificationReport(Message message) {
        if (offerMessage(message, driverIdentificationReportQueue)) {
            return;
        }
        log.error("驾驶员身份识别上报队列已满");
    }

    public void offerAlarmDeal(AlarmDealMessage alarmDealMessage) {
        if (alarmDealMessage.getT809MessageList().size() > alarmDealBatchSize) {
            List<T809Message> t809Messages = alarmDealMessage.getT809MessageList();
            String t809PlatId = alarmDealMessage.getT809PlatId();
            for (int i = 0; i < t809Messages.size(); i += alarmDealBatchSize) {
                AlarmDealMessage dealMessage = new AlarmDealMessage();
                int toIndex = Math.min(i + alarmDealBatchSize, t809Messages.size());
                dealMessage.setT809MessageList(t809Messages.subList(i, toIndex));
                dealMessage.setT809PlatId(t809PlatId);
                if (!offerMessage(dealMessage, alarmDealMessageQueue)) {
                    log.error("报警处理消息队列已满");
                    return;
                }
            }
        } else {
            if (!offerMessage(alarmDealMessage, alarmDealMessageQueue)) {
                log.error("报警处理消息队列已满");
            }
        }
    }



    /**
     * 该方法用于避免同一个队列中存在多条相同的数据
     * 1.map存在monitorId, 则更新数据
     * 2.map不存在monitorId, 则把数据存入map中, 并且把monitorId, 存入队列中
     * @param message message
     * @param queue   queue用于存储monitorId和message
     * @return true: 消息队列满了; false: 其他
     */
    private boolean commonOfferMonitory(Message message, ThrottlingQueue<String, Message> queue) {
        if (Objects.isNull(message)) {
            return true;
        }

        MsgDesc desc = message.getDesc();
        if (Objects.isNull(desc)) {
            return true;
        }

        String monitorId = desc.getMonitorId();
        try {
            return queue.offer(monitorId, message, 10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    private <T> boolean offerMessage(T object, LinkedBlockingQueue<T> queue) {
        try {
            boolean res = queue.offer(object, 10, TimeUnit.MILLISECONDS);
            if (!res) {
                // 如果队列已满，则移除头元素，更新尾元素
                queue.poll();
                queue.put(object);
            }
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}