package com.zw.platform.domain.multimedia;

import com.zw.protocol.msg.t808.body.T808GpsInfo;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/1.
 */
@Data
public class MultimediaData {
    private Long id;// 多媒体ID
    private Integer type;// 多媒体类型
    private Integer formatCode;// 多媒体格式编码
    private Integer eventCode;// 事件项编码
    private Integer wayId;// 通道ID
    private T808GpsInfo gpsInfo;// 位置信息
    private byte[] data;// 多媒体数据包

    private String mediaName;
    private String mediaUrl;
    private String vid;
    private Integer mediaId;
    private String mediaUrlNew;
    private String monitorName;
}
