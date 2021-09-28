package com.zw.platform.domain.oilsubsidy.line;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 方向和站点中间表实体-与表一一对应
 * @date 2020/10/911:38
 */
@Data
@AllArgsConstructor
public class DirectionStationMiddleDO {

    /**
     * 上下行ID
     */
    private String directionInfoId;
    /**
     * 站点ID
     */
    private String stationInfoId;
    /**
     * 顺序
     */
    private Short stationInfoOrder;
    /**
     * 方向信息(0代表上行，1代表下行)
     */
    private Integer directionType;
}
