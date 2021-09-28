package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取燃料类型
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FuelTypeDO extends BaseDO {
    /**
     * 燃料类型
     */
    private String fuelType;

    /**
     * 燃料类别
     */
    private String fuelCategory;

    /**
     * 描述信息
     */
    private String describes;
}
