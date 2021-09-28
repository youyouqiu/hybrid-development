package com.zw.platform.domain.vas.monitoring.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommandParametersQuery extends BaseQueryBean {
    private static final long serialVersionUID = 7501545698087408412L;

    /**
     * 模糊查询类型 0：监控对象 1：企业 2：分组
     */
    private String queryType;

    /**
     * 指令类型
     */
    private String commandType;

    /**
     * 协议类型(1[808-2013] 11[808-2019])
     */
    private Integer deviceType;
}
