package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 部标监管报表-行驶里程报表
 * @author penghj
 * @version 1.0
 * @date 2019/12/16 15:45
 */
@Data
public class DrivingMileageLocationDetails implements Serializable {
    private static final long serialVersionUID = 4286470378223718334L;
    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;
    /**
     * 定位时间 秒
     */
    private Long time;
    @ExcelField(title = "定位时间")
    private String timeStr;
    /**
     * acc状态 0:关 1：开
     */
    private String status;
    @ExcelField(title = "ACC")
    private String accStatusStr;
    /**
     * 油量
     */
    @ExcelField(title = "油量(L)")
    private String totalOilWearOne;
    /**
     * 位置
     */
    private String longitude;
    private String latitude;
    @ExcelField(title = "位置")
    private String address;
}
