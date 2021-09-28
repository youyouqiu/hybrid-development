package com.zw.talkback.domain.basicinfo;

import lombok.Data;

import java.util.UUID;

@Data
public class PeopleBasicInfo {

    private String id = UUID.randomUUID().toString();

    private String peopleId;

    private String basicId;

    /**
     * 1：技能，2：驾照类别
     */
    private Integer type;

    private Integer flag = 1;

    private String name;
}
