package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * 行驶记录仪下发应答记录实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DrivingRecordInfo extends BaseFormBean {
    private String monitorId; // 监控对象id
    @ExcelField(title = "监控对象")
    private String monitorName; // 监控对象标识

    @ExcelField(title = "所属企业")
    private String groupName; // 所属企业

    private String collectionCommand; // 采集命令

    @ExcelField(title = "采集命令")
    private String collectionCommandDescribe; // 采集命令描述

    private int msgSNAck; // 流水号

    private Date createDataTime;

    @ExcelField(title = "下发时间")
    private String createDataTimeStr;

    private Date updateDataTime; // 更新时间

    @ExcelField(title = "采集时间")
    private String updateDataTimeStr; // 更新时间

    @ExcelField(title = "内容")
    private String message; // 应答内容
}
