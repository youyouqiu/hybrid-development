package com.zw.platform.domain.oilsubsidy.line;

import com.zw.platform.util.OilSubsidy1301CommandField;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 1301指令实体
 * @date 2020/10/1216:39
 */
@Data
public class Line1301CommandDTO extends CommandOf809CommonDTO {

    /**
     * 线路标识
     */
    private String identify;
    /**
     * 线路名称
     */
    private String name;

    /**
     * 线路类型
     */
    private Integer lineType;

    /**
     * 方向信息(0代表上行，1代表下行)
     */
    private Integer directionType;
    /**
     * 里程信息(单位km）
     */
    private Double mile;
    /**
     * 线路间距(km)
     */
    private Double distance;
    /**
     * 夏季首班车时间
     */
    private String summerFirstTrainTime;
    /**
     * 夏季末班车时间
     */
    private String summerLastTrainTime;

    /**
     * 冬季首班车时间
     */
    private String winterFirstTrainTime;
    /**
     * 冬季末班车时间
     */
    private String winterLastTrainTime;

    /**
     * 早高峰开始时间
     */
    private String morningPeakStartTime;
    /**
     * 晚高峰开始时间
     */
    private String eveningPeakStartTime;
    /**
     * 早高峰结束时间
     */
    private String morningPeakEndTime;
    /**
     * 晚高峰结束时间
     */
    private String eveningPeakEndTime;
    /**
     * 高峰发车间隔
     */
    private Integer peakDepartureInterval;
    /**
     * 非高峰发车间隔
     */
    private Integer offPeakDepartureInterval;

    /**
     *起点站ID
     */
    private String firstStationId;

    /**
     *终点站ID
     */
    private String lastStationId;


    /**
     *起点站ID
     */
    private String firstStationName;

    /**
     *终点站ID
     */
    private String lastStationName;



    @Override
    public String toString() {
        return OilSubsidy1301CommandField.LINE_NAME + ":=" + this.name + ";" + OilSubsidy1301CommandField.LINE_NO + ":="
            + this.identify + ";" + OilSubsidy1301CommandField.IS_UP_DOWN + ":=" + this.directionType + ";"
            + OilSubsidy1301CommandField.ORERATEMILE + ":=" + this.mile + ";"
            + OilSubsidy1301CommandField.LINE_STATION_DISTANCE + ":=" + this.distance + ";"
            + OilSubsidy1301CommandField.RUN_LINE_TYPE + ":=" + this.lineType + ";"
            + OilSubsidy1301CommandField.START_STATION_NAME + ":=" + this.firstStationName + ";"
            + OilSubsidy1301CommandField.END_STATION_NAME + ":=" + this.lastStationName + ";"
            + OilSubsidy1301CommandField.S_START_TIME + ":=" + this.summerFirstTrainTime + ";"
            + OilSubsidy1301CommandField.S_END_TIME + ":=" + this.summerLastTrainTime + ";"
            + OilSubsidy1301CommandField.W_START_TIME + ":=" + this.winterFirstTrainTime + ";"
            + OilSubsidy1301CommandField.W_END_TIME + ":=" + this.winterLastTrainTime + ";"
            + OilSubsidy1301CommandField.MORNING_PEAK_START_TIME + ":=" + this.morningPeakStartTime + ";"
            + OilSubsidy1301CommandField.MORNING_PEAK_END_TIME + ":=" + this.morningPeakEndTime + ";"
            + OilSubsidy1301CommandField.EVENIGN_PEAK_START_TIME + ":=" + this.eveningPeakStartTime + ";"
            + OilSubsidy1301CommandField.EVENIGN_PEAK_END_TIME + ":=" + this.eveningPeakEndTime + ";"
            + OilSubsidy1301CommandField.PEAK_PLAN_TIME + ":=" + this.peakDepartureInterval + ";"
            + OilSubsidy1301CommandField.NONPEAK_PLAN_TIME + ":=" + this.offPeakDepartureInterval + ";";
    }

}
