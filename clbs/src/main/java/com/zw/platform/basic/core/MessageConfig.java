package com.zw.platform.basic.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 返回信息配置文件
 * @author zhangjuan
 */
@Component
@Getter
public class MessageConfig {
    @Value("${exist.bound}")
    private String existBound;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${config.delete.msg}")
    private String configDeleteMsg;

    @Value("${config.delete.brand.msg}")
    private String configDeleteBrandMsg;

    @Value("${config.delete.sim.msg}")
    private String configDeleteSimMsg;

    @Value("${config.delete.device.msg}")
    private String configDeleteDeviceMsg;

    @Value("${intercom.object.max.assignment}")
    private String intercomObjectMaxAssignment;

    @Value("${vehicle.brand.bound}")
    private String vehicleBrandBound;

}
