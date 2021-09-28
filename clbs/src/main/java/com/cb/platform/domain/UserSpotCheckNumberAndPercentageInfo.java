package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/21 17:24
 */
@Data
public class UserSpotCheckNumberAndPercentageInfo implements Serializable {
    private static final long serialVersionUID = -4636622727870042098L;

    /**
     * 用户
     */
    @ExcelField(title = "用户")
    private String userName;
    /**
     * 在岗时段
     */
    @ExcelField(title = "在岗时段")
    private String onDutyTime;
    private Long startTimeL;
    /**
     * 所属道路运输企业
     */
    @ExcelField(title = "所属道路运输企业")
    private String userGroupName;
    /**
     * 查看定位信息(次数)
     */
    @ExcelField(title = "查看定位信息")
    private Integer checkPositionInfoNum;
    /**
     * 查看历史轨迹(次数)
     */
    @ExcelField(title = "查看历史轨迹")
    private Integer checkHistoricalTrackNum;
    /**
     * 查看视频(次数)
     */
    @ExcelField(title = "查看视频")
    private Integer checkVideoNum;
    /**
     * 违章处理(次数)
     */
    @ExcelField(title = "违章处理")
    private Integer violationHandlingNum;
    /**
     * 车辆总数
     */
    @ExcelField(title = "车辆总数")
    private Integer vehicleCount;
    /**
     * 合计(次数)
     */
    @ExcelField(title = "合计")
    private Integer totalNum;
    /**
     * 查看定位信息(百分比)
     */
    @ExcelField(title = "查看定位信息")
    private String checkPositionInfoPercentage = "0%";
    /**
     * 查看历史轨迹(百分比)
     */
    @ExcelField(title = "查看历史轨迹")
    private String checkHistoricalTrackPercentage = "0%";
    /**
     * 查看视频(百分比)
     */
    @ExcelField(title = "查看视频")
    private String checkVideoPercentage = "0%";
    /**
     * 违章处理(百分比)
     */
    @ExcelField(title = "违章处理")
    private String violationHandlingPercentage = "0%";
    /**
     * 合计(百分比)
     */
    @ExcelField(title = "合计")
    private String totalPercentage = "0%";
}
