package com.zw.platform.push.factory;

import com.zw.platform.push.command.AlarmMessageDTO;

/**
 * 报警联动处理
 *
 * @author Zhang Yanhui
 * @since 2020/9/28 15:46
 */

public interface AlarmChainHandler {

    /**
     * 处理报警消息
     *
     * @param alarmMessageDTO 报警消息摘要
     */
    void handle(AlarmMessageDTO alarmMessageDTO);
}
