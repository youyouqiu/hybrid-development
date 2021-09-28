package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by wjy on 2017/6/13.
 */
@Data
public class AddressParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gps_latitude;
    private String gps_longitude;
    private String decID;
    private String time;
}
