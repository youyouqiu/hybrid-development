package com.cb.platform.domain;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleTravelForm extends BaseFormBean {
    private String vehicleId;

    @Size(max = 20, min = 1, message = "【行程单号】长度1~20位！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @NotNull(message = "行程单号不能为空", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "行程单号")
    private String travelId;

    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ExcelField(title = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ExcelField(title = "地点")
    @Size(max = 20, message = "【行程地点】长度1~20位！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String address;

    @ExcelField(title = "行程内容")
    @Size(max = 500, message = "【行程内容】长度1~500位！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String travelContent;

    @ExcelField(title = "备注")
    @Size(max = 50, message = "【备注】长度1~50位！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String remark;

}
