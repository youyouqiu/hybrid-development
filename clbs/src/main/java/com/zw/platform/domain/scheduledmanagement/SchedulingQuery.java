package com.zw.platform.domain.scheduledmanagement;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 13:41
 */
@Data
public class SchedulingQuery extends BaseQueryBean {
    private static final long serialVersionUID = 6852374309117869375L;

    /**
     * 组织id
     */
    Set<String> organizationIdSet;
}
