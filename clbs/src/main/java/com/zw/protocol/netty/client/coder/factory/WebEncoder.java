package com.zw.protocol.netty.client.coder.factory;

import com.alibaba.fastjson.JSON;
import com.zw.platform.util.ConstantUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class WebEncoder extends MessageToByteEncoder<Object> {
    protected void encode(ChannelHandlerContext context, Object object, ByteBuf buf) {
        String msgStr = JSON.toJSONString(object);
        byte[] msgBytes = msgStr.getBytes(ConstantUtil.T808_STRING_CODE);
        buf.writeInt(msgBytes.length);
        buf.writeBytes(msgBytes);
    }
}