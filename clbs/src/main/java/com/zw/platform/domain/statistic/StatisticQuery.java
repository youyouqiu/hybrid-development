package com.zw.platform.domain.statistic;

import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.UuidUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author zhouzongbo on 2018/9/10 16:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = -6975902634405843380L;

    /**
     * 传感器2
     */
    public static final int SENSOR_SEQUENCE_TWO = 1;
    /**
     * 传感器1
     */
    public static final int SENSOR_SEQUENCE_ONE = 0;

    /**
     * 开始时间
     */
    private Long startTime;
    private String startTimeStr;

    /**
     * 结束时间
     */
    private Long endTime;
    private String endTimeStr;

    /**
     * 车辆id
     */
    private String vehicleId;
    private byte[] vehicleByteId;

    /**
     * 适用于: 传感器报表（工时、载重等）
     */
    private Integer sensorSequence;

    /**
     * 轮胎号（胎压报表）
     */
    private Integer tyreNumber;

    private Integer status;

    /**
     * 单个车辆ID 组装 POSITIONAL 查询数据
     * @param query query
     */
    public static void buildPositionalQuery(StatisticQuery query) {
        byte[] vehicleByteId = UuidUtils.getBytesFromUUID(UUID.fromString(query.getVehicleId()));
        query.setVehicleByteId(vehicleByteId);
        query.setStartTime(LocalDateUtils.parseDateTime(query.getStartTimeStr()).getTime() / LocalDateUtils.SECOND);
        query.setEndTime(LocalDateUtils.parseDateTime(query.getEndTimeStr()).getTime() / LocalDateUtils.SECOND);
    }
}
