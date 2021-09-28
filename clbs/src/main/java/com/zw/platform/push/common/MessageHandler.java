package com.zw.platform.push.common;

import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/5/24 9:32
 */
@Component
public class MessageHandler {
    private static final Logger log = LogManager.getLogger(MessageHandler.class);
    @Value("${message.handler.thread.size:12}")
    private int alarmThreadCount;

    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;
    @Autowired
    private WebClientHandleCom webClientHandleCom;

    private static final PriorityBlockingQueue<Message> MSG_QUEUE = new PriorityBlockingQueue<>(50000, (msg1, msg2) -> {
        MsgDesc desc1 = msg1.getDesc();
        MsgDesc desc2 = msg2.getDesc();
        Integer msg1Priority = AcceptMessagePriority.getMessagePriority(desc1.getMessageType(), desc1.getMsgID());
        Integer msg2Priority = AcceptMessagePriority.getMessagePriority(desc2.getMessageType(), desc2.getMsgID());
        int compare = Integer.compare(msg1Priority, msg2Priority);
        if (compare != 0) {
            return compare;
        }
        String sysTime1 = desc1.getSysTime().replaceAll("[:\\- ]", "");
        String sysTime2 = desc2.getSysTime().replaceAll("[:\\- ]", "");
        return sysTime1.compareTo(sysTime2);
    });

    @PostConstruct
    public void init() {
        startHandleMsg();
    }

    public void offerMsg(Message message) {
        MsgDesc desc = message.getDesc();
        if (desc == null) {
            return;
        }
        boolean isFull = MSG_QUEUE.offer(message);
        if (!isFull) {
            log.error("消息队列已满");
        }
    }

    private void startHandleMsg() {
        for (int i = 0; i < alarmThreadCount; i++) {
            longTaskExecutor.execute(() -> {
                Message msg;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        msg = MSG_QUEUE.take();
                        webClientHandleCom.handle(msg);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("处理消息异常", e);
                    }
                }
            });
        }
    }
}
