package com.zw.platform.domain.singleVehicle;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 单车登录
 * 车牌号包装类，用于权限校验
 */

@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    /**
     * 车牌号
     */
    private String brand;

    public String getBrand() {
        return this.brand;
    }
}
