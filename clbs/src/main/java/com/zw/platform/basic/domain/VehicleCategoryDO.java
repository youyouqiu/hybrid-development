package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * zw_m_vehicle_category
 *
 * @author zhangjuan 2020-10-16
 */
@Data
@NoArgsConstructor
public class VehicleCategoryDO {
    /**
     * 车辆类别id
     */
    private String id;
    /**
     * 车辆类别
     */
    private String vehicleCategory;
    /**
     * 车辆或人的图标
     */
    private String ico;
    private String icoName;
    /**
     * 描述
     */
    private String description;
    /**
     * flag
     */
    private Integer flag;
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
     * 标准（0：通用；1：货运；2：工程机械）
     */
    private Integer standard;

    /**
     * 识别码
     */
    private String codeNum;

    /**
     * VehicleCategoryDTO 转 vehicleCategoryDTO
     *
     * @param vehicleCategoryDTO vehicleCategoryDTO
     */
    public VehicleCategoryDO(VehicleCategoryDTO vehicleCategoryDTO) {
        if (Objects.isNull(vehicleCategoryDTO.getId())) {
            this.id = UUID.randomUUID().toString();
            this.createDataTime = new Date();
            this.createDataUsername = SystemHelper.getCurrentUsername();
        } else {
            this.id = vehicleCategoryDTO.getId();
            this.updateDataTime = new Date();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }

        this.vehicleCategory = vehicleCategoryDTO.getCategory();
        this.ico = vehicleCategoryDTO.getIconId();
        this.flag = 1;
        this.standard = vehicleCategoryDTO.getStandard();
        this.description = vehicleCategoryDTO.getDescription();
    }
}
