package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarnMsgFileInfo implements Serializable {

    /**
     * 文件名称长度
     */
    private Integer fileNameLength;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型 0x00：图片 0x01：音频 0x02：视频 0x03：记录文件 0x04：其它 （沪标 0x00：图片 0x01：视频）
     */
    private Integer fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件格式 Ox0l:jpg Ox02:gif Ox03:png 0x04:wav 0x05:mp3 0x06:mp4 0x07:3gp 0x08:flv
     */
    private Integer fileFormat;
    /**
     * 文件 MD5 值，32 位大写
     */
    private String md5;

    /**
     * 文件 URL 的长度
     */
    private Integer fileUrlLengh;

    /**
     * 当前报警附件的完整 URL 地址
     */
    private String fileUrl;
}
