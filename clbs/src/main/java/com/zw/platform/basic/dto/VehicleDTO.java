package com.zw.platform.basic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 车辆信息DTO
 *
 * @author zhangjuan
 * @date 2020/9/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDTO extends BindDTO {
    @ApiParam(value = "类别标准")
    private Integer standard;

    @ApiParam(value = "车辆类别")
    private String vehicleCategoryName;

    @ApiParam(value = "车辆类别id")
    private String vehicleCategoryId;

    @ApiParam(value = "车辆类型(id：类型id)")
    private String vehicleType;

    @ApiParam(value = "车辆类型")
    private String vehicleTypeName;

    @ApiParam(value = "车主")
    private String vehicleOwner;

    @ApiParam(value = "车主电话")
    private String vehicleOwnerPhone;

    @ApiParam(value = "电话是否校验(0：未检验；1：已校验)(前端select，未选择传递-1,兼容ie)")
    private Integer phoneCheck;
    private String phoneCheckStr;

    @ApiParam(value = "车辆颜色 (0黑、1白、2红、3蓝、4紫、5黄、6绿、7粉、8棕、9灰)")
    private String vehicleColor;

    @ApiParam(value = "车辆颜色")
    private String vehicleColorStr;

    @ApiParam(value = "车牌颜色")
    private String plateColorStr;

    @ApiParam(value = "燃料类型id")
    private String fuelType;

    private String fuelTypeName;

    @ApiParam(value = "区域属性")
    private String areaAttribute;


    @ApiParam(value = "省、直辖市(未选择)")
    private String province;


    @ApiParam(value = "市、区")
    private String city;

    @ApiParam(value = "县")
    private String county;

    @ApiParam(value = "运营类别ID")
    private String vehiclePurpose;

    @ApiParam(value = "运营类别名称")
    private String vehiclePurposeName;

    @ApiParam(value = "行业类别")
    private String tradeName;

    @ApiParam(value = "车辆状态")
    private String isStartStr;

    private Integer isStart = 1;

    @ApiParam(value = "核定载人数")
    private Integer numberLoad;
    @ApiParam(value = "核定载质量(kg)")
    private String loadingQuality;

    @ApiParam(value = "车辆等级")
    private String vehicleLevel;

    @ApiParam(value = "车辆保险单号")
    private String vehicleInsuranceNumber;

    @ApiParam(value = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleTechnologyValidityStr;

    @ApiParam(value = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleTechnologyValidity;

    @ApiParam(value = "保养里程数")
    private Integer maintainMileage;

    @ApiParam(value = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date maintainValidity;

    @ApiParam(value = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String maintainValidityStr;

    @ApiParam(value = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehiclePlatformInstallDate;

    @ApiParam(value = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehiclePlatformInstallDateStr;

    @ApiParam(value = "是否维修(0：否；1：是)")
    private Integer stateRepair;

    @ApiParam(value = "是否维修(0：否；1：是)")
    private String stateRepairStr;

    @ApiParam(value = "道路运输证号")
    private String roadTransportNumber;

    /**
     * 运输证信息
     */
    @ApiParam(value = "车辆营运证号")
    private String vehiclOperationNumber;

    @ApiParam(value = "经营范围")
    private String scopeBusiness;

    @ApiParam(value = "核发机关")
    private String issuedAuthority;

    @ApiParam(value = "经营权类型(0：国有；1：集体；2：私营；3：个体；4：联营；5：股份制；6：外商投资；7：港澳台及其他)(short)")
    private Integer managementType;

    @ApiParam(value = "经营权类型")
    private String managementTypeStr;

    @ApiParam(value = "道路运输证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String roadTransportValidityStartStr;


    @ApiParam(value = "道路运输证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidityStart;

    @ApiParam(value = "道路运输证有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String roadTransportValidityStr;

    @ApiParam(value = "道路运输证有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date roadTransportValidity;


    @ApiParam(value = "线路牌号")
    private String lineNumber;

    @ApiParam(value = "始发地")
    private String provenance;

    @ApiParam(value = "途经站名")
    private String viaName;

    @ApiParam(value = "终到地")
    private String destination;

    @ApiParam(value = "始发站")
    private String departure;

    @ApiParam(value = "路线入口")
    private String routeEntry;

    @ApiParam(value = "终到站")
    private String destinationStation;

    @ApiParam(value = "路线出口")
    private String exportRoute;

    @ApiParam(value = "每日发班次数")
    private Integer dailyNumber;

    @ApiParam(value = "运输证提前提醒天数")
    private Integer managementRemindDays;

    @ApiParam(value = "营运状态(0:营运;1:停运;2:挂失;3:报废;4:歇业;5:注销;6:迁出(过户);7:迁出(转籍);8:其他)")
    private Integer operatingState;

    @ApiParam(value = "营运状态")
    private String operatingStateStr;

    /**
     * 行驶证信息
     */
    @ApiParam(value = "机架号(车架号 - 工程机械)")
    private String chassisNumber;

    @ApiParam(value = "发动机号")
    private String engineNumber;

    @ApiParam(value = "使用性质")
    private String usingNature;

    @ApiParam(value = "品牌型号(行驶证)")
    private String brandModel;

    @ApiParam(value = "行驶证有效期起(*Str导出使用)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationStartDateStr;

    @ApiParam(value = "行驶证有效期起(*Str导出使用)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationStartDate;

    @ApiParam(value = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationEndDateStr;


    @ApiParam(value = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationEndDate;


    @ApiParam(value = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;


    @ApiParam(value = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date licenseIssuanceDate;


    @ApiParam(value = "登记日期(行驶证)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationDateStr;


    @ApiParam(value = "登记日期(行驶证)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    @ApiParam(value = "行驶证提前提醒天数")
    private Integer registrationRemindDays;


    /**
     * 获取信息
     */
    @ApiParam(value = "车辆品牌")
    private String vehicleBrand;

    @ApiParam(value = "车辆型号(货运车辆)")
    private String vehicleModel;


    @ApiParam(value = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleProductionDateStr;


    @ApiParam(value = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vehicleProductionDate;

    @ApiParam(value = "首次上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String firstOnlineTimeStr;

    @ApiParam(value = "首次上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstOnlineTime;

    @ApiParam(value = "车辆购置方式(0:分期付款;1:一次性付清)")
    private Integer purchaseWay;

    @ApiParam(value = "车辆购置方式")
    private String purchaseWayStr;


    @ApiParam(value = "校验有效期至")
    @DateTimeFormat(pattern = "yyyy-MM")
    private String validEndDateStr;

    @ApiParam(value = "校验有效期至")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date validEndDate;

    @ApiParam(value = "执照上传数")
    private Integer licenseNumbers;


    @ApiParam(value = "总质量(kg)")
    private String totalQuality;


    @ApiParam(value = "准牵引总质量(kg)")
    private String tractionTotalMass;


    @ApiParam(value = "外廓尺寸-长(mm)")
    private Integer profileSizeLong;


    @ApiParam(value = "外廓尺寸-宽(mm)")
    private Integer profileSizeWide;


    @ApiParam(value = "外廓尺寸-高(mm)")
    private Integer profileSizeHigh;

    @ApiParam(value = "货厢内部尺寸-长(mm)")
    private Integer internalSizeLong;

    @ApiParam(value = "货厢内部尺寸-宽(mm)")
    private Integer internalSizeWide;

    @ApiParam(value = "货厢内部尺寸-高(mm)")
    private Integer internalSizeHigh;

    @ApiParam(value = "轴数")
    private Integer shaftNumber;

    @ApiParam(value = "轮胎数(@Max 参数最大支持Integer的最大值,Long在此处暂时不验证)")
    private Integer tiresNumber;

    @ApiParam(value = "轮胎规格")
    private String tireSize;

    /**
     * ****************************工程机械********************************
     */
    @ApiParam(value = "车主名(工程机械)")
    private String vehicleOwnerName;


    @ApiParam(value = "车主手机1")
    private String ownerPhoneOne;

    @ApiParam(value = "车主手机2")
    private String ownerPhoneTwo;

    @ApiParam(value = "车主手机3")
    private String ownerPhoneThree;


    @ApiParam(value = "车主座机")
    private String ownerLandline;

    @ApiParam(value = "车辆子类型Id")
    private String vehicleSubTypeId;

    @ApiParam(value = "车辆子类型名称(编辑使用)")
    private String vehicleSubType;

    @ApiParam(value = "自重")
    private Double selfRespect;


    @ApiParam(value = "工作能力")
    private Double abilityWork;


    @ApiParam(value = "工作半径")
    private Double workingRadius;


    @ApiParam(value = "机龄")
    @DateTimeFormat(pattern = "yyyy-MM")
    private String machineAgeStr;


    @ApiParam(value = "机龄")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date machineAge;

    @ApiParam(value = "品牌机型id(新增时存机型id) 如果为空传递-1")
    private String brandModelsId;

    @ApiParam(value = "机型名称")
    private String modelName;

    @ApiParam(value = "品牌名称")
    private String brandName;


    @ApiParam(value = "初始里程(此处长度过长，因此加一个0.01来判断)")
    private Double initialMileage;

    @ApiParam(value = "初始工时")
    private Double initialWorkHours;


    @ApiParam(value = "上线时间")
    private Date onlineTime;

    @ApiParam(value = "车辆照片")
    private String vehiclePhoto;

    @ApiParam(value = "行驶证号")
    private String licenseNo;


    @ApiParam(value = "创建时间")
    private String createDataTimeStr;

    @ApiParam(value = "修改时间")
    private String updateDataTimeStr;

    @ApiParam(value = "运输证照片")
    private String transportNumberPhoto;


    @ApiParam(value = "行驶证正本照片")
    private String drivingLicenseFrontPhoto;

    @ApiParam(value = "行驶证副本照片")
    private String drivingLicenseDuplicatePhoto;

    @ApiParam(value = "车辆图标ID")
    private String vehicleIcon;


    @ApiParam(value = "车辆图标名")
    private String vehicleIconName;


    @ApiParam(value = "备注")
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

    /**
     * 识别码
     */
    private String codeNum;

    /**
     * 运营类别识别码
     */
    private String purposeCodeNum;

    private String scopeBusinessIds;

    private String scopeBusinessCodes;

    @ApiModelProperty(value = "入网标识（0代表未入网，1带表入网）")
    private Integer accessNetwork;

}
