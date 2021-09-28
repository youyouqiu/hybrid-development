package com.zw.platform.push.mqserver;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class ZmqWebReceiver implements Runnable {

    private final String name;
    private final ZContext context;
    private final ZMQ.Socket frontend;
    private final ZMQ.Socket backend;
    private final Queue<String> workerQueue;

    private static final int HEARTBEAT_INTERVAL = 1000;

    ZmqWebReceiver(String name, String host, String inproc) {
        this.name = name;
        this.context = new ZContext(1);
        this.frontend = context.createSocket(SocketType.DEALER);
        this.frontend.setIdentity(name.getBytes());
        this.backend = context.createSocket(SocketType.ROUTER);

        this.frontend.connect(host);
        this.backend.bind("ipc://" + inproc);

        this.workerQueue = new LinkedList<>();
    }

    public void addWorker(String worker) {
        if (!workerQueue.contains(worker)) {
            this.workerQueue.add(worker);
        }
    }

    @Override
    public void run() {
        try (ZMQ.Poller poller = context.createPoller(2)) {
            poller.register(backend, ZMQ.Poller.POLLIN);
            poller.register(frontend, ZMQ.Poller.POLLIN);

            this.frontend.send(this.name);
            while (!Thread.currentThread().isInterrupted()) {
                int rc = poller.poll(HEARTBEAT_INTERVAL);
                if (rc == -1) {
                    break;
                }
                pollMessage(poller);
            }
            poller.unregister(frontend);
            poller.unregister(backend);
        }
        context.destroy();
        workerQueue.clear();

    }

    private void pollMessage(ZMQ.Poller poller) {
        if (poller.pollin(0)) {
            // 处理接收端消息
            String worker = backend.recvStr();
            backend.recvStr();
            addWorker(worker);
        }
        if (poller.pollin(1)) {
            // 处理发送端消息：收到消息后，找到接收者，将消息转发出去
            String sender = frontend.recvStr();
            String data = frontend.recvStr();

            String receiver = workerQueue.poll();
            if (receiver == null) {
                return;
            }
            backend.sendMore(receiver);
            backend.sendMore(sender);
            backend.send(data);

            workerQueue.add(receiver);
        }
    }
}
