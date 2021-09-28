package com.zw.protocol.netty.client.coder.factory;

import com.alibaba.fastjson.JSON;
import com.zw.platform.util.ConvertUtil;
import com.zw.protocol.msg.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.List;

public class WebDecoder extends ByteToMessageDecoder {
    private static final Logger log = LogManager.getLogger(WebDecoder.class);

    public WebDecoder() {
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
        String msg = in.toString(in.readerIndex(), length, Charset.forName("GBK"));
        in.skipBytes(length);
        try {
            Message message = JSON.parseObject(msg, Message.class);
            if (message != null) {
                logMessage(message);
                out.add(message);
            }
        } catch (Exception e) {
            log.error("无法解析此数据: {}", msg);
        }
    }

    private void logMessage(Message message) {
        String id = ConvertUtil.toHexString(message.getDesc().getMsgID());
        log.info("收到消息, ID:{}, 监控对象:{}", id, message.getDesc().getMonitorName());
    }
}
