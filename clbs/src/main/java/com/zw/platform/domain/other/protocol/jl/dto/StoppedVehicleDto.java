package com.zw.platform.domain.other.protocol.jl.dto;

import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description:
 * @date 2020/6/12
 **/
@Data
public class StoppedVehicleDto {

    private String monitorId;

    private String monitorName;

    private String plateColor;

    private String plateColorStr;

    private String day;

    /**
     * 状态  0	正常 1未定位 2离线
     */
    private Integer status;

    /**
     * 定位总数
     */
    private Integer totalNum;

    /**
     * 无效定位数
     */
    private Integer invalidNum;

    private String groupName;
}
