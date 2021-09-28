package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 信息配置-货运导入
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigTransportImportDTO extends ImportErrorData {

    /**
     * 货运平台字段名是：终端ID
     */
    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber;

    /**
     * 货运平台字段名是：终端⼚商名称
     */
    @ExcelField(title = "制造商")
    private String manuFacturer;

    @ExcelField(title = "终端型号")
    private String deviceName;

    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simCardNumber;

    @ExcelField(title = "道路运输证号")
    private String roadTransportNumber;

    /**
     * 货运平台字段名： 所属省
     */
    @ExcelField(title = "省、直辖市", required = true)
    private String province;

    /**
     * 货运平台字段名：所属市
     */
    @ExcelField(title = "市、区", required = true)
    private String city;

    /**
     * 货运平台字段名:所属县
     */
    @ExcelField(title = "县")
    private String county;

    /**
     * 货运平台字段名:车主/业户
     */
    @ExcelField(title = "所属企业", required = true)
    private String orgName;

    /**
     * 货运平台字段名:联系人
     */
    @ExcelField(title = "车主")
    private String vehicleOwner;

    /**
     * 货运平台字段名:联系人手机
     */
    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone;

    /**
     * 货运平台字段名:手机验证状态：未校验/已校验
     */
    @ExcelField(title = "电话是否校验")
    private String phoneCheckStr;

    /**
     * 首次上线时间,格式:2017-12-01 14:17:07
     */
    @ExcelField(title = "首次上线时间")
    private String firstOnlineTimeStr;

    /**
     * 货运平台字段名:车辆识别代码/车架号 样例：LGAX2A130H1021487
     */
    @ExcelField(title = "车架号", required = true)
    private String chassisNumber;

    @ExcelField(title = "车牌号", required = true, repeatable = false)
    private String brand;

    @ExcelField(title = "车牌颜色", required = true)
    private String plateColorStr;

    @ExcelField(title = "车辆类型", required = true)
    private String vehicleTypeName;

    @ExcelField(title = "车辆品牌")
    private String vehicleBrand;

    @ExcelField(title = "车辆型号")
    private String vehicleModel;

    @ExcelField(title = "总质量(kg)")
    private String totalQuality;

    @ExcelField(title = "核定载质量(kg)")
    private String loadingQuality;

    @ExcelField(title = "准牵引总质量(kg)")
    private String tractionTotalMass;

    @ExcelField(title = "外廓尺寸-长(mm)")
    private Integer profileSizeLong;

    @ExcelField(title = "外廓尺寸-宽(mm)")
    private Integer profileSizeWide;

    @ExcelField(title = "外廓尺寸-高(mm)")
    private Integer profileSizeHigh;

    @ExcelField(title = "货厢内部尺寸-长(mm)")
    private Integer internalSizeLong;

    @ExcelField(title = "货厢内部尺寸-宽(mm)")
    private Integer internalSizeWide;

    @ExcelField(title = "货厢内部尺寸-高(mm)")
    private Integer internalSizeHigh;

    @ExcelField(title = "轴数")
    private Integer shaftNumber;

    @ExcelField(title = "轮胎数")
    private Integer tiresNumber;

    @ExcelField(title = "轮胎规格")
    private String tireSize;

    @ExcelField(title = "车辆出厂日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String vehicleProductionDateStr;

    @ExcelField(title = "经营范围")
    private String scopeBusiness;

    @ExcelField(title = "车辆颜色")
    private String vehicleColorStr;

    /**
     * 货运平台字段名:道路运输经营许可证号
     */
    @ExcelField(title = "经营许可证号")
    private String vehicleOperationNumber;

    /**
     * 辆购置方式(0:分期付款;1:一次性付清)
     */
    @ExcelField(title = "车辆购置方式")
    private String purchaseWayStr;

    @ExcelField(title = "车辆保险到期时间")
    @Deprecated
    private String vehicleInsuranceEndDateStr;

    @ExcelField(title = "检验有效期至")
    private String validEndDateStr;

    @ExcelField(title = "执照上传数")
    private Integer licenseNumbers;

    /**
     * 货运平台字段名:服务合同到期时间
     */
    @ExcelField(title = "到期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String expireTimeStr;

    @ExcelField(title = "行驶证发证日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String licenseIssuanceDateStr;

    @ExcelField(title = "发动机号")
    private String engineNumber;

    /**
     * 保留字段
     */
    @ExcelField(title = "发动机型号")
    @Deprecated
    private String engineType;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

    private String orgId;
}
