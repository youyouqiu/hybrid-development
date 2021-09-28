package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.io.Serializable;

/**
 * 联动策略下发短信实体
 * create by denghuabing 2018.9.4
 */
@Data
public class MsgParamDTO implements Serializable {

    private static final long serialVersionUID = -7393783525583450938L;
    private String id;

    /**
     * 终端TTS语音
     */
    public static final String MARK_DEVICE_VOICE = "3";

    /**
     * 文本下发 - 通知
     */
    public static final int TEXT_TYPE_NOTIFICATION = 1;

    /**
     * 通知
     */
    public static final int MESSAGE_TYPE_ONE_NOTIFICATION = 1;

    /**
     * 中心导航信息
     */
    public static final int MESSAGE_TYPE_TWO_NOTIFICATION = 0;

    /**
     * 短信内容
     */
    private String msgContent;

    /**
     * 1:紧急；2:终端显示器显示；3:终端TTS语音；4:广告屏显示
     */
    private String marks;

    /**
     * 文本下发: 1:通知;2:服务
     */
    private Integer textType;

    /**
     * 1:通知;2:服务;3:紧急;
     */
    private Integer messageTypeOne;

    /**
     * 0: 中心导航信息; 1: CAN故障码信息
     */
    private Integer messageTypeTwo;

    /**
     * 通讯类型: 0:交通部JT/T808-2011(扩展);1:交通部JT/T808-2013;2:移为GV320;3:天禾;5:北斗天地协议;8:博实结;9:ASO;10:F3超长待机;11:808-2019
     */
    private Integer deviceType;
}
