package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class VehicleType implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String vehicleCategory;//车辆类别id
    private String category;//车辆类别
    private String ico;//图标ID
    private String icoName;//图标名字
    private String vehicleType;//车辆类型
    private String description;//类型描述
    private Short flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private Integer standard;
    private String codeNum;  //识别码

    /**
     * 车辆保养间隔(KM) 最大5位正整数
     */
    private Integer serviceCycle;

    public VehicleType(VehicleCategoryDTO vehicleCategory) {
        this.id = vehicleCategory.getId();
        this.vehicleCategory = vehicleCategory.getCategory();
        this.category = vehicleCategory.getCategory();
        this.ico = vehicleCategory.getIconId();
        this.icoName = vehicleCategory.getIconName();
        this.description = vehicleCategory.getDescription();
        this.standard = vehicleCategory.getStandard();
    }

    public VehicleType(VehicleTypeDTO vehicleType) {
        this.id = vehicleType.getId();
        this.vehicleCategory = vehicleType.getCategoryId();
        this.category = vehicleType.getCategory();
        this.codeNum = vehicleType.getCodeNum();
        this.serviceCycle = vehicleType.getServiceCycle();
        this.description = vehicleType.getDescription();
        this.vehicleType = vehicleType.getType();
    }
}
