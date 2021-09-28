package com.zw.platform.domain.reportManagement.form;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehOnlineStatusForm {
    private static final long serialVersionUID = 1L;

    private String vehId;

    private byte[] vehicleId;// 车辆id

    private Long date; //创建时间

    private String dateStr;

    private Integer warningNum;//预警数

    private Long lastTime;//最后在线时间

    private String lastTimeStr;

    private Double lastLat; //最后在线纬度

    private Double lastLon; //最后在线经度

    private String address;//转换后的地址





}
