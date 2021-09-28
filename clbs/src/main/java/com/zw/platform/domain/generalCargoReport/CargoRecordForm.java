package com.zw.platform.domain.generalCargoReport;

import lombok.Data;

import java.util.Date;


/**
 * @author CJY
 */
@Data
public class CargoRecordForm {
    /**
     * 时间
     */
    private Date time;

    /**
     * 企业id
     */
    private String groupId;

    /**
     * 操作类型，1：新增，2：修改，3：删除
     */
    private Integer type;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 营运状态，1：营运，0：停运
     */
    private String operatingState;
}
