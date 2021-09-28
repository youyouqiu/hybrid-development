package com.zw.platform.push.mqserver;

import com.zw.platform.push.common.MessageAsyncTaskExecutor;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.protocol.netty.client.service.ClientMessageCleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by LiaoYuecai on 2017/7/17.
 */
@Component
public class ZMQServer {

    private static final Logger logger = LogManager.getLogger(ZMQServer.class);
    private ZContext ctx;

    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;

    @Autowired
    private ClientMessageCleaner clientMessageCleaner;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;
    
    @Autowired
    private AlarmFactory alarmFactory;

    @Value("${zmqConfig.inproc}")
    private String inproc;

    @Value("${zmqConfig.port}")
    private Integer port;

    @Value("${zmqConfig.threadSum}")
    private Integer threadSum;

    private volatile boolean running;

    @Autowired
    private MessageAsyncTaskExecutor messageAsyncTaskExecutor;

    @PostConstruct
    public void init() {
        this.running = true;
        longTaskExecutor.execute(new Thread(() -> {
            ctx = new ZContext();
            ZMQ.Socket frontend = ctx.createSocket(SocketType.ROUTER);
            frontend.bind("tcp://*:" + port);
            //  Backend socket talks to workers over inproc
            ZMQ.Socket backend = ctx.createSocket(SocketType.DEALER);
            backend.bind("inproc://" + inproc);
            ZMQ.Socket control = ctx.createSocket(SocketType.PAIR);
            control.bind("inproc://control");
            ZMQServerWork work;
            for (int i = 0, m = threadSum; i < m; i++) {
                work = new ZMQServerWork(ctx, inproc, messageAsyncTaskExecutor);
                work.setClientMessageCleaner(clientMessageCleaner);
                work.setSimpMessagingTemplateUtil(simpMessagingTemplateUtil);
                work.setAlarmFactory(alarmFactory);
                //work.setAuthAddress(authAddress);
                longTaskExecutor.execute(work);
            }
            logger.info("ZMQ Server start!");
            while (running) {
                try {
                    ZMQ.proxy(frontend, backend, null, control);
                } catch (Exception e) {
                    logger.error("ZMQ.proxy exception.", e);
                }
            }

            logger.info("ZMQ Server shutdown.");
            ctx.destroy();
        }));
    }

    @PreDestroy
    public void close() {
        this.running = false;
        ZMQ.Socket socket = ctx.createSocket(SocketType.PAIR);
        socket.connect("inproc://control");
        socket.send("TERMINATE");
    }
}
