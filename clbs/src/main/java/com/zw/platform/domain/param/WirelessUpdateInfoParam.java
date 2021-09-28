package com.zw.platform.domain.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/27 9:33
 */
@Data
public class WirelessUpdateInfoParam implements Serializable {
    private static final long serialVersionUID = -2288438369084271204L;
    /**
     * 车id
     */
    private String vid;
    /**
     * 连接控制
     */
    private Integer waccessControl;
    /**
     * 拨号点名称
     */
    private String wdailName;
    /**
     * 拨号用户名
     */
    private String wdailUserName;
    /**
     * 拨号密码
     */
    private String wdailPwd;
    /**
     * 地址
     */
    private String waddress;
    /**
     * TCP端口
     */
    private Integer wtcpPort;
    /**
     * UDP端口
     */
    private Integer wudpPort;
    /**
     * 制造商ID
     */
    private String manufactorId;
    /**
     * 硬件版本
     */
    private String hardwareVersion;
    /**
     * 固件版本
     */
    private String firmwareVersion;
    /**
     * URL地址
     */
    private String url;
    /**
     * 连接到指定服务器时限
     */
    private Integer wtimeLimit;
}
