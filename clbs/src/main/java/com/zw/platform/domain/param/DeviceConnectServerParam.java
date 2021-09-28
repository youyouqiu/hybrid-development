package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * Created by FanLu on 2017/4/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceConnectServerParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vid;

    private Integer accessControl;//连接控制

    private String authCode; // 鉴权码

    private String dailName;//拨号点名称

    private String dailUserName;//拨号用户名

    private String dailPwd;//拨号密码

    private String address;//地址

    private Integer tcpPort;//TCP端口

    private Integer udpPort;//UDP端口

    private Integer timeLimit;//连接到指定服务器时限

    public String toString() {
        return accessControl + ";" + authCode + ";" + dailName + ";" + dailUserName + ";" + dailPwd + ";"
            + address + ";" + tcpPort + ";" + udpPort + ";" + timeLimit + ";";
    }
}
