package com.zw.platform.domain.netty;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2019/6/20 11:07
 */
@Data
public class ConfigImportSendInfo implements Serializable {

    private static final long serialVersionUID = -4861094348176805866L;

    private String oldDeviceType; //协议类型

    private String oldDeviceId; //原绑定设备id

    private String oldIdentification; //原绑定标识
}
