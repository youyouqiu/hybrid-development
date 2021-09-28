package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/10/14 18:13
 */
@Data
public class Buses {
    /**
     * 企业编
     */
    private String dwid;
    /**
     * 行业编码
     */
    private Integer hylb;
    /**
     * 车辆编码
     */
    private String clid;
    /**
     * 1 公交 2 出租 3 农客
     * 行业id
     */
    private String cphm;
    /**
     * 车牌颜色
     */
    private String cpys;
    /**
     * 车架号
     */
    private String cjh;
    /**
     * 车辆状态
     */
    private Integer clzt;
}
