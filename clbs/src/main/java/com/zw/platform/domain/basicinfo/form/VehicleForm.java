package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 车辆管理
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleForm extends BaseFormBean implements Serializable, ConverterDateUtil {
    private static final long serialVersionUID = 1L;

    /**
     * 车牌号
     */
    @ApiParam(value = "车牌号 【必填】", required = true)
    @NotEmpty(message = "【车牌号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车牌号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    @Pattern(message = "【车牌号】格式错误，只能输入中位，数字，字母，短横线！", regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5-]{2,20}$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    private String brand;

    /**
     * 所属企业
     */
    @ApiParam(value = "所属企业 【必填】", required = true)
    @ExcelField(title = "所属企业", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String groupName;

    /**
     * 终端编号
     */
    @ApiParam(value = "终端编号")
    @ExcelField(title = "终端号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String deviceNumber;

    /**
     * SIM卡号
     */
    @ApiParam(value = "终端手机号")
    @ExcelField(title = "终端手机号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String simcardNumber;

    /**
     * 分组名称
     */
    @ApiParam(value = "分组名称")
    private String assignmentName;

    /**
     * 分组
     */
    @ApiParam(value = "分组")
    @ExcelField(title = "分组", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String assign;

    /**
     * 类别标准
     */
    @ApiParam(value = "类别标准")
    @ExcelField(title = "类别标准", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String standard;

    /**
     * 车辆类别
     */
    @ApiParam(value = "车辆类别")
    @ExcelField(title = "车辆类别", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleCategoryName;

    /**
     * 车辆类别id
     */
    @ApiParam(value = "车辆类别id")
    private String vehicleCategoryId;

    /**
     * 车辆类型(id：可能是类型id也可能是子类型id)
     */
    @ApiParam(value = "车辆类型(id：可能是类型id也可能是子类型id)")
    @ExcelField(title = "车辆类型", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleType;

    private String vehicleTypeName;

    /* 运输证信息  */
    /**
     * 道路运输证号
     */
    @ApiParam(value = "道路运输证号")
    @Size(max = 24, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "道路运输证号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String roadTransportNumber;

    /* 行驶证信息 */
    /**
     * 行驶证号
     */
    @ApiParam(value = "行驶证号")
    @ExcelField(title = "行驶证号", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    @Size(max = 20, message = "【行驶证号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String licenseNo;

    /**
     * 车辆别名
     */
    @ApiParam(value = "车辆别名")
    @Size(max = 20, message = "【车辆别名】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆别名", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String aliases;

    /**
     * 车主
     */
    @ApiParam(value = "车主")
    @Pattern(regexp = "^([A-Za-z\\u4e00-\\u9fa5]{1,8})?$", message = "【车主】只能填写中文、字母,长度不能超过8", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车主", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleOwner;

    /**
     * 车主电话
     */
    @ApiParam(value = "车主电话")
    @ExcelField(title = "车主电话", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleOwnerPhone;

    /**
     * 车辆等级
     */
    @ApiParam(value = "车辆等级")
    @ExcelField(title = "车辆等级", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleLevel;

    /**
     * 电话是否校验(0：未检验；1：已校验)(前端select，未选择传递-1,兼容ie)
     */
    @ApiParam(value = "电话是否校验(0：未检验；1：已校验)(前端select，未选择传递-1,兼容ie)")
    @ExcelField(title = "电话是否校验", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    @Max(value = 1, message = "【电话是否校验】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【电话是否校验】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String phoneCheck;

    /**
     * 车辆颜色 (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)
     */
    @ApiParam(value = "车辆颜色 (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)")
    @Pattern(message = "【车辆颜色】填值错误！", regexp = "^\\s*$|^[0-9]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "车辆颜色", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleColor;

    /**
     * 车牌颜色（0蓝、1黄、2白、3黑）
     */
    @ApiParam(value = "车牌颜色（0蓝、1黄、2白、3黑）")
    @Pattern(message = "【车牌颜色】填值错误！", regexp = "^\\s*$|^[0-3]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "车牌颜色", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String plateColorStr;

    @ApiParam(value = "车牌颜色（0蓝、1黄、2白、3黑）")
    private Integer plateColor;

    /**
     * 燃料类型id
     */
    @ApiParam(value = "燃料类型id")
    @ExcelField(title = "燃料类型", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String fuelType;

    private String fuelTypeName;

    /**
     * 区域属性
     */
    @ApiParam(value = "区域属性")
    @Size(max = 20, message = "【区域属性】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "区域属性", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String areaAttribute;

    /**
     * 省、直辖市(未选择)
     */
    @ApiParam(value = "省、直辖市(未选择)")
    @Size(max = 20, message = "【省、直辖市】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "省、直辖市", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String province;

    /**
     * 市、区
     */
    @ApiParam(value = "市、区")
    @Size(max = 20, message = "【市、区】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "市、区", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String city;

    /**
     * 车辆状态
     */
    @ApiParam(value = "车辆状态")
    @ExcelField(title = "车辆状态", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String isStarts;

    /**
     * 县
     */
    @ApiParam(value = "县")
    @ExcelField(title = "县")
    @Size(max = 20, message = "【县】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String county;

    /**
     * 车辆用途Id
     */
    @ApiParam(value = "车辆用途Id")
    @Size(max = 64, message = "【运营类别】不能超过64个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "运营类别", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehiclePurpose;

    private String vehiclePurposeName;

    /**
     * 所属行业
     */
    @ExcelField(title = "所属行业", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String tradeName;

    private Integer isStart = 1;

    /**
     * 核定载人数
     */
    @ApiParam(value = "核定载人数")
    @Max(value = 9999, message = "【核定载人数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【核定载人数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "核定载人数", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private Short numberLoad;

    /**
     * 核定载质量(kg)
     */
    @ApiParam(value = "核定载质量(kg)")
    @ExcelField(title = "核定载质量", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【核定载质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String loadingQuality;

    /**
     * 车辆保险单号
     */
    @ApiParam(value = "车辆保险单号")
    @Size(max = 50, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车辆保险单号")
    private String vehicleInsuranceNumber;

    /**
     * 车辆技术等级有效期
     */
    @ApiParam(value = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "车辆技术等级有效期", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehicleTechnologyValidityStr;// 车辆技术登记有效期String

    /**
     * 车辆技术等级有效期
     */
    @ApiParam(value = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleTechnologyValidity;

    /**
     * 是否维修(0：否；1：是)
     */
    @ApiParam(value = "是否维修(0：否；1：是)")
    @Max(value = 1, message = "【是否维修】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【是否维修】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short stateRepair;

    /**
     * 是否维修(0：否；1：是)
     */
    @ApiParam(value = "是否维修(0：否；1：是)")
    @ExcelField(title = "是否维修", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String stateRepairStr;

    /**
     * 创建时间
     */
    @ApiParam(value = "创建时间")
    @ExcelField(title = "创建时间", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String createDataTimeStr;

    /**
     * 修改时间
     */
    @ApiParam(value = "修改时间")
    @ExcelField(title = "修改时间", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String updateDataTimeStr;

    /**
     * 保养里程数
     */
    @ApiParam(value = "保养里程数")
    @ExcelField(title = "保养里程数(km)", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private Integer maintainMileage;

    /**
     * 保养有效期
     */
    @ApiParam(value = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date maintainValidity;

    /**
     * 保养有效期
     */
    @ApiParam(value = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "保养有效期", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String maintainValidityStr;

    /**
     * 车台安装日期
     */
    @ApiParam(value = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehiclePlatformInstallDate;

    /**
     * 车台安装日期
     */
    @ApiParam(value = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "车台安装日期", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String vehiclePlatformInstallDateStr;

    /**
     * 备注
     */
    @ApiParam(value = "备注")
    @Size(max = 150, message = "【备注】不能超过150个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注", groups = { PublicVariable.EXPORT_VEHICLE_FIELDS })
    private String remark;

    /**
     * 车辆营运证号(经营许可证-工程机械修改)
     */
    @ApiParam(value = "车辆营运证号")
    @Size(max = 20, groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "经营许可证号")
    private String vehiclOperationNumber;

    /**
     * 经营范围
     */
    @ApiParam(value = "经营范围")
    @ExcelField(title = "经营范围")
    private String scopeBusiness;

    private String scopeBusinessIds;

    private String scopeBusinessCodes;

    /**
     * 核发机关
     */
    @ApiParam(value = "核发机关")
    @Size(max = 255, message = "【核发机关】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "核发机关")
    private String issuedAuthority;

    /**
     * 经营权类型(0：国有；1：集体；2：私营；3：个体；4：联营；5：股份制；6：外商投资；7：港澳台及其他)(short)
     */
    @ApiParam(value = "经营权类型(0：国有；1：集体；2：私营；3：个体；4：联营；5：股份制；6：外商投资；7：港澳台及其他)(short)")
    @Max(value = 7, message = "【经营权类型】最大值{7}", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【经营权类型】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short managementType;

    /**
     * 经营权类型
     */
    @ApiParam(value = "经营权类型")
    @ExcelField(title = "经营权类型")
    private String managementTypeStr;

    /**
     * 道路运输证有效期起
     */
    @ApiParam(value = "道路运输证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "道路运输证有效期起")
    private String roadTransportValidityStartStr;

    /**
     * 道路运输证有效期起
     */
    @ApiParam(value = "道路运输证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidityStart;

    /**
     * 道路运输有效期至 (道路运输有效期至 -工程机械修改)
     */
    @ApiParam(value = "终端编号")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ExcelField(title = "道路运输有效期至")
    private String roadTransportValidityStr;// 道路运输有效期至 string

    /**
     * 道路运输有效期至
     */
    @ApiParam(value = "终端编号")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidity;

    /**
     * 线路牌号
     */
    @ApiParam(value = "线路牌号")
    @Size(max = 255, message = "【线路牌号】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "线路牌号")
    private String lineNumber;

    /**
     * 始发地
     */
    @ApiParam(value = "始发地")
    @Size(max = 255, message = "【始发地】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "始发地")
    private String provenance;

    /**
     * 途经站名
     */
    @ApiParam(value = "途经站名")
    @Size(max = 255, message = "【途经站名】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "途经站名")
    private String viaName;

    /**
     * 终到地
     */
    @ApiParam(value = "终到地")
    @Size(max = 255, message = "【终到地】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "终到地")
    private String destination;

    /**
     * 始发站
     */
    @ApiParam(value = "始发站")
    @Size(max = 255, message = "【始发站】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "始发站")
    private String departure;

    /**
     * 路线入口
     */
    @ApiParam(value = "路线入口")
    @Size(max = 255, message = "【路线入口】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "路线入口")
    private String routeEntry;

    /**
     * 终到站
     */
    @ApiParam(value = "终到站")
    @Size(max = 255, message = "【终到站】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "终到站")
    private String destinationStation;

    /**
     * 路线出口
     */
    @ApiParam(value = "路线出口")
    @Size(max = 255, message = "【路线出口】最大长度255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "路线出口")
    private String exportRoute;

    /**
     * 每日发班次数
     */
    @ApiParam(value = "每日发班次数")
    @ExcelField(title = "每日发班次数")
    @Max(value = 9999, message = "【每日发班次数】最大值9999")
    @Min(value = 0, message = "【每日发班次数】最小值0")
    private Integer dailyNumber;

    /**
     * 运输证提前提醒天数
     */
    @ApiParam(value = "运输证提前提醒天数")
    @ExcelField(title = "运输证提前提醒天数")
    @Max(value = 9999, message = "【运输证提前提醒天数】最大值9999")
    @Min(value = 0, message = "【运输证提前提醒天数】最小值0")
    private Integer managementRemindDays;

    /**
     * 营运状态(0:营运;1:停运;2:挂失;3:报废;4:歇业;5:注销;6:迁出(过户);7:迁出(转籍);8:其他)
     */
    @ApiParam(value = "营运状态(0:营运;1:停运;2:挂失;3:报废;4:歇业;5:注销;6:迁出(过户);7:迁出(转籍);8:其他)")
    @Max(value = 8, message = "【营运状态】最大值8", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【营运状态】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer operatingState;

    /**
     * 营运状态
     */
    @ApiParam(value = "营运状态")
    @ExcelField(title = "营运状态")
    private String operatingStateStr;

    /**
     * 机架号(车架号 - 工程机械)
     */
    @ApiParam(value = "机架号(车架号 - 工程机械)")
    @Size(max = 50, message = "【车架号】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车架号")
    private String chassisNumber;

    /**
     * 发动机号
     */
    @ApiParam(value = "发动机号")
    @Size(max = 20, message = "【发动机号】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "发动机号")
    private String engineNumber;

    /**
     * 使用性质
     */
    @ApiParam(value = "使用性质")
    @ExcelField(title = "使用性质")
    @Size(max = 20, message = "【使用性质】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String usingNature;

    /**
     * 品牌型号(行驶证)
     */
    @ApiParam(value = "品牌型号(行驶证)")
    @ExcelField(title = "品牌型号")
    @Size(max = 20, message = "【品牌型号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String brandModel;

    /**
     * 行驶证有效期起(*Str导出使用)
     */
    @ApiParam(value = "行驶证有效期起(*Str导出使用)")
    @ExcelField(title = "行驶证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationStartDateStr;

    /**
     * 行驶证有效期起(*Str导出使用)
     */
    @ApiParam(value = "行驶证有效期起(*Str导出使用)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationStartDate;

    /**
     * 行驶证有效期至
     */
    @ApiParam(value = "行驶证有效期至")
    @ExcelField(title = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationEndDateStr;

    /**
     * 行驶证有效期至
     */
    @ApiParam(value = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationEndDate;

    /**
     * 行驶证发证日期
     */
    @ApiParam(value = "终端编号")
    @ExcelField(title = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;

    /**
     * 行驶证发证日期
     */
    @ApiParam(value = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date licenseIssuanceDate;

    /**
     * 登记日期(行驶证)
     */
    @ApiParam(value = "登记日期(行驶证)")
    @ExcelField(title = "行驶证登记日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationDateStr;

    /**
     * 登记日期(行驶证)
     */
    @ApiParam(value = "登记日期(行驶证)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    /**
     * 提前提醒天数(行驶证)
     */
    @ApiParam(value = "行驶证提前提醒天数")
    @ExcelField(title = "行驶证提前提醒天数")
    @Max(value = 9999, message = "【行驶证提前提醒天数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【行驶证提前提醒天数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer registrationRemindDays;

    /* 货运信息 */

    /**
     * 车辆品牌(货运车辆)
     */
    @ApiParam(value = "车辆品牌")
    @ExcelField(title = "车辆品牌")
    @Size(max = 20, message = "【车辆品牌】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String vehicleBrand;

    /**
     * 车辆型号(货运车辆)
     */
    @ApiParam(value = "车辆型号(货运车辆)")
    @ExcelField(title = "车辆型号")
    @Size(max = 20, message = "【车辆型号】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String vehicleModel;

    /**
     * 车辆出厂日期
     */
    @ApiParam(value = "终端编号")
    @ExcelField(title = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleProductionDateStr;

    /**
     * 车辆出厂日期
     */
    @ApiParam(value = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleProductionDate;

    /**
     * 首次上线时间
     */
    @ApiParam(value = "首次上线时间")
    @ExcelField(title = "首次上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String firstOnlineTimeStr;

    /**
     * 首次上线时间
     */
    @ApiParam(value = "首次上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstOnlineTime;

    /**
     * 车辆购置方式(0:分期付款;1:一次性付清)
     */
    @ApiParam(value = "车辆购置方式(0:分期付款;1:一次性付清)")
    @Max(value = 1, message = "【车辆购置方式】最大值1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = -1, message = "【车辆购置方式】最小值-1", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short purchaseWay;

    /**
     * 车辆购置方式
     */
    @ApiParam(value = "车辆购置方式")
    @ExcelField(title = "车辆购置方式")
    private String purchaseWayStr;

    /**
     * 校验有效期至
     */
    @ApiParam(value = "校验有效期至")
    @ExcelField(title = "校验有效期至")
    @DateTimeFormat(pattern = "yyyy-MM")
    private String validEndDateStr;

    /**
     * 校验有效期至
     */
    @ApiParam(value = "校验有效期至")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date validEndDate;

    /**
     * 执照上传数
     */
    @ApiParam(value = "执照上传数")
    @ExcelField(title = "执照上传数")
    @Max(value = 99, message = "【执照上传数】最大值99", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【执照上传数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Short licenseNumbers;

    /**
     * 总质量(kg)
     */
    @ApiParam(value = "总质量(kg)")
    @ExcelField(title = "总质量(kg)")
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【总质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String totalQuality;

    /**
     * 准牵引总质量(kg)
     */
    @ApiParam(value = "准牵引总质量(kg)")
    @ExcelField(title = "准牵引总质量(kg)")
    @Pattern(regexp = "^((?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9]))?$",
        message = "【准牵引总质量(kg)】0.1~9999999999(如果有小数范围则是0.1~99999999.9)")
    private String tractionTotalMass;

    /**
     * 外廓尺寸-长(mm)
     */
    @ApiParam(value = "外廓尺寸-长(mm)")
    @ExcelField(title = "外廓尺寸-长(mm)")
    @Max(value = 999999, message = "【外廓尺寸-长(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-长(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeLong;

    /**
     * 外廓尺寸-宽(mm)
     */
    @ApiParam(value = "外廓尺寸-宽(mm)")
    @ExcelField(title = "外廓尺寸-宽(mm)")
    @Max(value = 999999, message = "【外廓尺寸-宽(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-宽(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeWide;

    /**
     * 外廓尺寸-高(mm)
     */
    @ApiParam(value = "外廓尺寸-高(mm)")
    @ExcelField(title = "外廓尺寸-高(mm)")
    @Max(value = 999999, message = "【外廓尺寸-高(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【外廓尺寸-高(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer profileSizeHigh;

    /**
     * 货厢内部尺寸-长(mm)
     */
    @ApiParam(value = "货厢内部尺寸-长(mm)")
    @ExcelField(title = "货厢内部尺寸-长(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-长(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-长(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeLong;

    /**
     * 货厢内部尺寸-宽(mm)
     */
    @ApiParam(value = "货厢内部尺寸-宽(mm)")
    @ExcelField(title = "货厢内部尺寸-宽(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-宽(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-宽(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeWide;

    /**
     * 货厢内部尺寸-高(mm)
     */
    @ApiParam(value = "货厢内部尺寸-高(mm)")
    @ExcelField(title = "货厢内部尺寸-高(mm)")
    @Max(value = 999999, message = "【货厢内部尺寸-高(mm)】最大值999999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【货厢内部尺寸-高(mm)】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer internalSizeHigh;

    /**
     * 轴数
     */
    @ApiParam(value = "轴数")
    @ExcelField(title = "轴数")
    @Max(value = 9999, message = "【轴数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【轴数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer shaftNumber;

    /**
     * 轮胎数(@Max 参数最大支持Integer的最大值,Long在此处暂时不验证)
     */
    @ApiParam(value = "轮胎数(@Max 参数最大支持Integer的最大值,Long在此处暂时不验证)")
    @ExcelField(title = "轮胎数")
    @Max(value = 9999, message = "【轮胎数】最大值9999", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【轮胎数】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer tiresNumber;

    /**
     * 轮胎规格
     */
    @ApiParam(value = "轮胎规格")
    @ExcelField(title = "轮胎规格")
    @Size(max = 20, message = "【轮胎规格】最大长度20", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String tireSize;

    /* 工程机械信息 */

    /**
     * 车主名(工程机械)
     */
    @ApiParam(value = "车主名(工程机械)")
    @Pattern(regexp = "^([A-Za-z\\u4e00-\\u9fa5]{1,8})?$", message = "【车主姓名】只能填写中文、字母,长度不能超过8", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "车主姓名")
    private String vehicleOwnerName;

    /**
     * 车主手机1
     */
    @ApiParam(value = "车主手机1")
    @ExcelField(title = "车主手机1")
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【车主手机1】数字,长度7~13位", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String ownerPhoneOne;

    /**
     * 车主手机2
     */
    @ApiParam(value = "车主手机2")
    @ExcelField(title = "车主手机2")
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【车主手机2】数字,长度7~13位", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String ownerPhoneTwo;

    /**
     * 车主手机3
     */
    @ApiParam(value = "车主手机3")
    @ExcelField(title = "车主手机3")
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【车主手机3】数字,长度7~13位", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String ownerPhoneThree;

    /**
     * 车主座机
     */
    @ApiParam(value = "车主座机")
    @ExcelField(title = "车主座机")
    @Pattern(regexp = "^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$", message = "【车主座机】数字,长度7~13位", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    private String ownerLandline;

    /**
     * 车辆子类型Id(只用于接收前端传递的值,如果这个值存在，那么vehicle_type存,如果不存在，则存VehicleType)
     */
    @ApiParam(value = "车辆子类型Id")
    private String vehicleSubTypeId;

    /**
     * 车辆子类型名称(编辑使用)
     */
    @ApiParam(value = "车辆子类型名称(编辑使用)")
    @ExcelField(title = "车辆子类型名称")
    private String vehicleSubtypes;

    /**
     * 自重
     */
    @ApiParam(value = "自重")
    @ExcelField(title = "自重")
    @DecimalMax(value = "9999.9", message = "【自重】最大值9999.9", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @DecimalMin(value = "0", message = "【自重】最大小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double selfRespect;

    /**
     * 工作能力
     */
    @ApiParam(value = "工作能力")
    @ExcelField(title = "工作能力")
    @DecimalMax(value = "9999.9", message = "【工作能力】最大值9999.9", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @DecimalMin(value = "0", message = "【工作能力】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double abilityWork;

    /**
     * 工作半径
     */
    @ApiParam(value = "工作半径")
    @ExcelField(title = "工作半径")
    @DecimalMax(value = "999.9", message = "【工作半径】最大值999.9", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @DecimalMin(value = "0", message = "【工作半径】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double workingRadius;

    /**
     * 机龄
     */
    @ApiParam(value = "机龄")
    @DateTimeFormat(pattern = "yyyy-MM")
    @ExcelField(title = "机龄")
    private String machineAgeStr;

    /**
     * 机龄
     */
    @ApiParam(value = "机龄")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date machineAge;

    /**
     * 品牌机型id(新增时存机型id) 如果为空传递-1
     */
    @ApiParam(value = "品牌机型id(新增时存机型id) 如果为空传递-1")
    private String brandModelsId;

    /**
     * 机型名称
     */
    @ApiParam(value = "机型名称")
    @ExcelField(title = "机型名称")
    private String modelName;

    /**
     * 品牌名称
     */
    @ApiParam(value = "品牌名称")
    @ExcelField(title = "品牌名称")
    private String brandName;

    /**
     * 初始里程(此处长度过长，因此加一个0.01来判断)
     */
    @ApiParam(value = "初始里程(此处长度过长，因此加一个0.01来判断)")
    @ExcelField(title = "初始里程")
    @DecimalMax(value = "9999999.91", message = "【初始里程】最大值9999999.9", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @DecimalMin(value = "0", message = "【初始里程】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double initialMileage;

    /**
     * 初始工时
     */
    @ApiParam(value = "初始工时")
    @ExcelField(title = "初始工时")
    @DecimalMax(value = "9999999.91", message = "【初始工时】最大值9999999.9", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @DecimalMin(value = "0", message = "【初始工时】最小值0", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double initialWorkHours;

    /**
     * 车辆编号
     */
    // @ExcelField(title = "车辆编号")
    // @NotEmpty(message = "【车辆编号】不能为空！", groups = { ValidGroupAdd.class })
    // @Size(max = 20, message = "【姓名】，最大20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "车辆编号")
    private String vehicleNumber;

    /**
     * 组织id
     */
    @ApiParam(value = "组织id 【必填】", required = true)
    @NotEmpty(message = "【所属企业】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String groupId;

    /**
     * 上线时间
     */
    @ApiParam(value = "上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Deprecated
    private Date onlineTime;

    /**
     * 车辆照片
     */
    //    @ExcelField(title = "车辆照片")
    @ApiParam(value = "车辆照片")
    private String vehiclePhoto;

    /**
     * 运输证照片
     */
    private String transportNumberPhoto;

    /**
     * 行驶证正本照片
     */
    private String drivingLicenseFrontPhoto;

    /**
     * 行驶证副本照片
     */
    private String drivingLicenseDuplicatePhoto;

    /**
     * 车辆图标 id
     */
    @ApiParam(value = "车辆图标")
    private String vehicleIcon;

    /**
     * 车辆图标名
     */
    @ApiParam(value = "车辆图标名")
    private String vehicleIconName;

    /**
     * 设备id
     */
    @ApiParam(value = "设备id")
    private String deviceId;

    /**
     * 设备名称
     */
    @ApiParam(value = "设备名称")
    private String deviceName;

    /**
     * 通讯类型
     */
    @ApiParam(value = "通讯类型")
    private String deviceType; // 通讯类型

    /**
     * 车辆类型名称
     */
    @ApiParam(value = "车辆类型名称")
    private String vehiclet;

    /**
     * 分组id
     */
    @ApiParam(value = "分组id")
    private String assignId;

    /**
     * 分组所属组织id
     */
    @ApiParam(value = "分组所属组织id")
    private String assignGroup;

    /**
     *
     */
    @ApiParam(value = "子类型")
    private String vehType;

    /**
     * 备注
     */
    @ApiParam(value = "备注")
    @Size(max = 150, message = "【备注】不能超过150个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String registrationRemark;

    /**
     * 省域ID
     */
    private String provinceId;

    /**
     * 市域ID
     */
    private String cityId;

    /**
     * 市域ID
     */
    private String areaNumber;

    public static VehicleForm initConfigImport(ConfigImportForm config, String userName) {
        Date createDate = new Date();
        VehicleForm form = new VehicleForm();
        String[] assignmentAndGroup = config.getGroupName().split("@");
        form.setDeviceId(config.getDeviceID());
        form.setDeviceType(config.getDeviceType());
        form.setBrand(config.getCarLicense());
        form.setSimcardNumber(config.getSimcardNumber());
        form.setDeviceNumber(config.getDeviceNumber());
        form.setCreateDataUsername(userName);
        form.setGroupId(config.getGroupId());
        form.setGroupName(config.getCompanyName());
        form.setAssignmentName(assignmentAndGroup[0]);
        form.setVehicleType("default");
        form.setCodeNum("90");
        form.setVehType("其它车辆");
        form.setStandard("0");
        form.setVehicleCategoryId("default");
        form.setVehicleCategoryName("其他车辆");
        form.setCreateDataTime(createDate);
        form.setCreateDataTimeStr(DateFormatUtils.format(createDate, "yyyy-MM-dd"));
        form.setOperatingState(0);
        //        form.setVehicleIcon("a21c573d-b482-4a1d-8e89-de786284f74b"); // 默认图标
        form.setVehicleColor(VehicleColor.BLACK.getCodeVal());
        config.setPlateColor(PlateColor.getCodeOrDefaultByName(config.getPlateColorStr()));
        form.setPlateColor(PlateColor.getCodeOrDefaultByName(config.getPlateColorStr()));
        return form;
    }

    public static VehicleForm initConfigTransportImport(ConfigTransportImportForm config, String userName) {
        Date createDate = new Date();
        VehicleForm form = new VehicleForm();
        // String[] assignmentAndGroup = config.getGroupName().split("@");
        form.setDeviceId(config.getDeviceID());
        form.setDeviceType(config.getDeviceType());
        form.setBrand(config.getBrand());
        form.setSimcardNumber(config.getSimcardNumber());
        form.setDeviceNumber(config.getDeviceNumber());
        form.setCreateDataUsername(userName);
        form.setGroupId(config.getGroupId());
        // form.setGroupName(assignmentAndGroup[1]);
        // form.setAssignmentName(assignmentAndGroup[0]);
        form.setVehicleType(config.getVehicleType());
        form.setVehType(config.getVehType());
        form.setStandard(String.valueOf(config.getStandard()));
        form.setVehicleCategoryId(config.getVehicleCategoryId());
        form.setVehicleCategoryName(config.getVehicleCategoryName());
        form.setCreateDataTime(createDate);
        form.setCreateDataTimeStr(DateFormatUtils.format(createDate, "yyyy-MM-dd"));
        form.setVehicleIcon("a21c573d-b482-4a1d-8e89-de786284f74b"); // 默认图标
        form.setVehicleColor(getVehicleColor(config.getVehicleColorStr()));
        Integer plateColor = getPlateColor(config.getPlateColorStr());
        config.setPlateColor(plateColor);
        form.setPlateColor(plateColor);

        form.setRoadTransportNumber(config.getRoadTransportNumber());
        form.setProvince(config.getProvince());
        form.setCity(config.getCity());
        form.setCounty(config.getCounty());
        form.setVehicleOwner(config.getVehicleOwner());
        form.setVehicleOwnerPhone(config.getVehicleOwnerPhone());
        form.setPhoneCheck("已验证".equals(config.getPhoneCheck()) ? "1" : "0");
        form.setFirstOnlineTime(config.getFirstOnlineTime());
        form.setChassisNumber(config.getChassisNumber());
        form.setVehicleBrand(config.getVehicleBrand());
        form.setVehicleModel(config.getVehicleModel());
        form.setTotalQuality(config.getTotalQuality());
        form.setLoadingQuality(config.getLoadingQuality());
        form.setTractionTotalMass(config.getTractionTotalMass());
        form.setProfileSizeLong(config.getProfileSizeLong());
        form.setProfileSizeWide(config.getProfileSizeWide());
        form.setProfileSizeHigh(config.getProfileSizeHigh());
        form.setInternalSizeLong(config.getInternalSizeLong());
        form.setInternalSizeWide(config.getInternalSizeWide());
        form.setInternalSizeHigh(config.getInternalSizeHigh());
        form.setShaftNumber(config.getShaftNumber());
        form.setTiresNumber(config.getTiresNumber());
        form.setTireSize(config.getTireSize());
        form.setVehicleProductionDateStr(config.getVehicleProductionDateStr());
        form.setVehicleProductionDate(config.getVehicleProductionDate());
        form.setScopeBusiness(config.getScopeBusiness());
        form.setVehiclOperationNumber(config.getVehicleOperationNumber());
        form.setPurchaseWay("一次性付清".equals(config.getPurchaseWayStr()) ? (short) 1 : (short) 0);
        form.setValidEndDateStr(config.getValidEndDateStr());
        form.setLicenseNumbers(config.getLicenseNumbers());
        form.setLicenseIssuanceDate(config.getLicenseIssuanceDate());
        form.setEngineNumber(config.getEngineNumber());
        form.setOperatingState(0);
        return form;
    }

    public static Integer getPlateColor(String plateColorStr) {

        return PlateColor.getCodeOrDefaultByName(plateColorStr);
    }

    public static String getVehicleColor(String vehicleColorStr) {
        return VehicleColor.getCodeOrDefaultByName(vehicleColorStr);
    }

    /**
     * 识别码
     */
    private String codeNum;

    /**
     * 运营类别识别码
     */
    private String purposeCodeNum;

    /**
     * 行政区划代码
     */
    private Boolean hasDivisionCode = false;

    public VehicleDTO convert() {
        VehicleDTO vehicle = new VehicleDTO();
        BeanUtils.copyProperties(this, vehicle);
        vehicle.setAlias(this.aliases);
        vehicle.setName(this.brand);
        vehicle.setOrgId(this.groupId);
        vehicle.setOrgName(this.groupName);
        if (StringUtils.isNotBlank(this.isStarts)) {
            vehicle.setIsStart(Integer.parseInt(this.isStarts));
        }
        vehicle.setMonitorType(MonitorTypeEnum.VEHICLE.getType());
        vehicle.setBindType(Vehicle.BindType.UNBIND);
        if (Objects.nonNull(this.phoneCheck)) {
            vehicle.setPhoneCheck(Integer.parseInt(this.phoneCheck));
        }
        if (Objects.nonNull(this.numberLoad)) {
            vehicle.setNumberLoad(Integer.valueOf(this.numberLoad));
        }
        vehicle.setGroupId(null);
        vehicle.setGroupName(null);

        if (Objects.nonNull(this.managementType)) {
            vehicle.setManagementType(Integer.valueOf(this.managementType));
        }
        if (Objects.nonNull(this.stateRepair)) {
            vehicle.setStateRepair(Integer.valueOf(this.stateRepair));
        }
        if (Objects.nonNull(this.purchaseWay)) {
            vehicle.setPurchaseWay(Integer.valueOf(this.purchaseWay));
        }
        if (Objects.nonNull(this.licenseNumbers)) {
            vehicle.setLicenseNumbers(Integer.valueOf(this.licenseNumbers));
        }
        return vehicle;
    }

}
