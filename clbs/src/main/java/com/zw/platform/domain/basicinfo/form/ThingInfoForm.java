package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.ws.common.PublicVariable;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThingInfoForm extends BaseFormBean implements Serializable, ConverterDateUtil {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "【物品编号】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【物品编号】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "物品编号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String thingNumber; // 物品编号

    @NotEmpty(message = "【物品名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【物品名称】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "物品名称", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String name; // 物品名称

    @ExcelField(title = "所属企业")
    private String groupName;// 分组名称

    @ExcelField(title = "终端号")
    private String deviceNumber; // 终端

    @ExcelField(title = "终端手机号")
    private String simcardNumber; // SIM卡

    @ExcelField(title = "分组")
    private String assign; // 所属分组
    /**
     * 类别
     */
    @ExcelField(title = "物品类别", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String category;

    /**
     * 类型
     */
    @ExcelField(title = "物品类型", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String type;

    /**
     * 品牌
     */
    @ApiParam(value = "品牌")
    @ExcelField(title = "品牌", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String label;

    /**
     * 型号
     */
    @ApiParam(value = "型号")
    @ExcelField(title = "型号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String model;

    /**
     * 材料
     */
    @ApiParam(value = "主要材料")
    @ExcelField(title = "主要材料", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String material;
    @ApiParam(value = "物品重量")
    @Max(value = 999999999, message = "【物品重量】长度不超过9！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value = 0, message = "不能小于0", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "重量", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private Integer weight; // 物品重量

    /**
     * 规格
     */
    @ApiParam(value = "规格")
    @ExcelField(title = "规格", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String spec;

    /**
     * 制造商
     */
    @ApiParam(value = "制造商")
    @ExcelField(title = "制造商", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String manufacture;

    /**
     * 经销商
     */
    @ApiParam(value = "经销商")
    @ExcelField(title = "经销商", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String dealer;

    /**
     * 产地
     */
    @ApiParam(value = "产地")
    @ExcelField(title = "产地", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String place;

    /**
     * 生产日期
     */

    @ApiParam(value = "生产日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date productDate;

    @ExcelField(title = "生产日期", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String productDateStr;
    @ApiParam(value = "备注")
    @ExcelField(title = "备注", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String remark;

    /**
     * 所属企业Id
     */
    private String groupId;

    private String createDataTimeStr; // 创建时间 yyyy-MM-dd

    private String assignmentName; // 分组名称

    private String assignmentId; // 分组id

    private String assignmentGroupId; // 分组所属企业id

    /**
     * 物品照片名称
     */
    private String thingPhoto;

    private String categoryName;// 物品类别名称

    private String typeName;// 物品类型名称

    private String assignId;

    public static ThingInfoForm initConfigImport(ConfigImportForm config, String userName) {
        Date createDate = new Date();
        ThingInfoForm thingInfoForm = new ThingInfoForm();
        thingInfoForm.setThingNumber(config.getCarLicense());
        thingInfoForm.setName("");
        thingInfoForm.setType("0");
        thingInfoForm.setCategory("0");
        thingInfoForm.setGroupId(config.getGroupId());
        thingInfoForm.setCreateDataUsername(userName);
        thingInfoForm.setCreateDataTime(createDate);
        thingInfoForm.setDeviceNumber(config.getDeviceNumber());
        thingInfoForm.setSimcardNumber(config.getSimcardNumber());
        thingInfoForm.setCreateDataTimeStr(DateFormatUtils.format(thingInfoForm.getCreateDataTime(), "yyyy-MM-dd"));
        String[] assignmentAndGroup = config.getGroupName().split("@");
        thingInfoForm.setAssignmentName(assignmentAndGroup[0]);
        thingInfoForm.setGroupName(config.getGroupName());
        return thingInfoForm;
    }


}
