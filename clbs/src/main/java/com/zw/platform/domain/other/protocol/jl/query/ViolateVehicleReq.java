package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * "单个和统一设置"违规车辆上传
 * @author create by zhouzongbo on 2020/6/12.
 */
@Data
public class ViolateVehicleReq {
    /**
     * 监控对象ID
     */
    @NotEmpty
    private Set<String> monitorIds;

    /**
     * 违规时间  datetime
     */
    @NotNull(message = "违规时间不能为空")
    private Date violateTime;

    /**
     * 违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行;
     */
    @NotNull
    @Range(min = 1, max = 8)
    private Integer type;
}
