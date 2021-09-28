package com.zw.platform.dto.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 报警信息统计DTO
 * @author tianzhangxu
 * @date 2019-12-18 9:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmMessageDto implements Serializable {

    private static final long serialVersionUID = 1322934727195408121L;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 分组名称
     */
    @ExcelField(title = "所属分组")
    private String assignmentName;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 车牌颜色str
     */
    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警类型名称
     */
    @ExcelField(title = "报警类型")
    private String description;

    /**
     * 报警数量
     */
    @ExcelField(title = "报警数量")
    private Integer alarmNumber;

    /**
     * 已处理数量
     */
    @ExcelField(title = "已处理数")
    private Integer handleNumber = 0;

    /**
     * 报警详情list
     */
    private List<AlarmDetailDto> alarmDetails;

    /**
     * 分组key: 车辆ID + 报警类型
     */
    private String groupByKey;
}
