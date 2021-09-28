package com.zw.adas.push.nettyclient.client;

import com.zw.adas.push.nettyclient.AdasTCPClientExecutor;
import com.zw.adas.push.nettyclient.manager.AdasWebSubscribeManager;

public class AdasWebClient extends AdasTCPClientExecutor {

    @Override
    protected void registerChannel() {
        AdasWebSubscribeManager.getInstance().putChannel(id, channel);
    }
}
