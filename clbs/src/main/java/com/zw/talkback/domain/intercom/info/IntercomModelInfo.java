package com.zw.talkback.domain.intercom.info;

import lombok.Data;

/**
 * 对讲机型实体
 */
@Data
public class IntercomModelInfo {

    private String id;
    /**
     * 对讲机型对应的原始机型id
     */
    private Long originalModelId;

    /**
     * 对讲机型名称
     */
    private String name;
}
