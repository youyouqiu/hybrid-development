package com.zw.talkback.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendInfo implements Serializable {

    private static final long serialVersionUID = 2938772352068986479L;
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
}

