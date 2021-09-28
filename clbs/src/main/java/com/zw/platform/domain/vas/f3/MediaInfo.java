package com.zw.platform.domain.vas.f3;

import lombok.Data;


/**
 * 多媒体列表
 */
@Data
public class MediaInfo {
    private String simcardNumber; // 终端手机号

    private Integer alarmIdent; // 报警类型标识

    private String time; // 时间

    private Integer multiType; // 多媒体类型(0:图片 1:音频 2:视频 3:文本 4:其他)

    private Integer serialNumber; // 序号

    private Integer reserve; // 保留项
}
