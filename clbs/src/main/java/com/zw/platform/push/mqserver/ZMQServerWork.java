package com.zw.platform.push.mqserver;

import com.alibaba.fastjson.JSON;
import com.zw.platform.push.common.MessageAsyncTaskExecutor;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.protocol.msg.Message;
import com.zw.protocol.netty.client.service.ClientMessageCleaner;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * mq @author Tdz
 **/
@Data
public class ZMQServerWork implements Runnable {
    private static final Logger logger = LogManager.getLogger(ZMQServerWork.class);

    private AlarmFactory alarmFactory;

    private ZContext ctx;

    private String inproc;

    private String authAddress;

    private ClientMessageCleaner clientMessageCleaner;

    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    private MessageAsyncTaskExecutor messageAsyncTaskExecutor;

    public ZMQServerWork(ZContext ctx, String inproc, MessageAsyncTaskExecutor messageAsyncTaskExecutor) {
        this.ctx = ctx;
        this.inproc = inproc;
        this.messageAsyncTaskExecutor = messageAsyncTaskExecutor;
    }

    @Override
    public void run() {
        ZMQ.Socket worker = ctx.createSocket(SocketType.DEALER);
        worker.connect("inproc://" + inproc);
        try (ZMQ.Poller poller = ctx.createPoller(1)) {
            poller.register(worker, ZMQ.Poller.POLLIN);
            while (!Thread.currentThread().isInterrupted()) {
                int rc = poller.poll(5000L);
                if (rc == -1) {
                    break;
                }
                if (poller.pollin(0)) {
                    handleMessage(worker);
                }
            }
            logger.info("线程中断，释放资源");
            poller.unregister(worker);
        }
        ctx.destroy();
    }

    private void handleMessage(ZMQ.Socket worker) {
        ZMsg msg;
        ZFrame address;
        ZFrame content;
        msg = ZMsg.recvMsg(worker);
        address = msg.pop();
        content = msg.pop();
        try {
            final String msgJson = new String(content.getData(), ZMQ.CHARSET);
            logger.debug("平台收到报警信息:{}", msgJson);
            Message message = JSON.parseObject(msgJson, Message.class);
            messageAsyncTaskExecutor.offerAlarm(message);
        } catch (Exception e) {
            logger.error("ZMQ消息处理异常", e);
        } finally {
            msg.destroy();
            address.destroy();
            content.destroy();
        }
    }

}
