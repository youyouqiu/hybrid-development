package com.zw.platform.push.mqserver;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 监听围栏数据生产 @author  Tdz
 * @create 2018-01-16 15:29
 **/
@Component
@DependsOn(value = "zmqFenceQueue")
public class ZMQFencePub  {
    private static final Logger logger = LogManager.getLogger(ZMQFencePub.class);

    @Value("${zmqConfig.pubPort}")
    private String port;

    private static Socket client;

    private ZContext ctx;

    @PostConstruct
    public void init() {
        ctx = new ZContext();
        client = ctx.createSocket(SocketType.DEALER);
        client.connect(port);
    }

    @PreDestroy
    public void close() {
        ctx.destroy();
    }

    /**
     * 产生更改数据
     * @param type
     *            1:车辆信息;2:人员信息;3:关键点信息;4:圆信息;5:矩形信息;6:多边形信息;7:行政区域信息;8:线路信息;9:报警参数;10:导航路线;
     *            11:报警类型缓存;12:报警参数设置缓存;13:电子围栏信息缓存;14:线路分段信息;15：刷新月统计报表内存数据; 18:车辆IO对应的检测功能类型改变;
     *            19:809报警设置改变;  20:视频参数设置; 21:监控对象绑定的轮胎个数; 22：排班信息改变  23：任务信息改变;
     */
    public static void pubChangeFence(String type) {

        if (client != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.sendMore("fence");
            client.send(type);
            logger.info("围栏改变事件，code：" + type);
        }
    }

    public static void pubChangeParam(String obj) {

        if (client != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.sendMore("shanxi");
            client.send(obj);
            logger.info("山西809传递参数，param：" + obj);
        }

    }

    public static void pubAdasRiskParam(String obj) {
        if (client != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.sendMore("adasRisk");
            client.send(obj);
            logger.info("风险定义参数设置" + obj);
        }

    }

}