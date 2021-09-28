package com.zw.adas.push.nettyclient.client;

import com.zw.adas.push.nettyclient.AdasClientStart;
import com.zw.adas.push.nettyclient.factory.AdasWebClientHandler;
import com.zw.protocol.netty.client.coder.factory.WebDecoder;
import com.zw.protocol.netty.client.coder.factory.WebEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class AdasWebClientStart extends AdasClientStart {

    private static final Logger logger = LogManager.getLogger(AdasWebClientStart.class);

    private AdasWebClient executor;

    @Override
    protected void run() {
        executor = new AdasWebClient();
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel channel) {
                channel.pipeline()
                        .addLast(new IdleStateHandler(0, 0, 3 * 1000 * 60L,
                                TimeUnit.MILLISECONDS))
                        .addLast(new WebDecoder()).addLast(new WebEncoder())
                        .addLast(new AdasWebClientHandler(component, executor).setClientId(clientId));
            }
        };
        try {
            executor.start(adasApplicationEntity, channelInitializer);
        } catch (Exception e) {
            logger.error("WebClientStart异常", e);
        }
    }

    @Override
    public void close() {
        if (executor == null) {
            return;
        }
        executor.close();
    }
}
