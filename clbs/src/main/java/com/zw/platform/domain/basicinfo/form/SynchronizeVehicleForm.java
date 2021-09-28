package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.ws.common.PublicVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class SynchronizeVehicleForm extends BaseFormBean implements Serializable, ConverterDateUtil {
    private static final long serialVersionUID = 1L;

    /**
     * 终端编号
     */
    @NotNull
    @ExcelField(title = "终端号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String deviceNumber;

    /**
     * 车主
     */
    @Pattern(regexp = "^([A-Za-z\\u4e00-\\u9fa5]{1,8})?$", message = "【车主】只能填写中文、字母,长度不能超过8", groups = {
            ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "车主", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleOwner;

    /**
     * 车主电话
     */
    @ExcelField(title = "车主电话", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleOwnerPhone;

    /**
     * 备注
     */
    @Size(max = 150, message = "【备注】不能超过150个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "备注", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String remark;

    /**
     * 机架号(车架号 - 工程机械)
     */
    @Size(max = 50, message = "【车架号】不能超过50个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "车架号")
    private String chassisNumber;

    /**
     * 车台安装日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehiclePlatformInstallDate;

}
