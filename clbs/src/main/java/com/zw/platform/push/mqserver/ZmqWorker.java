package com.zw.platform.push.mqserver;

import lombok.Data;
import org.zeromq.ZFrame;

import java.util.Iterator;
import java.util.List;

/**
 * 工人类storm 各个节点
 */
@Data
public class ZmqWorker {
    /**
     * Address of worker
     */
    private ZFrame address;
    /**
     * 凭证
     */
    private String identity;
    /**
     * 设置超时时间
     */
    private long expire;

    public ZmqWorker(ZFrame address, int heartBeatInterval) {
        this.address = address;
        identity = new String(address.getData());
        expire = System.currentTimeMillis() + heartBeatInterval * 5;
    }

    /**
     * 添加
     *
     * @param zmqWorkers 队列中worker 数
     */
    public void ready(List<ZmqWorker> zmqWorkers) {
        Iterator<ZmqWorker> iterator = zmqWorkers.iterator();
        while (iterator.hasNext()) {
            ZmqWorker zmqWorker = iterator.next();
            if (identity.equalsIgnoreCase(zmqWorker.identity)) {
                iterator.remove();
                break;
            }
        }
        zmqWorkers.add(this);
    }

    /**
     * 获取下一个可用的worker地址
     *
     * @param zmqWorkers workers
     * @return ZFrame
     */
    public ZFrame nextZFrame(List<ZmqWorker> zmqWorkers) {
        // 删除第一个work
        ZmqWorker zmqWorker = zmqWorkers.remove(0);
        ZFrame zFrame = null;
        if (zmqWorker == null) {
            return zFrame;
        }
        zFrame = zmqWorker.address;
        return zFrame;
    }

    /**
     * 清空内存数据
     *
     * @param zmqWorkers workers
     */
    public static void purgeWorkers(List<ZmqWorker> zmqWorkers) {
        Iterator<ZmqWorker> iterator = zmqWorkers.iterator();
        while (iterator.hasNext()) {
            ZmqWorker zmqWorker = iterator.next();
            // 未超过时间的worker继续保留，否则移除
            if (System.currentTimeMillis() < zmqWorker.expire) {
                continue;
            }
            iterator.remove();
        }
    }
}