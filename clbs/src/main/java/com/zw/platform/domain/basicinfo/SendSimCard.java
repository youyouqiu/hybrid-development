package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;


@Data
public class SendSimCard implements Serializable {

    private String simId; // SIM卡id

    private String vehicleId; // 监控对象id

    private String parameterName; // 绑定id

    private Integer type; // 标识

    private String upTime; //  流量最后更新时间

    private String realId; // 真实SIM卡号-物联网卡平台sim卡号
}
