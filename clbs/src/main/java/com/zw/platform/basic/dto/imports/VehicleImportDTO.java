package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 车辆导入车辆通用模板数据导入
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VehicleImportDTO extends ImportErrorData {

    @ExcelField(title = "车牌号", required = true, repeatable = false)
    private String name;

    @ExcelField(title = "所属企业", required = true)
    private String orgName;

    @ExcelField(title = "类别标准", required = true)
    private String standardStr;

    @ExcelField(title = "车辆类别", required = true)
    private String vehicleCategoryName;

    @ExcelField(title = "车辆类型", required = true)
    private String vehicleTypeName;

    @ExcelField(title = "车辆别名")
    private String alias;

    @ExcelField(title = "车主")
    private String vehicleOwner;

    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone;

    @ExcelField(title = "车辆等级")
    private String vehicleLevel;

    @ExcelField(title = "电话是否校验")
    private String phoneCheckStr;

    @ExcelField(title = "区域属性")
    private String areaAttribute;

    @ExcelField(title = "省、直辖市")
    private String province;

    @ExcelField(title = "市、区")
    private String city;

    @ExcelField(title = "县")
    private String county;

    @ExcelField(title = "燃料类型")
    private String fuelTypeName;

    @ExcelField(title = "车辆颜色")
    private String vehicleColorStr;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;
    @ExcelField(title = "车辆状态")
    private String isStartStr;

    @ExcelField(title = "核定载人数")
    private Integer numberLoad;

    @ExcelField(title = "核定载质量")
    private String loadingQuality;

    @ExcelField(title = "车辆保险单号")
    private String vehicleInsuranceNumber;

    @ExcelField(title = "运营类别")
    private String vehiclePurposeName;

    @ExcelField(title = "所属行业")
    private String tradeName;

    @ExcelField(title = "是否维修")
    private String stateRepairStr;

    @ExcelField(title = "车辆照片")
    private String vehiclePhoto;

    @ExcelField(title = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleTechnologyValidityStr;

    @ExcelField(title = "道路运输证号")
    private String roadTransportNumber;

    @ExcelField(title = "经营许可证号")
    private String vehiclOperationNumber;

    @ExcelField(title = "经营范围")
    private String scopeBusiness;

    @ExcelField(title = "核发机关")
    private String issuedAuthority;

    @ExcelField(title = "经营权类型")
    private String managementTypeStr;

    @ExcelField(title = "道路运输证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String roadTransportValidityStartStr;

    @ExcelField(title = "道路运输证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String roadTransportValidityStr;

    @ExcelField(title = "线路牌号")
    private String lineNumber;

    @ExcelField(title = "始发地")
    private String provenance;

    @ExcelField(title = "途经站名")
    private String viaName;

    @ExcelField(title = "终到地")
    private String destination;

    @ExcelField(title = "始发站")
    private String departure;
    @ExcelField(title = "路线入口")
    private String routeEntry;

    @ExcelField(title = "终到站")
    private String destinationStation;

    @ExcelField(title = "路线出口")
    private String exportRoute;

    @ExcelField(title = "每日发班次数")
    private Integer dailyNumber;

    @ExcelField(title = "运输证提前提醒天数")
    private Integer managementRemindDays;

    @ExcelField(title = "营运状态")
    private String operatingStateStr;
    @ExcelField(title = "行驶证号")
    private String licenseNo;

    @ExcelField(title = "车架号")
    private String chassisNumber;

    @ExcelField(title = "发动机号")
    private String engineNumber;

    @ExcelField(title = "使用性质")
    private String usingNature;

    @ExcelField(title = "品牌型号")
    private String brandModel;

    @ExcelField(title = "行驶证有效期起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationStartDateStr;

    @ExcelField(title = "行驶证有效期至")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationEndDateStr;

    @ExcelField(title = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;

    @ExcelField(title = "行驶证登记日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String registrationDateStr;

    @ExcelField(title = "行驶证提前提醒天数")
    private Integer registrationRemindDays;

    @ExcelField(title = "保养里程数(km)")
    private Integer maintainMileage;

    @ExcelField(title = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String maintainValidityStr;

    @ExcelField(title = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehiclePlatformInstallDateStr;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

}
