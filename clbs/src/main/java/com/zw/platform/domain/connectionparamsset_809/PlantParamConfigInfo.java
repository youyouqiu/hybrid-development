package com.zw.platform.domain.connectionparamsset_809;

import lombok.Data;


/**
 * 监控对象绑定转发平台信息
 */
@Data
public class PlantParamConfigInfo {
    // 监控对象绑定id
    private String vehicleConfigId;
    private Integer centerId;
    private String zoneDescription;//行政区号
    private String groupId;//所属企业id
}
