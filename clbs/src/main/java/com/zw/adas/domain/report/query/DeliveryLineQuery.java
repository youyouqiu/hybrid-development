package com.zw.adas.domain.report.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 线路下发查询条件
 *
 * @author lijie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeliveryLineQuery extends BaseQueryBean {

    @NotEmpty(message = "车辆id不能为空！")
    private Set<String> monitorIds;

    @NotNull(message = "【开始时间】不能为空！")
    private String startTime;

    @NotNull(message = "【结束时间】不能为空！")
    private String endTime;

}
