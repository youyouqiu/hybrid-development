package com.zw.platform.domain.basicinfo;

import lombok.Data;
import java.io.Serializable;

/**
 * @author tianzhangxu
 * @date 2019.7.22
 * “登录页个性化配置”字段对应实体类
 */
@Data
public class LoginPersonalized implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 输入框位置 1：居左  2：居中 3：居右
     */
    private Integer inputPosition;

    /**
     * logo位置 1：左上  2：右上  3：跟随输入框
     */
    private Integer logoPosition;

    /**
     * 备案号颜色 默认值：rgb(45,45,45)
     */
    private String recordNumberColor;

    /**
     * 备案号阴影（描边） 默认值：rgb(255,255,255)
     */
    private String recordNumberShadow;

    /**
     * 按钮颜色  默认值：rgb(85,101,123)
     */
    private String buttonColor;

    /**
     * 预留颜色字段4
     */
    private String fourthColor;

    /**
     * 预留颜色字段5
     */
    private String fifthColor;
}
