package com.zw.app.domain.webMaster.personalized;

import lombok.Data;

import java.io.Serializable;

/**
 * app后台登录信息配置
 * @author lijie
 * @date 2018/8/22 15:55
 */
@Data
public class LoginConfigInfo implements Serializable {
    private String  logo; //logo
    private String  title;//登录标题
    private String  url;//网址
    private String  about;//关于登录的提示
    private String  forgetPwd;//忘记密码提示
}
