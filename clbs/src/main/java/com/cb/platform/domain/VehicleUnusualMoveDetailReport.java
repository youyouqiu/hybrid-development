package com.cb.platform.domain;

import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;


/**
 * 车辆异动行驶明细报表
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleUnusualMoveDetailReport {

    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "车牌颜色")
    private String color;

    @ExcelField(title = "道路运输企业")
    private String groupName;

    @ExcelField(title = "时间")
    private String alarmTime;

    private Long time;

    @ExcelField(title = "速度")
    private String speed;

    @ExcelField(title = "限速")
    private Double limitSpeed;

    private Integer alarmType;

    @ExcelField(title = "报警类型")
    private String alarmTypeStr;

    @ExcelField(title = "位置")
    private String address;

    private byte[] vehicleId;

    public String getAlarmTime() {
        if (null != time) {
            alarmTime = DateUtil.getDateToString(new Date(DateUtil.getMillisecond(time)), "");
        }
        return alarmTime;
    }

    public void setColor(String color) {
        if (!StringUtils.isEmpty(color) && StringUtils.isNumeric(color)) {
            this.color = PlateColor.getNameOrBlankByCode(color);
        } else {
            this.color = color;
        }
    }
}
