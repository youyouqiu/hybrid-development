package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * 功能描述:中位标准相关的多媒体项
 * @author zhengjc
 * @date 2020/5/8
 * @time 16:08
 */
@Data
public class WarnMsgFileInfoZw implements Serializable {

    /**
     * 文件名称长度
     */
    private Integer fileNameLength;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型 0x00：图片 0x01：音频 0x02：视频 0x03：记录文件 0x04：其它
     */
    private Integer fileType;

    /**
     * 文件大小
     */
    private Long fileSize;


    /**
     * 文件 URL 的长度
     */
    private Integer fileUrlLength;

    /**
     * 当前报警附件的完整 URL 地址
     */
    private String fileUrl;
}
