package com.zw.platform.event;

import org.springframework.context.ApplicationEvent;

public class ConfigUnbindVehicleEvent extends ApplicationEvent {
    /**
     * 单个监控对象
     */
    public static final int TYPE_SINGLE = 0;
    /**
     * 多个监控对象; 逗号分隔;
     */
    public static final int TYPE_MORE = 1;

    private String vehicleId;
    private Integer type;

    public ConfigUnbindVehicleEvent(Object source, String vehicleId, Integer type) {
        super(source);
        this.vehicleId = vehicleId;
        this.type = type;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Integer getType() {
        return type;
    }
}
