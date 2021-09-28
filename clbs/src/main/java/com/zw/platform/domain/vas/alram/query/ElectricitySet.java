package com.zw.platform.domain.vas.alram.query;

import lombok.Data;

@Data
public class ElectricitySet {
    /**
     * 保留项1 长度 22
     */
    private byte[] keep1 = new byte[10];
    /**
     * 数据标识
     */
    private Integer dataSign = 0;

    /**
     * 保留项2
     */
    private byte keep2 = 0;

    /**
     * 设备电量
     */
    private Integer deviceElectricity = 0XFFFF;
    /**
     * 行车电量
     */
    private Integer drivingElectricity = 0XFFFF;
    /**
     * 冷藏电量
     */
    private Integer coldStorageElectricity = 0XFFFF;
    /**
     * 保留项1 长度 28
     */
    private byte[] keep3 = new byte[38];
}
