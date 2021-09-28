package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * @author  Tdz
 * @create 2017-04-14 14:22
 **/
@Data
public class LineOutAlarm implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer lineID;
    private Integer type;
    //方向：0：进；	1：出
    private Integer direction;
}
