package com.zw.app.domain.webMaster.personalized;

import lombok.Data;

import java.io.Serializable;

/**
 * app后台个人中心信息实体
 * @author lijie
 * @date 2018/8/22 16:11
 */
@Data
public class PersonalConfigInfo implements Serializable{
    private String aboutUs;    //关于我们
    private String groupAvatar;//组织头像
}
