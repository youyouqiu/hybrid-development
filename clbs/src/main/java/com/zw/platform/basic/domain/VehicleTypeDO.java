package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * zw_m_vehicle_type
 * @author zhangjuan 2020-10-16
 */
@Data
@NoArgsConstructor
public class VehicleTypeDO {
    /**
     * 车辆类型
     */
    private String id;
    /**
     * 车辆类别id
     */
    private String vehicleCategory;
    private String category;
    /**
     * 车辆类型
     */
    private String vehicleType;
    /**
     * 类型描述
     */
    private String description;
    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private String updateDataUsername;
    /**
     * flag
     */
    private Integer flag;
    /**
     * 车辆类型id
     */
    private String pid;
    /**
     * 图片id
     */
    private String icoId;
    /**
     * 车辆子类型名
     */
    private String vehicleSubtypes;
    /**
     * 行驶方式（0：自行；1：运输）
     */
    private Integer drivingWay;
    /**
     * 识别码
     */
    private String codeNum;

    /**
     * 标准(车辆类别中的字段，不需要导出)（0：通用；1：货运；2：工程机械）
     */
    private String standard;
    /**
     * 车辆保养间隔(km)
     */
    private Integer serviceCycle;

    public VehicleTypeDO(VehicleTypeDTO typeDTO) {
        if (Objects.isNull(typeDTO.getId())) {
            this.id = UUID.randomUUID().toString();
            this.createDataTime = new Date();
            typeDTO.setId(id);
            this.createDataUsername = SystemHelper.getCurrentUsername();
        } else {
            this.id = typeDTO.getId();
            this.updateDataTime = new Date();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }
        this.vehicleCategory = typeDTO.getCategoryId();
        this.vehicleType = typeDTO.getType();
        this.description = typeDTO.getDescription();
        this.serviceCycle = typeDTO.getServiceCycle();
    }

    public VehicleTypeDO(VehicleSubTypeDTO subTypeDTO) {
        if (Objects.isNull(subTypeDTO.getId())) {
            this.id = UUID.randomUUID().toString();
            subTypeDTO.setId(id);
            this.createDataTime = new Date();
            this.createDataUsername = SystemHelper.getCurrentUsername();
        } else {
            this.id = subTypeDTO.getId();
            this.updateDataTime = new Date();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }
        this.vehicleCategory = subTypeDTO.getCategoryId();
        this.pid = subTypeDTO.getTypeId();
        this.vehicleType = subTypeDTO.getType();
        this.description = subTypeDTO.getDescription();
        this.vehicleSubtypes = subTypeDTO.getSubType();
        this.icoId = subTypeDTO.getIconId();
        this.drivingWay = subTypeDTO.getDrivingWay();
        this.flag = 1;
    }

    public VehicleTypeDO(VehicleTypeForm vehicleTypeForm) {
        this.id = vehicleTypeForm.getId();
        this.vehicleCategory = vehicleTypeForm.getVehicleCategory();
        this.vehicleType = vehicleTypeForm.getVehicleType();
        this.description = vehicleTypeForm.getDescription();
        this.createDataTime = vehicleTypeForm.getCreateDataTime();
        this.createDataUsername = vehicleTypeForm.getCreateDataUsername();
        this.updateDataTime = vehicleTypeForm.getUpdateDataTime();
        this.updateDataUsername = vehicleTypeForm.getUpdateDataUsername();
        this.flag = vehicleTypeForm.getFlag();
        this.pid = vehicleTypeForm.getId();
        this.icoId = vehicleTypeForm.getIco();
        this.serviceCycle = vehicleTypeForm.getServiceCycle();
    }
}
