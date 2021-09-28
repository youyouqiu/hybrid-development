package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;

import java.util.Set;

/**
 * 违规车辆导出信息
 * @author create by zhouzongbo on 2020/6/12.
 */
@Data
public class ViolateVehicleExportReq {

    private Set<String> monitorIds;

    /**
     * 违规日期
     */
    private String violateStartDate;
    private String violateEndDate;
    /**
     * 上传状态：0: 失败; 1: 成功
     */
    private Integer uploadState;

    /**
     * 违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行
     */
    private Integer type;
    private String simpleQueryParam;
}
