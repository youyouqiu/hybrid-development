package com.zw.platform.domain.oilsubsidy.line;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 809公共字段
 * @date 2020/10/1310:36
 */
@Data
public class CommandOf809CommonDTO {
    /**
     * ip
     */
    private String ip;

    /**
     * 接入码
     */
    private Integer centerId;

    /**
     * 车辆颜色
     */
    private Integer plateColor;

    /**
     * 车牌
     */
    private String brand;

    /**
     * 企业编号
     */
    private String orgCode;

    /**
     * 平台ID
     */
    private String forwardingPlatformId;

}
