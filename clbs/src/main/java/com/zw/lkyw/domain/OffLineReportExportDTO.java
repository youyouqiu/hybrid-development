package com.zw.lkyw.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * 两客一危-离线车辆查询导出类
 */
@Data
public class OffLineReportExportDTO {

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String brnad;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 分组名称
     */
    @ExcelField(title = "分组")
    private String assignmentName;

    /**
     * 车辆颜色
     */
    @ExcelField(title = "车辆颜色")
    private String color;

    /**
     * 终端号
     */
    @ExcelField(title = "终端号")
    private String deviceNumber;

    /**
     * SIM卡号
     */
    @ExcelField(title = "终端手机号")
    private String simcardNumber;

    /**
     * 离线时长
     */
    @ExcelField(title = "离线时长")
    private String offLineDay;

    /**
     * 最后在线时间
     */
    @ExcelField(title = "最后在线时间")
    private String lastTime;

    /**
     * 最后在线位置
     */
    @ExcelField(title = "最后在线位置")
    private String  lastLocation;
}
