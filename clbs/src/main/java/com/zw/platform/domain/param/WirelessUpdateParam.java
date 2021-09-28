package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class WirelessUpdateParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int controlType;//控制类型 0升级 1恢复设置
    private String restoreType;//升级/恢复类型
    private String vid;
    private Integer wAccessControl;//连接控制
    private String wDailName;//拨号点名称
    private String wDailUserName;//拨号用户名
    private String wDailPwd;//拨号密码
    private String wAddress;//地址
    private Integer wTcpPort;//TCP端口
    private Integer wUdpPort;//UDP端口
    private String manufactorId;//制造商ID
    private String hardwareVersion;//硬件版本
    private String firmwareVersion;//固件版本
    private String url;//URL地址
    private Integer wTimeLimit;//连接到指定服务器时限

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (!"".equals(url)) {
            str.append(url);
        }
        str.append(";");
        if (!"".equals(wDailName)) {
            str.append(wDailName);
        }
        str.append(";");
        if (!"".equals(wDailUserName)) {
            str.append(wDailUserName);
        }
        str.append(";");
        if (!"".equals(wDailPwd)) {
            str.append(wDailPwd);
        }
        str.append(";");
        if (!"".equals(wAddress)) {
            str.append(wAddress);
        }
        str.append(";");
        if (wTcpPort != null) {
            str.append(wTcpPort);
        }
        str.append(";");
        if (wUdpPort != null) {
            str.append(wUdpPort);
        }
        str.append(";");
        if (!"".equals(manufactorId)) {
            str.append(manufactorId);
        }
        str.append(";");
        if (!"".equals(hardwareVersion)) {
            str.append(hardwareVersion);
        }
        str.append(";");
        if (!"".equals(firmwareVersion)) {
            str.append(firmwareVersion);
        }
        str.append(";");
        if (wTimeLimit != null) {
            str.append(wTimeLimit);
        }
        str.append(";");
        return str.toString();
    }

    public String sendParamString() {
        StringBuilder str = new StringBuilder();
        str.append("ftp://").append(wDailUserName).append(":").append(wDailPwd);
        str.append("@").append(wAddress).append(":").append(wTcpPort);
        if (!firmwareVersion.startsWith("/")) {
            str.append("/");
        }
        str.append(firmwareVersion);
        //		str.append(";;;;;;;;;;;");
        str.append(";;").append(wDailUserName).append(";").append(wDailPwd).append(";").append(wAddress).append(";")
            .append(wTcpPort).append(";").append(";");
        str.append(!"".equals(manufactorId) ? manufactorId + ";" : ";");
        str.append(";").append(firmwareVersion).append(";");
        str.append(wTimeLimit != null ? wTimeLimit + ";" : ";");
        return str.toString();
    }

}
