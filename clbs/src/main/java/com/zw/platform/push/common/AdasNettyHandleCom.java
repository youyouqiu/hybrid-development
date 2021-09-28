package com.zw.platform.push.common;

import com.zw.protocol.msg.Message;

/**
 * 实现处理adas1208的逻辑
 *
 */
public interface AdasNettyHandleCom {
    /**
     * 处理ftp的1208消息
     * @param message
     */
    void deal1208Message(Message message);

    /**
     * 处理文件流的9212消息
     * @param message
     */
    void deal9212Message(Message message);
}
