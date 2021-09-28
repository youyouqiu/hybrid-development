package com.zw.platform.domain.realTimeVideo;


import lombok.Data;

import java.io.Serializable;


@Data
public class CloudTerraceForm implements Serializable {
    /* 车辆ID */
    private String vehicleId;

    /* 通道号 */
    private Integer channelNum;

    /* value：0或1或2或3或4 */
    private Integer control;

    /**
     * 0:云台旋转 0x9301
     * 1:云台调整焦距控制 0x9302
     * 2:云台调整光圈控制 0x9303
     * 3:云台雨刷控制 0x9304
     * 4:红外补光控制 0x9305
     * 5:云台变倍控制 0x9306
     */
    private Integer type;

    private Integer speed;
}
