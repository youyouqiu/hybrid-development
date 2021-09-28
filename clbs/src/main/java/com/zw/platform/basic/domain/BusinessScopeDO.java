package com.zw.platform.basic.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * zw_m_business_scope_config
 *
 * @author zhangjuan 2020-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessScopeDO {

    @ApiModelProperty(value = "企业 或者 车id")
    private String id;
    @ApiModelProperty(value = "运营范围id")
    private String businessScopeId;
    @ApiModelProperty(value = "类型 1 企业 2 车")
    private String type;
}
