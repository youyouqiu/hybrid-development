package com.zw.platform.dto.driverMiscern;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/25
 **/
@Data
public class DriverDiscernStatisticsExport {

    /**
     * 车辆名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;
    /**
     * 企业名称
     */
    @ExcelField(title = "所属企业")
    private String orgName;

    @ExcelField(title = "比对结果")
    private String identificationResultStr;

    @ExcelField(title = "比对相似度")
    private String matchRateStr;

    @ExcelField(title = "比对相似度阈值")
    private String matchThresholdStr;

    @ExcelField(title = "比对类型")
    private String identificationTypeStr;

    private String driverId;

    @ExcelField(title = "比对人脸id")
    private String faceId;

    @ExcelField(title = "比对驾驶员姓名")
    private String driverName;

    @ExcelField(title = "从业资格证号")
    private String cardNumber;

    @ExcelField(title = "比对时间")
    private String identificationTimeStr;

}
