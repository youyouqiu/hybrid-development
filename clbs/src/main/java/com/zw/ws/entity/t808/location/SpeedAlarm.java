package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by wjy on 2017/5/3.
 */
@Data
public class SpeedAlarm implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer lineID;
    private Integer type;
}
