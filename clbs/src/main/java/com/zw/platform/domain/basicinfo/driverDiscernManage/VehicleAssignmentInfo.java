package com.zw.platform.domain.basicinfo.driverDiscernManage;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 车辆分组关系实体
 * @Author Tianzhangxu
 * @Date 2020/11/3 16:18
 */
@Data
public class VehicleAssignmentInfo implements Serializable {
    private static final long serialVersionUID = 6291042887629593999L;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 分组名称（多个逗号分隔）
     */
    private String assignmentName;
}
