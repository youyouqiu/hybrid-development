package com.zw.platform.domain.vas.alram.query;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/7/9 17:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IoAlarmHandleInfo extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 7819438839442907205L;

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String plateNumber;

    /**
     * 车牌颜色
     */
    @ExcelField(title = "车牌颜色")
    private String plateColorString;
    private String plateColor;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String name;
    private byte[] groupId;

    /**
     * 所属分组
     */
    @ExcelField(title = "所属分组")
    private String assignmentName;

    /**
     * 从业人员名称
     */
    @ExcelField(title = "从业人员")
    private String professionalsName;

    /**
     * 状态
     */
    @ExcelField(title = "处理状态")
    private String alarmStatus;
    private int status;

    /**
     * 描述
     */
    @ExcelField(title = "报警类型")
    private String description;

    /**
     * 行驶速度
     */
    @ExcelField(title = "报警开始速度")
    private String speed;


    /**
     * 行车记录仪速度
     */
    @ExcelField(title = "行车记录仪速度")
    private String recorderSpeed;

    /**
     * 报警开始时间
     */
    @ExcelField(title = "报警开始时间")
    private String startTime;
    private Long alarmStartTime;

    /**
     * 报警结束时间
     */
    @ExcelField(title = "报警结束时间")
    private String endTime;
    private Long alarmEndTime;
    /**
     * 报警开始经纬度
     */
    private String alarmStartLocation;

    /**
     * 报警结束经纬度
     */
    private String alarmEndLocation;
    /**
     * 报警开始经度
     */
    @ExcelField(title = "报警开始经度")
    private String alarmStartlongtitude;
    /**
     * 报警开始纬度
     */
    @ExcelField(title = "报警开始纬度")
    private String alarmStartlatitude;
    /**
     * 报警结束经度
     */
    @ExcelField(title = "报警结束经度")
    private String alarmEndlongtitude;
    /**
     * 报警结束纬度
     */
    @ExcelField(title = "报警结束纬度")
    private String alarmEndlatitude;

    /**
     * 报警开始位置
     */
    @ExcelField(title = "报警开始位置")
    private String alarmStartSpecificLocation;

    /**
     * 报警结束位置
     */
    @ExcelField(title = "报警结束位置")
    private String alarmEndSpecificLocation;

    /**
     * 处理人名称
     */
    @ExcelField(title = "处理人")
    private String personName;
    private String personId;

    /**
     * 处理时间
     */
    @ExcelField(title = "处理时间")
    private String handleTime;

    /**
     * 处理方式
     */
    @ExcelField(title = "处理方式")
    private String handleType;

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;

    /**
     * 车id byte数组
     */
    private byte[] vehicleIdHbase;

    /**
     * 车id
     */
    private String vehicleId;

    /**
     * 流水号
     */
    private String swiftNumber;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警来源
     */
    private Integer alarmSource;
    /**
     * 监控对象类型 0:车 1:人
     */
    private Integer monitorType;
}
