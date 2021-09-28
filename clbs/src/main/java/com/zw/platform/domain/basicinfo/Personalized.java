package com.zw.platform.domain.basicinfo;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by yangyi on 2017/5/4
 */
@Data
public class Personalized extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String loginLogo; //登录页logo
    private String homeLogo; // 首页logo
    private String webIco;//网页标题ico
    private String topTitle; //置顶标题
    private String copyright; //版权信息
    private String websiteName; //官网域名
    private String recordNumber;// 备案编号
    private String groupId; //组织ID
    private String frontPage;//首页链接
    private String frontPageUrl; // 首页链接url
    private String loginBackground;//登录页背景图
    private  String videoBackground;//视频背景图
    /**
     * 平台: 服务到期提前提醒天数
     */
    private Integer serviceExpireReminder;

    /**
     * 平台网址
     */
    private String platformSite;

    /**
     * 登录页个性化配置
     */
    private String loginPersonalization;

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
}
