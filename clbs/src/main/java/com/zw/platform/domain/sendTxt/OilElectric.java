package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


@Data
public class OilElectric implements T808MsgBody {
    private Integer type;

    private Integer sensorId;

    private Integer sign;

    private Integer controlType;

    private Integer controlStauts;

    private Integer controlIo;

    private Integer controlTime;
}
