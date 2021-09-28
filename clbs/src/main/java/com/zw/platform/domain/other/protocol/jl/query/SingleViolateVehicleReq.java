package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * "单个和统一设置"违规车辆上传
 * @author create by zhouzongbo on 2020/6/12.
 */
@Data
public class SingleViolateVehicleReq {
    /**
     * 监控对象ID
     */
    @NotNull
    private String monitorId;

    /**
     * 违规时间  datetime
     */
    @NotNull
    private Date violateTime;

    /**
     * 违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行;
     */
    @NotNull
    @Range(min = 1, max = 8)
    private Integer type;
}
