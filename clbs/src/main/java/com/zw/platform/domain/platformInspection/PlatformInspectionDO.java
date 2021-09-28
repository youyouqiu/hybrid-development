package com.zw.platform.domain.platformInspection;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 *  巡检记录表
 * @author create by  on 2020-11-17.
 */
@Data
public class PlatformInspectionDO {

    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 车id
     */
    private String vehicleId;

    /**
     * 巡检的类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检)
     */
    private Integer inspectionType;

    /**
     * 巡检人
     */
    private String inspector;

    /**
     * 巡检时间
     */
    private Date inspectionTime;

    /**
     * 巡检状态(1下发中，2下发成功，3终端响应超时，4终端离线
     */
    private Integer inspectionStatus;

    /**
     * 巡检结果的表的id
     */
    private String inspectionResultId;

    /**
     * 逻辑删除标记
     */
    private Integer flag;
}