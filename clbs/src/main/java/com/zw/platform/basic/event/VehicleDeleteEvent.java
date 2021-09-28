package com.zw.platform.basic.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 车辆删除事件
 * @author zhangjuan
 */
@Getter
public class VehicleDeleteEvent extends ApplicationEvent {
    private List<String> ids;
    private String ipAddress;

    public VehicleDeleteEvent(Object source, List<String> ids, String ipAddress) {
        super(source);
        this.ids = ids;
        this.ipAddress = ipAddress;
    }
}
