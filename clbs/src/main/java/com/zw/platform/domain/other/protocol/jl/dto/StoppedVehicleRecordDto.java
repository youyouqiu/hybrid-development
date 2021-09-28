package com.zw.platform.domain.other.protocol.jl.dto;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.UUID;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/6/12
 **/
@Data
public class StoppedVehicleRecordDto {

    public static final Integer SUCCESS_CODE = 1;
    public static final Integer ERROR_CODE = 0;

    private String id = UUID.randomUUID().toString();

    private String monitorId;

    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 停运开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @ExcelField(title = "停运开始日期")
    private String startDateStr;

    /**
     * 停运结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @ExcelField(title = "停运开始日期")
    private String endDateStr;

    /**
     * 报停原因: 1:天气; 2:车辆故障; 3: 路阻; 4: 终端报修; 9: 其他(默认)
     */
    private Integer stopCauseCode;

    @ExcelField(title = "报停原因")
    private String stopCauseCodeStr;

    /**
     * 车牌颜色：1蓝，2黄，3黑，4白，9其他,90:农蓝, 91农黄,92农绿,93黄绿色,94渐变绿色
     */
    private Integer plateColor;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    @ExcelField(title = "所属企业")
    private String groupName;

    private Date uploadTime;

    @ExcelField(title = "上报时间")
    private String uploadTimeStr;

    /**
     * 上传状态：0: 失败; 1: 成功
     */
    private Integer uploadState;

    @ExcelField(title = "上传状态")
    private String uploadStateStr;

    /**
     * 操作人
     */
    @ExcelField(title = "操作人")
    private String operator;

    private String errorMsg;
}
