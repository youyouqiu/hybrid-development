package com.zw.platform.domain.reportManagement.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 车辆上线率实体类
 *
 * @author zjc
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehOnlineStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;// 车牌号

    @ExcelField(title = "所属企业")
    private String groupName;// 所属企业

    @ExcelField(title = "分组")
    private String assignmentNames;// 分组

    @ExcelField(title = "车牌颜色")
    private String plateColor;// 车牌颜色

    @ExcelField(title = "车辆类型")
    private String vehicleType;// 车辆类型

    @ExcelField(title = "上线天数")
    private Integer onlineDays;//上线天数

    @ExcelField(title = "总天数")
    private Integer totalDays;// 总天数

    private Double onlineRate;  // 上线率

    @ExcelField(title = "上线率")
    private String onlineRateStr;

    @ExcelField(title = "离线时长")
    private String OfflineTime;// 离线时长

    @ExcelField(title = "预警数")
    private Integer warningNum;//预警数

    private Double avgOnlineWarningNum;//平均上线预警数

    @ExcelField(title = "平均上线预警数")
    private String avgWarningNum;//平均上线预警数

    @ExcelField(title = "最后在线位置")
    private String lastOnlineLocation;//最后在线位置

    private Double longitude;//经度

    private Double latitude;//纬度

    private String vehicleId;//车辆id

    private byte[] vidBytes;

    private Long createDate;

    private Long lastTime;

}
