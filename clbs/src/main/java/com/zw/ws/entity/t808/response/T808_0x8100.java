package com.zw.ws.entity.t808.response;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/10/26.
 */
@Data
public class T808_0x8100 implements Serializable {

    private static final long serialVersionUID = 1L;

    /// <summary>
    /// 应答消息流水号
    /// </summary>
    private Integer msgSN;

    /// <summary>
    /// 应答结果，0：成功；1：车辆已被注册；2：数据库中无该车辆；3：终端已被注册；4：数据库中无该终端
    /// </summary>
    private Integer regRet;

    /**
     * 鉴权码
     */
    private String authCode;
}
