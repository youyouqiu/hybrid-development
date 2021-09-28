package com.zw.platform.basic.dto.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员查询
 *
 * @author zhnagjuan
 * @date 2020/10/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PeopleQuery extends BaseQueryBean {

    /**
     * 组织Id
     */
    private String orgId;

    /**
     * 分组Id
     */
    private String groupId;
}
