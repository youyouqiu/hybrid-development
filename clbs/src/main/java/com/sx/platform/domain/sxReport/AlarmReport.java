package com.sx.platform.domain.sxReport;


import lombok.Data;

/**
 * @author  yangyi
 * 查询报警明细实体类
 */
@Data
public class AlarmReport {
    private byte[] vehicleId; // 车辆ID

    private Integer speedType; // 限速类型

    private Integer alarmType;//报警来源

    private Integer alarmSource;//报警来源

    private Integer calStandard;//超速标准

    private String speed; // 最高速度

    private Long alarmStartTime; // 报警开始时间

    private Long alarmEndTime; // 报警结束时间

    private String alarmStartLocation = " "; // 报警开始位置

    private String alarmEndLocation = ""; // 报警结束位置



}
