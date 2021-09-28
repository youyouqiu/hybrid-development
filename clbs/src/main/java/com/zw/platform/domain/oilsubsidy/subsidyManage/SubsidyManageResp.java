package com.zw.platform.domain.oilsubsidy.subsidyManage;

import lombok.Data;

import java.io.Serializable;

/**
 * 补发管理详情出参实体
 * @author tianzhangxu
 * @Date 2021/3/26 9:20
 */
@Data
public class SubsidyManageResp implements Serializable {
    private static final long serialVersionUID = 3477966365826002328L;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;
}
