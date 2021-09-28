package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/26 10:04
 */
@Data
public class VehicleSpotCheckNumberCountInfo implements Serializable {
    private static final long serialVersionUID = 7437434618940900317L;

    /**
     * 车牌号
     */
    @ExcelField(title = "车牌号")
    private String plateNumber;

    /**
     * 车牌颜色
     */
    @ExcelField(title = "车牌颜色")
    private String plateColor;

    /**
     * 车辆类型
     */
    @ExcelField(title = "车辆类型")
    private String vehicleType;

    /**
     * 所属道路运输企业
     */
    @ExcelField(title = "所属道路运输企业")
    private String groupName;

    /**
     * 查看定位信息(次数)
     */
    @ExcelField(title = "查看定位信息")
    private Integer checkPositionInfoNum = 0;

    /**
     * 查看历史轨迹(次数)
     */
    @ExcelField(title = "查看历史轨迹")
    private Integer checkHistoricalTrackNum = 0;

    /**
     * 查看视频(次数)
     */
    @ExcelField(title = "查看视频")
    private Integer checkVideoNum = 0;

    /**
     * 违章处理(次数)
     */
    @ExcelField(title = "违章处理")
    private Integer violationHandlingNum = 0;

    /**
     * 合计(次数)
     */
    @ExcelField(title = "合计")
    private Integer totalNum;

    public void addCheckPositionInfoNum() {
        this.checkPositionInfoNum = checkPositionInfoNum + 1;
    }

    public void addCheckHistoricalTrackNum() {
        this.checkHistoricalTrackNum = checkHistoricalTrackNum + 1;
    }

    public void addCheckVideoNum() {
        this.checkVideoNum = checkVideoNum + 1;
    }

    public void addViolationHandlingNum() {
        this.violationHandlingNum = violationHandlingNum + 1;
    }

    public void setTotalNumByAdd() {
        this.totalNum =
            this.checkPositionInfoNum + this.checkHistoricalTrackNum + this.checkVideoNum + this.violationHandlingNum;
    }
}
