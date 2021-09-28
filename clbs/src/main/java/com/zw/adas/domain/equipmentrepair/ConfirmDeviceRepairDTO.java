package com.zw.adas.domain.equipmentrepair;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设备维修上报确认
 * @author zhangjuan
 */
@Data
public class ConfirmDeviceRepairDTO {
    /**
     * 主键(企业id_报修时间_故障类型_车辆id)
     */
    @NotNull(message = "【主键】不能为空")
    private String primaryKey;

    /**
     * 确认状态 0:确认 1：误报
     */
    @NotNull(message = "【确认状态】不能为空")
    private Integer confirmStatus;

    /**
     * 备注信息
     */
    private String remark;


}
