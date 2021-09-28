package com.zw.platform.domain.forwardplatform;


import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/3/1.
 */
@Data
public class ThirdPlatFormConfigView {
    private String id;
    private String configId;
    private String thirdPlatformId;
    /**
     * IP地址
     */
    private String thirdPlatformIp;
    /**
     * port
     */
    private Integer thirdPlatformPort;
    /**
     * 车牌号
     */
    private String brand;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 车辆ID
     */
    private String vehicleId;
    /**
     * 第三方平台连接描述
     */
    private String thirdPlatformDescription;
}
