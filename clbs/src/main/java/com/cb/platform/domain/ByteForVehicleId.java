package com.cb.platform.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hujun
 * @date 2018/6/12 10:00
 */
@Data
public class ByteForVehicleId implements Serializable{
    private static final long serialVersionUID = 1L;
    byte[] vehicleId;
}
