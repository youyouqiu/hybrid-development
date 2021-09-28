package com.zw.platform.util.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisSensorQuery {
    /**
     * 组织id
     */
    private String orgId;

    /**
     * 分组id
     */
    private String groupId;

    /**
     * 查询条件
     */
    private String query;

    /**
     * 协议类型
     */
    private Integer protocol;
}
