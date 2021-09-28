package com.zw.platform.basic.dto.export;

import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * 车辆导出字段
 * @author 张娟
 */
@Data
public class VehicleExportDTO implements Serializable, ConverterDateUtil {
    private static final long serialVersionUID = 1L;
    private String id;

    @ExcelField(title = "车牌号")
    private String brand;

    private String orgId;

    @ExcelField(title = "所属企业")
    private String orgName;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "终端手机号")
    private String simCardNumber;

    private String groupId;

    @ExcelField(title = "分组")
    private String groupName;

    @ExcelField(title = "类别标准")
    private String standard;

    @ExcelField(title = "车辆类别")
    private String vehicleCategoryName;

    @ExcelField(title = "车辆类型")
    private String vehicleTypeName;

    @ExcelField(title = "道路运输证号")
    private String roadTransportNumber;

    @ExcelField(title = "行驶证号")
    private String licenseNo;

    @ExcelField(title = "车辆别名")
    private String alias;

    @ExcelField(title = "车主")
    private String vehicleOwner;

    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone;

    @ExcelField(title = "车辆等级")
    private String vehicleLevel;

    @ExcelField(title = "电话是否校验")
    private String phoneCheck;

    private String vehicleColor;

    @ExcelField(title = "车辆颜色")
    private String vehicleColorStr;

    @ApiParam(value = "车牌颜色（0蓝、1黄、2白、3黑）")
    private Integer plateColor;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    @ExcelField(title = "燃料类型")
    private String fuelTypeName;
    @ExcelField(title = "区域属性")
    private String areaAttribute;

    @ExcelField(title = "省、直辖市")
    private String province;

    @ExcelField(title = "市、区")
    private String city;

    @ExcelField(title = "车辆状态")
    private String isStartStr;

    @ExcelField(title = "运营类别")
    private String vehiclePurposeName;

    @ExcelField(title = "所属行业")
    private String tradeName;

    @ExcelField(title = "核定载人数")
    private Short numberLoad;

    @ExcelField(title = "核定载质量")
    private String loadingQuality;

    @ExcelField(title = "车辆技术等级有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleTechnologyValidityStr;

    @ExcelField(title = "是否维修")
    private String stateRepairStr;

    @ExcelField(title = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String createDataTimeStr;

    @ExcelField(title = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String updateDataTimeStr;

    @ExcelField(title = "保养里程数(km)")
    private Integer maintainMileage;

    @ExcelField(title = "保养有效期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String maintainValidityStr;

    @ExcelField(title = "车台安装日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehiclePlatformInstallDateStr;

    @ExcelField(title = "备注")
    private String remark;

}

