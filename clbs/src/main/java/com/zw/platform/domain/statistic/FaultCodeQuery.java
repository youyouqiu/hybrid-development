package com.zw.platform.domain.statistic;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 故障码
 * @author zhouzongbo on 2018/12/28 15:44
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FaultCodeQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 7588988648207263940L;

    /**
     * 监控对象ID
     */
    private String monitorIds;

    private List<String> monitorList;

    /**
     * 开始时间
     */
    private String startDateTime;

    /**
     * 结束时间
     */
    private String endDateTime;
}
