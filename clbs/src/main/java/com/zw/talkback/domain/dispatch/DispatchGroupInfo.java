package com.zw.talkback.domain.dispatch;

import lombok.Data;

@Data
public class DispatchGroupInfo {
    private String id;

    private String name;

    private String groupId;

    private String groupName;

    private String organizationCode;

    private String address;

    private String contactName;

    private String phone;

    private String description;

    private String groupCallNumber;

    private Long userId;
}
