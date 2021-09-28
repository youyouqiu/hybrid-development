package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 乘客流量报表
 *
 * @author zhangsq
 * @date 2018/3/22 18:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Ridership extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vehicleId; //车id

    @ExcelField(title = "车牌号")
    private String brand; //车牌号

    @ExcelField(title = "所属企业")
    private String groupName; //所属企业

    @ExcelField(title = "分组名称")
    private String assignmentName; //分组名称

    private Integer plateColor; //车牌颜色（1蓝，2黄，3黑，4白，9其他）

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    @ExcelField(title = "车辆类型")
    private String vehicleType; //车辆类型

    private Date startTime; //上车时间

    @ExcelField(title = "上车时间")
    private String startTimeStr;

    private Date endTime;   //下车时间

    @ExcelField(title = "下车时间")
    private String endTimeStr;   //下车时间

    @ExcelField(title = "上车人数")
    private Integer aboard; //上车人数

    @ExcelField(title = "下车人数")
    private Integer getOff; //下车人数
}
