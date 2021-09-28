package com.cb.platform.domain.chat;

import java.util.List;

import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/8
 */
@Data
public class User {
    /**
     * 用户唯一id
     */
    private String userId;
    /**
     * 聊天的名字
     */
    private String userRealName;
    /**
     * 属于哪个组织,用于获取好友列表，组织信息
     */
    private String userGroupTopId;
    /**
     * 用户角色 1管理员，2普通成员
     */
    private int userRole;
    /**
     * 用户头像url
     */
    private String userImgUrl;

    private List<String> userGroupList;
}
