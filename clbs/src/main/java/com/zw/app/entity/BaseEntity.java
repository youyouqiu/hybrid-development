package com.zw.app.entity;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;


/***
 @Author gfw
 @Date 2018/12/7 17:19
 @Description APP基本参数
 @version 1.0
 **/
@Data
public class BaseEntity {
    /**
     * APP当前版本
     */
    @NotNull(message = "APP当前版本不能为空")
    @DecimalMin(value = "10000",message = "APP版本错误")
    private Integer version;

    /**
     * APP平台 android/ios
     */
    @NotNull(message = "平台类型不能为空")
    private String platform;

    protected String exceptionInfo = "";

    public Object[] getArgs() {
        return null;
    }

    public Class<?>[] getArgClasses() {
        return null;
    }

    public String getExceptionInfo() {
        return "";
    }

}
