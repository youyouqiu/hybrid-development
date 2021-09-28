package com.cb.platform.domain;


import com.zw.platform.util.excel.annotation.ExcelField;
import java.io.Serializable;
import java.util.Date;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleScheduler extends BaseFormBean implements Serializable {

    // 车牌号
    @ExcelField(title = "车牌号")
    private String brand;

    // 车牌颜色
    @ExcelField(title = "车牌颜色")
    private String color;

    // 车牌颜色
    @ExcelField(title = "车辆类型")
    private String vehicleType;

    // 企业名称
    @ExcelField(title = "所属企业")
    private String groupName;

    // 调度信息发送内容
    @ExcelField(title = "内容")
    private String content;

    // 调度信息发送时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelField(title = "发送时间")
    private Date sendTime;

    // 调度信息发送人
    @ExcelField(title = "发送人")
    private String sendUsername;

    // 车辆ID
    private String vehicleId;

    // 组织ID
    private String groupId;



    // 调度信息发送日期
    private Date sendDate;





    // 调度信息次数
    private String times;
}
