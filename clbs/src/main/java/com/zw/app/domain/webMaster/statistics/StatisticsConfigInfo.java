package com.zw.app.domain.webMaster.statistics;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 统计类型详细信息
 * @author lijie
 * @date 2018/12/10 09:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticsConfigInfo extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private  String groupId;//组织id
    private  Integer groupDefault;//是否为组织默认数据
    private  String groupName;//组织名字
    private  String name;//统计名字
    private  Integer number;//统计类型序号
    private  Integer appVersion;//当前配置对应的app版本号

}
