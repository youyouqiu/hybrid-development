package com.zw.adas.domain.define.setting;

import lombok.Data;


@Data
public class AdasParamInfo {
    private String vehicleId;

    private String paramType;

    private String paramId;

    private Integer msgSN;

    private String parameterName;
}
