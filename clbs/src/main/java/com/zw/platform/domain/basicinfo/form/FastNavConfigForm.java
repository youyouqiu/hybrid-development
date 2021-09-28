package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页定制信息
 * create by denghuabing 2018.12.13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FastNavConfigForm extends BaseFormBean {

    private String userId;//用户的entryuuid

    private String url;//链接地址

    private String urlId;//链接地址id（权限判断使用）

    private String urlName;//地址名

    private String order;//排序

    private String description;//描述

    /**导航类型 0：站内导航，1：站外导航*/
    private Integer navType;
}
