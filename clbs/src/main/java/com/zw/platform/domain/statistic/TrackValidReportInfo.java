package com.zw.platform.domain.statistic;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhouzongbo on 2019/5/31 15:54
 */
@Data
public class TrackValidReportInfo implements Serializable {
    private static final long serialVersionUID = 4505619737224793560L;

    private static final int MAX_SPEED = 160;
    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "标识颜色")
    private String color;

    @ExcelField(title = "对象类型")
    private String vehicleType;

    /**
     * 轨迹段起始速度
     */
    @ExcelField(title = "轨迹段起始速度")
    private String startSpeed = "0";

    /**
     * 轨迹段起始里程
     */
    @ExcelField(title = "轨迹段起始里程")
    private String startMileage = "0.0";

    /**
     * 轨迹段起始时间
     */
    private Long startTime = 0L;

    /**
     * 轨迹段终止速度
     */
    @ExcelField(title = "轨迹段终止速度")
    private String endSpeed = "0";

    /**
     * 轨迹段终止里程
     */
    @ExcelField(title = "轨迹段终止里程")
    private String endMileage = "0.0";

    /**
     * 轨迹段终止时间
     */
    private Long endTime = 0L;

    /**
     * 0: 异常; 1:正常;
     * 1.轨迹段终止里程 - 轨迹段起始里程 < 0; 异常
     * 2.(轨迹段终止里程 - 轨迹段起始里程) / 该段轨迹的间隔时间 < 160:  异常
     * 3.其他情况正常
     */
    private Integer trackValid;

    @ExcelField(title = "轨迹有效性")
    private String trackValidStr;

    private String longtitude;
    private String latitude;

    public void setTrackValid() {
        if (Objects.isNull(startMileage) && Objects.isNull(endMileage)) {
            this.trackValid = 0;
            this.trackValidStr = "异常";
            return;
        }

        double mileageValue = Double.valueOf(endMileage) - Double.valueOf(startMileage);
        if (mileageValue < 0) {
            this.trackValid = 0;
            this.trackValidStr = "异常";
            return;
        }
        long timeValue = endTime - startTime;
        if (timeValue > 0 && mileageValue / timeValue > MAX_SPEED) {
            this.trackValid = 0;
            this.trackValidStr = "异常";
            return;
        }
        this.trackValid = 1;
        this.trackValidStr = "正常";
    }
}
