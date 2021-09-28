package com.zw.lkyw.domain.videoCarouselReport;

import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;

/**
 *视频巡检实体
 */
@Data
public class VideoCarouselReport {

    @ExcelField(title = "车牌")
    private String monitorName;
    private Integer signColor;

    @ExcelField(title = "车牌颜色")
    private String color;
    //车辆类型
    @ExcelField(title = "车辆类型")
    private String objectType = "其他车辆";

    @ExcelField(title = "企业名称")
    private String groupName;

    @ExcelField(title = "合计巡检次数")
    private long totalNum;

    @ExcelField(title = "合计巡检成功次数")
    private long totalSucNum;

    private String monitorId;

    private String groupId;

}