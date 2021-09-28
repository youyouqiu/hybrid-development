package com.zw.platform.util;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.service.basicinfo.VehicleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorTypeUtil {

    @Autowired
    private VehicleService vehicleService;
    private static Logger log = LogManager.getLogger(MonitorTypeUtil.class);

    /**
     * 根据对象类型查询车、物的类型
     * @deprecated 需要重写查询逻辑
     */
    public String findByMonitorTypeAndId(String monitorType, String id) {
        try {
            //如果对象类型为空或者id为空或者对象类型为人则直接返回
            if (StringUtils.isBlank(monitorType) || StringUtils.isBlank(id) || "1".equals(monitorType)) {
                return "";
            }
            if ("0".equals(monitorType)) {
                VehicleInfo vehicleInfo = vehicleService.findVehicleById(id);
                if (vehicleInfo != null) {
                    return vehicleInfo.getVehiclet();
                }
            } else if ("2".equals(monitorType)) {
                return "其他物品";
            }
        } catch (Exception e) {
            log.error("根据对象类型和Id获取车、物的类型错误", e);
        }

        return null;
    }

}
