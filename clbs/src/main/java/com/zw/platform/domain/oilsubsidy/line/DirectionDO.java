package com.zw.platform.domain.oilsubsidy.line;

import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wanxing
 * @Title: 上下行实体-与表一一对应
 * @date 2020/10/911:33
 */
@Data
@Accessors(chain = true)
public class DirectionDO {

    private String id = UUID.randomUUID().toString();

    /**
     * 路线ID
     */
    private String lineId;
    /**
     * 方向信息(0代表上行，1代表下行)
     */
    @Min(value = 0, message = "方向最小为0")
    @Max(value = 9, message = "方向最大为1")
    @NotNull(message = "方向不能为空")
    private Integer directionType;
    /**
     * 里程信息(单位km）
     */
    @NotNull(message = "里程信息不能为空")
    private Double mile;
    /**
     * 线路间距(km)
     */
    @NotNull(message = "线路间距不能为空")
    private Double distance;
    /**
     * 夏季首班车时间
     */
    @NotNull(message = "夏季首班车时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "夏季首班车时间不符合 mm:ss规则")
    private String summerFirstTrainTime;
    /**
     * 夏季末班车时间
     */
    @NotNull(message = "夏季末班车时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "夏季末班车时间不符合 mm:ss规则")
    private String summerLastTrainTime;

    /**
     * 冬季首班车时间
     */
    @NotNull(message = "冬季首班车时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "冬季首班车时间不符合 mm:ss规则")
    private String winterFirstTrainTime;
    /**
     * 冬季末班车时间
     */
    @NotNull(message = "冬季末班车时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "冬季末班车时间不符合 mm:ss规则")
    private String winterLastTrainTime;

    /**
     * 早高峰开始时间
     */
    @NotNull(message = "早高峰开始时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "早高峰开始时间不符合 mm:ss规则")
    private String morningPeakStartTime;
    /**
     * 晚高峰开始时间
     */
    @NotNull(message = "晚高峰开始时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "晚高峰开始时间不符合 mm:ss规则")
    private String eveningPeakStartTime;
    /**
     * 早高峰结束时间
     */
    @NotNull(message = "早高峰结束时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "早高峰结束时间不符合 mm:ss规则")
    private String morningPeakEndTime;
    /**
     * 晚高峰结束时间
     */
    @NotNull(message = "晚高峰结束时间不能为空")
    @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]", message = "晚高峰结束时间不符合 mm:ss规则")
    private String eveningPeakEndTime;
    /**
     * 高峰发车间隔
     */
    @NotNull(message = "高峰发车间隔不能为空")
    @Min(value = 0, message = "高峰发车间隔最小为0")
    @Max(value = 7200, message = "高峰发车间隔最大为7200")
    private Integer peakDepartureInterval;
    /**
     * 非高峰发车间隔
     */
    @NotNull(message = "非高峰发车间隔不能为空")
    @Min(value = 0, message = "非高峰发车间隔最小为0")
    @Max(value = 7200, message = "非高峰发车间隔最大为7200")
    private Integer offPeakDepartureInterval;

    /**
     * 起点站ID
     */
    @NotNull(message = "上/下行起点站不能为空")
    private String firstStationId;

    /**
     *终点站ID
     */
    @NotNull(message = "上/下行终点站不能空")
    private String lastStationId;

    /**
     * DTO 转 DO
     * @param
     * @return
     */
    public DirectionDO copyDto2DO() {
        DirectionDO directionDO = new DirectionDO();
        return directionDO;
    }

    /**
     *  DO 转 DTO
     * @param
     * @return
     */
    public DirectionDTO copyDo2DTO() {
        DirectionDTO directionDTO = new DirectionDTO();
        return directionDTO;
    }
}
