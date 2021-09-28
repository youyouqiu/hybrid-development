package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报警信息统计实体类
 * @author hujun
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmInformation extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelField(title = "车牌号")
    private String plateNumber;
    @ExcelField(title = "所属分组")
    private String assignmentName;
    @ExcelField(title = "车牌颜色")
    private String plateColor;
    @ExcelField(title = "车辆类型")
    private String vehicleType;
    @ExcelField(title = "紧急报警")
    private Integer majorAlarm = 0;
    @ExcelField(title = "超速报警")
    private Integer speedAlarm = 0;
    @ExcelField(title = "非法点火故障")
    private Integer vehicleII = 0;
    @ExcelField(title = "超时停车")
    private Integer timeoutParking = 0;
    @ExcelField(title = "路线偏离")
    private Integer routeDeviation = 0;
    @ExcelField(title = "疲劳驾驶报警")
    private Integer tiredAlarm = 0;
    @ExcelField(title = "进出区域")
    private Integer inOutArea = 0;
    @ExcelField(title = "进出线路")
    private Integer inOutLine = 0;

    public Integer getAlarmNum() {
        return majorAlarm + speedAlarm + vehicleII + timeoutParking + routeDeviation + tiredAlarm + inOutArea
            + inOutLine;
    }
}
