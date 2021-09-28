package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.FuelTypeDO;
import lombok.Data;

@Data
public class FuelTypeDTO {
    /**
     * 主键ID
     */
    private String id;
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

    public FuelTypeDTO(FuelTypeDO fuelTypeDO) {
        this.id = fuelTypeDO.getId();
        this.fuelType = fuelTypeDO.getFuelType();
        this.fuelCategory = fuelTypeDO.getFuelCategory();
        this.describes = fuelTypeDO.getDescribes();
    }
}
