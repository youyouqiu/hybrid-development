package com.zw.talkback.domain.dispatch;

import java.util.Objects;

/**
 * 调度平台错误码
 */
public enum DispatchErrorMessageEnum {
    /**
     * 调度平台 请求相关常用错误码
     */
    CODE_0(0, "操作成功"), CODE_1(1, "操作失败"), CODE1(-1, "登陆失败，用户名或密码错误"), CODE2(-2, "您还未登陆"), CODE_1000(1000,
        "无效pid"), CODE_1001(1001, "被叫忙"), CODE_1002(1002, "被叫不在线"), CODE_1003(1003, "组内无其它在线用户"), CODE_1004(1004,
        "呼叫超时"), CODE_1005(1005, "请先停止群组监听"), CODE_1006(1006, "请先停止个人监听"), CODE_1007(1007, "请先停止环境监听"), CODE_1008(1008,
        "强插失败"), CODE_1009(1009, "请填写正确群组号码"), CODE_1010(1010, "请填写正确用户号码"), CODE_1011(1011, "其它错误");

    private Integer code;

    private String message;

    DispatchErrorMessageEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(Integer code) {
        if (code == null) {
            return null;
        }
        for (DispatchErrorMessageEnum e : DispatchErrorMessageEnum.values()) {
            if (Objects.equals(code, e.getCode())) {
                return e.message;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}