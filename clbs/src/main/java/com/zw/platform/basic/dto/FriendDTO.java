package com.zw.platform.basic.dto;

import lombok.Data;

/**
 * 好友信息
 * @author zhangjuan
 */
@Data
public class FriendDTO {
    public static final int TYPE_INTERCOM_OBJECT = 1;
    public static final int TYPE_DISPATCHER = 0;

    private Long userId;

    private String userName;

    private String userNumber;

    private Long friendId;

    private String name;

    private String iconSkin;
    /**
     * 类型: 0: 调度员; 1: 对讲对象
     */
    private Integer type;

    private String monitorType;
}
