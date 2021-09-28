package com.zw.platform.dto.netaccessproveforward;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 11:27
 */
@Data
public class NetAccessProveForwardVehicleDto implements Serializable {
    private static final long serialVersionUID = -4238154709114054602L;

    /**
     * 车id
     */
    private String vehicleId;
    /**
     * 车牌号
     */
    private String brand;
    /**
     * 平台名称
     */
    private String plantFormName;
    /**
     * 平台id
     */
    private String plantFormIp;
    /**
     * 端口
     */
    private String plantFormPort;

    public NetAccessProveForwardVehicleDto() {
    }

    public NetAccessProveForwardVehicleDto(String vehicleId, String brand, String plantFormName, String plantFormIp,
        String plantFormPort) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.plantFormName = plantFormName;
        this.plantFormIp = plantFormIp;
        this.plantFormPort = plantFormPort;
    }
}
