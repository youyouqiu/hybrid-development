package com.zw.platform.dto.location;

import lombok.Data;


/**
 * 地址DTO
 * @author Administrator
 */
@Data
public class AddressDTO {
    /**
     * 经纬度坐标
     */
    private String coordinate;

    /**
     * 具体地址
     */
    private String address;
}
