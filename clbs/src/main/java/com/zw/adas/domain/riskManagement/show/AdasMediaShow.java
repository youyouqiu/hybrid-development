package com.zw.adas.domain.riskManagement.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/2/19 13:39
 @Description 风险处置预览展现实体
 @version 1.0
 **/
@Data
public class AdasMediaShow {
    /**
     * 多媒体名称
     */
    private String mediaName;

    /**
     * 多媒体url
     */
    private String mediaUrl;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 风险事件类型
     */
    private Integer eventId;

    /**
     * 风险事件类型
     */
    private String riskEventType;


    public AdasMediaShow(String mediaName, String mediaUrl) {
        this.mediaName = mediaName;
        this.mediaUrl = mediaUrl;
    }

    public AdasMediaShow() {

    }
}
