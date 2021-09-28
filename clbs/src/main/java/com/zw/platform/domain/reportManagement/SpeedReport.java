package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 车辆超速报警报表实体类
 * @author hujun
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SpeedReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;//车牌号

    @ExcelField(title = "分组")
    private String assignmentName;//分组

    @ExcelField(title = "从业人员")
    private String professionalsName;//从业人员

    @ExcelField(title = "超速次数")
    private Integer speedNumber;//超速次数

    @ExcelField(title = "最大速度")
    private Double maxSpeed;//最大速度

    @ExcelField(title = "最小速度")
    private Double minSpeed;//最小速度

    @ExcelField(title = "平均速度")
    private Double averageSpeed;//平均速度

    private byte[] vehicleId; // 监控对象id

    private Double totalSpeed;//总速度

    private Integer totalNum;//总记录条数

    private String vid;//车id

}