package com.zw.protocol.netty.client.coder.factory;

import com.alibaba.fastjson.JSON;
import com.zw.protocol.msg.VideoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 音视频解码器 @author Tdz
 * @since 2018-01-04 15:08
 **/
public class VideoDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(VideoDecoder.class);

    public VideoDecoder() {
        setCumulator(ByteToMessageDecoder.COMPOSITE_CUMULATOR);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int length = in.readInt();
        if (length < 0) {
            return;
        }
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        String jsonStr = in.toString(in.readerIndex(), length, Charset.forName("GBK"));
        in.skipBytes(length);
        try {
            VideoMessage message = JSON.parseObject(jsonStr, VideoMessage.class);
            if (message != null) {
                out.add(message);
            }
        } catch (Exception e) {
            log.error("无法解析此数据:{}", jsonStr);
        }
    }
}
