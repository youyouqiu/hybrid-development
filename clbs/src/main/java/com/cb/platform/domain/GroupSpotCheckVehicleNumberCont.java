package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


/**
 * 道路运输企业抽查车辆数量统计表
 */
@Data
public class GroupSpotCheckVehicleNumberCont {

    /**
     *  企业名称
     */
    @ExcelField(title = "道路运输企业")
    private String groupName;

    /**
     *  查看定位信息
     */
    @ExcelField(title = "查看定位信息")
    private Integer groupCheckPositionNumber;

    /**
     * 查看历史轨迹
     */
    @ExcelField(title = "查看历史轨迹")
    private Integer groupCheckHistoricalTrackNumber;

    /**
     * 查看视频
     */
    @ExcelField(title = "查看视频")
    private Integer groupCheckVideoNumber;

    /**
     * 违章处理
     */
    @ExcelField(title = "违章处理")
    private Integer groupViolationHandingNumber;

    /**
     * 车辆总数
     */
    @ExcelField(title = "车辆总数")
    private Integer groupVehicleSum;

    /**
     * 合计(被抽查的车辆总数)
     */
    @ExcelField(title = "合计")
    private Integer groupSpotCheckVehicleSummation;

    /**
     * 查看定位信息(百分比)
     */
    @ExcelField(title = "查看定位信息(百分比)")
    private String groupCheckPositionInfoPercentage = "0%";

    /**
     * 查看历史轨迹(百分比)
     */
    @ExcelField(title = "查看历史轨迹(百分比)")
    private String groupCheckHistoricalTrackPercentage = "0%";

    /**
     * 查看视频(百分比)
     */
    @ExcelField(title = "查看视频(百分比)")
    private String groupCheckVideoPercentage = "0%";

    /**
     * 违章处理(百分比)
     */
    @ExcelField(title = "违章处理(百分比)")
    private String groupViolationHandlingPercentage = "0%";

    /**
     * 合计(百分比)
     */
    @ExcelField(title = "合计")
    private String groupTotalPercentage = "0%";
}
