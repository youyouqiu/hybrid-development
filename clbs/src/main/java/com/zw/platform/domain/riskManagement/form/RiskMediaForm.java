package com.zw.platform.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * 风险媒体实体
 *
 * @author  Tdz
 * @create 2017-08-31 9:30
 **/
@Data
public class RiskMediaForm extends BaseFormBean {
    /**
     * 多媒体类型（0：图像；1：音频；2：视频）
     */
    private Short type;

    /**
     * 多媒体文件名称
     */
    private String mediaName;

    /**
     * 多媒体文件存储路径
     */
    private String mediaUrl;

    /**
     * 文件格式（0：JPEG 1：TIF 2：MP3 3：WAV 4：WMV 5AVI）
     */
    private Short formatCode;

    /**
     * 事件编码（0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发）
     */
    private Short eventCode;

    /**
     * 通道ID
     */
    private Short wayId;

    /**
     * 关联车id
     */
    private String vehicleId;

    /**
     * 多媒体id
     */
    private Integer mediaId;

    /**
     * 风险id
     */
    private String riskId;

    /**
     * 风险事件id
     */
    private String riskEventId;

    /**
     * 多媒体对应的来源（0：风险事件；1：风险）
     */
    private Short source;

}
