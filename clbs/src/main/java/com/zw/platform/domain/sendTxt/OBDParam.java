package com.zw.platform.domain.sendTxt;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2018/10/10 15:27
 */
@Data
public class OBDParam implements Serializable {

    /**
     * 外设 ID OBD：0xA0
     */
    private Integer sensorID;
    /**
     * 消息长度 0x08
     */
    private Integer dataLen;
    /**
     * 车型ID
     */
    private Long vehicleTypeId;
    /**
     * 单位 ms；默认1000ms  范围：0x0001-0xFFFF
     0xFFFFFFFF表示不修改
     */
    private Integer uploadTime;

    private String vehicleId;
}
