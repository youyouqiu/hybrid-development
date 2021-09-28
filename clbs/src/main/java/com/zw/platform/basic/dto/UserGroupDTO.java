package com.zw.platform.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wanxing
 * @Title: 用户分组
 * @date 2020/11/216:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupDTO {
    /**
     * 用户Id
     */
    private String userId;
    /**
     * 分组Id
     */
    private String groupId;

    /**
     * 用户名称
     */
    private String userName;
}
