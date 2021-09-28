package com.zw.platform.basic.event;

import com.zw.platform.basic.dto.VehicleDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 车辆更新事件
 *
 * @author zhangjuan
 */
@Getter
public class VehicleUpdateEvent extends ApplicationEvent {
    /**
     * 修改后的车辆信息
     */
    private List<VehicleDTO> curVehicleList;

    /**
     * 修改前的车辆信息
     */
    private List<VehicleDTO> oldVehicleList;

    public VehicleUpdateEvent(Object source, List<VehicleDTO> curVehicleList, List<VehicleDTO> oldVehicleList) {
        super(source);
        this.curVehicleList = curVehicleList;
        this.oldVehicleList = oldVehicleList;
    }
}
