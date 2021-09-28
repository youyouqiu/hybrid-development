package com.zw.platform.basic.dto.export;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信息配置导入实体
 */
@Data
@NoArgsConstructor
public class ConfigExportDTO {
    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "监控类型")
    private String monitorType;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    @ExcelField(title = "所属企业")
    private String orgName;

    @ExcelField(title = "分组")
    private String groupName;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "通讯类型")
    private String deviceType;

    @ExcelField(title = "终端厂商")
    private String terminalManufacturer;

    @ExcelField(title = "终端型号")
    private String terminalType;

    @ExcelField(title = "功能类型")
    private String functionalType;

    @ExcelField(title = "终端手机号")
    private String simCardNumber;

    @ExcelField(title = "真实SIM卡号")
    private String realSimCardNumber;

    @ExcelField(title = "计费日期")
    private String billingDate;

    @ExcelField(title = "到期日期")
    private String expireDate;

    @ExcelField(title = "从业人员")
    private String professionalNames;

    @ExcelField(title = "创建时间")
    private String createDataTime;

    @ExcelField(title = "修改时间")
    private String updateDataTime;

    public ConfigExportDTO(BindDTO bindDTO) {
        this.monitorName = bindDTO.getName();
        this.monitorType = MonitorTypeEnum.getNameByType(bindDTO.getMonitorType());
        this.plateColorStr = PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor());
        this.orgName = bindDTO.getOrgName();
        this.groupName = bindDTO.getGroupName();
        this.deviceNumber = bindDTO.getDeviceNumber();
        this.deviceType = ProtocolEnum.getDeviceNameByDeviceType(bindDTO.getDeviceType());
        this.terminalManufacturer = bindDTO.getTerminalManufacturer();
        this.terminalType = bindDTO.getTerminalType();
        this.functionalType = ConstantUtil.getDeviceFunctionType(bindDTO.getFunctionalType());
        this.simCardNumber = bindDTO.getSimCardNumber();
        this.realSimCardNumber = bindDTO.getRealSimCardNumber();
        this.billingDate = bindDTO.getBillingDate();
        this.expireDate = bindDTO.getExpireDate();
        this.professionalNames = bindDTO.getProfessionalNames();
        this.createDataTime = bindDTO.getBindDate();
        this.updateDataTime = bindDTO.getUpdateBindDate();
    }
}
