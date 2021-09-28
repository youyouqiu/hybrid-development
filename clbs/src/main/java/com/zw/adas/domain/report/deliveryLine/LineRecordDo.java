package com.zw.adas.domain.report.deliveryLine;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/***
 @Author lijie
 @Date 2021/1/8 16:12
 @Description 线路下发记录
 @version 1.0
 **/
@Data
public class LineRecordDo {
    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 车id
     */
    private String vehicleId;

    /**
     * 车牌
     */
    private String brand;

    /**
     * 车辆颜色
     */
    private Integer vehicleColor;

    /**
     * 线路id
     */
    private String lineId;

    /**
     * 线路id
     */
    private String lineUuid;

    /**
     * 风险围栏id
     */
    private String fenceConfigId;

    /**
     * 收到上级平台的路线信息时间
     */
    private Date receiveTime;

    /**
     * 流水号
     */
    private Integer swiftNumber;

    /**
     * 逻辑删除标记
     */
    private Integer flag;

}
