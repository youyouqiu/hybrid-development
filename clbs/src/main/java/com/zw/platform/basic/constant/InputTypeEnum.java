package com.zw.platform.basic.constant;

import lombok.Getter;

/**
 * 信息配置录入方式
 * @author zhangjuan
 */
public enum InputTypeEnum {
    /**
     * 快速录入
     */
    FAST_INPUT(1, "快速录入"),

    /**
     * 极速录入
     */
    TOP_SPEED_INPUT(2, "极速录入"),

    /**
     * 流程录入
     */
    PROCESS_INPUT(3, "流程录入"),

    SCAN_INPUT(4, "扫码录入");

    InputTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Getter
    private Integer code;

    @Getter
    private String name;
}
