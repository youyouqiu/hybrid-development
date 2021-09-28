package com.zw.platform.util.common;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * 后台校验的处理类
 */
public class SpringBindingResultWrapper {
    /**
     * 后台错误处理的方法
     */
    public static String warpErrors(final BindingResult errors) {
        // 再次确认错误
        if (!errors.hasErrors()) {
            return "";
        }
        StringBuilder errorMsg = new StringBuilder();
        // 获取错误集合
        List<FieldError> fieldErrors = errors.getFieldErrors();
        // 获取有多少个错误
        int length = fieldErrors.size();
        // 循环错误，倒着取值
        for (int i = length - 1; i >= 0; i--) {
            // 获取一个错误
            FieldError error = fieldErrors.get(i);
            // 判断是否有汉字，有才拼接，避免抛出的异常
            if (gbk(error.getDefaultMessage() + "")) {
                if (i != 0) {
                    // 不是左后一个错误，需要拼接换行符
                    errorMsg.append(error.getDefaultMessage()).append("<br />&nbsp;&nbsp;&nbsp;");
                } else {
                    // 如果是最后一个错误就不用拼接换行符
                    errorMsg.append(error.getDefaultMessage());
                }
            }
        }
        return errorMsg.toString();
    }

    /**
     * 判断字符串是否有汉字
     * unicode中文范围
     * 汉字编码范围:\u4e00-\u9FA5
     */
    private static boolean gbk(final String data) {
        char[] chars = data.toCharArray();
        for (char c : chars) {
            //有一个中文字符就返回
            if (c >= 0x4E00 && c <= 0x9FA5) {
                return true;
            }
        }
        return false;
    }
}
