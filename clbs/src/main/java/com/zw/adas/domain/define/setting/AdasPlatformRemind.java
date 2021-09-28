package com.zw.adas.domain.define.setting;

import lombok.Data;


@Data
public class AdasPlatformRemind {
    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 报警提示音
     */
    private boolean isPromptTone;

    /**
     * 闪烁提示
     */
    private boolean isBlinkingPrompt;

    /**
     * 弹窗提醒
     */
    private boolean isPopupPrompt;

    /**
     * 0(长时间未处理),1(其他阈值达到弹窗)
     */
    private int popupType = 1;

    /**
     * 报警时间
     */
    private Long warmTime;

}
