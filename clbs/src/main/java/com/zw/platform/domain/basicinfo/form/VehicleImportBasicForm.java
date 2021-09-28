package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.lock.dto.VehicleBrand;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

/**
 * 导入车辆到平台时使用的类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleImportBasicForm extends VehicleBrand {
    private static final long serialVersionUID = 3891181993511220669L;

    /*通用*/
    /**
     * 车牌号
     */
    @NotEmpty(message = "【车牌号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车牌号")
    private String brand;

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 类别标准
     */
    @ExcelField(title = "类别标准")
    private String standard;

    /**
     * 车辆类别
     */
    @ExcelField(title = "车辆类别")
    private String vehicleCategoryName;
    /**
     * 类别id
     */
    private String vehicleCategoryId;

    /**
     * 车辆类型
     */
    @ExcelField(title = "车辆类型")
    private String vehicleType;

    /**
     * 车辆别名
     */
    @Size(max = 20, message = "【车辆别名】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆别名")
    private String aliases;

    /**
     * 车主
     */
    @Size(max = 20, message = "【车主】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车主")
    private String vehicleOwner;

    /**
     * 车主电话
     */
    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone;

    /**
     * 车辆等级
     */
    @ExcelField(title = "车辆等级")
    private String vehicleLevel;

    /**
     * 电话是否校验(0：未检验；1：已校验)(前端select，未选择传递-1,兼容ie)
     */
    @ExcelField(title = "电话是否校验")
    @Max(value = 1, message = "【电话是否校验】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【电话是否校验】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String phoneCheck;

    /**
     * 区域属性
     */
    @Size(max = 20, message = "【区域属性】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "区域属性")
    private String areaAttribute;

    /**
     * 省、直辖市
     */
    @Size(max = 20, message = "【省、直辖市】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "省、直辖市")
    private String province;

    /**
     * 市、区
     */
    @Size(max = 20, message = "【市、区】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "市、区")
    private String city;

    /**
     * 县
     */
    @ExcelField(title = "县")
    @Size(max = 20, message = "【县】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String county;

    /**
     * 燃料类型
     */
    @ExcelField(title = "燃料类型")
    private String fuelType;

    /**
     * 车辆颜色 (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)
     */
    @Pattern(message = "【车辆颜色】填值错误！", regexp = "^\\s*$|^[0-9]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "车辆颜色")
    private String vehicleColor;

    /**
     * 车牌颜色（0蓝、1黄、2白、3黑）
     */
    @Pattern(message = "【车牌颜色】填值错误！", regexp = "^\\s*$|^[0-3]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "车牌颜色")
    private String plateColorStr;
    private Integer plateColor;

    /**
     * 车辆状态
     */
    @ExcelField(title = "车辆状态")
    private String isStarts;

    /**
     * 核定载人数
     */
    @Max(value = 9999, message = "【核定载人数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【核定载人数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "核定载人数")
    private Short numberLoad;

    /**
     * 核定载质量(kg)
     */
    @ExcelField(title = "核定载质量(kg)")
    private String loadingQuality;

    /**
     * 车辆保险单号
     */
    @Size(max = 50, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆保险单号")
    private String vehicleInsuranceNumber;

    /**
     * 车辆用途
     */
    @Size(max = 50, message = "【车辆用途】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆用途")
    private String vehiclePurpose;

    /**
     * 所属行业
     */
    @ExcelField(title = "所属行业")
    private String tradeName;

    /**
     * 维修状态(0：否；1：是)
     */
    @Max(value = 1, message = "【维修状态】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【维修状态】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short stateRepair;
    @ExcelField(title = "维修状态")
    private String stateRepairStr;

    /**
     * 车辆照片
     */
    @ExcelField(title = "车辆照片")
    private String vehiclePhoto;

    /**
     * 车辆技术登记有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "车辆技术等级有效期")
    private String vehicleTechnologyValidityStr;// 车辆技术登记有效期String
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleTechnologyValidity;

    /**
     * 道路运输证号
     */
    @Size(max = 20, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "道路运输证号")
    private String roadTransportNumber;

    /**
     * 经营许可证号
     */
    @Size(max = 20, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "经营许可证号")
    private String vehiclOperationNumber;

    /**
     * 经营范围
     */
    @ExcelField(title = "经营范围")
    private String scopeBusiness;

    private String scopeBusinessIds;
    private String scopeBusinessCodes;

    /**
     * 核发机关
     */
    @Size(max = 255, message = "【核发机关】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "核发机关")
    private String issuedAuthority;

    /**
     * 经营权类型(0：国有；1：集体；2：私营；3：个体；4：联营；5：股份制；6：外商投资；7：港澳台及其他)(short)
     */
    @Max(value = 7, message = "【经营权类型】最大值{7}", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【经营权类型】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short managementType;
    @ExcelField(title = "经营权类型")
    private String managementTypeStr;

    /**
     * 道路运输证有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "道路运输证有效期起")
    private String roadTransportValidityStartStr;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidityStart;

    /**
     * 道路运输证有效期（道路运输有效期至）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "道路运输证有效期")
    private String roadTransportValidityStr;// 道路运输证有效期string
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidity;

    /**
     * 线路牌号
     */
    @Size(max = 255, message = "【线路牌号】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "线路牌号")
    private String lineNumber;

    /**
     * 始发地
     */
    @Size(max = 255, message = "【始发地】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "始发地")
    private String provenance;

    /**
     * 途经站名
     */
    @Size(max = 255, message = "【途经站名】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "途经站名")
    private String viaName;

    /**
     * 终到地
     */
    @Size(max = 255, message = "【终到地】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "终到地")
    private String destination;

    /**
     * 始发站
     */
    @Size(max = 255, message = "【始发站】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "始发站")
    private String departure;

    /**
     * 路线入口
     */
    @Size(max = 255, message = "【路线入口】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "路线入口")
    private String routeEntry;

    /**
     * 终到站
     */
    @Size(max = 255, message = "【终到站】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "终到站")
    private String destinationStation;

    /**
     * 路线出口
     */
    @Size(max = 255, message = "【路线出口】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "路线出口")
    private String exportRoute;

    /**
     * 每日发班次数
     */
    @ExcelField(title = "每日发班次数")
    @Max(value = 9999, message = "【每日发班次数】最大值9999")
    @Min(value = 0, message = "【每日发班次数】最小值0")
    private Integer dailyNumber;

    /**
     * 运输证提前提醒天数
     */
    @ExcelField(title = "运输证提前提醒天数")
    @Max(value = 9999, message = "【运输证提前提醒天数】最大值9999")
    @Min(value = 0, message = "【运输证提前提醒天数】最小值0")
    private Integer managementRemindDays;

    /**
     * 营运状态(0:营运;1:停运;2:挂失;3:报废;4:歇业;5:注销;6:迁出(过户);7:迁出(转籍);8:其他)
     */
    @Max(value = 8, message = "【营运状态】最大值8", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【营运状态】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer operatingState;

    @ExcelField(title = "营运状态")
    private String operatingStateStr;

    /**
     * 行驶证号
     */
    @ExcelField(title = "行驶证号")
    @Size(max = 20, message = "【行驶证号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String licenseNo;

    /**
     * 机架号(车架号)
     */
    @Size(max = 50, message = "【机架号】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "机架号")
    private String chassisNumber;

    /**
     * 发动机号
     */
    @Size(max = 20, message = "【发动机号】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "发动机号")
    private String engineNumber;

    /**
     * 使用性质
     */
    @ExcelField(title = "使用性质")
    @Size(max = 20, message = "【使用性质】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String usingNature;

    /**
     * 品牌型号(行驶证)
     */
    @ExcelField(title = "品牌型号")
    @Size(max = 20, message = "【品牌型号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String brandModel;

    /**
     * 行驶证有效期起(*Str导出使用)
     */
    @ExcelField(title = "行驶证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationStartDateStr;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationStartDate;

    /**
     * 行驶证有效期至
     */
    @ExcelField(title = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationEndDateStr;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationEndDate;

    /**
     * 行驶证发证日期
     */
    @ExcelField(title = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date licenseIssuanceDate;

    /**
     * 登记日期(行驶证)
     */
    @ExcelField(title = "行驶证登记日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationDateStr;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    /**
     * 提前提醒天数(行驶证)
     */
    @ExcelField(title = "行驶证提前提醒天数")
    @Max(value = 9999, message = "【行驶证提前提醒天数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【行驶证提前提醒天数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer registrationRemindDays;

    /**
     * 保养里程数
     */
    @ExcelField(title = "保养里程数")
    private Integer maintainMileage;

    /**
     * 保养有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date maintainValidity;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "保养有效期")
    private String maintainValidityStr;

    /**
     * 车台安装日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehiclePlatformInstallDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "车台安装日期")
    private String vehiclePlatformInstallDateStr;


    /*工程机械*/
    /**
     * 车主名(工程机械)  此处非空验证需要判断
     */
    //    @ExcelField(title = "车主名")
    private String vehicleOwnerName;

    /**
     * 车主手机1
     */
    //    @ExcelField(title = "车主手机1")
    private String ownerPhoneOne;

    /**
     * 车主手机2
     */
    //    @ExcelField(title = "车主手机2")
    private String ownerPhoneTwo;

    /**
     * 车主手机3
     */
    //    @ExcelField(title = "车主手机3")
    private String ownerPhoneThree;

    /**
     * 车主座机
     */
    //    @ExcelField(title = "车主座机")
    private String ownerLandline;

    /**
     * 车辆子类型Id(只用于接收前端传递的值,如果这个值存在，那么vehicle_type存,如果不存在，则存VehicleType)
     */
    private String vehicleSubTypeId;
    /**
     * 车辆子类型名称(编辑使用)
     */
    //    @ExcelField(title = "车辆子类型名称")
    private String vehicleSubtypes;

    /**
     * 自重
     */
    //    @ExcelField(title = "自重")
    private Double selfRespect;

    /**
     * 工作能力
     */
    //    @ExcelField(title = "工作能力")
    private Double abilityWork;

    /**
     * 工作半径
     */
    //    @ExcelField(title = "工作半径")
    private Double workingRadius;

    /**
     * 机龄
     */
    @DateTimeFormat(pattern = "yyyy-MM")
    //    @ExcelField(title = "机龄")
    private String machineAgeStr;
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date machineAge;

    /**
     * 品牌名称
     */
    //    @ExcelField(title = "品牌名称")
    private String brandName;

    /**
     * 机型名称
     */
    //    @ExcelField(title = "机型名称")
    private String modelName;

    /**
     * 品牌机型id(新增时存机型id) 如果为空传递-1
     */
    private String brandModelsId;

    /**
     * 初始里程
     */
    //    @ExcelField(title = "初始里程")
    private Double initialMileage;

    /**
     * 初始工时
     */
    //    @ExcelField(title = "初始工时")
    private Double initialWorkHours;

    /*货运*/
    /**
     * 车辆品牌(货运车辆)
     */
    //    @ExcelField(title = "车辆品牌")
    @Size(max = 20, message = "【车辆品牌】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String vehicleBrand;

    /**
     * 车辆型号(货运车辆)
     */
    //    @ExcelField(title = "车辆型号")
    @Size(max = 20, message = "【车辆型号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String vehicleModel;

    /**
     * 车辆出厂日期
     */
    //    @ExcelField(title = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleProductionDateStr;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleProductionDate;

    /**
     * 首次上线时间
     */
    //    @ExcelField(title = "首次上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String firstOnlineTimeStr;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstOnlineTime;

    /**
     * 车辆购置方式(0:分期付款;1:一次性付清)
     */
    @Max(value = 1, message = "【车辆购置方式】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【车辆购置方式】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short purchaseWay;
    //    @ExcelField(title = "车辆购置方式")
    private String purchaseWayStr;

    /**
     * 校验有效期至
     */
    //    @ExcelField(title = "校验有效期至")
    @DateTimeFormat(pattern = "yyyy-MM")
    private String validEndDateStr;

    @DateTimeFormat(pattern = "yyyy-MM")
    private Date validEndDate;

    /**
     * 执照上传数
     */
    //    @ExcelField(title = "执照上传数")
    @Max(value = 99, message = "【执照上传数】最大值99", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【执照上传数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short licenseNumbers;

    /**
     * 总质量(kg)
     */
    //    @ExcelField(title = "总质量(kg)")
    private String totalQuality;

    /**
     * 准牵引总质量(kg)
     */
    //    @ExcelField(title = "准牵引总质量(kg)")
    private String tractionTotalMass;

    /**
     * 外廓尺寸-长(mm)
     */
    //    @ExcelField(title = "外廓尺寸-长(mm)")
    private Integer profileSizeLong;

    /**
     * 外廓尺寸-宽(mm)
     */
    //    @ExcelField(title = "外廓尺寸-宽(mm)")
    private Integer profileSizeWide;

    /**
     * 外廓尺寸-高(mm)
     */
    //    @ExcelField(title = "外廓尺寸-高(mm)")
    private Integer profileSizeHigh;

    /**
     * 货厢内部尺寸-长(mm)
     */
    //    @ExcelField(title = "货厢内部尺寸-长(mm)")
    private Integer internalSizeLong;

    /**
     * 货厢内部尺寸-宽(mm)
     */
    //    @ExcelField(title = "货厢内部尺寸-宽(mm)")
    private Integer internalSizeWide;

    /**
     * 货厢内部尺寸-高(mm)
     */
    //    @ExcelField(title = "货厢内部尺寸-高(mm)")
    private Integer internalSizeHigh;

    /**
     * 轴数
     */
    //    @ExcelField(title = "轴数")
    private Integer shaftNumber;

    /**
     * 轮胎数(@Max 参数最大支持Integer的最大值,Long在此处暂时不验证)
     */
    //    @ExcelField(title = "轮胎数")
    private Integer tiresNumber;

    /**
     * 轮胎规格
     */
    //    @ExcelField(title = "轮胎规格")
    @Size(max = 20, message = "【轮胎规格】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String tireSize;

    /**
     * 备注
     */
    private String registrationRemark;



    /**
     * uuid
     */
    @ApiParam(value = "uuid")
    private String id = UUID.randomUUID().toString();

    /**
     * 是否显示
     */
    @ApiParam(value = "是否显示")
    private Integer flag = 1;

    /**
     * 优先级
     */
    @ApiParam(value = "优先级")
    private Integer priority = 1;

    /**
     * 顺序
     */
    @ApiParam(value = "顺序")
    private Integer sortOrder = 1;

    /**
     * 是否可编辑
     */
    @ApiParam(value = "是否可编辑")
    private Integer editable = 1;

    /**
     * 是否可用
     */
    @ApiParam(value = "是否可用")
    private Integer enabled = 1;

    /**
     * 数据创建时间
     */
    @ApiParam(value = "数据创建时间")
    private Date createDataTime = new Date();

    /**
     * 创建者username
     */
    @ApiParam(value = "创建者username")
    private String createDataUsername;

    /**
     * 数据修改时间
     */
    @ApiParam(value = "数据修改时间")
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    @ApiParam(value = "修改者username")
    private String updateDataUsername;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
