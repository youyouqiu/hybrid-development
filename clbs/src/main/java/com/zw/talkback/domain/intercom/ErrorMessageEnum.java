package com.zw.talkback.domain.intercom;

import java.util.Objects;

public enum ErrorMessageEnum {

    /**
     * 管理平台错误码
     */
    CODE_0(0, "操作成功"), CODE_1(1, "操作失败"), CODE1(-1, "登陆失败，用户名或密码错误"), CODE2(-2, "您还未登陆"), CODE_1000(1000,
        "无效pid"), CODE_1001(1001, "查询异常"), CODE_1002(1002, "登陆名已被使用"), CODE_1003(1003, "名称已经存在"), CODE_1004(
        1004, "已超过最大支持的二级客户数"), CODE_1005(1005, "已创建的二级客户数不能大于当前修改的二级客户数"), CODE_1006(1006, "已超过最大支持的群组数"), CODE_1007(
        1007, "已创建的群组数不能大于当前修改的群组数"), CODE_1008(1008, "已超过最大支持的II类账号"), CODE_1009(1009,
        "已创建的II类账号数不能大于当前修改的II类账号数"), CODE_1010(1010, "已超过最大支持的III类账号"), CODE_1011(1011,
        "已创建的III类账号数不能大于当前修改的III类账号数"), CODE_1012(1012, "只能创建一个I类账号"), CODE_1013(1013, "设备号码已被创建为用户"), CODE_1014(1014,
        "个呼号码已被使用"), CODE_1015(1015, "群组号码已被使用"), CODE_1016(1016, "只能创建一个全呼组"), CODE_1017(1017, "没有找到该代理商"), CODE_1018(
        1018, "没有找到该客户"), CODE_1019(1019, "没有找到该设备"), CODE_1020(1020, "没有找到该用户"), CODE_1021(1021, "没有找到该群组"), CODE_1022(
        1022, "日期无效"), CODE_1023(1023, "只能查询一个月内的数据"), CODE_1024(1024, "只能查询七天内的数据"), CODE_1025(1025,
        "只能查询3天内的数据"), CODE_1026(1026, "只能查询1天内的数据"), CODE_1027(1027, "其它错误");

    public static final int SUCCESS_CODE = 0;
    private Integer code;

    private String message;

    ErrorMessageEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(Integer code) {
        if (code == null) {
            return null;
        }
        for (ErrorMessageEnum e : ErrorMessageEnum.values()) {
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
