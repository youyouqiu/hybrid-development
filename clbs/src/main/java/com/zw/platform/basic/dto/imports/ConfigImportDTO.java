package com.zw.platform.basic.dto.imports;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import com.zw.ws.common.PublicVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 信息配置导入实体类
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigImportDTO extends ImportErrorData {
    @ExcelField(title = "监控对象", required = true, repeatable = false)
    private String monitorName;

    /**
     * 监控对象类型:人、车、物
     */
    @ExcelField(title = "监控对象类型", required = true)
    private String monitorType;

    @ExcelField(title = "车牌颜色(仅车辆时必填)")
    private String plateColorStr;

    @ExcelField(title = "所属企业", required = true)
    private String orgName;

    /**
     * 分组名称，多个之间用逗号隔开
     */
    @ExcelField(title = "分组")
    private String groupName;

    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber;

    @ExcelField(title = "通讯类型", required = true)
    private String deviceTypeName;

    @ExcelField(title = "终端厂商")
    private String terminalManufacturer;

    @ExcelField(title = "终端型号")
    private String terminalType;

    @ExcelField(title = "功能类型", required = true)
    private String functionalType;

    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simCardNumber;

    @ExcelField(title = "真实SIM卡号")
    private String realSimCardNumber;

    @ExcelField(title = "计费日期")
    private String billingDate;

    @ExcelField(title = "到期日期")
    private String expireDate;

    @ExcelField(title = "从业人员")
    private String professionalNames;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public BindDTO convertToBindDTO() {
        BindDTO bindDTO = new BindDTO();
        bindDTO.setName(this.monitorName);
        bindDTO.setMonitorType(MonitorTypeEnum.getTypeByName(this.monitorType));
        bindDTO.setMonitorTypeName(this.monitorType);
        bindDTO.setPlateColor(PlateColor.getCodeByName(this.plateColorStr));
        bindDTO.setPlateColorStr(this.plateColorStr);
        bindDTO.setOrgName(this.orgName);
        bindDTO.setGroupName(this.groupName);
        bindDTO.setDeviceNumber(this.deviceNumber);
        bindDTO.setDeviceType(ProtocolEnum.getDeviceTypeByDeviceName(this.deviceTypeName));
        bindDTO.setTerminalManufacturer(this.terminalManufacturer);
        bindDTO.setTerminalType(this.terminalType);
        bindDTO.setFunctionalType(PublicVariable.getFunctionTypeId(this.functionalType));
        bindDTO.setSimCardNumber(this.simCardNumber);
        bindDTO.setRealSimCardNumber(this.realSimCardNumber);
        bindDTO.setBillingDate(this.billingDate);
        bindDTO.setExpireDate(this.expireDate);
        bindDTO.setProfessionalNames(this.professionalNames);
        bindDTO.setErrorMsg(this.errorMsg);
        if (Objects.equals(MonitorTypeEnum.VEHICLE.getTypeName(), this.monitorType)) {
            bindDTO.setVehiclePassword("000000");
        }
        return bindDTO;

    }

}
