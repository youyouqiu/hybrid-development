package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * 信息配置导入货运Form
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigTransportImportForm extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber; // (货运平台)终端ID

    @ExcelField(title = "制造商")
    private String manuFacturer; // (货运平台字段名)终端⼚商名称

    @ExcelField(title = "终端名称")
    private String deviceName;// 终端型号

    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simcardNumber; // SIM卡卡号

    @ExcelField(title = "道路运输证号")
    private String roadTransportNumber; // 道路运输证号

    @ExcelField(title = "省、直辖市", required = true)
    private String province; // 所属省

    @ExcelField(title = "市、区", required = true)
    private String city; // 所属市

    @ExcelField(title = "县")
    private String county; // 所属县

    @ExcelField(title = "所属企业", required = true)
    private String groupName; // 车主/业户

    @ExcelField(title = "车主")
    private String vehicleOwner; // 联系人

    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone; // 联系人手机

    @ExcelField(title = "电话是否校验")
    private String phoneCheck; // ⼿机验证状态

    @ExcelField(title = "首次上线时间")
    private String firstOnlineTimeStr;
    private Date firstOnlineTime;// 首次上线时间

    @ExcelField(title = "车架号", required = true)
    private String chassisNumber; //车辆识别代码/车架号

    @ExcelField(title = "车牌号", required = true, repeatable = false)
    private String brand;//车牌号

    @ExcelField(title = "车牌颜色", required = true)
    private String plateColorStr;
    private Integer plateColor;

    @ExcelField(title = "车辆类型", required = true)
    private String vehicleType;

    @ExcelField(title = "车辆品牌")
    private String vehicleBrand;

    @ExcelField(title = "车辆型号")
    private String vehicleModel;

    /**
     * 总质量(kg)
     */
    @ExcelField(title = "总质量(kg)")
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【总质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String totalQuality;

    /**
     * 核定载质量(kg)
     */
    @ExcelField(title = "核定载质量(kg)")
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【核定载质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String loadingQuality;

    /**
     * 准牵引总质量(kg)
     */
    @ExcelField(title = "准牵引总质量(kg)")
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【准牵引总质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String tractionTotalMass;

    /**
     * 外廓尺寸-长(mm)
     */
    @ExcelField(title = "外廓尺寸-长(mm)")
    @Max(value = 999999, message = "【外廓尺寸-长(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-长(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeLong;

    /**
     * 外廓尺寸-宽(mm)
     */
    @ExcelField(title = "外廓尺寸-宽(mm)")
    @Max(value = 999999, message = "【外廓尺寸-宽(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-宽(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeWide;

    /**
     * 外廓尺寸-高(mm)
     */
    @ExcelField(title = "外廓尺寸-高(mm)")
    @Max(value = 999999, message = "【外廓尺寸-高(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-高(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeHigh;

    /**
     * 货厢内部尺寸-长(mm)
     */
    @ExcelField(title = "货厢内部尺寸-长(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-长(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-长(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeLong;

    /**
     * 货厢内部尺寸-宽(mm)
     */
    @ExcelField(title = "货厢内部尺寸-宽(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-宽(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-宽(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeWide;

    /**
     * 货厢内部尺寸-高(mm)
     */
    @ExcelField(title = "货厢内部尺寸-高(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-高(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-高(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeHigh;

    /**
     * 轴数
     */
    @ExcelField(title = "轴数")
    @Max(value = 9999, message = "【轴数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【轴数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer shaftNumber;

    /**
     * 轮胎数(@Max 参数最大支持Integer的最大值,Long在此处暂时不验证)
     */
    @ExcelField(title = "轮胎数")
    @Max(value = 9999, message = "【轮胎数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【轮胎数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer tiresNumber;

    /**
     * 轮胎规格
     */
    @ExcelField(title = "轮胎规格")
    @Size(max = 20, message = "【轮胎规格】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String tireSize;

    /**
     * 车辆出厂日期
     */
    @ExcelField(title = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleProductionDateStr;
    private Date vehicleProductionDate;

    /**
     * 经营范围
     */
    @ExcelField(title = "经营范围")
    private String scopeBusiness;

    /**
     * 车身颜色 (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)
     */
    @Pattern(message = "【车辆颜色】填值错误！", regexp = "^\\s*$|^[0-9]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "车辆颜色")
    private String vehicleColorStr;
    private Integer vehicleColor;

    /**
     * 道路运输经营许可证号
     */
    @Size(max = 20, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "经营许可证号")
    private String vehicleOperationNumber;

    /**
     * 辆购置方式(0:分期付款;1:一次性付清)
     */
    @ExcelField(title = "车辆购置方式")
    private String purchaseWayStr;

    /**
     * 保留字段, 暂不使用(平台有单独的保险模块)
     */
    @ExcelField(title = "车辆保险到期时间")
    @Deprecated
    private String vehicleInsuranceEndDate;

    /**
     * yyyy-MM
     */
    @ExcelField(title = "检验有效期至")
    private String validEndDateStr;
    private Date validEndDate;

    /**
     * 执照上传数
     */
    @ExcelField(title = "执照上传数")
    @Max(value = 99, message = "【执照上传数】最大值99", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【执照上传数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short licenseNumbers;

    /**
     * 服务合同到期时间(信息配置列表到期时间)
     */
    @ExcelField(title = "到期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String expireTimeStr;
    private Date expireTime;

    /**
     * 行驶证发证日期
     */
    @ExcelField(title = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;
    private Date licenseIssuanceDate;

    /**
     * 发动机号
     */
    @Size(max = 20, message = "【发动机号】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "发动机号")
    private String engineNumber;

    /**
     * 保留字段,
     */
    @ExcelField(title = "发动机型号")
    @Deprecated
    private String engineType;

    /**
     * 车辆id
     */
    private String brandID;

    /**
     * 终端id
     */
    private String deviceID;

    /**
     * Sim卡id
     */
    private String simID;

    /**
     * 服务周期id
     */
    private String serviceLifecycleId = "";

    /**
     * 分组id，逗号相隔
     */
    private String assignIds = "";
    /**
     * 分组名称，逗号相隔
     */
    private String assignNames = "";
    // 分组所属组织id
    private String assignGroups = "";
    /**
     * 从业人员名称，逗号相隔
     */
    private String professionalNames = "";
    /**
     * 所属企业名称
     */
    private String groupId;

    private String vehType;

    /**
     * 车辆类别id
     */
    private String vehicleCategoryId;

    /**
     * 车辆类别名称
     */
    private String vehicleCategoryName;
    /**
     * 标准
     */
    private Integer standard;
    private String type;// 物品类型

    private String typeName;// 物品类型名称

    /*新增*/
    private String deviceType;

    private String monitorType;
    private String functionalType;

    private String durDateStr;
    private String billingDateStr;
    private Date billingDate;

    private String peripheralsId;

    private String brandModelsId;

    private String terminalTypeId;

    private String terminalManufacturer; //终端厂商

    private String terminalType; // 终端型号
}
