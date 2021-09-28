package com.zw.platform.push.mqserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;

/**
 * Paranoid Pirate Pattern
 * @author zhouzongbo on 2018/11/13 21:58
 */
@Component
public class ZmqFenceQueue {

    private static final Logger logger = LogManager.getLogger(ZmqFenceQueue.class);

    /**
     * 重连次数
     */
    private static final int HEARTBEAT_LIVENESS = 3;

    /**
     * 心跳时间
     */
    private static final int HEARTBEAT_INTERVAL = 1000;

    /**
     * 是否准备好
     */
    private static final String P_PIRATE_IS_READY = "READY";

    /**
     * 心跳标识
     */
    private static final String P_PIRATE_HEARTBEAT = "HEARTBEAT";

    /**
     * 队列
     */
    private static final List<ZmqWorker> zmqWorkers = new LinkedList<>();

    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;

    /**
     * 客户端连接
     */
    @Value("${zmqConfig.clbs.frontendAddress}")
    private String frontendAddress;

    /**
     * storm连接
     */
    @Value("${zmqConfig.storm.backendAddress}")
    private String backendAddress;

    private ZContext context;
    private Socket frontend;
    private Socket backend;
    private Poller poller;
    private volatile boolean isRunning;

    @PostConstruct
    public void initZmqQueue() {
        context = new ZContext();
        frontend = context.createSocket(org.zeromq.ZMQ.ROUTER);
        backend = context.createSocket(org.zeromq.ZMQ.ROUTER);

        frontend.bind(frontendAddress);
        backend.bind(backendAddress);
        poller = context.createPoller(2);
        // 注册轮询
        poller.register(backend, Poller.POLLIN);
        poller.register(frontend, Poller.POLLIN);
        longTaskExecutor.execute(new ZmqPiratePatternQueue());
    }

    class ZmqPiratePatternQueue implements Runnable {

        @Override
        public void run() {
            isRunning = true;
            long heartBeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL * 2;
            ZMsg msg;
            ZFrame fenceType;
            ZFrame messageData;
            int rc;
            byte[] data;
            byte[] fenceTypeByte;
            ZFrame zframe;
            ZmqWorker zmqWorker;
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                rc = poller.poll(HEARTBEAT_INTERVAL * 2L);
                if (rc == -1) {
                    break;
                }

                // 处理worker发送过来的消息
                if (poller.pollin(0)) {
                    msg = ZMsg.recvMsg(backend);
                    if (msg == null) {
                        continue;
                    }
                    // 只要worker发送任何消息过来,意味着worker已经连接
                    zframe = msg.unwrap();
                    zmqWorker = new ZmqWorker(zframe, HEARTBEAT_INTERVAL);
                    zmqWorker.ready(zmqWorkers);
                }

                // 处理client发送来的消息
                if (poller.pollin(1)) {
                    msg = ZMsg.recvMsg(frontend);
                    if (msg == null) {
                        continue;
                    }

                    logger.info("workers size = " + zmqWorkers.size());
                    if (zmqWorkers.size() > 0) {
                        msg.pop();
                        fenceType = msg.getFirst();
                        messageData = msg.getLast();

                        // 保存client传递过来的数据, 用于向每个worker分发
                        data = messageData.getData();
                        fenceTypeByte = fenceType.getData();

                        for (ZmqWorker worker : zmqWorkers) {
                            msg.clear();
                            // 消息内容
                            msg.push(new ZFrame(data));
                            // 空帧
                            msg.push(new ZFrame(fenceTypeByte));
                            // msg.send()会清空frames, 如果短时间内有多条数据发送过来,会造成数据丢失,此处复制一个ZFrame用于避免该情况
                            msg.push(worker.getAddress().duplicate());
                            // 发送地址
                            msg.send(backend);
                        }
                        fenceType.destroy();
                        messageData.destroy();
                    }
                    msg.destroy();
                }

                if (System.currentTimeMillis() >= heartBeatAt) {
                    for (ZmqWorker worker : zmqWorkers) {
                        // 向特定的worker发送心跳验证
                        worker.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                        zframe = new ZFrame(P_PIRATE_HEARTBEAT);
                        zframe.send(backend, 0);
                    }
                    heartBeatAt = System.currentTimeMillis() + HEARTBEAT_INTERVAL * 2;
                    //logger.debug("storm nodes = {} ;send heartbeat to storm", zmqWorkers.size());
                }
                // 清空内存数据
                ZmqWorker.purgeWorkers(zmqWorkers);
            }

            // 服务端出现故障,清空队列中的信息
            zmqWorkers.clear();
            poller.unregister(frontend);
            poller.unregister(backend);
            context.destroy();
        }
    }

    @PreDestroy
    public void close() {
        isRunning = false;
        context.destroy();
    }
}
