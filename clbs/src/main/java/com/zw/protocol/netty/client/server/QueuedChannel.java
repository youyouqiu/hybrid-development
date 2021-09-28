package com.zw.protocol.netty.client.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 消息放入队列，由若干消费者一一委托netty channel发送
 * <p>
 * 因队列发送，实现类很可能不去实现Future特性，如需此功能，请直接调用 {@link #getDelegate()} 发送消息
 * <p>
 * 因队列特性，flush()的行为将由队列消费者决定，这里调用也不会有意义
 *
 * @author Zhang Yanhui
 * @since 2020/6/3 15:48
 */

public interface QueuedChannel extends Channel {

    ChannelFuture sendNetty(Object msg);

    Channel getDelegate();

    QueuedChannel setDelegate(Channel channel);

    QueuedChannel resetDelegate();

    ConcurrentLinkedQueue<Object> getQueue();
}
