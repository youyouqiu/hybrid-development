package com.zw.protocol.netty.client.server;

import com.zw.protocol.netty.TCPClientExecutor;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;

public class VideoServer extends TCPClientExecutor {

    @Override
    protected void registerChannel() {
        WebSubscribeManager.getInstance().putVideoChannel(id, channel);
    }
}
