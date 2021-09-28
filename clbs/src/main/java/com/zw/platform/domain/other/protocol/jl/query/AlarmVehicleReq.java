package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 11:53
 */
@Data
public class AlarmVehicleReq {
    /**
     * 监控对象ID
     */
    @NotEmpty
    private Set<String> monitorIds;
    /**
     * 报警开始时间
     */
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmStartTime;
    /**
     * 报警结束时间
     */
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmEndTime;
    /**
     * 报警类型 0:紧急报警; 10:疲劳报警; 200:禁入报警; 201:禁出报警; 210:偏航报警
     * 41:超速报警; 53:夜间行驶报警;
     */
    @NotNull
    private Integer alarmType;
    /**
     * 报警处理状态 1:处理中; 2:已处理完毕; 3:不做处理; 4:将来处理;
     */
    @NotNull
    private Integer alarmHandleStatus;

}
