package com.zw.protocol.netty.client.coder.factory;

import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.protocol.msg.VideoMessage;
import com.zw.protocol.netty.TCPClientExecutor;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Tdz
 * @since 2018-01-05 14:44
 **/
public class VideoClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(VideoClientHandler.class);

    private final TCPClientExecutor executor;

    private final WebClientHandleCom component;

    private String clientId;

    public VideoClientHandler(WebClientHandleCom component, TCPClientExecutor executor) {
        this.component = component;
        this.executor = executor;
    }

    public VideoClientHandler setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        VideoMessage message = (VideoMessage) msg;
        component.videoHandle(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连")) {
            LOG.error("Unexpected exception from downstream, cause={}", cause.getMessage());
            return;
        }
        LOG.error("Unexpected exception from downstream, cause={}", cause.getMessage(), cause);

        if (cause instanceof IOException) {
            LOG.warn("Channel is closed, remote address={}...", getRemoteAddress(context));
        }

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        WebSubscribeManager.getInstance().removeVideoChannel(clientId);
        ctx.channel().eventLoop().schedule(executor::connect, 5L, TimeUnit.SECONDS);
    }

    public SocketAddress getLocalAddress(ChannelHandlerContext context) {
        return context.channel().localAddress();
    }

    public SocketAddress getRemoteAddress(ChannelHandlerContext context) {
        return context.channel().remoteAddress();
    }
}
