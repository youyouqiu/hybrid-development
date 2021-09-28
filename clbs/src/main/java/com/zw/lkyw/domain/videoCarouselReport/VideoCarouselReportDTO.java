package com.zw.lkyw.domain.videoCarouselReport;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VideoCarouselReportDTO<T> implements Serializable {

    /**
     * 错误编码
     */
    private String code;
    /**
     * 错误消息
     */
    private String message;
    
    private List<T> data;
}
