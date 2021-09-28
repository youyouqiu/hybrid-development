package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.VehicleDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * zw_m_cargo_group_vehicle
 *
 * @author zhangjuan 2020-10-27
 */
@Data
@NoArgsConstructor
public class CargoGroupVehicleDO {
    /**
     * 普货企业车辆表，用于记录历史
     */
    private String id;
    /**
     * 操作时间
     */
    private Date time;
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 操作类型，1：新增，2：修改，3：删除
     */
    private Integer type;
    /**
     * 车辆ID
     */
    private String vehicleId;
    /**
     * 营运状态，1：营运，0：停运
     */
    private Integer operatingState;
    private Integer flag;

    private int getTypeByOperate(String operation) {
        //默认是删除
        int type;
        switch (operation) {
            case "add":
                type = 1;
                break;
            case "update":
                type = 2;
                break;
            case "delete":
                type = 3;
                break;
            default:
                type = 3;
        }
        return type;
    }

    public CargoGroupVehicleDO(VehicleDTO vehicle, String operation) {
        this.id = UUID.randomUUID().toString();
        this.time = new Date();
        this.orgId = vehicle.getOrgId();
        this.type = getTypeByOperate(operation);
        this.vehicleId = vehicle.getId();
        this.operatingState = vehicle.getIsStart();
        this.flag = 1;
    }
}
