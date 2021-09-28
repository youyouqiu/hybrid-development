package com.zw.platform.domain.infoconfig.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class AssignmentVehicleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    private String assignmentId;

    /**
     * 监控对象ID
     */
    private String vehicleId;

    /**
     * 监控对象数量
     */
    private int monitorNum = 1;
}
