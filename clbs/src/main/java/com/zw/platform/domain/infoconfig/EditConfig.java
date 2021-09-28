package com.zw.platform.domain.infoconfig;

import lombok.Data;

/**
 * Created by Administrator on 2016/9/20.
 */
@Data
public class EditConfig {
    /** 信息配置 */
    private String configId;
    /** 车辆id */
    private String vehicleId;
    /** 分组id */
    private String groupId;
    /** 终端ID */
    private String deviceId;
    /** SIM卡ID */
    private String simCardId;
    private String configIdForBrand;
    private String configIdForDevice;
    private String configIdForSim;
}
