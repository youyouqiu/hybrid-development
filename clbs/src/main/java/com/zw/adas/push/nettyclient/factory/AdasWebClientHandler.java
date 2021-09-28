package com.zw.adas.push.nettyclient.factory;

import com.zw.adas.push.common.AdasWebClientHandleCom;
import com.zw.adas.push.nettyclient.AdasTCPClientExecutor;
import com.zw.adas.push.nettyclient.manager.AdasWebSubscribeManager;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class AdasWebClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AdasWebClientHandler.class);

    private final AdasWebClientHandleCom component;

    private String clientId;

    public AdasWebClientHandler setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    private final AdasTCPClientExecutor executor;

    public AdasWebClientHandler(AdasWebClientHandleCom component, AdasTCPClientExecutor executor) {
        this.component = component;
        this.executor = executor;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("adas媒体上传netty服务端已断开连接！");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
        ctx.channel().writeAndFlush(MsgUtil.getMsg(ConstantUtil.WEB_INFORM_CLIENT_ID, clientId));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg == null) {
            return;
        }
        Message message = (Message) msg;
        //logger.info(message.toString());
        component.handle(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连")) {
            logger.error("Unexpected exception from downstream, cause={}", cause.getMessage());
            return;
        }
        logger.error("Unexpected exception from downstream, cause={}", cause.getMessage(), cause);

        if (cause instanceof IOException) {
            logger.warn("Channel is closed, remote address={}...", getRemoteAddress(context));
        }

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        AdasWebSubscribeManager.getInstance().removeChannel(clientId);
        ctx.channel().eventLoop().schedule(executor::connect, 5L, TimeUnit.SECONDS);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().writeAndFlush(MsgUtil.getMsg(ConstantUtil.WEB_INFORM_CLIENT_ID, clientId));
        }
    }

    public SocketAddress getLocalAddress(ChannelHandlerContext context) {
        return context.channel().localAddress();
    }

    public SocketAddress getRemoteAddress(ChannelHandlerContext context) {
        return context.channel().remoteAddress();
    }
}