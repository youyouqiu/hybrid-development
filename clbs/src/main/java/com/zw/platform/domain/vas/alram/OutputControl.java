package com.zw.platform.domain.vas.alram;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.ws.entity.OutputControlSettingDO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;

/***
 @Author lijie
 @Date 2020/5/9 11:42
 @Description 输出控制实体
 @version 1.0
 **/
@Data
public class OutputControl extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = -6120585601464368770L;
    private String protocolType;

    @NotEmpty(message = "车辆id不能为空")
    private String vehicleId;

    @NotEmpty(message = "外设id不能为空")
    private Integer peripheralId;

    /**
     * 控制时长
     */
    private Integer controlTime;

    @NotEmpty(message = "输出口不能为空")
    private Integer outletSet;

    @NotEmpty(message = "控制类型不能为空")
    private Integer controlSubtype;

    /**
     * 控制状态 0:断开; 1:闭合;
     */
    private Integer controlStatus;

    /**
     * 模拟量输出比例
     */
    private Float analogOutputRatio;

    /**
     * 是否是联动策略
     */
    private Integer autoFlag;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutputControl that = (OutputControl) o;
        return Objects.equals(vehicleId, that.vehicleId) && Objects.equals(peripheralId, that.peripheralId) && Objects
            .equals(outletSet, that.outletSet) && Objects.equals(controlSubtype, that.controlSubtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, peripheralId, outletSet, controlSubtype);
    }

    public static OutputControl of(OutputControlDTO req, String vehicleId, String deviceType) {
        OutputControl outputControl = new OutputControl();
        outputControl.setProtocolType(deviceType);
        outputControl.setVehicleId(vehicleId);
        outputControl.setPeripheralId(req.getPeripheralId());
        outputControl.setControlTime(req.getControlTime());
        outputControl.setOutletSet(req.getOutletSet());
        outputControl.setControlSubtype(req.getControlSubtype());
        outputControl.setAnalogOutputRatio(req.getAnalogOutputRatio());
        outputControl.setAutoFlag(req.getAutoFlag());
        outputControl.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return outputControl;
    }

    public static OutputControl of(OutputControlSettingDO settingDO) {
        OutputControl outputControl = new OutputControl();
        outputControl.setVehicleId(settingDO.getVehicleId());
        outputControl.setPeripheralId(settingDO.getPeripheralId());
        outputControl.setControlTime(settingDO.getControlTime());
        outputControl.setOutletSet(settingDO.getOutletSet());
        outputControl.setControlSubtype(settingDO.getControlSubtype());
        outputControl.setAnalogOutputRatio(settingDO.getAnalogOutputRatio());
        outputControl.setControlStatus(settingDO.getControlStatus());
        return outputControl;
    }
}
