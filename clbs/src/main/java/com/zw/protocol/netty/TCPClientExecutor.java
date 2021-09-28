package com.zw.protocol.netty;

import com.zw.protocol.netty.common.ApplicationEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TCPClientExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(TCPClientExecutor.class);

    private final Bootstrap client = new Bootstrap();

    /**
     * Netty worker组大小，默认2倍CPU核数
     */
    protected final EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() / 2);

    protected Channel channel;

    /**
     * 用户重新时,将用户定位的车辆位置缓存发送给F3
     */
    private int connectCount = 1;

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

    public void start(final ApplicationEntity entry, final ChannelInitializer<SocketChannel> channelInitializer) {
        this.host = entry.getHost();
        this.port = entry.getPort();
        this.id = entry.getId();

        initClient(channelInitializer);

        connect();
    }

    private void initClient(ChannelInitializer<SocketChannel> channelInitializer) {
        client.group(group).channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_SNDBUF, 128 * 1024)
            .option(ChannelOption.SO_RCVBUF, 64 * 1024)
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024))
            .handler(channelInitializer);
    }

    public void connect() {
        if (!active) {
            return;
        }
        client.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.cause() != null) {
                // LOG.error("客户端连接失败", future.cause());
                return;
            }
            channel = future.channel();
            registerChannel();
        });
    }

    protected abstract void registerChannel();

    public void close() {
        this.active = false;
        this.group.shutdownGracefully();
        LOG.info("关闭客户端: {}", id);
    }


    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public int getPort() {
        return port;
    }
}
