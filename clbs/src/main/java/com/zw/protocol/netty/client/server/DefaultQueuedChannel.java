package com.zw.protocol.netty.client.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 消息放入队列，由若干消费者一一委托netty channel发送
 * <p>
 * 因队列发送，当前未实现Future，如需此功能，请直接调用 {@link #getDelegate()} 同步调用
 * <p>
 * flush()的行为将由队列消费者决定，这里调用也不会有意义
 *
 * @author Zhang Yanhui
 * @see #getDelegate()
 * @since 2020/6/3 15:48
 */

public class DefaultQueuedChannel implements QueuedChannel {

    @Getter
    @Setter
    @Accessors(chain = true)
    @Delegate(excludes = ExcludedMethods.class)
    private Channel delegate;

    private volatile Channel defaultDelegate;

    @Getter
    private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();

    public DefaultQueuedChannel(Channel delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public ChannelFuture write(Object msg) {
        queue.offer(msg);
        return delegate.newSucceededFuture();
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return write(msg);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return write(msg);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return write(msg);
    }

    @Override
    public ChannelFuture sendNetty(Object msg) {
        return delegate.writeAndFlush(msg);
    }

    @Override
    public QueuedChannel resetDelegate() {
        if (null == defaultDelegate) {
            synchronized (this) {
                if (null == defaultDelegate) {
                    defaultDelegate = new DefaultQueuedChannel(null) {

                        @Override
                        public boolean isWritable() {
                            return false;
                        }

                        @Override
                        public ChannelFuture write(Object msg) {
                            queue.offer(msg);
                            return null;
                        }
                    };
                }
            }
        }
        delegate = defaultDelegate;
        return this;
    }

    private interface ExcludedMethods {

        boolean isWritable();

        ChannelFuture write(Object msg);

        ChannelFuture writeAndFlush(Object msg);

        ChannelFuture write(Object msg, ChannelPromise promise);

        ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    }
}
