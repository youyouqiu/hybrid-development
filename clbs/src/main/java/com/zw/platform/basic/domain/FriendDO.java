package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友信息表 zw_m_friend
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FriendDO extends BaseDO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 类型: 0: 调度员; 1: 对讲对象
     */
    private Integer type;
}
