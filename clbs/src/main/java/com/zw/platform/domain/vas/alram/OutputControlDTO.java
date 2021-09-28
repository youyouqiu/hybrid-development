package com.zw.platform.domain.vas.alram;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;

/***
 * 输出控制
 @author lijie
 @date 2020/5/9 11:42
 @version 1.0
 **/
@Data
public class OutputControlDTO implements Serializable {

    private static final long serialVersionUID = -8718005698413821792L;
    private String id;

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
     * 模拟量输出比例
     */
    private Float analogOutputRatio;

    /**
     * 是否是联动策略
     */
    private Integer autoFlag;
}
