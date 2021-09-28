package com.zw.protocol.netty.client.server;

import com.zw.protocol.netty.ServerStart;
import com.zw.protocol.netty.client.coder.factory.VideoClientHandler;
import com.zw.protocol.netty.client.coder.factory.VideoDecoder;
import com.zw.protocol.netty.client.coder.factory.VideoEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 视频netty服务端 @author Tdz
 * @since 2018-01-04 15:03
 **/
public class VideoClientStart extends ServerStart {

    private static final Logger logger = LogManager.getLogger(VideoClientStart.class);

    private VideoServer executor;

    @Override
    public void run() {
        executor = new VideoServer();
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) {
                channel.pipeline()
                    // .addLast(new IdleStateHandler(3 * 60L, 3 * 60L, 3 * 60L, TimeUnit.SECONDS))
                    // .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                    .addLast(new VideoDecoder())
                    .addLast(new VideoEncoder())
                    .addLast(new VideoClientHandler(component, executor).setClientId(clientId));
            }
        };
        try {
            executor.start(applicationEntity, channelInitializer);
        } catch (Exception e) {
            logger.error("VideoClientStart异常" + e);
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
