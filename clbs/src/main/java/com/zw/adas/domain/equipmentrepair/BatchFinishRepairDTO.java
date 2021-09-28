package com.zw.adas.domain.equipmentrepair;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 批量完成车辆信息上报
 *
 * @author zhangjuan
 */
@Data
public class BatchFinishRepairDTO {
    /**
     * 主键列表,多个用逗号隔开(企业id_报修时间_故障类型_车辆id)
     */
    @NotNull(message = "【主键】不能为空")
    private String primaryKeys;

    /**
     * 维修日期 yyyy-MM-dd
     */
    @NotNull(message = "【维修日期】 不能为空")
    private String repairDate;

}
