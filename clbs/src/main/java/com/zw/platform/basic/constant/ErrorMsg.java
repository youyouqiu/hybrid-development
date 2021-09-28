package com.zw.platform.basic.constant;

/**
 * 错误信息用于BusinessException的异常信息
 */
public enum ErrorMsg {
    DEVICE_EXIST("终端号已存在"), SIM_CARD_EXIST("终端手机号已存在"),
    ;

    ErrorMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    private String msg;

}
