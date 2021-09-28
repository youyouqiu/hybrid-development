package com.zw.platform.domain.redis;

import com.zw.platform.domain.basicinfo.form.DeviceGroupForm;
import com.zw.platform.domain.basicinfo.form.SimGroupForm;
import com.zw.platform.domain.basicinfo.form.VehicleGroupForm;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


/**
 * 封装信息配置导入绑定的车、设备、simcard用来维护没有绑定的缓存
 */
@Data
public class RedisBindingBean {

    private Set<VehicleGroupForm> vehicleGroupForms;

    private Set<VehicleGroupForm> peopleGroupFprms;

    private Set<VehicleGroupForm> thingGroupFprms;

    private Set<DeviceGroupForm> deviceGroupForms;

    private Set<SimGroupForm> simGroupForms;

    public RedisBindingBean() {
        vehicleGroupForms = new HashSet<>();
        peopleGroupFprms = new HashSet<>();
        thingGroupFprms = new HashSet<>();
        deviceGroupForms = new HashSet<>();
        simGroupForms = new HashSet<>();
    }

    public void addVehicleGroup(VehicleGroupForm form) {
        vehicleGroupForms.add(form);
    }

    public void addPeopleGroup(VehicleGroupForm form) {
        peopleGroupFprms.add(form);
    }

    public void addThingGroup(VehicleGroupForm form) {
        thingGroupFprms.add(form);
    }

    public void addDeviceGroup(DeviceGroupForm form) {
        deviceGroupForms.add(form);
    }

    public void addSimGroup(SimGroupForm form) {
        simGroupForms.add(form);
    }
}
