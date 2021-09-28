package com.zw.adas.push.mqserver;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import com.zw.platform.task.AdasGuideTask;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class AdasZmqWebReceiver implements Runnable {

    private static final Logger logger = LogManager.getLogger(AdasZmqWebReceiver.class);
    private final String name;
    private final ZContext context;
    private final ZMQ.Socket frontend;
    private final Queue<String> workerQueue;
    private final Disruptor<AdasZmqServer.AdasMessage> disruptor;
    private final AdasGuideTask guideTask;
    private static final int HEARTBEAT_INTERVAL = 1000;

    AdasZmqWebReceiver(String name, String host, String inproc, ZContext context,
        Disruptor<AdasZmqServer.AdasMessage> disruptor, AdasGuideTask guideTask) {
        this.name = name;
        this.context = context;
        this.frontend = context.createSocket(SocketType.DEALER);
        this.frontend.setIdentity(name.getBytes());
        this.disruptor = disruptor;
        this.frontend.connect(host);
        this.workerQueue = new LinkedList<>();
        this.guideTask = guideTask;
    }

    public void addWorker(String worker) {
        if (!workerQueue.contains(worker)) {
            this.workerQueue.add(worker);
        }
    }

    @Override
    public void run() {
        ZMQ.PollItem[] items = { new ZMQ.PollItem(frontend, ZMQ.Poller.POLLIN) };
        Selector selector;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("zmqWebReceiver 获取到selector失败!");
            return;
        }
        logger.info("Adas zmq start!");
        int rc;
        String data;
        while (!Thread.currentThread().isInterrupted()) {
            rc = ZMQ.poll(selector, items, HEARTBEAT_INTERVAL);
            if (rc == -1) {
                break;
            }
            if (items[0].isReadable()) {
                //第一个，需要抛弃掉。
                frontend.recvStr();
                data = frontend.recvStr();
                logger.info("ADAS message :" + data);
                //检查ftp上车的证据文件是否创建
                if (AdasZmqServer.getAdasVehicleDirFlag()) {
                    logger.info("当天的adas证据文件夹没有创建，开始创建证据文件夹！");
                    guideTask.createFtpMediaDirectory(false);
                }
                if (disruptor != null) {
                    disruptor.publishEvent(new AdasEventProducer(data));
                }
                //workerQueue.add(receiver);
            } else {
                // heartbeat
                this.frontend.send(this.name);
            }
        }
        context.destroy();
        //workerQueue.clear();

    }

    @Data
    static class AdasEventProducer implements EventTranslator<AdasZmqServer.AdasMessage> {
        private String message;

        public AdasEventProducer(String message) {
            this.message = message;
        }

        @Override
        public void translateTo(AdasZmqServer.AdasMessage event, long sequence) {
            event.setMessage(message);
        }
    }

}
