package com.zw.adas.domain.equipmentrepair;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 批量确认车辆信息上报
 * @author zhangjuan
 */
@Data
public class BatchConfirmRepairDTO {
    /**
     * 主键列表,多个用逗号隔开(企业id_报修时间_故障类型_车辆id)
     */
    @NotNull(message = "【主键】不能为空")
    private String primaryKeys;

    /**
     * 确认状态 0:确认 1：误报
     */
    @NotNull(message = "【确认状态】不能为空")
    private Integer confirmStatus;

}
