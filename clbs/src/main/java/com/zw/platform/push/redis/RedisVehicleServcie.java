package com.zw.platform.push.redis;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.singleVehicle.SingleLocationInfo;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jiangxiaoqiang on 2016/11/3.
 */
@Log4j2
@Component
@Deprecated
public class RedisVehicleServcie {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ThingService thingService;

    @Autowired
    private MonitorIconService monitorIconService;

    public void getMonitorDetail(String monitorId, SingleLocationInfo info) {
        try {
            BindDTO bindDTO = MonitorUtils.getBindDTO(monitorId);
            MonitorInfo monitorInfo = info.getMonitorInfo();
            if (bindDTO == null) {
                log.error("未获取到监控对象绑定信息，车id:" + monitorId);
                return;
            }
            if (monitorInfo == null) {
                monitorInfo = new MonitorInfo();
            }

            // 组装绑定信息
            monitorInfo.setMonitorId(bindDTO.getId());
            monitorInfo.setMonitorName(bindDTO.getName());
            monitorInfo.setAssignmentName(bindDTO.getGroupName());
            monitorInfo.setSimcardNumber(bindDTO.getSimCardNumber());
            monitorInfo.setDeviceNumber(bindDTO.getDeviceNumber());
            monitorInfo.setGroupName(bindDTO.getOrgName());
            monitorInfo.setProfessionalsName(bindDTO.getProfessionalNames());
            monitorInfo.setTerminalManufacturer(bindDTO.getTerminalManufacturer());
            monitorInfo.setTerminalType(bindDTO.getTerminalType());
            String icoName = monitorIconService.getMonitorIcon(monitorId);
            if (StringUtils.isNotBlank(icoName)) {
                monitorInfo.setMonitorIcon(icoName);
            }
            if ("0".equals(bindDTO.getMonitorType())) { // 车
                VehicleDTO vehicleDTO = vehicleService.getById(monitorId);
                if (vehicleDTO != null) {
                    // 组装车辆信息
                    monitorInfo.setMonitorType(0);
                    monitorInfo.setVehicleType(vehicleDTO.getVehicleType());
                    monitorInfo.setPlateColorName(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                    monitorInfo.setPlateColor(vehicleDTO.getPlateColor());
                }
            } else if ("1".equals(bindDTO.getMonitorType())) { // 人
                monitorInfo.setMonitorType(1);
            } else { // 物
                ThingDTO thingDTO = thingService.getById(monitorId);
                if (thingDTO != null) {
                    monitorInfo.setMonitorType(2);
                    monitorInfo.setVehicleType(thingDTO.getTypeName());
                    monitorInfo.setLabel(thingDTO.getLabel());
                    monitorInfo.setModel(thingDTO.getModel());
                    monitorInfo.setMaterial(thingDTO.getMaterial());
                    monitorInfo.setWeight(String.valueOf(thingDTO.getWeight()));
                    monitorInfo.setSpec(thingDTO.getSpec());
                    monitorInfo
                        .setProductDate(DateUtil.formatDate(thingDTO.getProductDate(), DateUtil.DATE_Y_M_D_FORMAT));
                }
            }
            if (info.getMonitorInfo() == null) {
                info.setMonitorInfo(monitorInfo);
            }
        } catch (Exception e) {
            log.error("从redis中获取监控对象信息遇到错误", e);
        }
    }

}
