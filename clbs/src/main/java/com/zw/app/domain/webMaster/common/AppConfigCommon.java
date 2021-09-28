package com.zw.app.domain.webMaster.common;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;

/**
 * 报警对象详细信息
 * @author lijei
 * @date 2018/8/27 18:29
 */
@Data
public class AppConfigCommon extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private  String groupId;//组织id
    private  int groupDefault;//是否为组织默认数据
    private  String groupName;//组织名字
    private  String category;//类别
    private  String name;//名称
    private  String type;//类型
    private  Integer appVersion;//该配置对应的app版本号
}
