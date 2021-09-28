package com.zw.platform.basic.dto;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 经营类型
 * @date 2021/2/515:33
 */
@Data
public class BusinessScopeDTO {

    /**
     * scopeId
     */
    private String businessScopeId;
    /**
     * 经营范围
     */
    private String businessScope;
    /**
     * 经营范围编码
     */
    private String businessScopeCode;
    /**
     * id
     */
    private String id;
}
