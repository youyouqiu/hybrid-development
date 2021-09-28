package com.zw.protocol.netty.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by LiaoYuecai on 2017/6/21.
 */
public class HBaseDataHandler {
    private static final Logger logger = LoggerFactory.getLogger(HBaseDataHandler.class);
    private LinkedBlockingQueue<String> queue ;
    private LinkedBlockingQueue<String> queueBd;

    private static HBaseDataHandler handler;

    private HBaseDataHandler() {
        queue = new LinkedBlockingQueue<String>();
        queueBd = new LinkedBlockingQueue<String>();
    }

    public static synchronized HBaseDataHandler getInstance() {
        if (handler == null)
            handler = new HBaseDataHandler();
        return handler;
    }

    public void add(String str) {
        queue.add(str);
    }

    public String take() {
        if (!queue.isEmpty()) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                logger.error("队列获取数据异常", e);
            }
        }
        return  null;
    }

    public void addBd(String str) {
        queueBd.add(str);
    }

    public String takeBd() {
        if (!queue.isEmpty()) {
            try {
                return queueBd.take();
            } catch (InterruptedException e) {
                logger.error("队列获取数据异常", e);
            }
        }
        return  null;
    }

}
