package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * zw_m_vehicle_purpose
 *
 * @author zhangjuan 2020-10-14
 */
@Data
@NoArgsConstructor
public class VehiclePurposeDO {
    /**
     * 车辆用途
     */
    private String id;
    /**
     * 车辆用途类别
     */
    private String purposeCategory;
    /**
     * 说明
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
     * 识别码
     */
    private String codeNum;

    /**
     * 常用构造函数
     *
     * @param id              id
     * @param purposeCategory purposeCategory
     * @param description     description
     * @param codeNum         codeNum
     */
    public VehiclePurposeDO(String id, String purposeCategory, String description, String codeNum) {
        this.id = id;
        this.purposeCategory = purposeCategory;
        this.description = description;
        this.codeNum = codeNum;
    }
}
