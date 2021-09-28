package com.zw.platform.domain.vas.monitoring.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by wjy on 2017/9/26.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TrackPlaybackAlarmForm  extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelField(title = "监控对象")
    private String vehicleLicense; // 车牌号

    @ExcelField(title = "所属分组")
    private String group; // 分组

    @ExcelField(title = "报警信息")
    private String alarmInformation;

    @ExcelField(title = "处理状态")
    private String processingState;

    @ExcelField(title = "处理人")
    private String handlePerson;

    @ExcelField(title = "报警开始时间")
    private String alarmStartTime;

    @ExcelField(title = "报警开始位置")
    private String alarmStartAddress;

    @ExcelField(title = "报警结束时间")
    private String alarmEndTime;

    @ExcelField(title = "报警结束位置")
    private String alarmEndAddress;

    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;
}
