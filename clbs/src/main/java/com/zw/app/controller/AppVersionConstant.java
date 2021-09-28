package com.zw.app.controller;

/***
 @Author gfw
 @Date 2018/12/14 9:11
 @Description app版本常量
 @version 1.0
 **/
public enum AppVersionConstant {

    APP_VERSION_ONE(10200), // TODO 后期版本的叠加
    APP_VERSION_TWO(10201),

    APP_VERSION_THREE(10202),

    // APP2.0.0新增
    APP_VERSION_FOUR(20000),

    APP_VERSION_FIVE(20100),

    APP_VERSION_SIX(20102),
    APP_VERSION_SEVEN(20103),

    APP_VERSION_EIGHT(20105),

    APP_VERSION_NINE(20202),

    APP_VERSION_232(20302);

    //    APP_VERSION_THREE(10400);
    private Integer value;

    AppVersionConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
