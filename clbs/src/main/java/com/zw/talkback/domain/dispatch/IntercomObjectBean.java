package com.zw.talkback.domain.dispatch;

import lombok.Data;

@Data
public class IntercomObjectBean {
    /**
     * 对讲对象平台id
     */
    private String id;

    /**
     * 对讲对象对讲平台id
     */
    private Long userId;

    /**
     * 所属组织id
     */
    private String groupId;

    /**
     * 所属组织name
     */
    private String groupName;

    /**
     * 对讲对象name
     */
    private String name;
}