package com.zw.platform.domain.oilsubsidy.subsidyManage;

import lombok.Data;

import java.util.List;

/**
 * 车辆定位信息补报消息（0x1203）
 * @author penghj
 * @version 1.0
 * @date 2021/3/29 14:34
 */
@Data
public class VehicleLocationSupplementaryData {
    /**
     * 补传数据总数，范围1~10
     */
    private Integer gnssCnt;
    /**
     * 定位详情集合
     */
    private List<VehicleLocationSupplementaryInfo> gpsList;
}
