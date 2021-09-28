package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouzongbo on 2018/4/17 10:50
 */
@Data
@NoArgsConstructor
public class VehicleSubTypeInfo implements Serializable {
    private static final long serialVersionUID = 2658565748628225581L;

    private String id;
    /**
     * 类别Id
     */
    private String vehicleCategory;

    /**
     * 车辆类别名
     */
    private String category;
    /**
     * 车辆类型id
     */
    private String pid;

    private String vehicleType;

    private String description;

    private String icoId;

    private String icoName;

    private String vehicleSubtypes;
    /**
     * 行驶方式（0：自行；1：运输）
     */
    private String drivingWay;
    private Short flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

    public VehicleSubTypeInfo(VehicleSubTypeDTO subType) {
        this.id = subType.getId();
        this.vehicleCategory = subType.getCategoryId();
        this.category = subType.getCategory();
        this.pid = subType.getTypeId();
        this.vehicleType = subType.getType();
        this.icoId = subType.getIconId();
        this.icoName = subType.getIconName();
        this.vehicleSubtypes = subType.getSubType();
        this.drivingWay = String.valueOf(subType.getDrivingWay());
        this.description = subType.getDescription();
    }
}
