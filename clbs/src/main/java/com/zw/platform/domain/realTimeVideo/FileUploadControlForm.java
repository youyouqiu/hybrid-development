package com.zw.platform.domain.realTimeVideo;

import lombok.Data;


/**
 * 文件上传控制指令实体
 */
@Data
public class FileUploadControlForm {

    private String vehicleId; // 监控对象id

    private int channelNumber; // 通道号

    private int msgSn; // 应答流水号，对应文件上传消息9206的流水号

    private int control; // 上传控制 0暂停 1继续 2取消
}
