package com.zw.platform.event;

import com.zw.platform.domain.infoconfig.ConfigList;
import org.springframework.context.ApplicationEvent;

/**
 * 信息录入绑定修改监听事件
 */
public class ConfigUpdateEvent extends ApplicationEvent {
    private ConfigList oldConfig;
    private ConfigList newConfig;
    private String intercomJsonStr;

    public ConfigUpdateEvent(Object source, ConfigList oldConfig, ConfigList newConfig, String intercomJsonStr) {
        super(source);
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
        this.intercomJsonStr = intercomJsonStr;
    }

    public ConfigList getOldConfig() {
        return oldConfig;
    }

    public ConfigList getNewConfig() {
        return newConfig;
    }

    public String getIntercomJsonStr() {
        return intercomJsonStr;
    }
}
