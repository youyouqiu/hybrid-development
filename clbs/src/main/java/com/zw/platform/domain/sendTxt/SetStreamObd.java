package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;

@Data
public class SetStreamObd implements Serializable {

    private static final long serialVersionUID = 4256998346040327393L;

    private Long vehicleTypeId;

    private Integer uploadTime;
}
