package com.zw.platform.domain.realTimeVideo;

import java.util.Date;

import lombok.Data;


/**
 * 资源列表文件上传实体
 */
@Data
public class FileUploadForm {
    private String vehicleId; // 监控对象Id

    private String FTPServerIp; // FTP服务器IP地址

    private int FTPort; // FTP服务器端口号

    private String FTPUserName; // FTP服务器用户名

    private String FTPassword; // FTP服务器用户名对应的密码

    private String fileUploadPath; // 文件上传路径

    private int executeOn; // 执行条件

    private int channelNumber; // 通道号

    private String startTime; // 开始时间

    private String endTime; // 结束时间

    private int resourceType; // 资源类型(0:音视频 1:音频 2:视频 3:视频或者音视频 4:图片)

    private int streamType; // 码流类型(0:所有码流 1：主码流 2：子码流)

    private int storageAddress; // 存储位置(0:主存储器或灾备存储器 1：主存储器 2：灾备存储器)

    private long alarmSign; // 报警标识
    
    private String filesize;
    
    private Date createDataTime;
    
    private String tempUrl; // 临时文件夹路径
}
