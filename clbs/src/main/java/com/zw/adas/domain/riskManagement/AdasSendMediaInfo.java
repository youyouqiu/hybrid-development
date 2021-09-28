package com.zw.adas.domain.riskManagement;

import lombok.Data;

@Data
public class AdasSendMediaInfo {

    //文件服务器地址
    private String fileHost;

    //文件服务器TCP端口
    private Integer fileTcpPort;

    //文件服务器UDP端口
    private Integer fileUdpPort;

    //media信息
    private AlarmSign alarmSign;

    //报警编号（uuid）
    private String riskEventId;


}
