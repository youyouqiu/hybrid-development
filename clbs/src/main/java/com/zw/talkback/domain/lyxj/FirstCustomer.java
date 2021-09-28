package com.zw.talkback.domain.lyxj;

import lombok.Data;

/**
 * 一级客户实体
 */
@Data
public class FirstCustomer {
    /**
     * 会话ID
     */
    private String pid;
    /**
     * 上级客户ID
     * 登陆是二级客户时为一级客户ID
     * 登陆是一级客户时为-1
     */
    private Long parentId;
    /**
     * 客户ID
     */
    private Long custId;
    /**
     * 客户类型
     * 1：一级客户
     * 2：二级客户
     */
    private Integer type;
    /**
     * 客户名称
     */
    private String custName;
    /**
     * 对讲功能
     * 1:支持
     * 0:不支持
     */
    private Integer supportTalk;
    /**
     * 视频功能
     * 1:支持
     * 0:不支持
     */
    private Integer supportVideo;
    /**
     * 传感功能
     * 1:支持
     * 0:不支持
     */
    private Integer supportSensor;

}
