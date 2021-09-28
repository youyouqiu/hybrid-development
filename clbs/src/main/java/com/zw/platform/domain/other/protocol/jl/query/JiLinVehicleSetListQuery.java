package com.zw.platform.domain.other.protocol.jl.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 10:35
 */
@Data
public class JiLinVehicleSetListQuery extends BaseQueryBean {
    private static final long serialVersionUID = 7961031251064625965L;
    /**
     * 车辆id 逗号分隔
     */
    @NotBlank
    private String vehicleIds;
}
