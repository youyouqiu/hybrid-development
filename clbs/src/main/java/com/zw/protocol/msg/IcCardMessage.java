package com.zw.protocol.msg;

import lombok.Data;

@Data
public class IcCardMessage implements MsgBean {

    private String vehicleId;
    private Object data;
    private Integer type;

    public IcCardMessage() {
    }

    public IcCardMessage(String vehicleId, Object data, Integer type) {
        this.vehicleId = vehicleId;
        this.data = data;
        this.type = type;
    }
}
