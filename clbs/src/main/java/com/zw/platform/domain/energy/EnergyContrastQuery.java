package com.zw.platform.domain.energy;

import java.io.Serializable;

import lombok.Data;

@Data
public class EnergyContrastQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String vehicleId;

    private long vtime;// gps时间

    private String status;//acc

    private String speed;//速度

    private String gpsMile;//里程

}