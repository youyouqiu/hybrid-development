package com.zw.adas.push.nettyclient;

import com.zw.adas.push.nettyclient.common.AdasApplicationEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Slf4j
public abstract class AdasTCPClientExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(AdasTCPClientExecutor.class);

    private final Bootstrap client = new Bootstrap();

    protected final EventLoopGroup group = new NioEventLoopGroup((Runtime.getRuntime().availableProcessors() / 3));

    protected Channel channel;

    protected volatile boolean active = true;

    private String host;

    private int port;

    protected String id;

    public Channel getChannel() {
        return channel;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void start(final AdasApplicationEntity entry, final ChannelInitializer<SocketChannel> channelInitializer) {
        this.host = entry.getHost();
        this.port = entry.getPort();
        this.id = UUID.randomUUID().toString();
        initServer(channelInitializer);
        connect();
    }

    private void initServer(ChannelInitializer<SocketChannel> channelInitializer) {
        client.group(group).channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_SNDBUF, 64 * 1024)
            .option(ChannelOption.SO_RCVBUF, 64 * 1024)
            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
            .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
            .handler(channelInitializer);
    }

    public void connect() {
        if (!active) {
            return;
        }
        client.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channel = future.channel();
                registerChannel();
            }
        });
    }

    protected abstract void registerChannel();

    public void close() {
        this.active = false;
        this.group.shutdownGracefully();
        LOG.info("关闭客户端: {}", id);
    }

}