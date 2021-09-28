package com.zw.platform.domain.vas.alram;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

@Data
public class OutputControlSend implements T808MsgBody {

    private String vid;

    private Integer type;

    private Integer sensorId;

    private Integer sign;

    private Integer controlType;

    private Integer controlStauts;

    private Integer controlIo;

    private Integer controlTime;

}
