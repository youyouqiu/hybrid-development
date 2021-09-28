package com.zw.platform.domain.other.protocol.jl.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 10:53
 */
@Data
public class JiLinVehicleSetResp implements Serializable {
    private static final long serialVersionUID = 5115407527878072012L;
    /**
     * 车辆id
     */
    private String id;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 上传类型
     */
    private String unloadType;
    /**
     * 车牌颜色
     */
    private String plateColorStr;
    /**
     * 分组名称 逗号分隔
     */
    private String assignmentNames;
    /**
     * 所属企业名称
     */
    private String groupName;
}
