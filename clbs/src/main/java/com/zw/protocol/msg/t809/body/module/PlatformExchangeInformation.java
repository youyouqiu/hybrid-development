package com.zw.protocol.msg.t809.body.module;

import lombok.Data;

import java.io.Serializable;

/**
 * 平台交换信息实体
 * @author hujun
 * @date 2018/11/30 15:37
 */
@Data
public class PlatformExchangeInformation implements Serializable {
    private Integer serverCommand;
    private String brand;
    private String startTime;
    private String endTime;
    private String ip;// 上级平台ip
    private Integer centerId;// 接入码
    private String platFormId;// 转发平台id
}
