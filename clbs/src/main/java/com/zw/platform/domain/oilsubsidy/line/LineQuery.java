package com.zw.platform.domain.oilsubsidy.line;

import java.util.List;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 路线查询类
 * @date 2020/10/919:05
 */
@Data
public class LineQuery extends BaseQueryBean {

    /**
     * 企业ID,前端传值
     */
    private String orgId;

    /**
     * 用户权限企业
     */
    private List<String> currentUserOrgIds;
}
