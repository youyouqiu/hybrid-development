package com.zw.protocol.netty.client.coder.factory;

import com.zw.platform.push.common.MessageHandler;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.netty.TCPClientExecutor;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ManagedResource(objectName = "com.zw:name=WebClientHandler")
public class WebClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(WebClientHandler.class);

    private final TCPClientExecutor executor;

    private final MessageHandler messageHandler;

    private String clientId;

    private final AtomicInteger counter = new AtomicInteger(0);

    public WebClientHandler(TCPClientExecutor executor, MessageHandler messageHandler) {
        this.executor = executor;
        this.messageHandler = messageHandler;
    }

    @ManagedAttribute
    public AtomicInteger getCounter() {
        return counter;
    }

    public WebClientHandler setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
        ctx.channel().writeAndFlush(MsgUtil.getMsg(ConstantUtil.WEB_INFORM_CLIENT_ID, clientId));
        if (executor.getConnectCount() > 1) {
            //重连需要将原有订阅车辆位置的关系下发到F3
            WebSubscribeManager.getInstance().resendPositionSubscribe();
            final Set<String> allPositions = WsSessionManager.INSTANCE.getAllPositions();
            WebSubscribeManager.INSTANCE.sendMsgToAll(allPositions, ConstantUtil.WEB_SUBSCRIPTION_ADD);
            if (executor.getConnectCount() >= Integer.MAX_VALUE) {
                //防止计算器超过数量
                executor.setConnectCount(2);
            }
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            counter.getAndSet(0);
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg == null) {
            return;
        }
        counter.incrementAndGet();
        Message message = (Message) msg;
        messageHandler.offerMsg(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        if (cause.getMessage().contains("远程主机强迫关闭了一个现有的连")) {
            LOG.error("Unexpected exception from downstream, cause={}", cause.getMessage());
            // TODO Netty断开重连后F3是否会保持之前的订阅？
            WebSubscribeManager.getInstance().clearStatusSubscribe();
            return;
        }
        LOG.error("Unexpected exception from downstream, cause={}", cause.getMessage(), cause);

        if (cause instanceof IOException) {
            LOG.warn("Channel is closed, remote address={}...", getRemoteAddress(context));
        }

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        WebSubscribeManager.getInstance().removeChannel(clientId);
        ctx.channel().eventLoop().schedule(executor::connect, 5L, TimeUnit.SECONDS);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(MsgUtil.getMsg(ConstantUtil.WEB_INFORM_CLIENT_ID, clientId));
        }
    }

    public SocketAddress getRemoteAddress(ChannelHandlerContext context) {
        return context.channel().remoteAddress();
    }
}