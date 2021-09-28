package com.zw.adas.domain.report.query;

import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 单条车辆与终端运行状态查询
 *
 * @author zhangjuan
 */
@Data
public class SingleVehicleStateQuery {
    /**
     * 车辆所属组织ID
     */
    @NotEmpty(message = "【组织】不能为空！")
    private String orgId;

    /**
     * 车辆ID
     */
    @NotEmpty(message = "【车辆ID】不能为空！")
    private String vehicleId;

    /**
     * 维修上报时间或监控对象定位时间
     */
    @NotNull(message = "【时间】不能为空！")
    private Date date;

    /**
     * 数据来源
     * 0:0x0200
     * 1:0x0710
     */
    private int dataSource = 1;
}
