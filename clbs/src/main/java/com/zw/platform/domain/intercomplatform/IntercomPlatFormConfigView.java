package com.zw.platform.domain.intercomplatform;


import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/3/1.
 */
@Data
public class IntercomPlatFormConfigView {
    private String id;
    private String configId;
    private String intercomPlatformId;
    private String intercomPlatformIP;//IP地址
    private Integer intercomPlatformPort;//port
    private String brand;//车牌号
    private String vehicleId;//车辆ID
    private String intercomPlatformName;
    private String intercomPlatformDescription;
}
