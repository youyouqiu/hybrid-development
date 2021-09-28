package com.zw.platform.domain.other.protocol.jl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 违规车辆上传记录
 * @author create by zhouzongbo on 2020/6/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolateVehicleDO {

    /**
     * id
     */
    private String id;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 违规时间
     */
    private Date violateTime;
    /**
     * 违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行
     */
    private Integer type;
    /**
     * 车牌颜色：1蓝，2黄，3黑，4白，9其他，90:农蓝， 91农黄，92农绿，93黄绿色，94渐变绿色
     */
    private Integer plateColor;
    /**
     * 所属企业
     */
    private String groupName;
    /**
     * 上报时间：时间戳(年月日时分秒)
     */
    private Date uploadTime;
    /**
     * 上传状态：0: 失败; 1: 成功
     */
    private Integer uploadState;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 错误信息
     */
    private String errorMsg;
}
