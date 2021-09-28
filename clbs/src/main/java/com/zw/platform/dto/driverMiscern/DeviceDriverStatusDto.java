package com.zw.platform.dto.driverMiscern;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 终端驾驶员识别管理下发状态及结果DTO
 * @author tianzhangxu
 * @date 2020/9/29 11:50
 */
@Data
@NoArgsConstructor
public class DeviceDriverStatusDto implements Serializable {
    private static final long serialVersionUID = -598893289472852839L;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 下发状态 0:等待下发; 1:下发失败; 2:下发中; 3:下发成功
     */
    private Integer issueStatus;

    /**
     * 下发结果 0:终端已应答 1:终端未应答 2:终端离线
     */
    private Integer issueResult;

    /**
     * 驾驶人员列表 - 查询成功时间. query_success_time
     */
    private Date querySuccessTime;

    public DeviceDriverStatusDto(String vehicleId, Integer issueStatus, Integer issueResult) {
        this(vehicleId, issueStatus, issueResult, null);
    }

    public DeviceDriverStatusDto(String vehicleId, Integer issueStatus, Integer issueResult, Date querySuccessTime) {
        this.vehicleId = vehicleId;
        this.issueStatus = issueStatus;
        this.issueResult = issueResult;
        this.querySuccessTime = querySuccessTime;
    }
}
