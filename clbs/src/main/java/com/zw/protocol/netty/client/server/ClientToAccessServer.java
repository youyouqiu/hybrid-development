package com.zw.protocol.netty.client.server;

import com.zw.protocol.netty.TCPClientExecutor;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;

public class ClientToAccessServer extends TCPClientExecutor {

    @Override
    protected void registerChannel() {
        super.setConnectCount(super.getConnectCount() + 1);
        WebSubscribeManager.getInstance().putChannel(id, channel);
    }

}