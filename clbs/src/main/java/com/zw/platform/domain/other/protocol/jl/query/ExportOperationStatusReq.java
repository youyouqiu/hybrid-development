package com.zw.platform.domain.other.protocol.jl.query;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/16 15:46
 */
@Data
public class ExportOperationStatusReq {

    /**
     * 车牌号码
     */
    private String vehicleNo;
    /**
     * 车牌颜色(蓝色,黄色,黑色,白色,农黄,农蓝,农绿,其他)
     */
    private String plateColorStr;
    /**
     * 营运状态(营运,停运,挂失,迁出(过户),迁出(转籍),报废,歇业,注销,其他)
     */
    private String runStatusStr;
    /**
     * 返回时间
     */
    private String returnTimeStr;
}
