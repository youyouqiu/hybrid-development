package com.zw.platform.basic.event;

import com.zw.platform.basic.dto.BindDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 信息配置修改事件
 */
@Getter
public class ConfigUpdateEvent extends ApplicationEvent {
    /**
     * 当前绑定信息
     */
    private BindDTO curBindDTO;

    /**
     * 历史绑定信息
     */
    private BindDTO oldBindDTO;

    public ConfigUpdateEvent(Object source, BindDTO curBindDTO, BindDTO oldBindDTO) {
        super(source);
        this.curBindDTO = curBindDTO;
        this.oldBindDTO = oldBindDTO;
    }
}
