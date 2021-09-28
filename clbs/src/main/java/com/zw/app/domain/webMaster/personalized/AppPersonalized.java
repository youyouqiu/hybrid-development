package com.zw.app.domain.webMaster.personalized;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库app后台配置映射实体
 * @author lijie
 * @date 2018/8/22 11:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppPersonalized extends BaseFormBean {
    private static final long serialVersionUID = 1L;
    private int groupDefault; //是否组织默认
    private String groupId; //组织ID
    private String loginLogo; //登录页logo
    private String groupAvatar; // 组织logo
    private String loginTitle;//网页标题ico
    private int maxObjectnumber; //最多选择对象数量
    private int alarmTimeLimit; //报警数据查询时间范围
    private int historyTimeLimit; //历史数据查询时间范围
    private int aggregationNumber;// 聚合数量
    private String aboutPlatform;//关于我们
    private String passwordPrompt; //忘记密码提示
    private String loginPrompt; //登录提示
    private String websiteName; //网址
    private String adasFlag; //是否开启adas
}
