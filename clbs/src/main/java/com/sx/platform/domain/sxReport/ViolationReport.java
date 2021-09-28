package com.sx.platform.domain.sxReport;


import lombok.Data;


/**
 * @author  yangyi
 * 查询报警违章明细实体类
 */
@Data
public class ViolationReport {

    private byte[] vehicleId; // 车辆ID

    private long alarmStartTime; // 报警开始时间

    private Integer calStandard; // 报警标准

    private Integer alarmSource; // 报警来源

    private Integer alarmType; // 报警类型

    private long alarmEndTime; // 报警结束时间

    private String startLocation = ""; // 超速开始位置

    private String alarmCount = ""; // 超速次数
    
    private Long durationTime; // 违章持续时长

}
